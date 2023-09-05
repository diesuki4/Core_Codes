## 월드 오브 탱크 블리츠 모작 핵심 코드 [(전체 소스 보기)](https://github.com/diesuki4/Clone-World_of_Tanks)

#### 월드 오브 탱크 블리츠를 AI와 대전하는 PC 게임으로 모작한 게임입니다.

인원: 클라이언트 2명

개발 기간: 2022/07/26 - 2022/08/29

개발 환경
- Unity
- C#

참고 사항
- 매우 촉박한 일정으로 인해, Getter/Setter를 이용한 멤버 은닉보다는 public으로 선언해 기능 구현에 집중했습니다.
<br/><br/>

[ArtilleryCannon](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/ArtilleryCannon.cs)
- AI 자주포 탱크의 포탑 컴포넌트입니다.
<br/><br/>

[ArtilleryMissile](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/ArtilleryMissile.cs)
- AI 자주포 탱크가 발사하는 폭발 미사일입니다.
- 구면 보간을 이용해 미사일을 이동시킵니다.
- 폭발 반경 내의 적팀에게 데미지를 입힙니다.
<br/><br/>

[ArtilleryTank](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/ArtilleryTank.cs)
- 일정 시간마다 적을 찾아 미사일을 발사하는 AI 자주포 탱크입니다.
<br/><br/>

[Cannon](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/Cannon.cs)
- 일반 AI 탱크의 포탑 컴포넌트입니다.
<br/><br/>

[MGFieldDamageManager](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/MGFieldDamageManager.cs)
- 자기장에 붙어 있는 데미지 처리 컴포넌트입니다.
- 현재 자기장의 반지름을 계산하고, 자기장 밖 플레이어들의 HP를 감소시킵니다.
<br/><br/>

[OutlineManager](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/OutlineManager.cs)
- 플레이어 화면 상 크로스헤어 안에 들어온 탱크에 외곽선을 표시하는 컴포넌트입니다.
- 샤워기와 같은 원리로, 화면 중앙에서 반지름이 radius인 원 내에 deltaRadius/deltaAngle 마다 레이를 쏘아 탱크를 확인합니다.
<br/><br/>

[Projectile](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/Projectile.cs)
- 일반 AI 탱크가 발사하는 포탄입니다.
<br/><br/>

[TagObjectFinder](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/TagObjectFinder.cs)
- 많은 AI 탱크가 태그를 찾는 부하를 줄이기 위해 구현된 풀입니다.
<br/><br/>

[TankAI](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/TankAI.cs)
- 간단한 FSM을 통해 일반 AI 탱크를 동작시키는 컴포넌트입니다.
- 타겟 탐색, 목적지 이동, 공격을 수행합니다.
<br/><br/>

[TrackMover](https://github.com/diesuki4/Core_Codes/blob/main/%EC%9B%94%EB%93%9C%20%EC%98%A4%EB%B8%8C%20%ED%83%B1%ED%81%AC%20%EB%B8%94%EB%A6%AC%EC%B8%A0%20%EB%AA%A8%EC%9E%91/TrackMover.cs)
- UV 오프셋을 조정해 일반 AI 탱크의 무한궤도 효과를 표현하는 컴포넌트입니다.
