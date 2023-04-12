#!/usr/bin/env bash

# Download a data package as a zip archive.

set -e # exit on error

HOST="http://localhost:8088"

[[ $# -ne 4 ]] && {
  echo "Usage: $0 <uid> <scope> <revision> <revision>"
  exit 1
}

uid="${1:?}"
scope="${2:?}"
identifier="${3:?}"
revision="${4:?}"

if [[ -z ${EDI_PASSWORD} ]]; then
  read -rsp "Password (export EDI_PASSWORD to avoid prompt): " password
else
  password="${EDI_PASSWORD}"
fi

out_file="${scope}.${identifier}.${revision}.zip"

printf 'Downloading: %s\n' "${out_file}"

(
  set -x # echo commands

  status="$(
    curl \
      --silent \
      --output "${out_file}" \
      --user "uid=${uid},o=EDI,dc=edirepository,dc=org:${password}" \
      --request GET \
      "${HOST}/package/download/eml/${scope}/${identifier}/${revision}"
  )"

  #    --data-binary "@${version}" \
  #    --header "Content-Type: application/xml" \

  printf 'Status: %s\n' "${status}"
)
