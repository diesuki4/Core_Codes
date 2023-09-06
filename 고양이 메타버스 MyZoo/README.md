## 고양이 메타버스 MyZoo 핵심 코드 [(전체 소스 보기)](https://github.com/diesuki4/MTVSAC_MYZOO)

#### 가상 고양이를 키우며 지역 유저들과 소통할 수 있는 고양이 메타버스입니다.

인원: 클라이언트 2명, 네트워크 2명, AI 2명, 아트 2명

코딩 기여도: ![20%](https://progress-bar.dev/20)

개발 기간: 2022/08/31 - 2022/09/02

개발 환경
- Unity
- C#
- REST API
- UniTask

참고 사항
- 게임잼 형태로 3일간 진행되다 보니, 많은 내용을 구현하지는 못 했습니다.
<br/><br/>

[HttpManager](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%A0%EC%96%91%EC%9D%B4%20%EB%A9%94%ED%83%80%EB%B2%84%EC%8A%A4%20MyZoo/HttpManager.cs)
- HTTP 요청을 처리하는 클래스입니다.
- 코루틴을 통해 GET/POST 요청을 보내고, 완료시 델리게이트에 등록된 작업을 수행합니다.
<br/><br/>

[HttpRequester](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%A0%EC%96%91%EC%9D%B4%20%EB%A9%94%ED%83%80%EB%B2%84%EC%8A%A4%20MyZoo/HttpRequester.cs)
- HTTP 요청에 필요한 멤버들을 선언한 클래스입니다.
<br/><br/>

[LoginUIManager](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%A0%EC%96%91%EC%9D%B4%20%EB%A9%94%ED%83%80%EB%B2%84%EC%8A%A4%20MyZoo/LoginUIManager.cs)
- 로그인 UI 매니저입니다.
- 고양이 이미지 업로드, 로그인 버튼 등의 콜백 함수가 구현되어 있습니다.
<br/><br/>

[MainGameManager](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%A0%EC%96%91%EC%9D%B4%20%EB%A9%94%ED%83%80%EB%B2%84%EC%8A%A4%20MyZoo/MainGameManager.cs)
- 인게임에서 가상 고양이와 놀아줄 때 사용되는 게임 매니저입니다.
- 일정 시간 마다 호감도, 굶주림, 청결도를 감소시키며 서버에 게임 데이터를 업로드합니다.
