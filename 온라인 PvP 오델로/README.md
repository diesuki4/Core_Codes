## 온라인 PvP 오델로 핵심 코드 [(전체 소스 보기)](https://github.com/diesuki4/Online_PvP_Othello)

#### TCP/IP 소켓 통신 기반 서버-클라이언트로 구성된 온라인 PvP 오델로 게임입니다.

인원: 학부 3명

코딩 기여도: ![95%](https://progress-bar.dev/95)

개발 기간: 2017/11/04 - 2018/01/05

개발 환경
- Java
- TCP/IP
- 멀티스레드
- 서버-클라이언트 모델
- MVC 패턴
- DB (MariaDB, phpMyAdmin)
- Nginx
- Linux (라즈베리파이 3 모델 B+)

참고 사항
- 분량이 매우 많으므로, 설명이나 파일 최상단 주석을 보고 내용이 궁금하신 경우에만 코드를 보실 것을 권장드립니다.
<br/><br/>

## 서버

[Main](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/Main.java)
- main() 함수를 실행해 (회원 가입/회원 탈퇴/개인정보 변경/전적 검색/전적 갱신/로그인/매치 메이킹/대기열에서 삭제/채팅) 서버들을 구동시킵니다.
<br/><br/>

[ServerConstants](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/ServerConstants.java)
- 서버 주소, 각 서버의 포트 번호, 통신 프로토콜 상수들이 정의되어 있는 클래스입니다.
<br/><br/>

[ServerManager](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/ServerManager.java)
- 모든 서버의 참조를 저장하는 싱글톤 객체입니다.
<br/><br/>

[MemberDTO](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/MemberDTO.java)
- 유저의 개인 정보를 정의한 클래스입니다.
<br/><br/>

[MemberDAO](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/MemberDAO.java)
- DB에 접근해 타 서버에서 필요한 정보들을 가공해 제공하는 클래스입니다.
<br/><br/>

[LoginServer](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/LoginServer.java) (포트: 32437/TCP)
- 로그인/로그아웃을 담당하는 서버입니다.
- 로그인 성공시 유효성 검사용 랜덤 숫자(세션 ID)를 발급하며, 타 서버에 유효성 검사 기능도 제공합니다.
- 매 요청마다 자식 스레드를 생성해 처리하며, 클라이언트에서 연결이 종료되면 스레드도 종료됩니다.
<br/><br/>

[MatchServer](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/MatchServer.java) (포트: 32438/TCP)
- LinkedBlockingQueue(Thread-safe, 대기열이 비었을 경우 wait) 자료구조를 활용해 유저들의 PvP 매칭을 진행합니다.
- 대기열의 크기가 2 이상이면 FIFO에 따라 Front의 2명을 꺼내, 게임 진행 자식 스레드를 생성해 게임을 진행시킵니다.
- 게임은 [Reversi](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/Reversi.java)(오델로 판) 객체를 통해 진행되며, 자식 스레드는 입출력과 상태 전송, 탈주 판정을 진행합니다.
<br/><br/>

[PollServer](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/PollServer.java) (포트: 32440/TCP)
- 유저가 PvP 매칭을 요청하고, 매칭되기 전에 취소해버리면 [MatchServer](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/MatchServer.java)의 대기열에 그대로 남게 됩니다.
- 따라서, 취소 시에 유저 ID, 세션 ID를 받아 대기열에서 제거하는 기능을 제공합니다.
<br/><br/>

[UpdateServer](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/UpdateServer.java) (포트: 32439/TCP)
- 온라인 PvP의 대전 결과를 받아 전적을 갱신하는 서버입니다.
<br/><br/>

[ChatUser](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/ChatUser.java)
- 채팅을 위한 한 명의 유저 데이터를 정의합니다.
- 내 아이디, 상대 아이디, 연결 소켓, 입력 스트림, 출력 스트림을 저장합니다.
<br/><br/>

[ChatServer](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/ChatServer.java) (포트: 32441/TCP)
- 유저들의 PvP 대전 시 1:1 라이브 채팅을 제공하는 서버입니다.
- 메인 스레드는 연결 요청을 받고, 각 유저의 연결 요청마다 자식 스레드를 생성해 메시지를 받고 전달합니다.
<br/><br/>

[Reversi](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/Reversi.java)
- 내부적으로 오델로를 실행하는 코어이며, 싱글 AI 대전과 온라인 PvP에서 동일한 클래스를 사용합니다.
- 돌 위치를 입력하면 알아서 내부적으로, 둘 수 있는 위치인지 판정, 돌 뒤집기, 차례 넘기기를 수행합니다.
- 싱글 AI 대전의 경우, 돌을 둘 수 있는 위치 중 난이도에 따라 알고리즘을 수행해 두는 위치가 달라집니다.
<br/><br/>

[Encryptor](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/Encryptor.java)
- 네트워크 통신에서 암복호화를 담당하는 클래스이며, Encrypt/Decrypt 기능을 제공합니다.
- 최대 길이 20의 문자열까지 지원하며, 길이 40의 랜덤 문자열에 길이 정보와 원본 문자열을 지정된 위치에 포함시켜 암호화합니다.
- 사실상 암호화보다는 난독화에 가깝습니다.
<br/><br/>

## 클라이언트

[Main](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8/Main.java)
- main() 함수를 실행해 게임 실행에 필요한 정보들을 초기화하고, 시작 UI를 띄웁니다.
<br/><br/>

[Login](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8/Login.java)
- 로그인/회원가입 화면입니다.
- 로그인 서버에 요청을 보내고, [ServerConstants](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/ServerConstants.java)에 정의된 프로토콜로 결과를 판별합니다.
<br/><br/>

[PlayReversi](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8/PlayReversi.java)
- [Reversi](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/Reversi.java)(오델로 판) 객체를 통해, 난이도에 따라 싱글 AI 대전을 진행하는 화면(패널)입니다.
<br/><br/>

[PlayReversiOnline](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8/PlayReversiOnline.java)
- 자식 스레드에서 게임을 진행 중인 [MatchServer](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/MatchServer.java)에 돌 위치를 전송하고, 결과를 받아 온라인 PvP를 진행하는 화면(패널)입니다.
- 진행 중에 [ChatServer](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/ChatServer.java)와 통신하며 1:1 채팅을 이용할 수 있습니다.
- 대전의 승자가 [UpdateServer](https://github.com/diesuki4/Core_Codes/blob/main/%EC%98%A8%EB%9D%BC%EC%9D%B8%20PvP%20%EC%98%A4%EB%8D%B8%EB%A1%9C/%EC%84%9C%EB%B2%84/UpdateServer.java)에 전적 갱신을 요청합니다.
