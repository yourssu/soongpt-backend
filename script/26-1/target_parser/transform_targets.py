import json
import yaml
import re
import os

# Configuration Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_YML_PATH = os.path.abspath(os.path.join(BASE_DIR, "data.yml"))
INPUT_JSON_PATH = os.path.join(BASE_DIR, "unique_targets.json")
OUTPUT_JSON_PATH = os.path.join(BASE_DIR, "parsed_unique_targets.json")
UNMAPPED_JSON_PATH = os.path.join(BASE_DIR, "unmapped_targets.json")

# Department Aliases (Based on PLAN.md)
# Values can be string or list of strings
DEPT_ALIAS = {
    # IT College
    "컴퓨터": "컴퓨터학부",
    "소프트": "소프트웨어학부",
    "AI융합": "AI융합학부",
    "글미": "글로벌미디어학부",
    "글로벌미디어": "글로벌미디어학부",
    "미디어경영": "미디어경영학과",
    "디지털미디어": "미디어경영학과",
    "정보보호": "정보보호학과",
    "정통전": "전자정보공학부 IT융합전공",
    "전자공학전공": "전자정보공학부 전자공학전공",
    "IT융합전공": "전자정보공학부 IT융합전공",
    
    # Engineering
    "기계": "기계공학부",
    "화공": "화학공학과",
    "화학공학": "화학공학과",
    "전기": "전기공학부",
    "전기공학": "전기공학부",
    "전자공학": "전자정보공학부 전자공학전공",
    "건축": ["건축학부 건축학전공", "건축학부 실내건축전공", "건축학부 건축공학전공"],
    "건축학": ["건축학부 건축학전공", "건축학부 실내건축전공", "건축학부 건축공학전공"], # Generic match triggers all
    "건축학부": ["건축학부 건축학전공", "건축학부 실내건축전공", "건축학부 건축공학전공"],
    "건축공학": "건축학부 건축공학전공",
    "실내건축": "건축학부 실내건축전공",
    "신소재": "신소재공학과",
    "산업정보": "산업정보시스템공학과",
    
    # Natural Science
    "물리": "물리학과",
    "화학": "화학과",
    "수학": "수학과",
    "의생명": "의생명시스템학부",
    "의생명시스템": "의생명시스템학부",
    "통계": "정보통계보험수리학과",
    "통계보험": "정보통계보험수리학과",
    
    # Business
    "경영": "경영학부",
    "경영학부": "경영학부",
    "벤처": "벤처중소기업학과",
    "벤처중소": "벤처중소기업학과",
    "회계": "회계학과",
    "회계세무": "회계세무학과",
    "금융": "금융학부",
    "혁신경영": "혁신경영학과",
    "복지경영": "복지경영학과",
    
    # Economics
    "경제": "경제학과",
    "글로벌통상": "글로벌통상학과",
    "국제무역": "국제무역학과",
    "금융경제": "금융경제학과",
    "평생교육": "평생교육학과",
    
    # Humanities
    "국문": "국어국문학과",
    "영문": "영어영문학과",
    "독문": "독어독문학과",
    "불문": "불어불문학과",
    "중문": "중어중문학과",
    "일문": "일어일문학과",
    "일어일문": "일어일문학과",
    "철학": "철학과",
    "사학": "사학과",
    "기독교": "기독교학과",
    "문예창작": "예술창작학부 문예창작전공",
    "문예창작전공": "예술창작학부 문예창작전공",
    "영화예술": "예술창작학부 영화예술전공",
    "영화예술전공": "예술창작학부 영화예술전공",
    "스포츠": "스포츠학부",
    
    # Law
    "법학": "법학과",
    "국제법무": "국제법무학과",
    
    # Social Science
    "사복": "사회복지학부",
    "사회복지": "사회복지학부",
    "행정": "행정학부",
    "정외": "정치외교학과",
    "정치외교": "정치외교학과",
    "언론": "언론홍보학과",
    "언론홍보": "언론홍보학과",
    "정보사회": "정보사회학과",
    
    # Baird
    "자유전공": "자유전공학부",
    
    # New Depts
    "차세대반도체": "차세대반도체학과",
    "AI소프트": "AI소프트웨어학부",
    "AI소프트웨어": "AI소프트웨어학부",
}

COLLEGE_ALIAS = {
    "IT대": "IT대학",
    "공과대": "공과대학",
    "공대": "공과대학",
    "인문대": "인문대학",
    "자연대": "자연과학대학",
    "경상대": "경영대학", # 구 명칭
    "경영대": "경영대학", # 추가

    "경통대": "경제통상대학",
    "사회대": "사회과학대학",
    "법대": "법과대학",
    "법과대": "법과대학", # 추가
    "AI대": "AI대학", # 가칭/신설
}

# Category to College Mapping
CATEGORY_MAPPING = {
    "인문사회계열": ["인문대학", "사회과학대학"],
    "인문사회계열만": ["인문대학", "사회과학대학"],  # "~만" 포함
    "자연과학계열": ["자연과학대학"],
    "인문사회자연계": ["인문대학", "사회과학대학", "자연과학대학"],
}

class IdManager:
    def __init__(self):
        self.college_map = {} # name -> id
        self.department_map = {} # name -> id
        self.department_to_college = {} # dept_id -> college_id
        self.college_id_to_name = {} # id -> name
        self.department_id_to_name = {} # id -> name
        
    def load_from_yaml(self, path):
        with open(path, 'r', encoding='utf-8') as f:
            data = yaml.safe_load(f)
            
        college_id_counter = 1
        department_id_counter = 1
        
        for college in data['ssu-data']['colleges']:
            c_name = college['name']
            if c_name not in self.college_map:
                self.college_map[c_name] = college_id_counter
                self.college_id_to_name[college_id_counter] = c_name
                college_id_counter += 1
            
            c_id = self.college_map[c_name]
            
            for dept_name in college.get('departments', []):
                # Clean up department name if needed
                if dept_name not in self.department_map:
                    self.department_map[dept_name] = department_id_counter
                    self.department_id_to_name[department_id_counter] = dept_name
                    self.department_to_college[department_id_counter] = c_id
                    department_id_counter += 1

    def get_college_id(self, name):
        # Check direct match
        if name in self.college_map:
            return self.college_map[name]
        # Check alias
        if name in COLLEGE_ALIAS:
            mapped_name = COLLEGE_ALIAS[name]
            return self.college_map.get(mapped_name)
        return None

    def get_department_ids(self, name):
        """Returns a list of department IDs."""
        name = name.strip()
        ids = []
        
        # Helper to resolve a single name to an ID
        def resolve_single(n):
             if n in self.department_map:
                 return self.department_map[n]
             return None

        # Direct match
        direct_id = resolve_single(name)
        if direct_id:
            return [direct_id]
            
        # Alias match
        if name in DEPT_ALIAS:
            mapped = DEPT_ALIAS[name]
            if isinstance(mapped, list):
                for m in mapped:
                    mid = resolve_single(m)
                    if mid: ids.append(mid)
            else:
                mid = resolve_single(mapped)
                if mid: ids.append(mid)
            return ids
            
        # Partial match heuristic (risky but useful for "국어국문" vs "국어국문학과")
        # Removing legacy partial match if not needed, or keeping it but ensuring it doesn't conflict
        # For safety, let's keep it limited or remove if confident in aliases. 
        # Given the "AI소프트" case, if it wasn't in alias, this loop might have helped IF "AI소프트" was in dept name.
        # But "AI소프트웨어학부" contains "AI소프트".
        # Let's keep it but make it return ALL matches? No, unsafe.
        # Only return if exactly one match or strong match?
        # I will iterate keys and if 'name' is substring of key, add it.
        # But "건축" is substring of "건축공학", "건축학", "실내건축".
        # If I return all containing "건축", "건축공학" query would work, but "건축" query would return everything.
        # This acts like one-to-many.
        
        partial_matches = []
        for stored_name, stored_id in self.department_map.items():
            if name in stored_name: 
                 partial_matches.append(stored_id)
        
        if partial_matches:
            # If we found matches via substring, use them.
            # But duplicate filtering later handles over-matching?
            return partial_matches

        return []

def parse_target(text, id_manager):
    # Normalize
    original_text = text
    text = text.replace(",", " , ").replace(";", " ; ").replace("/", " / ").replace("-", " ") # Pad delimiters, replace hyphens
    
    # Flags
    has_strict_flag = "대상외수강제한" in text or "타학과수강제한" in text
    has_exclude_keyword = "제외" in text or "제한" in text
    
    is_excluded = has_strict_flag or has_exclude_keyword
    is_foreigner_only = "순수외국인" in text or "외국국적" in text or "외국인" in text or "교환학생" in text
    is_military_only = "군위탁" in text
    is_teaching_cert = "교직이수자" in text or "교직이수" in text
    
    # Remove flags for cleaner parsing
    clean_text = re.sub(r"\(\s*(대상외수강제한|타학과수강제한|수강제한)\s*\)", "", text)
    clean_text = re.sub(r'순수외국인[^\s]*', '', clean_text)
    clean_text = re.sub(r'외국국적[^\s]*', '', clean_text)
    clean_text = clean_text.replace("교환학생", "").replace("군위탁", "").replace("입학생", "").replace("교직이수자", "").replace("교직이수", "")
    clean_text = clean_text.strip()
    
    results = []
    unmapped_tokens = []
    
    # Case 1: University Wide (전체, 전체학년)
    if "전체학년" in clean_text and clean_text == "전체학년":
        targets = []
        # If category exclusion (e.g., "전체학년 교환학생(대상외수강제한)")
        if (is_foreigner_only or is_military_only) and has_exclude_keyword:
            # Base allowed target
            targets.append({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": 1,
                "maxGrade": 5,
                "isExcluded": False,
                "isForeignerOnly": False,
                "isMilitaryOnly": False,
                "isTeachingCertificateStudent": False,
                "isStrictRestriction": has_strict_flag
            })
            # Category excluded target
            targets.append({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": 1,
                "maxGrade": 5,
                "isExcluded": True,
                "isForeignerOnly": is_foreigner_only,
                "isMilitaryOnly": is_military_only,
                "isTeachingCertificateStudent": is_teaching_cert,
                "isStrictRestriction": has_strict_flag
            })
        else:
            # Single target
            targets.append({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": 1,
                "maxGrade": 5,
                "isExcluded": is_excluded,
                "isForeignerOnly": is_foreigner_only,
                "isMilitaryOnly": is_military_only,
                "isTeachingCertificateStudent": is_teaching_cert,
                "isStrictRestriction": has_strict_flag
            })
        return targets, []
    if clean_text == "전체" or clean_text == "":
         # If text became empty after cleaning (e.g. "순수외국인 제한" -> ""), it might be a global constraint
         if is_foreigner_only or is_military_only:
             # Fall through to specific handling below instead of early return
             pass
         elif clean_text == "전체":
             return [{
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": 1, 
                "maxGrade": 5,
                "isExcluded": is_excluded,
                "isForeignerOnly": is_foreigner_only,
                "isMilitaryOnly": is_military_only,
                "isTeachingCertificateStudent": is_teaching_cert,
                "isStrictRestriction": has_strict_flag
            }], []
        
    # Pre-parse exclusion blocks in parentheses e.g. (중문 제외), (영어영문학과제외), (전자공학수강제한), (자연대 수강불가)
    # This must be done BEFORE splitting tokens to preserve context
    exclusion_matches = re.findall(r'\(([^)]*?(?:제외|수강제한|수강불가)[^)]*?)\)', clean_text)
    
    # We will invoke parse_target recursively on these blocks, but force isExcluded=True on results
    # And we need to remove them from clean_text so they don't get added as positive targets
    for match in exclusion_matches:
        # Remove exclusion keywords from the match string so it parses as a dept/college
        inner_text = match.replace("제외", "").replace("수강제한", "").replace("수강불가", "").replace("수강", "").replace("제한", "")
        
        # Recursive parse (using a dummy ID manager? No, use real one)
        # We assume inner text defines the departments to exclude
        ex_targets, _ = parse_target(inner_text, id_manager)
        
        for t in ex_targets:
            # Force exclusion flag
            t["isExcluded"] = True
            # Also inherit other flags? Maybe grade, but usually these are purely dept exclusions
            # Ensure we don't accidentally inherit 'University' scope from empty inner text
            if t["scopeType"] != "UNIVERSITY":
                # Add to results directly? Or current_targets?
                # We add to results but we need to track them.
                # Let's add to final_targets list accumulation logic.
                # We can add to results immediately, but we need to ensure grade info is synced if missing?
                # Usually (Dep Except) implies the same grade as main text. 
                # But parse_target recursion might return defaults.
                pass 
            
        results.extend(ex_targets)
        
        # Remove this block from clean_text
        clean_text = clean_text.replace(f"({match})", " ")
        
    # Re-evaluate flags after modification? No, flags were global.
    # But if we removed all text, we might be left with empty string or "전체"
    
    # Split by lines if any (continue with modified clean_text)
    lines = clean_text.split('\n')
    if len(lines) > 1:
        for line in lines:
            res, unmapped = parse_target(line, id_manager)
            results.extend(res)
            unmapped_tokens.extend(unmapped)
            
        # Don't propagate strict flags in multiline - each line is independent
        # For FOREIGNER/MILITARY/TEACHING_CERT/STRICT_RESTRICTION, propagate
        if is_foreigner_only: 
             for r in results: r["isForeignerOnly"] = True
        if is_military_only:
             for r in results: r["isMilitaryOnly"] = True
        if is_teaching_cert:
             for r in results: r["isTeachingCertificateStudent"] = True
        if has_strict_flag:
             for r in results: r["isStrictRestriction"] = True

        return results, unmapped_tokens
        
    # Regex for "N학년 ..." or "N~M학년 ..." or "전체학년 ..."
    # Pattern: (Grade Part) (Dept/College Part)
    
    # 1. Parse Grade
    min_grade = 1
    max_grade = 5
    
    grade_match = re.search(r'(?:(\d)~(\d)|(\d))학년|전체학년', clean_text)
    has_grade_spec = False
    
    if grade_match:
        has_grade_spec = True
        if "전체학년" in grade_match.group(0):
            min_grade, max_grade = 1, 5
        elif grade_match.group(1) and grade_match.group(2): # 1~3학년
            min_grade = int(grade_match.group(1))
            max_grade = int(grade_match.group(2))
        elif grade_match.group(3): # 1학년
            min_grade = int(grade_match.group(3))
            max_grade = min_grade
            
    
    # 2. Parse Departments/Colleges
    # Extract candidate words
    # Remove grade part
    text_no_grade = re.sub(r'(?:(\d)~(\d)|(\d))학년|전체학년', ' ', clean_text)
    
    # Ensure delimiters are spaced out BEFORE removing other chars, if not done already
    text_no_grade = text_no_grade.replace(",", " , ").replace(";", " ; ")
    
    # Remove special chars (keep spaces and alphanumeric/Korean)
    # Removing () as they are handled or irrelevant for token extraction now
    text_no_grade = re.sub(r'[();]', ' ', text_no_grade)
    
    tokens = [t.strip() for t in text_no_grade.split() if t.strip()]
    
    current_targets = []
    
    # Define skip tokens (administrative terms, delimiters, special categories)
    SKIP_TOKENS = {
        # Basic administrative terms
        "교직이수자", "대상외수강제한", "순수외국인", "입학생",
        "제외", "포함", "수강제한", "타학과수강제한", "군위탁",
        # Delimiters and common words
        ",", ";", "/", "제한", "및", "가능", "수강신청", "수강불가", "등", "구",
        # Special student categories (교환학생 removed - treated as foreigner)
        "학점교류생", "국내대학", "외국국적학생",
        "계약학과", "선취업후진학학과", "장기해외봉사", "현장실습",
        "축구단", "장애학생", "승인자에", "한함", "실습학교", "확정된", "학생만",
        # Category-level terms moved to CATEGORY_MAPPING - now supported!
        # Removed: "인문사회계열", "자연과학계열", "인문사회자연계", "인문사회계열만"
        # Old/invalid department names
        "순환경제·친환경화학소재", "빅데이터컴퓨팅융합", "지식재산융합",
    }

    for token in tokens:
        if token in SKIP_TOKENS:
            continue

        # Skip "전체" ONLY IF it stands alone or doesn't have exclusion context.
        if token == "전체":
             continue

        # Check Category Mapping (e.g., "인문사회계열" -> ["인문대학", "사회과학대학"])
        if token in CATEGORY_MAPPING:
            for college_name in CATEGORY_MAPPING[token]:
                col_id = id_manager.get_college_id(college_name)
                if col_id:
                    current_targets.append({
                        "scopeType": "COLLEGE",
                        "collegeName": id_manager.college_id_to_name[col_id],
                        "departmentName": None,
                        "token": token,
                        "isStrictRestriction": has_strict_flag
                    })
            continue

        # Checking Department (returns LIST of IDs now)
        dept_ids = id_manager.get_department_ids(token)
        if dept_ids:
            for d_id in dept_ids:
                current_targets.append({
                    "scopeType": "DEPARTMENT",
                    "collegeName": None,
                    "departmentName": id_manager.department_id_to_name[d_id],
                    "token": token,
                    "isStrictRestriction": has_strict_flag
                })
            continue

        # Checking College
        col_id = id_manager.get_college_id(token)
        if col_id:
            current_targets.append({
                "scopeType": "COLLEGE",
                "collegeName": id_manager.college_id_to_name[col_id],
                "departmentName": None,
                "token": token,
                "isStrictRestriction": has_strict_flag
            })
            continue

        # If reached here, token is unmapped
        unmapped_tokens.append(token)
        
    # Apply grade/flags to newly parsed positive matches
    for t in current_targets:
        t["minGrade"] = min_grade
        t["maxGrade"] = max_grade
        # Main text targets (outside parenthetical exclusions) are ALWAYS allowed
        # Only targets from exclusion blocks (results list) will be marked as excluded
        t["isExcluded"] = False
            
        t["isForeignerOnly"] = is_foreigner_only
        t["isMilitaryOnly"] = is_military_only
        t["isTeachingCertificateStudent"] = is_teaching_cert
        t["isStrictRestriction"] = has_strict_flag
        if "token" in t: del t["token"]
        
    # Also update the exclusion_matches results with the grade info if they didn't have it
    # Because exclusion blocks usually lack grade info (e.g. "전체 (중문 제외)")
    # We should apply the main grade spec to them too.
    # BUT: Don't override if the exclusion block had its own grade spec (e.g., "수강제한:1학년 ...")
    for r in results:
        # Check if they look like recursion defaults (1-5 grade) and we have specific grade
        # Only override if the target is using default grades (1-5) and main text has specific grade
        is_using_defaults = (r.get("minGrade") == 1 and r.get("maxGrade") == 5)
        if has_grade_spec and is_using_defaults:
             r["minGrade"] = min_grade
             r["maxGrade"] = max_grade
             
        # Also inherit foreigner/military/teaching cert/strict flags
        if is_foreigner_only: r["isForeignerOnly"] = True
        if is_military_only: r["isMilitaryOnly"] = True
        if is_teaching_cert: r["isTeachingCertificateStudent"] = True
        if has_strict_flag: r["isStrictRestriction"] = True
        
    # Merge
    current_targets.extend(results) # Add pre-parsed exclusions

    # Post-processing: Handle "College(Dept1, Dept2)" pattern
    # If we have both a college AND departments, and the pattern is "CollegeName(...)",
    # remove the college (keep only the departments)
    has_college = any(t["scopeType"] == "COLLEGE" for t in current_targets)
    has_departments = any(t["scopeType"] == "DEPARTMENT" for t in current_targets)

    if has_college and has_departments:
        # Check if the pattern matches "단과대(학과들)" in the text
        # Look for college name followed by parentheses with department names inside
        # More strict: college name with max 1 space before paren, and must NOT contain keywords like "수강제한", "제외"
        college_with_parens_pattern = r'(인문대|자연대|사회대|법대|경통대|경영대|공대|공과대|IT대|AI대)\s?\(([^)]+)\)'
        match = re.search(college_with_parens_pattern, original_text)
        if match:
            paren_content = match.group(2)
            # Only remove colleges if the parentheses contain department names, not exclusion keywords
            has_exclusion_keywords = any(kw in paren_content for kw in ['수강제한', '제외', '수강불가', '계약'])
            if not has_exclusion_keywords:
                # Remove college targets, keep only departments
                current_targets = [t for t in current_targets if t["scopeType"] != "COLLEGE"]

    # Post-process targets with grade info
    final_targets = []
    seen_targets = set()

    def add_target(t):
        # Create a unique key for deduplication
        # Include exclusion flags to distinguish between allowed and excluded targets
        key = (
            t["scopeType"],
            t.get("collegeName"),
            t.get("departmentName"),
            t.get("isExcluded"),
            t.get("isForeignerOnly"),
            t.get("isMilitaryOnly"),
            t.get("minGrade"),
            t.get("maxGrade")
        )
        if key not in seen_targets:
            seen_targets.add(key)
            final_targets.append(t)

    if not current_targets:
        # Special case: "국내대학 학점교류생 제외" -> UNIVERSITY allowed
        if "국내대학" in original_text and "학점교류생" in original_text and has_exclude_keyword:
             add_target({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": min_grade,
                "maxGrade": max_grade,
                "isExcluded": False,
                "isForeignerOnly": False,
                "isMilitaryOnly": False,
                "isTeachingCertificateStudent": False
            })
        # If no dept/college found, but has grade spec (e.g. "1학년" -> University wide 1 grade)
        elif has_grade_spec and not unmapped_tokens:
             add_target({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": min_grade,
                "maxGrade": max_grade,
                "isExcluded": is_excluded,
                "isForeignerOnly": is_foreigner_only,
                "isMilitaryOnly": is_military_only,
                 "isTeachingCertificateStudent": is_teaching_cert
            })
        # Special Case: Category exclusion (e.g. "순수외국인 제한", "교환학생 제외")
        # Create TWO targets: base allowed + category excluded
        elif (is_foreigner_only or is_military_only) and has_exclude_keyword:
             # Target 1: Base allowed (everyone can take)
             add_target({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": min_grade,
                "maxGrade": max_grade,
                "isExcluded": False,  # Base is allowed
                "isForeignerOnly": False,
                "isMilitaryOnly": False,
                "isTeachingCertificateStudent": False
            })
             # Target 2: Category excluded
             add_target({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": min_grade,
                "maxGrade": max_grade,
                "isExcluded": True,  # This category is excluded
                "isForeignerOnly": is_foreigner_only,
                "isMilitaryOnly": is_military_only,
                "isTeachingCertificateStudent": is_teaching_cert
            })
        # Special Case: Category inclusion without exclusion keyword
        elif is_foreigner_only or is_military_only:
             add_target({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": min_grade,
                "maxGrade": max_grade,
                "isExcluded": is_excluded,
                "isForeignerOnly": is_foreigner_only,
                "isMilitaryOnly": is_military_only,
                 "isTeachingCertificateStudent": is_teaching_cert
            })
    else:
        for t in current_targets:
            add_target(t)
            
    # Logic to add UNIVERSITY scope if 'exclude' keyword is present (Blacklist logic)
    # But ONLY if:
    # 1. We have exclusion targets (final_targets)
    # 2. NOT in strict mode (strict implies whitelist)
    # 3. Main text had NO explicit targets (e.g., "전체(중문 제외)" vs "전자공학(IT융합 제한)")
    # Check: if current_targets was empty before extending with results, then we need UNIVERSITY base
    has_main_text_targets = len(current_targets) > len(results)  # current_targets = main + results
    
    if has_exclude_keyword and final_targets and not has_strict_flag and not has_main_text_targets:
         # Check if UNIVERSITY scope is already present (avoid duplication)
         if not any(t["scopeType"] == "UNIVERSITY" for t in final_targets):
             final_targets.insert(0, {
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": min_grade,
                "maxGrade": max_grade,
                "isExcluded": False, # The base scope is ALLOWED
                "isForeignerOnly": False, # Base target is for GENERAL population
                "isMilitaryOnly": False,
                "isTeachingCertificateStudent": False,
                "isStrictRestriction": False  # Base UNIVERSITY target for exclusion is not strict
            })

    return final_targets, unmapped_tokens

def main():
    print("Loading ID mappings...")
    id_manager = IdManager()
    id_manager.load_from_yaml(DATA_YML_PATH)
    print(f"Loaded {len(id_manager.college_map)} colleges and {len(id_manager.department_map)} departments.")
    
    print(f"Reading targets from {INPUT_JSON_PATH}...")
    with open(INPUT_JSON_PATH, 'r', encoding='utf-8') as f:
        data = json.load(f)
        
    unique_targets = data.get("targets", [])
    print(f"Found {len(unique_targets)} unique target strings.")
    
    output_data = []
    unmapped_data = []
    
    for item in unique_targets:
        text = item["text"]
        count = item["count"]
        
        parsed, unmapped = parse_target(text, id_manager)
        
        output_data.append({
            "original_text": text,
            "count": count,
            "parsed_targets": parsed
        })
        
        if unmapped:
            unmapped_data.append({
                "original_text": text,
                "unmapped_tokens": unmapped
            })
        
    print(f"Writing results to {OUTPUT_JSON_PATH}...")
    with open(OUTPUT_JSON_PATH, 'w', encoding='utf-8') as f:
        json.dump(output_data, f, ensure_ascii=False, indent=2)
        
    print(f"Writing unmapped targets to {UNMAPPED_JSON_PATH}...")
    with open(UNMAPPED_JSON_PATH, 'w', encoding='utf-8') as f:
        json.dump(unmapped_data, f, ensure_ascii=False, indent=2)
        
    print("Done.")

if __name__ == "__main__":
    main()
