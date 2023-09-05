## 카트라이더 러쉬+ 모작 핵심 코드 [(전체 소스 보기)](https://github.com/diesuki4/Clone-KartRider_Rush_Plus)

#### 카트라이더 러쉬+를 AI와 대전하는 PC 게임으로 모작한 게임입니다.

인원: 클라이언트 2명

개발 기간: 2022/06/28 - 2022/07/25

개발 환경
- Unity
- C#

참고 사항
- 매우 촉박한 일정으로 인해, Getter/Setter를 이용한 멤버 은닉보다는 public으로 선언해 기능 구현에 집중했습니다.
<br/><br/>

[Audio Manager](https://github.com/diesuki4/Core_Codes/blob/main/%EC%B9%B4%ED%8A%B8%EB%9D%BC%EC%9D%B4%EB%8D%94%20%EB%9F%AC%EC%89%AC%2B%20%EB%AA%A8%EC%9E%91/AudioManager.cs)
- 싱글톤 패턴으로 구현한 사운드 매니저입니다.
<br/><br/>

[Boost](https://github.com/diesuki4/Core_Codes/blob/main/%EC%B9%B4%ED%8A%B8%EB%9D%BC%EC%9D%B4%EB%8D%94%20%EB%9F%AC%EC%89%AC%2B%20%EB%AA%A8%EC%9E%91/Boost.cs)
- 플레이어 카트의 부스터 담당 컴포넌트입니다.
<br/><br/>

[PlayerDrive](https://github.com/diesuki4/Core_Codes/blob/main/%EC%B9%B4%ED%8A%B8%EB%9D%BC%EC%9D%B4%EB%8D%94%20%EB%9F%AC%EC%89%AC%2B%20%EB%AA%A8%EC%9E%91/PlayerDrive.cs)
- 플레이어 기본 주행 및 조작 담당 컴포넌트입니다.
