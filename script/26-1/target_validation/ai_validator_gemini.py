from __future__ import annotations

import json
import re
import ssl
import urllib.error
import urllib.request
from pathlib import Path
from typing import Callable

from schemas import AiValidationResult, TargetRef

GeminiRequestFn = Callable[[str, dict, int], dict]


class GeminiFlashValidator:
    def __init__(
        self,
        api_key: str,
        model: str = "gemini-flash-3.0",
        timeout_seconds: int = 20,
        prompt_path: str | None = None,
        request_fn: GeminiRequestFn | None = None,
    ) -> None:
        self.api_key = api_key.strip()
        self.model = _resolve_model_alias(model)
        self.timeout_seconds = timeout_seconds
        self.request_fn = request_fn or self._default_request
        self.system_prompt = self._load_prompt(prompt_path)

    def validate(
        self,
        raw_target_text: str,
        parsed_targets: list[TargetRef],
        unparsed_tokens: list[str],
        allowed_departments: list[str],
    ) -> AiValidationResult:
        if not self.api_key:
            return AiValidationResult(verdict="SKIPPED", reason="missing gemini api key")

        payload = self._build_payload(raw_target_text, parsed_targets, unparsed_tokens, allowed_departments)
        endpoint = f"https://generativelanguage.googleapis.com/v1beta/{_to_model_path(self.model)}:generateContent?key={self.api_key}"

        try:
            response = self.request_fn(endpoint, payload, self.timeout_seconds)
        except Exception as error:
            return AiValidationResult(verdict="SKIPPED", reason=f"gemini request failed: {error}")

        parsed_response = self._parse_response(response, allowed_departments)
        return parsed_response

    def _build_payload(
        self,
        raw_target_text: str,
        parsed_targets: list[TargetRef],
        unparsed_tokens: list[str],
        allowed_departments: list[str],
    ) -> dict:
        input_payload = {
            "raw_target_text": raw_target_text,
            "parsed_targets": [{"department": target.department, "grade": target.grade} for target in parsed_targets],
            "unparsed_tokens": unparsed_tokens,
            "allowed_departments": allowed_departments,
        }

        prompt = (
            f"{self.system_prompt}\n\n"
            "입력 데이터(JSON):\n"
            f"{json.dumps(input_payload, ensure_ascii=False)}\n\n"
            "JSON만 출력하세요."
        )

        return {
            "contents": [{"parts": [{"text": prompt}]}],
            "generationConfig": {
                "temperature": 0,
                "responseMimeType": "application/json",
            },
        }

    @staticmethod
    def _load_prompt(prompt_path: str | None) -> str:
        default_prompt = (
            "당신은 수강대상 검증기입니다. 반드시 JSON만 출력하세요.\n"
            "규칙:\n"
            "1) allowed_departments에 없는 학과를 생성하지 마세요.\n"
            "2) 불확실하면 추측하지 말고 unknown_terms에 남기세요.\n"
            "3) verdict는 OK/WARN/FAIL 중 하나만 사용하세요.\n"
            "4) normalized_targets는 [{department:string, grade:int}] 형식입니다.\n"
            "5) grade는 1~5만 허용하세요.\n"
            "출력 스키마:\n"
            "{\"verdict\":\"OK|WARN|FAIL\","
            "\"normalized_targets\":[{\"department\":\"...\",\"grade\":1}],"
            "\"unknown_terms\":[\"...\"],"
            "\"confidence\":0.0,"
            "\"reason\":\"...\"}"
        )
        if not prompt_path:
            return default_prompt

        file_path = Path(prompt_path)
        if not file_path.exists():
            return default_prompt
        file_prompt = file_path.read_text(encoding="utf-8").strip()
        return file_prompt or default_prompt

    def _default_request(self, endpoint: str, payload: dict, timeout_seconds: int) -> dict:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        request = urllib.request.Request(
            endpoint,
            data=body,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        context = _build_ssl_context()

        try:
            with urllib.request.urlopen(request, timeout=timeout_seconds, context=context) as response:
                return json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            details = error.read().decode("utf-8", errors="ignore")
            raise RuntimeError(f"HTTP {error.code}: {details}") from error

    @staticmethod
    def _parse_response(response: dict, allowed_departments: list[str]) -> AiValidationResult:
        candidates = response.get("candidates", [])
        if not candidates:
            return AiValidationResult(verdict="SKIPPED", reason="empty candidates")

        content = candidates[0].get("content", {})
        parts = content.get("parts", [])
        if not parts:
            return AiValidationResult(verdict="SKIPPED", reason="empty parts")

        text = parts[0].get("text", "")
        json_text = _extract_json(text)
        if not json_text:
            return AiValidationResult(verdict="SKIPPED", reason="invalid json payload")

        try:
            parsed = json.loads(json_text)
        except Exception:
            return AiValidationResult(verdict="SKIPPED", reason="failed to decode json payload")

        verdict = str(parsed.get("verdict", "SKIPPED")).upper()
        unknown_terms = [str(item).strip() for item in parsed.get("unknown_terms", []) if str(item).strip()]
        confidence = float(parsed.get("confidence", 0.0) or 0.0)
        reason = str(parsed.get("reason", "")).strip()

        allowed = set(allowed_departments)
        normalized_targets: list[TargetRef] = []
        seen: set[tuple[str, int]] = set()
        for item in parsed.get("normalized_targets", []):
            if not isinstance(item, dict):
                continue
            department = str(item.get("department", "")).strip()
            grade_raw = item.get("grade")
            if not department or department not in allowed:
                continue
            try:
                grade = int(grade_raw)
            except Exception:
                continue
            if grade < 1 or grade > 5:
                continue
            key = (department, grade)
            if key in seen:
                continue
            seen.add(key)
            normalized_targets.append(TargetRef(department=department, grade=grade))

        return AiValidationResult(
            verdict=verdict,
            normalized_targets=normalized_targets,
            unknown_terms=unknown_terms,
            confidence=max(0.0, min(confidence, 1.0)),
            reason=reason,
        )


def _extract_json(text: str) -> str:
    stripped = (text or "").strip()
    if not stripped:
        return ""

    if stripped.startswith("```"):
        stripped = re.sub(r"^```(?:json)?", "", stripped).strip()
        stripped = re.sub(r"```$", "", stripped).strip()

    if stripped.startswith("{") and stripped.endswith("}"):
        return stripped

    first = stripped.find("{")
    last = stripped.rfind("}")
    if first == -1 or last == -1 or first >= last:
        return ""
    return stripped[first : last + 1]


def _build_ssl_context() -> ssl.SSLContext:
    try:
        import certifi  # type: ignore

        return ssl.create_default_context(cafile=certifi.where())
    except Exception:
        return ssl.create_default_context()


def _resolve_model_alias(model: str) -> str:
    normalized = (model or "").strip()
    if not normalized:
        return "gemini-3-flash-preview"

    aliases = {
        "gemini-flash-3.0": "gemini-3-flash-preview",
        "gemini-flash-3": "gemini-3-flash-preview",
    }
    return aliases.get(normalized, normalized)


def _to_model_path(model: str) -> str:
    if model.startswith("models/"):
        return model
    return f"models/{model}"
