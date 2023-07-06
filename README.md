# Dataverse events capturing using Webhook

This repository demonstrate how to capture Events from Dataverse world using power platform Webhook plugin and Azure functions (as webhook implementation) to save events data to Azure storage Table.

![image](https://github.com/CSE-Neutrino/dataverse-webhook-sample/assets/6573603/fd70ca19-9de6-4dd0-98d6-799276ac65cb)

## Prerequisites

- [Azure function tools](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local?tabs=windows%2Cportal%2Cv2%2Cbash&pivots=programming-language-csharp)
- [Java JDK 17+](https://openjdk.org/install/)

`**Note:** In the case of using the dev-container and VSCode, all dependencies are already installed.`

## Get Started

### Test locally

- Clone this repository: `git clone https://github.com/CSE-Neutrino/dataverse-webhook-sample.git`.
- Open the folder using VSCode
- Add your local settings file [`local.settings.json`](local.settings.json)
![local.settings.json](assets/local.settings.png)
- Update Azure storage connection string `AzureWebJobsStorage` config to valid connection `note: storage emulator can't be used inside dev-container`
- Press `F5` to start Azure function host
- From `Azure extension tab on the extensions left bar`, right click on  `Workspace> Local Project>  Functions> DataverseEventHandler` and click `Execute Function Now` then file request body
![run-function](assets/run-function.png)

### Test on Dataverse and Azure environment

- Create new Azure function App with Java as runtime
![create a new function app](assets/create-function-app.png)
- Deploy function to the newly created function App
![deploy to function app](assets/deploy-function-app.png)
- Update storage account connection string Configuration > `AzureWebJobsStorage`
![Update-storage-account-connection](assets/update-sa-connection.png)
- Get `function url` and `Access Key` from Azure portal
![Get function Url and Access keys](assets/function-url.png)
- Register new webhook to Microsoft dynamics environment using [Plugin registration tool](https://learn.microsoft.com/en-us/power-apps/developer/data-platform/download-tools-nuget#download-and-launch-tools-using-power-platform-cli)
![register new webhook](assets/register-webhook-prt.png)
- Create new Step to handle specific event (update) for dataverse entity (account)
![register-webhook-step](assets/register-webhook-step.png)
- Update dataverse table record using [Power platform make portal](https://make.preview.powerapps.com/)
![update-dataverse-table](assets/update-dataverse-table.png)
- Check data has been created using data explorer on Storage Account > Tables > DataverseEvents
![storage-account-data](assets/storage-account-data.png)