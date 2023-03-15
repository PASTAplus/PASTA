#!/usr/bin/env bash

# Delete a journal citation.

set -e # exit on error

HOST="http://localhost:8088"

[[ $# -ne 2 ]] && {
  echo "Usage: $0 <uid> <citation ID>"
  exit 1
}

uid="${1:?}"
citation_id="${2:?}"

if [[ -z ${EDI_PASSWORD} ]]; then
  read -rsp "Password (export EDI_PASSWORD to avoid prompt): " password
else
  password="${EDI_PASSWORD}"
fi

(
  set -x # echo commands

  curl \
    --include \
    --user "uid=${uid},o=EDI,dc=edirepository,dc=org:${password}" \
    --request DELETE \
    "${HOST}/package/citation/eml/${citation_id}"
)
