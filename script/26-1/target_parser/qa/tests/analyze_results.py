import json
from datetime import datetime
import os

# Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PARENT_DIR = os.path.abspath(os.path.join(BASE_DIR, "..", ".."))  # target_parser directory
PARSED_JSON_PATH = os.path.join(PARENT_DIR, "data", "parsed_unique_targets.json")
UNMAPPED_JSON_PATH = os.path.join(PARENT_DIR, "data", "unmapped_targets.json")
TEST_DIR = BASE_DIR  # Now we're in the test directory

# Load data
with open(PARSED_JSON_PATH, 'r', encoding='utf-8') as f:
    parsed_data = json.load(f)

with open(UNMAPPED_JSON_PATH, 'r', encoding='utf-8') as f:
    unmapped_data = json.load(f)

# Create unmapped lookup
unmapped_lookup = {item["original_text"]: item["unmapped_tokens"] for item in unmapped_data}

# Analysis categories
suspicious_items = []

for item in parsed_data:
    original = item["original_text"]
    count = item["count"]
    parsed = item["parsed_targets"]

    issues = []
    confidence = "high"  # Start with high confidence

    # Check 1: Has unmapped tokens
    if original in unmapped_lookup and unmapped_lookup[original]:
        issues.append({
            "type": "unmapped_tokens",
            "detail": f"Unmapped tokens: {unmapped_lookup[original]}"
        })
        confidence = "low"

    # Check 2: No parsed targets at all
    if not parsed:
        issues.append({
            "type": "no_parsing",
            "detail": "No targets were parsed from this text"
        })
        confidence = "low"

    # Check 3: UNIVERSITY scope with null college/department but no clear "전체" indicator
    for target in parsed:
        if target["scopeType"] == "UNIVERSITY" and target["collegeName"] is None and target["departmentName"] is None:
            # Check if original text clearly indicates university-wide
            if "전체" not in original and "전체학년" not in original:
                # Only flag if it's not obviously a special case
                if not (target.get("isForeignerOnly") or target.get("isMilitaryOnly") or target.get("isTeachingCertificateStudent")):
                    issues.append({
                        "type": "ambiguous_university_scope",
                        "detail": f"UNIVERSITY scope without clear '전체' indicator: {target}"
                    })
                    if confidence == "high":
                        confidence = "medium"

    # Check 4: Multiple departments parsed from single token (like "건축")
    dept_count = len([t for t in parsed if t["scopeType"] == "DEPARTMENT"])
    if dept_count > 3:
        issues.append({
            "type": "multiple_departments",
            "detail": f"Parsed {dept_count} departments - may be overly broad"
        })
        if confidence == "high":
            confidence = "medium"

    # Check 5: Complex original text but simple parsing
    if len(original) > 20 and len(parsed) == 1:
        # Check if the single parsed item is just UNIVERSITY scope
        if parsed[0]["scopeType"] == "UNIVERSITY":
            issues.append({
                "type": "oversimplified",
                "detail": f"Complex text (len={len(original)}) reduced to single UNIVERSITY scope"
            })
            if confidence == "high":
                confidence = "medium"

    # Check 6: Exclusion logic - check if both allowed and excluded targets exist
    has_excluded = any(t.get("isExcluded", False) for t in parsed)
    has_allowed = any(not t.get("isExcluded", False) for t in parsed)
    if has_excluded and not has_allowed and "제외" in original:
        # Exclusion without base target might be wrong
        issues.append({
            "type": "exclusion_without_base",
            "detail": "Has excluded targets but no allowed base target"
        })
        confidence = "low"

    # Check 7: Strict restriction flags
    has_strict = any(t.get("isStrictRestriction", False) for t in parsed)
    if has_strict:
        # Strict restrictions should usually have specific departments
        if all(t["scopeType"] == "UNIVERSITY" for t in parsed):
            issues.append({
                "type": "strict_without_specifics",
                "detail": "Strict restriction flag on UNIVERSITY scope"
            })
            if confidence == "high":
                confidence = "medium"

    # Add to suspicious items if there are issues OR confidence is not high
    if issues or confidence != "high":
        suspicious_items.append({
            "original_text": original,
            "count": count,
            "parsed_targets": parsed,
            "confidence": confidence,
            "issues": issues
        })

# Generate timestamp for filename
timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
output_path = os.path.join(TEST_DIR, f"suspicious_parsing_{timestamp}.json")

# Write results
with open(output_path, 'w', encoding='utf-8') as f:
    json.dump({
        "metadata": {
            "timestamp": timestamp,
            "total_targets": len(parsed_data),
            "suspicious_count": len(suspicious_items),
            "confidence_breakdown": {
                "low": len([s for s in suspicious_items if s["confidence"] == "low"]),
                "medium": len([s for s in suspicious_items if s["confidence"] == "medium"]),
                "high": len([s for s in suspicious_items if s["confidence"] == "high"])
            }
        },
        "suspicious_items": suspicious_items
    }, f, ensure_ascii=False, indent=2)

print(f"Analysis complete!")
print(f"Total targets analyzed: {len(parsed_data)}")
print(f"Suspicious items found: {len(suspicious_items)}")
print(f"  - Low confidence: {len([s for s in suspicious_items if s['confidence'] == 'low'])}")
print(f"  - Medium confidence: {len([s for s in suspicious_items if s['confidence'] == 'medium'])}")
print(f"  - High confidence: {len([s for s in suspicious_items if s['confidence'] == 'high'])}")
print(f"\nResults saved to: {output_path}")
