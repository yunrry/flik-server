import json
from collections import defaultdict

def create_notion_table():
    """JSON에서 이름만 추출하여 노션 복사용 표 생성"""
    
    # JSON 파일 로드
    with open('response_1757668259132.json', 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    items = data['response']['body']['items']['item']
    
    # depth1별로 그룹화
    depth1_groups = defaultdict(lambda: defaultdict(list))
    
    for item in items:
        depth1_name = item['lclsSystm1Nm']
        depth2_name = item['lclsSystm2Nm']
        depth3_name = item['lclsSystm3Nm']
        
        depth1_groups[depth1_name][depth2_name].append(depth3_name)
    
    # 노션 표 형식으로 출력
    print("| 대분류 | 중분류 | 소분류 |")
    print("|--------|--------|--------|")
    
    for depth1_name in sorted(depth1_groups.keys()):
        depth2_items = list(depth1_groups[depth1_name].items())
        
        # 첫 번째 중분류부터 시작
        for i, (depth2_name, depth3_list) in enumerate(depth2_items):
            depth3_list = sorted(depth3_list)
            
            if i == 0:
                # 첫 번째 중분류는 대분류와 함께 표시
                print(f"| **{depth1_name}** | **{depth2_name}** | {', '.join(depth3_list)} |")
            else:
                # 나머지 중분류는 대분류 열 비우기
                print(f"| | **{depth2_name}** | {', '.join(depth3_list)} |")
        
        # 대분류 구분을 위한 빈 행
        print("| | | |")

def create_simple_table():
    """간단한 형태의 표 생성"""
    
    # JSON 파일 로드
    with open('response_1757668259132.json', 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    items = data['response']['body']['items']['item']
    
    # depth1별로 그룹화
    depth1_groups = defaultdict(lambda: defaultdict(list))
    
    for item in items:
        depth1_name = item['lclsSystm1Nm']
        depth2_name = item['lclsSystm2Nm']
        depth3_name = item['lclsSystm3Nm']
        
        depth1_groups[depth1_name][depth2_name].append(depth3_name)
    
    # 간단한 표 형식으로 출력
    print("| 분류 | 하위 분류 |")
    print("|------|----------|")
    
    for depth1_name in sorted(depth1_groups.keys()):
        print(f"| **{depth1_name}** | |")
        
        for depth2_name in sorted(depth1_groups[depth1_name].keys()):
            depth3_list = sorted(depth1_groups[depth1_name][depth2_name])
            depth3_text = " • ".join(depth3_list)
            print(f"| | **{depth2_name}** |")
            print(f"| | {depth3_text} |")
        
        print("| | |")

def create_flat_list():
    """평면적인 리스트 형태로 출력"""
    
    # JSON 파일 로드
    with open('response_1757668259132.json', 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    items = data['response']['body']['items']['item']
    
    # depth1별로 그룹화
    depth1_groups = defaultdict(list)
    
    for item in items:
        depth1_name = item['lclsSystm1Nm']
        depth2_name = item['lclsSystm2Nm']
        depth3_name = item['lclsSystm3Nm']
        
        depth1_groups[depth1_name].append(f"{depth2_name} > {depth3_name}")
    
    # 리스트 형태로 출력
    for depth1_name in sorted(depth1_groups.keys()):
        print(f"\n## {depth1_name}")
        for item in sorted(depth1_groups[depth1_name]):
            print(f"- {item}")

if __name__ == "__main__":
    print("=== 노션 표 형식 (3열) ===")
    create_notion_table()
    
    print("\n\n=== 노션 표 형식 (2열) ===")
    create_simple_table()
    
    print("\n\n=== 마크다운 리스트 형식 ===")
    create_flat_list()