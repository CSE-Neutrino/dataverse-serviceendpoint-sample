#!/usr/bin/env bash
# This scripts package and import the solution to the target environment.

# Exit on errors
set -e

# Check for required parameters
if [[ $1 == "" ]]; then
    echo "Error missing required parameters: make deploy <solution_name>"
    exit 1
fi

# init vaiables
solution_name="$1"

# create solution package
pac solution pack --zipfile solutions/$solution_name.zip --folder solutions/$solution_name --packagetype 'Managed'

# import soultion package to target environment
pac solution import --path solutions/$solution_name.zip --force-overwrite

# install solution to target environment
#pac solution publish #--async
