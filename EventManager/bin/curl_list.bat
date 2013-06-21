@ECHO OFF

SET SERVICE_HOST=http://localhost:8080
REM SET SERVICE_HOST=http://pasta-s.lternet.edu
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk
SET SCOPE=knb-lter-lno
SET IDENTIFIER=1
SET REVISION=2

curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/eventmanager/subscription/eml"
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/eventmanager/subscription/eml?scope=%SCOPE%"
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/eventmanager/subscription/eml?scope=%SCOPE%&identifier=%IDENTIFIER%"
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/eventmanager/subscription/eml?scope=%SCOPE%&identifier=%IDENTIFIER%&revision=%REVISION%"
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/eventmanager/subscription/eml?scope=%SCOPE%&scope=knb-lter-xyz"
