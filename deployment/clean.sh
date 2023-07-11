#!/usr/bin/env bash
# This scripts generates test keys and certificates for the sample.
# In a production environment such artifacts should be generated
# by a proper certificate authority and handled in a secure manner.

# init vaiables
solution_name="RegisterServiceEndpointsSolution"

# create environment settings
pac solution delete --solution-name $solution_name
