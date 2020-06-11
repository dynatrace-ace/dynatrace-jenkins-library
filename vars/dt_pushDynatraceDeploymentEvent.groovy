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

  def http = new HTTPBuilder( dtTenantUrl + '/api/v1/events' );

  http.request( POST, JSON ) { req ->
    headers.'Authorization' = "Api-Token ${dtApiToken}"
    headers.'Content-Type' = 'application/json'

    body = postBody

    response.success = { resp, json ->
      echo "Deployment Event Posted Successfully! ${resp.status}"
    }
    response.failure = { resp, json ->
      echo """[dt_pushDynatraceDeploymentEvent] Failed To Post Event: ${resp.statusLine}
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
