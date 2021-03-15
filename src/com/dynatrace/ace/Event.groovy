package com.dynatrace.ace

import groovy.json.JsonOutput

// reference: https://www.dynatrace.com/support/help/dynatrace-api/environment-api/events/post-event/ 
// public method that the Jenkinsfile script should call
def pushDynatraceEvent( Map args ) {
  switch(args.eventType) {
  case "CUSTOM_ANNOTATION":
    return pushDynatraceAnnotationEvent(args)
  case "CUSTOM_CONFIGURATION":
    return pushDynatraceConfigurationEvent(args)
  case "CUSTOM_DEPLOYMENT":
    return pushDynatraceDeploymentEvent(args)
  case "CUSTOM_INFO":
    return pushDynatraceInfoEvent(args)
  case "MARKED_FOR_TERMINATION":
    return pushDynatraceMarkedForTerminationEvent(args)
  case "AVAILABILITY_EVENT":
    return pushDynatraceAvailabilityEvent(args)
  case "PERFORMANCE_EVENT":
    return pushDynatracePerformanceEvent(args)
  case "RESOURCE_CONTENTION":
    return pushDynatraceResourceContentionEvent(args)
  case "ERROR_EVENT":
    return pushDynatraceErrorEvent(args)
  default:
    echo "Invalid eventType: " + args.eventType
    return false
  }
}

// shared function that make the API call to Dynatrace. 
def sendDynatraceEvent( String dtTenantUrl, String dtApiToken, Map postBody ) {

  def createEventResponse = httpRequest contentType: 'APPLICATION_JSON', 
    customHeaders: [[maskValue: true, name: 'Authorization', value: "Api-Token ${dtApiToken}"]], 
    httpMode: 'POST',
    requestBody: JsonOutput.toJson(postBody),
    responseHandle: 'STRING',
    url: "${dtTenantUrl}/api/v1/events",
    validResponseCodes: "200:403",
    ignoreSslErrors: true

  if (createEventResponse.status == 200) {
    echo "Dynatrace event posted successfully!"
    return true
  } else {
    echo "Failed To post Dynatrace event:" + createEventResponse.content
    return false
  }
}

def pushDynatraceAnnotationEvent( Map args ) {
  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : "CUSTOM_ANNOTATION"
  String source = args.containsKey("source") ? args.source : "Jenkins"
  String description = args.containsKey("description") ? args.description : ""
  String annotationDescription = args.containsKey("annotationDescription") ? args.annotationDescription : ""
  String annotationType = args.containsKey("annotationType") ? args.annotationType : ""
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
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

  println "Posting Annotation event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)
}

def pushDynatraceConfigurationEvent(Map args){
  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : "CUSTOM_CONFIGURATION"
  String description = args.containsKey("description") ? args.description : ""
  String source = args.containsKey("source") ? args.source : "Jenkins"
  String configuration = args.containsKey("configuration") ? args.configuration : ""
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
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

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    tags: tagRule[0].tags,
    description: description,
    source: source,
    configuration: configuration,
    customProperties: customProperties
  ]

  println "Posting Configuration event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)
}

def pushDynatraceDeploymentEvent( Map args ) {
  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : "CUSTOM_DEPLOYMENT"
  String source = args.containsKey("source") ? args.source : "Jenkins"
  String deploymentName = args.containsKey("deploymentName") ? args.deploymentName : "${env.JOB_NAME}"
  String deploymentVersion = args.containsKey("deploymentVersion") ? args.deploymentVersion : "${env.VERSION}"
  String deploymentProject = args.containsKey("deploymentProject") ? args.deploymentProject : ""
  String ciBackLink = args.containsKey("ciBackLink") ? args.ciBackLink : "${env.BUILD_URL}"
  String remediationAction = args.containsKey("remediationAction") ? args.remediationAction : "null"
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
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

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    deploymentName: deploymentName,
    deploymentVersion: deploymentVersion,
    deploymentProject: deploymentProject,
    ciBackLink: ciBackLink,
    remediationAction: remediationAction,
    customProperties: customProperties,
    tags: tagRule[0].tags,
    source: source
  ]

  println "Posting Custom Deployment event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)

}

def pushDynatraceInfoEvent( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : "CUSTOM_INFO"
  String source = args.containsKey("source") ? args.source : "Jenkins"
  String description = args.containsKey("description") ? args.description : ""
  String title = args.containsKey("title") ? args.title : ""
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
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

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    description: description,
    title: title,
    customProperties: customProperties,
    source: source,
    tags: tagRule[0].tags
  ]

  println "Posting Info event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)
}

def pushDynatraceErrorEvent( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : "ERROR_EVENT"
  String source = args.containsKey("source") ? args.source : "Jenkins"
  String description = args.containsKey("description") ? args.description : ""
  String title = args.containsKey("title") ? args.title : ""
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
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
  if(title == "" ) {
    echo "title is a mandatory parameter!"
    return 1
  }

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    description: description,
    title: title,
    customProperties: customProperties,
    source: source,
    tags: tagRule[0].tags
  ]

  println "Posting Error event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)
}

def pushDynatraceMarkedForTerminationEvent( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : "MARKED_FOR_TERMINATION"
  String source = args.containsKey("source") ? args.source : "Jenkins"
  String description = args.containsKey("description") ? args.description : ""
  String title = args.containsKey("title") ? args.title : ""
  String ciBackLink = args.containsKey("ciBackLink") ? args.ciBackLink : "${env.BUILD_URL}"
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
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

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    description: description,
    title: title,
    ciBackLink: ciBackLink,
    customProperties: customProperties,
    source: source,
    tags: tagRule[0].tags
  ]

  println "Posting Marked For Termination event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)
}

def pushDynatraceAvailabilityEvent( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : "AVAILABILITY_EVENT"
  String source = args.containsKey("source") ? args.source : "Jenkins"
  String description = args.containsKey("description") ? args.description : ""
  String title = args.containsKey("title") ? args.title : ""
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
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
  if(title == "" ) {
    echo "title is a mandatory parameter!"
    return 1
  }

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    description: description,
    title: title,
    customProperties: customProperties,
    source: source,
    tags: tagRule[0].tags
  ]

  println "Posting Availability event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)
}

def pushDynatracePerformanceEvent( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : "PERFORMANCE_EVENT"
  String source = args.containsKey("source") ? args.source : "Jenkins"
  String description = args.containsKey("description") ? args.description : ""
  String title = args.containsKey("title") ? args.title : ""
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
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
  if(title == "" ) {
    echo "title is a mandatory parameter!"
    return 1
  }

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    description: description,
    title: title,
    customProperties: customProperties,
    source: source,
    tags: tagRule[0].tags
  ]

  println "Posting Performance event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)
}

def pushDynatraceResourceContentionEvent( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : "RESOURCE_CONTENTION"
  String source = args.containsKey("source") ? args.source : "Jenkins"
  String description = args.containsKey("description") ? args.description : ""
  String title = args.containsKey("title") ? args.title : ""
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
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
  if(title == "" ) {
    echo "title is a mandatory parameter!"
    return 1
  }

  def postBody = [
    eventType: eventType,
    attachRules: [tagRule: tagRule],
    description: description,
    title: title,
    customProperties: customProperties,
    source: source,
    tags: tagRule[0].tags
  ]

  println "Posting Resource Contention event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)
}

return this
