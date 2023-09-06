## 공연 플랫폼 SMU:S 핵심 코드 [(전체 소스 보기)](https://github.com/diesuki4/MTVSAC_SMUS)

#### 창작자가 자신의 가상 공연을 제작할 수 있는 메타버스 공연 플랫폼입니다.

인원: 클라이언트 3명 / 아트 3명

코딩 기여도: ![80%](https://progress-bar.dev/80)

개발 기간: 2022/10/17 - 2022/12/01

개발 환경
- Unity
- C#
- Photon Network
- 모션 캡처 (Unity Barracuda, Google Mediapipe)
- DB (MariaDB)
- FTP (vsFTPd)
- WebDAV (Nginx)
- Linux (라즈베리파이 3 모델 B+)

참고 사항
- 팀에 네트워크 인원이 없어 직접 DB와 파일 서버를 구축했습니다.
- 클라이언트로 참여했기 때문에, API 서버 구축이나 세션 관리 등은 하지 않았습니다.
<br/><br/>

## 네트워크 송수신 관련

[DBManager](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%B5%EC%97%B0%20%ED%94%8C%EB%9E%AB%ED%8F%BC%20SMUS/Network/DBManager.cs)
- DB에 접근해 쿼리를 요청하고 결과를 간소화시켜 반환하기 위한 클래스입니다.
- DataTable 형식의 결과를 레코드(딕셔너리로 구현)의 리스트로 변환해 반환합니다.
<br/><br/>

[FTPManager](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%B5%EC%97%B0%20%ED%94%8C%EB%9E%AB%ED%8F%BC%20SMUS/Network/FTPManager.cs)
- FTP 서버에 접근해 파일을 업로드/다운로드 하는 클래스입니다.
<br/><br/>

[ConcertManager](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%B5%EC%97%B0%20%ED%94%8C%EB%9E%AB%ED%8F%BC%20SMUS/Network/ConcertManager.cs)
- FTP, DB 매니저를 활용해 공연 정보를 주고 받는 클래스입니다.
<br/><br/>

[AccountManager](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%B5%EC%97%B0%20%ED%94%8C%EB%9E%AB%ED%8F%BC%20SMUS/Network/AccountManager.cs)
- 로그인, 로그아웃, 회원가입, 개인정보 변경을 담당하는 계정 매니저입니다.
<br/><br/>

## 타임라인 관련

[TL_Types](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%B5%EC%97%B0%20%ED%94%8C%EB%9E%AB%ED%8F%BC%20SMUS/Timeline/TL_Types.cs)
- 타임라인에 찍을 키에 필요한 데이터를 정의한 클래스입니다.
<br/><br/>

[TL_Timeline](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%B5%EC%97%B0%20%ED%94%8C%EB%9E%AB%ED%8F%BC%20SMUS/Timeline/TL_Timeline.cs)
- 하나의 소품의 키 목록을 관리하기 위한 클래스입니다.
- 타임라인은 Dictionaty[GUID] = SortedSet 형태로 저장됩니다.
- 각 소품은 고유의 GUID를 가지며, 타임라인 키는 프레임이 빠른 순으로 저장됩니다.
<br/><br/>

[TL_Utility](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%B5%EC%97%B0%20%ED%94%8C%EB%9E%AB%ED%8F%BC%20SMUS/Timeline/TL_Utility.cs)
- 타임라인을 cdata 형식으로 저장하고 불러오기 위한 유틸리티 클래스입니다.
<br/><br/>

[TimelineManager](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%B5%EC%97%B0%20%ED%94%8C%EB%9E%AB%ED%8F%BC%20SMUS/Timeline/TimelineManager.cs)
- 공연 창작툴의 타임라인을 담당하는 클래스입니다.
- 타임라인에 등록된 소품 목록을 관리하며, 소품과 키 추가, 공연 저장과 불러오기 기능을 제공합니다.
<br/><br/>

## 창작툴 관련

[BuildingSystem](https://github.com/diesuki4/Core_Codes/blob/main/%EA%B3%B5%EC%97%B0%20%ED%94%8C%EB%9E%AB%ED%8F%BC%20SMUS/BuildingSystem.cs)
- 공연 창작툴의 중심이 되는 싱글톤 오브젝트이며, 그리드에 붙은 컴포넌트입니다.
- 그리드 상의 소품 목록 관리, 셀 관리, 겹침 판정 등의 기능을 수행합니다.
