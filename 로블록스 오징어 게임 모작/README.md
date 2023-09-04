## 로블록스 오징어 게임 모작 핵심 코드 [(전체 소스 보기)](https://github.com/diesuki4/KBYJ_Squid_Game)

#### 인기 멀티플레이어 게임 로블록스 오징어 게임을 모작했습니다.

인원: 클라이언트 2명

개발 기간: 2022/09/07 - 2022/10/02

개발 환경
- Unity
- C#
- Photon Network
<br/><br/>

[SHTGameManager](https://github.com/diesuki4/Core_Codes/blob/main/%EB%A1%9C%EB%B8%94%EB%A1%9D%EC%8A%A4%20%EC%98%A4%EC%A7%95%EC%96%B4%20%EA%B2%8C%EC%9E%84%20%EB%AA%A8%EC%9E%91/SHTGameManager.cs): 달고나(Sugar Honeycomb Toffee) 게임 매니저입니다.
- RPC를 활용해 게임이 끝난 플레이어 수를 동기화하고, 방장은 게임 종료를 확인합니다.
<br/><br/>

[UI_TextDialogue](https://github.com/diesuki4/Core_Codes/blob/main/%EB%A1%9C%EB%B8%94%EB%A1%9D%EC%8A%A4%20%EC%98%A4%EC%A7%95%EC%96%B4%20%EA%B2%8C%EC%9E%84%20%EB%AA%A8%EC%9E%91/UI_TextDialogue.cs): 차례로 입력한 대화들을 큐에 넣고 하나씩 띄워주는 유틸리티 클래스입니다.
- 싱글톤 패턴으로 구현해 어디서든지 쉽게 사용할 수 있습니다.
- 델리게이트를 활용해 시작, 종료시 추가 작업을 수행할 수 있습니다.
