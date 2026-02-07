from __future__ import annotations

from pathlib import Path


def _strip_quotes(value: str) -> str:
    return value.strip().strip("'").strip('"').strip()


def _deduplicate_preserve_order(values: list[str]) -> list[str]:
    unique: list[str] = []
    seen: set[str] = set()
    for value in values:
        if value in seen:
            continue
        seen.add(value)
        unique.append(value)
    return unique


def load_departments(data_yml_path: str) -> list[str]:
    data_file = Path(data_yml_path)
    if not data_file.exists():
        raise FileNotFoundError(f"data.yml not found: {data_yml_path}")

    text = data_file.read_text(encoding="utf-8")
    departments = _load_departments_with_pyyaml(text)
    if departments:
        return _deduplicate_preserve_order(departments)
    return _deduplicate_preserve_order(_load_departments_with_fallback(text))


def load_aliases(alias_yml_path: str) -> dict[str, str]:
    alias_file = Path(alias_yml_path)
    if not alias_file.exists():
        return {}

    text = alias_file.read_text(encoding="utf-8")
    aliases = _load_aliases_with_pyyaml(text)
    if aliases:
        return aliases
    return _load_aliases_with_fallback(text)


def _load_departments_with_pyyaml(text: str) -> list[str]:
    try:
        import yaml  # type: ignore
    except Exception:
        return []

    try:
        parsed = yaml.safe_load(text) or {}
    except Exception:
        return []

    ssu_data = parsed.get("ssu-data", {})
    colleges = ssu_data.get("colleges", [])
    departments: list[str] = []
    for college in colleges:
        if not isinstance(college, dict):
            continue
        college_departments = college.get("departments", [])
        if isinstance(college_departments, list):
            departments.extend(str(item).strip() for item in college_departments if str(item).strip())
    return departments


def _load_departments_with_fallback(text: str) -> list[str]:
    departments: list[str] = []
    in_departments_block = False
    departments_indent = -1

    for raw_line in text.splitlines():
        line = raw_line.rstrip()
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue

        indent = len(line) - len(line.lstrip(" "))
        if in_departments_block and indent <= departments_indent:
            in_departments_block = False

        if stripped.startswith("departments:"):
            in_departments_block = True
            departments_indent = indent
            continue

        if in_departments_block and stripped.startswith("- "):
            department = _strip_quotes(stripped[2:])
            if department:
                departments.append(department)

    return departments


def _load_aliases_with_pyyaml(text: str) -> dict[str, str]:
    try:
        import yaml  # type: ignore
    except Exception:
        return {}

    try:
        parsed = yaml.safe_load(text) or {}
    except Exception:
        return {}

    aliases = parsed.get("aliases", {})
    if not isinstance(aliases, dict):
        return {}

    normalized: dict[str, str] = {}
    for alias, canonical in aliases.items():
        alias_name = str(alias).strip()
        canonical_name = str(canonical).strip()
        if alias_name and canonical_name:
            normalized[alias_name] = canonical_name
    return normalized


def _load_aliases_with_fallback(text: str) -> dict[str, str]:
    aliases: dict[str, str] = {}
    in_aliases_block = False
    aliases_indent = -1

    for raw_line in text.splitlines():
        line = raw_line.rstrip()
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue

        indent = len(line) - len(line.lstrip(" "))
        if in_aliases_block and indent <= aliases_indent:
            in_aliases_block = False

        if stripped == "aliases:":
            in_aliases_block = True
            aliases_indent = indent
            continue

        if not in_aliases_block:
            continue

        if ":" not in stripped:
            continue

        alias, canonical = stripped.split(":", 1)
        alias_name = _strip_quotes(alias)
        canonical_name = _strip_quotes(canonical)
        if alias_name and canonical_name:
            aliases[alias_name] = canonical_name

    return aliases
