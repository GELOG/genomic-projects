#!/bin/bash
#HIRO_PATH="~/src/go/src/github.com/peterhellberg/hiro"
HIRO_PATH="$HOME/src/go/src/github.com/peterhellberg/hiro"
#HIRO_PATH="$HOME/opt/go-1.2-amd64/gopath/src/github.com/peterhellberg/hiro"

MERGED_BLUEPRINT_FILE=$1
HTML_BLUEPRINT_FILE=$2

echo $HOME
go run $HIRO_PATH/main.go -input $MERGED_BLUEPRINT_FILE -output $HTML_BLUEPRINT_FILE
