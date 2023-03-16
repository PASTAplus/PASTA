#!/usr/bin/env bash

# Create a journal citation.

set -e # exit on error

HOST="http://localhost:8088"

[[ $# -ne 2 ]] && {
  echo "Usage: $0 <uid> <citation XML file>"
  exit 1
}

uid="${1:?}"
xml_file="${2:?}"

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
    --request POST \
    --data-binary "@${xml_file}" \
    --header "Content-Type: application/xml" \
    "${HOST}/package/citation/eml"
)
