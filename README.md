# Overview

Jenkins shared library for integrating calls the Dynatrace API with your Jenkins Pipelines.

## Usage

This directive, `@Library('dynatrace@master') _` is added to the top of a Jenkinsfile script to load the Dynatrace library. Then within the various pipeline stages, Dynatrace library functions are called with the required and optional parameters that map to the required by the Dynatrace API requests.  The Dynatrace URL and API token is configured within Jenkins or can be optionally passed as a parameter to the function call. 

## Library functions:

**1. Push Information Events** 
  * Send deployments, configuration changes, and testing activity for monitored services. 
  * [Information Events](PUSHEVENTS.md) usage details
  * [Dynatrace API Documentation](https://www.dynatrace.com/support/help/dynatrace-api/environment-api/events/post-event)

**2. Synthetic Monitors** 
  * Create synthetic HTTP monitors to check the availability of your resources—websites or API endpoints.
  * [Synthetic HTTP monitor](HTTPMONITOR.md) usage details
  * [Dynatrace HTTP monitor Documentation](https://www.dynatrace.com/support/help/how-to-use-dynatrace/synthetic-monitoring/http-monitors/create-an-http-monitor/)
  * [Dynatrace API Documentation](https://www.dynatrace.com/support/help/dynatrace-api/environment-api/synthetic/synthetic-monitors/post-a-monitor/)

# Setup

## Prerequisites

**#1 - Jenkins server**  

You may have your own, but if not one option is to run Jenkins as [Docker container](https://github.com/jenkinsci/docker/blob/master/README.md).  This command will start it up and prompt for setting up initial user and default plugins.
```
docker run -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts
```

**#2 - Dynatrace tenant and API Token**

If you don't have Dynatrace, then sign up for a [free trial](https://www.dynatrace.comc/trial). To generate a Dynatrace API token, follow these steps:

1. Select Settings in the navigation menu.
1. Go to Integration > Dynatrace API.
1. Select Generate token.
1. Enter a name for your token.
1. You can accept the default, but you need API v1 **Read and Write configuration** permissions 
1. Select Generate.

## Install and configure the Dynatrace Jenkins Library

1. Login to Jenkins 
1. Navigate to Manage Jenkins > Configure System

    ![](./images/config-menu.png)

1. Find the **Global Pipeline Libraries** section, click add new and fill in as shown below

    * Select **Git** as the type
    * Project repositotry = https://github.com/dynatrace-ace/dynatrace-jenkins-library.git

    ![](./images/config-lib.png)

## Configure the Dynatrace URL and API Token

The Dynatrace URL and API Token can be passed into the function calls, but by default they look for Jenkins global environment variables for these values.  To configure the Jenkins environment variables:

1. Login to Jenkins 
1. Navigate to Manage Jenkins > Configure System
1. Find the **Global properties** section, click the **Environment variables** checkbox and add these two variables as shown below 

    * DT_API_TOKEN = The API token created for you Dynatrace tenant
    * DT_TENANT_URL = The URL to your Dynatrace tenant      

    ![](./images/config-env.png)

1. Save settings





