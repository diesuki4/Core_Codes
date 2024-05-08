@echo off

set GAME_NAME=MyGameName
set SOURCE_DIRECTORY=LinuxServer
set LISTEN_PORT=7777
set REMOTE_DESTINATION=ec2-00-000-000-00.ap-northeast-2.compute.amazonaws.com
set REMOTE_USER=ec2-user
set AUTH_PRIVATE_KEY=Auth.ppk

echo [1] 새로 업로드 및 재실행
echo [2] 새로 업로드 및 재실행 + 로그 출력
echo [3] 재실행
echo [4] 재실행 + 로그 출력
echo [5] 종료
echo [q] 나가기
choice /c:12345q /n /m "선택: "

set INPUT=%ERRORLEVEL%
if %INPUT% EQU 1 goto UploadNewAndRestart
if %INPUT% EQU 2 goto UploadNewAndRestartWithLog
if %INPUT% EQU 3 goto Restart
if %INPUT% EQU 4 goto RestartWithLog
if %INPUT% EQU 5 goto Kill
exit /b

rem ##################################
:UploadNewAndRestart

(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "pkill -f '%GAME_NAME%'"
(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "rm -rf %SOURCE_DIRECTORY%"

(echo n) | pscp -r -i %AUTH_PRIVATE_KEY% ./%SOURCE_DIRECTORY% %REMOTE_USER%@%REMOTE_DESTINATION%:

(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "chmod +x %SOURCE_DIRECTORY%/%GAME_NAME%Server.sh"
(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "nohup ./%SOURCE_DIRECTORY%/%GAME_NAME%Server.sh -port=%LISTEN_PORT% 1> /dev/null 2>&1 &"

goto Exit
rem ##################################
:UploadNewAndRestartWithLog

(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "pkill -f '%GAME_NAME%'"
(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "rm -rf %SOURCE_DIRECTORY%"

(echo n) | pscp -r -i %AUTH_PRIVATE_KEY% ./%SOURCE_DIRECTORY% %REMOTE_USER%@%REMOTE_DESTINATION%:

(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "chmod +x %SOURCE_DIRECTORY%/%GAME_NAME%Server.sh"
(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "nohup ./%SOURCE_DIRECTORY%/%GAME_NAME%Server.sh -port=%LISTEN_PORT% &"

goto Exit
rem ##################################
:Restart

(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "pkill -f '%GAME_NAME%'"
(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "nohup ./%SOURCE_DIRECTORY%/%GAME_NAME%Server.sh -port=%LISTEN_PORT% 1> /dev/null 2>&1 &"

goto Exit
rem ##################################
:RestartWithLog

(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "pkill -f '%GAME_NAME%'"
(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "nohup ./%SOURCE_DIRECTORY%/%GAME_NAME%Server.sh -port=%LISTEN_PORT% &"

goto Exit
rem ##################################
:Kill

(echo n) | plink -i %AUTH_PRIVATE_KEY% %REMOTE_USER%@%REMOTE_DESTINATION% "pkill -f '%GAME_NAME%'"

:Exit
echo.
echo.
pause
