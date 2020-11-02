package com.dynatrace.ace

import groovy.json.JsonOutput

def postProblemComment( Map args ) {

  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"

  String problemId = args.containsKey("problemId") ? args.problemId : ""
  String comment = args.containsKey("comment") ? args.comment : ""
  String user = args.containsKey("user") ? args.user : ""
  String context = args.containsKey("context") ? args.context : ""

  // check minimum required params
  if(problemId == "" ) {
    echo "problemId is a mandatory parameter!"
    return 1
  }
  if(comment == "" ) {
      echo "comment is a mandatory parameter!"
      return 1
  }
  if(user == "" ) {
      echo "user is a mandatory parameter!"
      return 1
  }

  def postBody = [
    comment: comment,
    user: user,
    context: context
  ]

  def postProblemResponse = httpRequest contentType: 'APPLICATION_JSON', 
    customHeaders: [[maskValue: true, name: 'Authorization', value: "Api-Token ${dtApiToken}"]], 
    httpMode: 'POST',
    requestBody: JsonOutput.toJson(postBody),
    responseHandle: 'STRING',
    url: "${dtTenantUrl}/api/v1/problem/details/${problemId}/comments",
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