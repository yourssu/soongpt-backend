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
    "건축": "건축학부",
    "건축학": "건축학부 건축학전공",
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
    "AI소프트": "AI소프트웨어학과",
    "AI소프트웨어": "AI소프트웨어학과",
}

COLLEGE_ALIAS = {
    "IT대": "IT대학",
    "공과대": "공과대학",
    "인문대": "인문대학",
    "자연대": "자연과학대학",
    "경상대": "경영대학", # 구 명칭줘
    
    "경통대": "경제통상대학",
    "사회대": "사회과학대학",
    "법대": "법과대학",
    "AI대": "AI대학", # 가칭/신설
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

    def get_department_id(self, name):
        name = name.strip()
        # Direct match
        if name in self.department_map:
            return self.department_map[name]
        # Alias match
        if name in DEPT_ALIAS:
            mapped_name = DEPT_ALIAS[name]
            return self.department_map.get(mapped_name)
            
        # Partial match heuristic (risky but useful for "국어국문" vs "국어국문학과")
        for stored_name in self.department_map.keys():
            if name in stored_name: # e.g. "컴퓨터" in "컴퓨터학부"
                 # Prefer exact alias first, but if not found...
                 pass 
        return None

def parse_target(text, id_manager):
    # Normalize
    original_text = text
    text = text.replace(",", " , ").replace(";", " ; ") # Pad delimiters
    
    # Flags
    has_strict_flag = "대상외수강제한" in text or "타학과수강제한" in text
    has_exclude_keyword = "제외" in text or "제한" in text
    
    is_excluded = has_strict_flag or has_exclude_keyword
    is_foreigner_only = "순수외국인" in text or "외국국적" in text or "외국인" in text
    is_military_only = "군위탁" in text
    
    # Remove flags for cleaner parsing
    clean_text = re.sub(r'\(.*?(대상외수강제한|타학과수강제한|수강제한).*?\)', '', text)
    clean_text = re.sub(r'순수외국인[^\s]*', '', clean_text)
    clean_text = clean_text.replace("군위탁", "").replace("입학생", "").replace("제한", "")
    clean_text = clean_text.strip()
    
    results = []
    unmapped_tokens = []
    
    # Case 1: University Wide (전체, 전체학년)
    if "전체학년" in clean_text and clean_text == "전체학년":
        return [{
            "scopeType": "UNIVERSITY",
            "collegeName": None,
            "departmentName": None,
            "minGrade": 1, 
            "maxGrade": 5,
            "isExcluded": is_excluded,
            "isForeignerOnly": is_foreigner_only,
            "isMilitaryOnly": is_military_only
        }], []
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
                "isMilitaryOnly": is_military_only
            }], []
        
    # Split by lines if any
    lines = clean_text.split('\n')
    if len(lines) > 1:
        for line in lines:
            res, unmapped = parse_target(line, id_manager)
            results.extend(res)
            unmapped_tokens.extend(unmapped)
            
        # Propagate STRICT flags only. Do not propagate 'exclude' keyword as it is local or handled by base addition.
        if has_strict_flag:
            for r in results:
                r["isExcluded"] = True
        if is_foreigner_only:
            for r in results:
                r["isForeignerOnly"] = True
        if is_military_only:
            for r in results:
                r["isMilitaryOnly"] = True
                
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
    
    for token in tokens:
        if token in ["대상외수강제한", "순수외국인", "입학생", "제외", "포함", "수강제한", "타학과수강제한", "군위탁"]:
            continue
            
        # Skip "전체" ONLY IF it stands alone or doesn't have exclusion context.
        # But actually, we just need to ensure we don't treat "전체" as a department token.
        if token == "전체":
             continue
            
        # Checking Department
        dept_id = id_manager.get_department_id(token)
        if dept_id:
            current_targets.append({
                "scopeType": "DEPARTMENT",
                "collegeName": None,
                "departmentName": id_manager.department_id_to_name[dept_id],
                "token": token
            })
            continue
            
        # Checking College
        col_id = id_manager.get_college_id(token)
        if col_id:
            current_targets.append({
                "scopeType": "COLLEGE",
                "collegeName": id_manager.college_id_to_name[col_id],
                "departmentName": None,
                "token": token
            })
            continue
            
        # If reached here, token is unmapped
        unmapped_tokens.append(token)
    
    # Post-process targets with grade info
    final_targets = []
    if not current_targets:
        # If no dept/college found, but has grade spec (e.g. "1학년" -> University wide 1 grade)
        if has_grade_spec and not unmapped_tokens:
             final_targets.append({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": min_grade,
                "maxGrade": max_grade,
                "isExcluded": is_excluded,
                "isForeignerOnly": is_foreigner_only,
                "isMilitaryOnly": is_military_only
            })
        # Special Case: Empty text but specific flags (e.g. "순수외국인 제외")
        elif is_foreigner_only or is_military_only:
             final_targets.append({
                "scopeType": "UNIVERSITY",
                "collegeName": None,
                "departmentName": None,
                "minGrade": min_grade,
                "maxGrade": max_grade,
                "isExcluded": is_excluded,
                "isForeignerOnly": is_foreigner_only,
                "isMilitaryOnly": is_military_only
            })
    else:
        for t in current_targets:
            t["minGrade"] = min_grade
            t["maxGrade"] = max_grade
            t["isExcluded"] = is_excluded
            t["isForeignerOnly"] = is_foreigner_only
            t["isMilitaryOnly"] = is_military_only
            del t["token"]
            final_targets.append(t)
            
    # Logic to add UNIVERSITY scope if 'exclude' keyword is present (Blacklist logic)
    # But ONLY if we have some targets extracted (the targets being excluded)
    if has_exclude_keyword and final_targets:
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
                "isMilitaryOnly": False
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
