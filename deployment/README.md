# Solution Deployment

The powerApps solution can be deployed to different enviroments using any of the following methods

- [Github Actions](https://learn.microsoft.com/en-us/power-platform/alm/devops-github-actions)
- [Power Platform CLI](https://learn.microsoft.com/en-us/power-platform/developer/cli/reference/solution)
- [Dataverse Web APIs](https://learn.microsoft.com/en-us/dynamics365/customerengagement/on-premises/developer/entities/solution?view=op-9-1)

## Using Github Actions

```yml
name: build-deploy-solution
# Export solution from DEV environment
#  unpack it and prepare, commit and push a git branch with the changes
#  
#  using spn-auth with Client ID and Tenant ID for environemnet login 
#  
#  using secrets for
#   SP_CLIENTID           Service Principal App ID
#   SP_TENANTID           Service Principal Azure AD tenant
#   SP_SECRET             Service Principal secrtes
#   DevEnvironmentUrl     Url referencing the development environment


on:
  workflow_dispatch:

env:
  solution_name: RegisterServiceEndpointsSolution
  solution_package_folder: deployment/packages/
  solution_source_folder: deployment/solutions/

jobs:
  deploy-to-dev:
    runs-on: windows-latest
    env:
      RUNNER_DEBUG: 1

    steps:
    - uses: actions/checkout@v2
      with:
        lfs: true

    - name: who-am-i action
      uses: microsoft/powerplatform-actions/who-am-i@0.4.0
      with:
        environment-url: ${{secrets.DevEnvironmentUrl}}
        app-id: ${{secrets.SP_CLIENTID}}
        client-secret: ${{ secrets.SP_SECRET }}
        tenant-id: ${{secrets.SP_TENANTID}}

    - name: Pack solution
      uses: microsoft/powerplatform-actions/pack-solution@0.4.0
      with:
        solution-folder: ${{ env.solution_source_folder}}/${{ env.solution_name }}
        solution-file: ${{ env.solution_package_folder}}/${{ env.solution_name }}.zip
        solution-type: Managed

    - name: Import solution as Managed to build env
      uses: microsoft/powerplatform-actions/import-solution@0.4.0
      with:
        environment-url: ${{secrets.DevEnvironmentUrl}}
        app-id: ${{secrets.SP_CLIENTID}}
        client-secret: ${{ secrets.SP_SECRET }}
        tenant-id: ${{secrets.SP_TENANTID}}
        solution-file: ${{ env.solution_package_folder}}/${{ env.solution_name }}.zip
        force-overwrite: true
        publish-changes: true

```

## Using Power Platform CLI

```bash

# init vaiables
solution_name="$1"

# create solution package
pac solution pack --zipfile solutions/$solution_name.zip --folder solutions/$solution_name --packagetype 'Managed'

# import soultion package to target environment
pac solution import --path solutions/$solution_name.zip --force-overwrite

# install solution to target environment
pac solution publish --async false --max-async-wait-time 60

```

Note: Both deployment methods require manual configuration to be done after solution importing: setup security related config like webhook ApiKey or Servicebug plugin Connection String.