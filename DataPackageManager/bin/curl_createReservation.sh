#!/usr/bin/env bash

# Create a reservation for a scope.

set -e # exit on error

host="http://host:8088"

read -rp "User (e.g., EDI):  " user
read -rsp "Password: " password
echo
read -rp "Scope (e.g., edi): " scope

set -x # echo commands
curl -i -u "uid=${user:?},o=EDI,dc=edirepository,dc=org:${password:?}" -X POST "${host}/package/reservation/eml/${scope:?}"
