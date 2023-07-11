#!/usr/bin/env bash
# This scripts generates test keys and certificates for the sample.
# In a production environment such artifacts should be generated
# by a proper certificate authority and handled in a secure manner.

# init vaiables
solution_name="RegisterServiceEndpointsSolution"
environment_name="dev"
environment_url="https://neutrino.crm11.dynamics.com"

# CLI Login to the target environment
# CLI supports service principals for authentication
#pac auth create --url $environment_url

# create environment settings
pac solution create-settings --solution-folder $solution_name --settings-file $solution_name.$environment_name.json
