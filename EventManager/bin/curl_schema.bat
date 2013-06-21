@ECHO OFF

SET SERVICE_HOST=http://localhost:8080
REM SET SERVICE_HOST=http://pasta-s.lternet.edu
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=

curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/eventmanager/subscription/eml/schema"
