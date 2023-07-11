#!/usr/bin/env bash
# This scripts package and import the solution to the target environment.

# init vaiables
solution_name="RegisterServiceEndpointsSolution"

# create solution package
pac solution pack --zipfile solutions/$solution_name.zip --folder solutions/$solution_name --packagetype 'Managed'

# import soultion package to target environment
pac solution import --path solutions/$solution_name.zip --force-overwrite

# install solution to target environment
#pac solution publish #--async
