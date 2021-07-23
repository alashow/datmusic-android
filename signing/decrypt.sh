#!/bin/sh

KEY=$1

echo "Decrypting Release key"
openssl aes-256-cbc -d -base64 -pbkdf2 -in signing/datmusic-release.jks.aes -out signing/datmusic-release.jks -k $KEY

echo "Decrypting Play Store Account key"
openssl aes-256-cbc -d -base64 -pbkdf2 -in signing/play-account.json.aes -out signing/play-account.json -k $KEY