#!/usr/bin/env bash

# Update a journal citation.

set -e # exit on error

HOST="http://localhost:8088"

[[ $# -ne 3 ]] && {
  echo "Usage: $0 <uid> <citation ID> <citation XML file>"
  exit 1
}

uid="${1:?}"
citation_id="${2:?}"
xml_file="${3:?}"

[[ -f ${xml_file} ]] || {
  echo "File not found: ${xml_file}"
  exit 1
}

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
    --request PUT \
    --data-binary "@${xml_file}" \
    --header "Content-Type: application/xml" \
    "${HOST}/package/citation/eml/${citation_id}"
)
