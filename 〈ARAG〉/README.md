## &lt;ARAG&gt; 핵심 코드 [(전체 소스 보기)](https://github.com/diesuki4/ARAG)

#### 언리얼 엔진 학습을 위해 만들었던 RPG 장르 [정말 멋진 게임(A Really Awesome Game)](https://diesuki4.tistory.com/category/%EA%B2%8C%EC%9E%84%20%EA%B0%9C%EB%B0%9C/%3CARAG%3E)입니다.

인원: 1인 개발

코딩 기여도: ![100%](https://progress-bar.dev/100)

개발 기간: 2023/06/06 - 2023/09/16

개발 환경
- Unreal Engine
- C++
<br/><br/>

## 캐릭터

[Character](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Character/ARCharacter.h)
- 메인 캐릭터 클래스입니다.
- 캐릭터 상태를 동기화하기 위해 옵저버 패턴으로 구현했습니다.
- 캐릭터 데이터를 Data Component, 전투 관련 작업을 Combat Component로 관리합니다.
<br/><br/>

[CharacterAnimInstance](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Character/ARCharacterAnimInstance.h)
- 메인 캐릭터 애님 인스턴스입니다.
- 무기 장착 시 그에 맞는 애니메이션 몽타주가 설정되며, 몽타주 제어 기능을 제공합니다.
<br/><br/>

## 캐릭터 / 씬 컴포넌트

[QuiverComponent](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Character/SceneComponents/ARQuiverComponent.h)
- 활(Bow) 무기 장착 시 캐릭터에 추가되는 화살집 컴포넌트입니다.
- Static Mesh Component를 상속해 제작했으며, 화살 액터 배열을 관리합니다.
<br/><br/>

## 캐릭터 / 무기

[WeaponBase](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Character/Weapons/ARWeaponBase.h)
- 무기 Base 클래스입니다.
- 무기 장착과 해제, 좌클릭과 우클릭 시 동작, 근접 공격 함수 등을 오버라이드 할 수 있도록 제공합니다.
- 무기별 애니메이션 몽타주, 장착 소켓, 데미지, Aim Offset 사용 여부 등의 속성을 갖습니다.
<br/><br/>

[WeaponSkeletalBase](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Character/Weapons/ARWeaponSkeletalBase.h) (WeaponBase - WeaponSkeletalBase)
- Skeletal Mesh를 메시로 갖는(애니메이션이 있는) 무기 Base 클래스입니다.
- Skeletal Mesh Component를 멤버로 가지며, WeaponBase의 장착/해제 함수를 오버라이드해 캐릭터에 붙이고 뗄 수 있도록 합니다.
<br/><br/>

[Bow](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Character/Weapons/ARBow.h) (WeaponBase - WeaponSkeletalBase - Bow)
- 활 무기입니다.
- 활 조준(좌클릭 Pressed), 활 쏘기(좌클릭 Released), 화살 다시 집어넣기 (우클릭 Pressed) 기능을 제공합니다.
- WeaponBase의 함수들을 오버라이드해 구현했습니다.
<br/><br/>

[Sword](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Character/Weapons/ARSword.h) (WeaponBase - WeaponSkeletalBase - Sword)
- 검 무기입니다.
- 공격(좌클릭 Pressed) 기능을 제공합니다.
- WeaponBase의 함수들을 오버라이드해 구현했습니다.
<br/><br/>

## 몬스터

[MonsterBase](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Monster/ARMonsterBase.h)
- 몬스터 Base 클래스입니다.
- 몬스터 상태를 동기화하기 위해 옵저버 패턴으로 구현했습니다.
- 캐릭터 상호작용 관련 설정값, Locomotion 애니메이션 등의 몬스터 데이터를 Data Component로 관리합니다.
<br/><br/>

[MonsterController](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Monster/ARMonsterController.h)
- 몬스터 AI 컨트롤러입니다.
- 비헤이비어 트리로 AI를 구동시키며 , AI Perception 컴포넌트로 캐릭터를 감지합니다.
<br/><br/>

[MonsterAnimInstance](https://github.com/diesuki4/Core_Codes/blob/main/%E3%80%88ARAG%E3%80%89/Monster/ARMonsterAnimInstance.h)
- 몬스터 애님 인스턴스입니다.
- ABP에서 사용될 변수의 값을 갱신하고, Data Component로 가져온 애니메이션을 ABP에 제공합니다.
