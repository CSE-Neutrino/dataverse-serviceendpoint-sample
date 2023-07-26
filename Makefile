SHELL := /bin/bash

.PHONY: help
.DEFAULT_GOAL := help

help: ## 💬 This help message :)
	@grep -E '[a-zA-Z_-]+:.*?## .*$$' $(firstword $(MAKEFILE_LIST)) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-23s\033[0m %s\n", $$1, $$2}'

deploy: ## 🔐 deploy 
	@echo -e "----\e[34mStart $@\e[0m----" || true
	@cd deployment && ./deploy.sh $(solution_name)
	@echo -e "----\e[34mCompleted\e[0m----"

clean: ## 🔐 Generate the Certificates using openssl
	@echo -e "----\e[34mStart $@\e[0m----" || true
	@cd deployment && ./clean.sh $(solution_name)
	@echo -e "----\e[34mCompleted\e[0m----"

