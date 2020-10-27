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
    requestBody: toJson(postBody),
    responseHandle: 'STRING',
    url: "${dtTenantUrl}/api/v1/events",
    validResponseCodes: "200",
    ignoreSslErrors: true

    if (createEventResponse.status == 200) {
      echo "Custom info event posted successfully!"
    } else {
      echo "Failed To post event:" + createEventResponse.content
      return false
    }

  return true
}
