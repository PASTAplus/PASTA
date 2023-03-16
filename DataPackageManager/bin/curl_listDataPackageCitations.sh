#!/usr/bin/env bash

# Purpose: List data package citations
# Usage: bin/curl_listDataPackageCitations.sh

service_host=http://localhost:8088
scope=edi
identifier=0
revision=3

curl -i -G ${service_host}/package/citations/eml/${scope}/${identifier}/${revision}
