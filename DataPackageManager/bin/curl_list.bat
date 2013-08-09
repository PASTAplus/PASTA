@ECHO OFF
ECHO DataPackageManager-0.1 test suite for use on local Windows 7 development workstation 

SET SERVICE_HOST=http://localhost:8080
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk
SET SCOPE=knb-lter-nin
SET BOGUS_SCOPE=xxx
SET IDENTIFIER=1
SET ENTITY_ID=NoneSuchBugCount

ECHO DP-5: DP-5 List Data Package Scopes, Identifiers, Revisions, or Data Entities
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%SCOPE%
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/data/eml/%SCOPE%/%IDENTIFIER%/1
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%?filter=oldest
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%SCOPE%/%IDENTIFIER%?filter=newest
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%BOGUS_SCOPE%
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%BOGUS_SCOPE%/%IDENTIFIER%
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%BOGUS_SCOPE%/%IDENTIFIER%?filter=oldest
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/eml/%BOGUS_SCOPE%/%IDENTIFIER%?filter=newest
