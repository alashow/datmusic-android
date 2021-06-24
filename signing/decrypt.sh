#!/bin/sh

KEY=$1

echo "Decrypting Release key"
openssl aes-256-cbc -d -a -in signing/alashov-release.jks.aes -out signing/alashov-release.jks -k $KEY

echo "Decrypting Play Store Account key"
openssl aes-256-cbc -d -a -in signing/play-account.json.aes -out signing/play-account.json -k $KEY