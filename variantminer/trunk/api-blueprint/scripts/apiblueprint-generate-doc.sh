#!/bin/bash

# Absolute path to this script. /home/user/bin/foo.sh
SCRIPT=$(readlink -f $0)
# Absolute path this script is in. /home/user/bin
SCRIPT_PATH=`dirname "$SCRIPT"`
BLUEPRINT_PATH=`dirname "$SCRIPT_PATH"`

MERGED_BLUEPRINT_FILE=$1
HTML_BLUEPRINT_FILE=$2
TEMPLATE="$BLUEPRINT_PATH/layout/default-multi.jade"

#echo $HOME
#echo $TEMPLATE

aglio -t "$TEMPLATE" -i $MERGED_BLUEPRINT_FILE -o $HTML_BLUEPRINT_FILE

