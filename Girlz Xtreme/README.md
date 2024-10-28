## Girlz Xtreme 핵심 코드 [(전체 소스 보기)](https://github.com/futurelabunseen/C-KyungbinCho/tree/test)

#### 언리얼 엔진 5로 제작한 멀티플레이어 PvP 하이퍼 슈팅 장르 Girlz Xtreme입니다.

인원: 1인 개발

코딩 기여도: ![100%](https://progress-bar.dev/100)

개발 기간: 2024/03/17 - 2024/06/28

개발 환경
- Unreal Engine
- C++
- AWS
- Dedicated Server
- GAS
- Python
- gRPC
<br/><br/>

## 캐릭터

[GxCharacter](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/Character/GxCharacter.h)
- 모든 영웅의 Base가 되는 클래스입니다.
- 많은 기능을 캐릭터에 넣지 않고 컴포넌트로 분리하려고 노력했습니다.
<br/><br/>

## 플레이어 스테이트

[GxPlayerState](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/Player/GxPlayerState.h)
- ASC와 Attribute를 관리하는 플레이어 스테이트입니다.
- Attribute 변경 이벤트를 처리합니다.
<br/><br/>

## 메인 플레이 게임 모드

[GxPlayGameMode](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/GameModes/GxPlayGameMode.h)
- gRPC를 활용해 Python 매치메이커와 통신합니다.
- 초기화될 때 매치메이커에 자신의 "IP:포트"를 등록합니다.
- PostLogin(), Logout() 함수에서 클라이언트가 입장/퇴장할 때마다 매치메이커에 자신의 현재 인원을 갱신시킵니다.
- 인원이 가득 차면 플레이를 시작하고 매치메이커에 자신의 주소를 삭제하도록 합니다.
- 플레이 시작 후 모두가 나가버리면 다시 초기화합니다.
<br/><br/>

## 기저 어빌리티 클래스

[GxGameplayAbility](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/AbilitySystem/GxGameplayAbility.h)
- 모든 어빌리티의 Base가 되는 클래스입니다.
- InstancingPolicy, NetExecutionPolicy 등 주요 프로퍼티의 기본 값을 설정합니다.
- CanActivateAbility() 함수에서 현재 영웅이 시전할 수 있는지 확인합니다.
<br/><br/>

## 질풍참 어빌리티

[GxGameplayAbility_SwiftStrike](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/AbilitySystem/Abilities/GxGameplayAbility_SwiftStrike.h)
- 공격A 영웅의 질풍참 스킬 어빌리티입니다.
- 모션 워핑 기능을 활용해 구현했습니다.
<br/><br/>

## 용검 어빌리티

[GxGameplayAbility_DragonBlade](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/AbilitySystem/Abilities/GxGameplayAbility_DragonBlade.h)
- 공격A 영웅의 용검 궁극기 어빌리티입니다.
- 용검 시전 중에는 새로운 Input Mapping Context를 추가해, 좌클릭을 다르게 처리합니다.
- 좌클릭 시 베기(Slash) 어빌리티를 트리거합니다.
<br/><br/>

## 기저 무기 클래스

[GxWeapon](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/Weapons/GxWeapon.h)
- 모든 무기의 Base가 되는 클래스입니다.
<br/><br/>

## 유용한 전역 BP 함수들

[GxStatics](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/GxStatics.h)
- BP 어디서나 사용할 수 있는 함수들입니다.
- 현재 DS의 포트 번호를 가져오는 함수가 구현돼 있습니다.
<br/><br/>

## 유용한 전역 C++ 함수들

[GxUtilityFunctions](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/GxUtilityFunctions.h)
- C++ 어디서나 사용할 수 있는 함수들입니다.
- enum class 열거자의 값을 기반 타입으로 반환하는 함수가 구현돼 있습니다.
<br/><br/>

## AWS 리눅스 인스턴스 제어 스크립트

[Manage Linux Server.bat](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/Manage%20Linux%20Server.bat)
- 원격 AWS 리눅스 인스턴스에서 DS 를 쉽게 관리하기 위한 배치 파일입니다.
- 새로 업로드 / 재실행 / 종료 기능을 지원합니다.
- pscp, plink를 활용합니다.
<br/><br/>

## Protocol Buffer

[match.proto](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/Matchmaker/match.proto)
- 매치메이킹을 위한 gRPC 통신에 사용되는 Protobuf 파일입니다.
<br/><br/>

## Python 매치메이커

[match_server.py](https://github.com/diesuki4/Core_Codes/blob/main/Girlz%20Xtreme/Matchmaker/match_server.py)
- Python gRPC 매치메이킹 서버입니다.
- {DS 주소: 현재 인원} 을 Value 로 정렬되는 딕셔너리에 저장합니다.
- 가장 앞에 있는(인원 수가 많은) DS 부터 매칭합니다.
