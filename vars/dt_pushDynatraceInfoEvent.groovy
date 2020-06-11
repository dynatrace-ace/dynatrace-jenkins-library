@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' )

import groovyx.net.http.HTTPBuilder
import groovy.json.JsonOutput
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

/***************************\
  This function assumes we run on a standard Jenkins Agent.

  Returns either 0(=no errors), 1(=pushing event failed)
\***************************/
@NonCPS
def call( Map args ) {
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


  def http = new HTTPBuilder( dtTenantUrl + '/api/v1/events' );

  http.request( POST, JSON ) { req ->
    headers.'Authorization' = "Api-Token ${dtApiToken}"
    headers.'Content-Type' = 'application/json'

    body = postBody

    response.success = { resp, json ->
      println "Info Event Posted Successfully! ${resp.status}"
      return 0
    }
    response.failure = { resp, json ->
      echo """[dt_pushDynatraceInfoEvent] Failed To Post Event: ${resp.statusLine}
        HTTP Message: ${resp.statusLine}"
        JSON: ${json.toMapString()}"
        API Message: ${json && json.error && json.error.message ? json.error.message : 'N/A'}"
        <------"
        POST Body: ${postBody.toMapString()}"""

      return 1
    }
  }
  return 0
}
