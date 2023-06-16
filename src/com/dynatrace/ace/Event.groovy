package com.dynatrace.ace

import groovy.json.JsonOutput

// reference: https://www.dynatrace.com/support/help/dynatrace-api/environment-api/events/post-event/ 
// public method that the Jenkinsfile script should call

def checkEventType( Map args ) {
  // probably should just allow all event types /api/v2/eventTypes
  switch(args.eventType) {
  case "CUSTOM_ANNOTATION":
    return pushDynatraceEvent(args)
  case "CUSTOM_CONFIGURATION":
    return pushDynatraceEvent(args)
  case "CUSTOM_DEPLOYMENT":
    return pushDynatraceEvent(args)
  case "CUSTOM_INFO":
    return pushDynatraceEvent(args)
  case "MARKED_FOR_TERMINATION":
    return pushDynatraceEvent(args)
  case "AVAILABILITY_EVENT":
    return pushDynatraceEvent(args)
  case "PERFORMANCE_EVENT":
    return pushDynatraceEvent(args)
  case "RESOURCE_CONTENTION":
    return pushDynatraceEvent(args)
  case "ERROR_EVENT":
    return pushDynatraceEvent(args)
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
    url: "${dtTenantUrl}/api/v2/events/ingest",
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

def pushDynatraceEvent( Map args ) {
  // check input arguments
  if (!checkEventType(args)) {
    return 1
  }

  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  String eventType = args.containsKey("eventType") ? args.eventType : ""
  String title = args.containsKey("title") ? args.title : ""
  String entitySelector = args.containsKey("entitySelector") ? args.entitySelector: ""
  def properties = args.containsKey("properties") ? args.properties : [ ]

  // check minimum required params
  if(eventType == "" ) {
    echo "eventType is a mandatory parameter!"
    return 1
  }
  if(title == "" ) {
    echo "title is a mandatory parameter!"
    return 1
  }
  if(entitySelector == "" ) {
    echo "entitySelector is a mandatory parameter!"
    return 1
  }

  def postBody = [
    eventType: eventType,
    entitySelector: entitySelector,
    properties: properties,
  ]

  println "Posting " + eventType + " event..."
  return sendDynatraceEvent(dtTenantUrl,dtApiToken,postBody)
}


return this
