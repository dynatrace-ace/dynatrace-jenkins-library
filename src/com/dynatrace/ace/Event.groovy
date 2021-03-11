package com.dynatrace.ace

import groovy.json.JsonOutput

def pushDynatraceAnnotationEvent( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
  String source = args.containsKey("source") ? args.source : "Jenkins"

  String description = args.containsKey("description") ? args.description : ""
  String annotationDescription = args.containsKey("annotationDescription") ? args.annotationDescription : ""
  String annotationType = args.containsKey("annotationType") ? args.annotationType : ""

  def customProperties = args.containsKey("customProperties") ? args.customProperties : [ ]

  // check minimum required params
  if(tagRule == "" ) {
    echo "tagRule is a mandatory parameter!"
    return 1
  }
  if(annotationType == "" ) {
      echo "annotationType is a mandatory parameter!"
      return 1
  }
  if(source == "" ) {
      echo "source is a mandatory parameter!"
      return 1
  }
  if(annotationDescription == "" ) {
      echo "annotationDescription is a mandatory parameter!"
      return 1
  }

  String eventType = "CUSTOM_ANNOTATION"

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    description: description,
    source: source,
    annotationType: annotationType,
    annotationDescription: annotationDescription,
    customProperties: customProperties,
    tags: tagRule[0].tags
  ]

  def createEventResponse = httpRequest contentType: 'APPLICATION_JSON', 
    customHeaders: [[maskValue: true, name: 'Authorization', value: "Api-Token ${dtApiToken}"]], 
    httpMode: 'POST',
    requestBody: JsonOutput.toJson(postBody),
    responseHandle: 'STRING',
    url: "${dtTenantUrl}/api/v1/events",
    validResponseCodes: "200:403",
    ignoreSslErrors: true

    if (createEventResponse.status == 200) {
      echo "Custom info event posted successfully!"
    } else {
      echo "Failed To post event:" + createEventResponse.content
      return false
    }

  return true
}

def pushDynatraceConfigurationEvent(Map args){
    // check input arguments
    String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
    String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
    def tagRule = args.containsKey("tagRule") ? args.tagRule : ""

    String description = args.containsKey("description") ? args.description : ""
    String source = args.containsKey("source") ? args.source : "Jenkins"
    String configuration = args.containsKey("configuration") ? args.configuration : ""

    def customProperties = args.containsKey("customProperties") ? args.customProperties : [ ]

    // check minimum required params
    if(tagRule == "" ) {
        echo "tagRule is a mandatory parameter!"
        return 1
    }
    if(description == "" ) {
        echo "description is a mandatory parameter!"
        return 1
    }
    if(source == "" ) {
        echo "source is a mandatory parameter!"
        return 1
    }
    if(configuration == "" ) {
        echo "configuration is a mandatory parameter!"
        return 1
    }

    String eventType = "CUSTOM_CONFIGURATION"

    def postBody = [
      eventType: eventType,
      attachRules: [tagRule: tagRule],
      tags: tagRule[0].tags,
      description: description,
      source: source,
      configuration: configuration,
      customProperties: customProperties
    ]

    def createEventResponse = httpRequest contentType: 'APPLICATION_JSON', 
      customHeaders: [[maskValue: true, name: 'Authorization', value: "Api-Token ${dtApiToken}"]], 
      httpMode: 'POST',
      requestBody: JsonOutput.toJson(postBody),
      responseHandle: 'STRING',
      url: "${dtTenantUrl}/api/v1/events",
      validResponseCodes: "200:403",
      ignoreSslErrors: true

      if (createEventResponse.status == 200) {
        echo "Custom info event posted successfully!"
      } else {
        echo "Failed To post event:" + createEventResponse.content
        return false
      }

    return true
}

def pushDynatraceDeploymentEvent( Map args ) {
  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
  String source = args.containsKey("source") ? args.source : "Jenkins"

  String deploymentName = args.containsKey("deploymentName") ? args.deploymentName : "${env.JOB_NAME}"
  String deploymentVersion = args.containsKey("deploymentVersion") ? args.deploymentVersion : "${env.VERSION}"
  String deploymentProject = args.containsKey("deploymentProject") ? args.deploymentProject : ""
  String ciBackLink = args.containsKey("ciBackLink") ? args.ciBackLink : "${env.BUILD_URL}"
  String remediationAction = args.containsKey("remediationAction") ? args.remediationAction : "null"

  println "Posting Custom Deployment event..."

  def customProperties = args.containsKey("customProperties") ? args.customProperties : [ ]

  // check minimum required params
  if(tagRule == "" ) {
    echo "tagRule is a mandatory parameter!"
    return 1
  }
  if(deploymentName == "" ) {
      echo "deploymentName is a mandatory parameter!"
      return 1
  }
  if(source == "" ) {
      echo "source is a mandatory parameter!"
      return 1
  }
  if(deploymentVersion == "" ) {
      echo "deploymentVersion is a mandatory parameter!"
      return 1
  }

  String eventType = "CUSTOM_DEPLOYMENT"

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    deploymentName: deploymentName,
    deploymentVersion: deploymentVersion,
    deploymentProject: deploymentProject,
    ciBackLink: ciBackLink,
    remediationAction: remediationAction,
    tags: tagRule[0].tags,
    source: source
  ]

  def createEventResponse = httpRequest contentType: 'APPLICATION_JSON', 
    customHeaders: [[maskValue: true, name: 'Authorization', value: "Api-Token ${dtApiToken}"]], 
    httpMode: 'POST',
    requestBody: JsonOutput.toJson(postBody),
    responseHandle: 'STRING',
    url: "${dtTenantUrl}/api/v1/events",
    validResponseCodes: "200:403",
    ignoreSslErrors: true

    if (createEventResponse.status == 200) {
      echo "Custom info event posted successfully!"
    } else {
      echo "Failed To post event:" + createEventResponse.content
      return false
    }

  return true
}

def pushDynatraceInfoEvent( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
  String source = args.containsKey("source") ? args.source : "Jenkins"

  String description = args.containsKey("description") ? args.description : ""
  String title = args.containsKey("title") ? args.title : ""

  def customProperties = args.containsKey("customProperties") ? args.customProperties : [ ]

  // check minimum required params
  if(tagRule == "" ) {
    echo "tagRule is a mandatory parameter!"
    return 1
  }
  if(source == "" ) {
      echo "source is a mandatory parameter!"
      return 1
  }
  if(description == "" ) {
      echo "description is a mandatory parameter!"
      return 1
  }

  String eventType = "CUSTOM_INFO"

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    description: description,
    title: title,
    customProperties: customProperties,
    source: source,
    tags: tagRule[0].tags
  ]

  def createEventResponse = httpRequest contentType: 'APPLICATION_JSON', 
    customHeaders: [[maskValue: true, name: 'Authorization', value: "Api-Token ${dtApiToken}"]], 
    httpMode: 'POST',
    requestBody: JsonOutput.toJson(postBody),
    responseHandle: 'STRING',
    url: "${dtTenantUrl}/api/v1/events",
    validResponseCodes: "200:403",
    ignoreSslErrors: true

    if (createEventResponse.status == 200) {
      echo "Custom info event posted successfully!"
    } else {
      echo "Failed To post event:" + createEventResponse.content
      return false
    }

  return true
}

return this
