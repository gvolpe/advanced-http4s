#!/usr/bin/env bash

curl -v -F "this_is_a_name=@upload.txt" localhost:8080/v1/multipart