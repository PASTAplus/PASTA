@ECHO OFF

SET SERVICE_HOST=http://localhost:8080
REM SET SERVICE_HOST=http://pasta-s.lternet.edu
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk

curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/2"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/3"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/4"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/5"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/6"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/7"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/8"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/9"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/10"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/11"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/12"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/13"
curl -i -b auth-token=%AUTH_TOKEN_DCOSTA% -G "%SERVICE_HOST%/eventmanager/subscription/eml/14"
