package com.dynatrace.ace

import groovy.json.JsonOutput

def closeProblem( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"

  String problemId = args.containsKey("problemId") ? args.problemId : ""
  String message = args.containsKey("message") ? args.message : ""

  // check minimum required params
  if(problemId == "" ) {
    echo "problemId is a mandatory parameter!"
    return 1
  }

  def postBody = []
  if(message != "" ) {
    postBody = [
      message: message
    ]
  }

  def postProblemResponse = httpRequest contentType: 'APPLICATION_JSON', 
    customHeaders: [[maskValue: true, name: 'Authorization', value: "Api-Token ${dtApiToken}"]], 
    httpMode: 'POST',
    requestBody: JsonOutput.toJson(postBody),
    responseHandle: 'STRING',
    url: "${dtTenantUrl}/api/v2/problems/${problemId}/close",
    validResponseCodes: "200",
    ignoreSslErrors: true

    if (postProblemResponse.status == 200) {
      echo "Problem ${problemId} Successfully Closed!"
    } else {
      echo "Failed To Close Problem ${problemId}:" + postProblemResponse.content
      return false
    }

  return true
}

def postProblemComment( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"

  String problemId = args.containsKey("problemId") ? args.problemId : ""
  String message = args.containsKey("message") ? args.message : ""
  String context = args.containsKey("context") ? args.context : ""

  // check minimum required params
  if(problemId == "" ) {
    echo "problemId is a mandatory parameter!"
    return 1
  }
  if(message == "" ) {
      echo "message is a mandatory parameter!"
      return 1
  }

  def postBody = [
    message: message,
    context: context
  ]

  def postProblemResponse = httpRequest contentType: 'APPLICATION_JSON', 
    customHeaders: [[maskValue: true, name: 'Authorization', value: "Api-Token ${dtApiToken}"]], 
    httpMode: 'POST',
    requestBody: JsonOutput.toJson(postBody),
    responseHandle: 'STRING',
    url: "${dtTenantUrl}/api/v2/problems/${problemId}/comments",
    validResponseCodes: "200",
    ignoreSslErrors: true

    if (postProblemResponse.status == 200) {
      echo "Problem comment posted successfully to ${problemId}!"
    } else {
      echo "Failed To post Problem comment to ${problemId}:" + postProblemResponse.content
      return false
    }

  return true
}

return this