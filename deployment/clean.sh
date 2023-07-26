#!/usr/bin/env bash
# This scripts generates test keys and certificates for the sample.
# In a production environment such artifacts should be generated
# by a proper certificate authority and handled in a secure manner.


# Exit on errors
set -e

# Check for required parameters
if [[ $1 == "" ]]; then
    echo "Error missing required parameters: make clean <solution_name>"
    exit 1
fi

# init vaiables
solution_name="$1"

# create environment settings
pac solution delete --solution-name $solution_name
