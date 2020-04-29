import groovy.json.JsonOutput

/***************************\
  This function assumes we run on a Jenkins Agent that has curl command available.

  Returns either 0(=no errors), 1(=pushing event failed)
\***************************/
def call( Map args ) 
    
    /*  String dtTenantUrl, 
        String dtApiToken 
        def tagRule 

        String deploymentName 
        String deploymentVersion 
        String deploymentProject
        String ciBackLink 
        String remediationAction

        def customProperties
    */
{
    // check input arguments
    String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
    String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
    def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
    
    String deploymentName = args.containsKey("deploymentName") ? args.deploymentName : "${env.JOB_NAME}"
    String deploymentVersion = args.containsKey("deploymentVersion") ? args.deploymentVersion : "${env.VERSION}"
    String deploymentProject = args.containsKey("deploymentProject") ? args.deploymentProject : ""
    String ciBackLink = args.containsKey("ciBackLink") ? args.ciBackLink : "${env.BUILD_URL}"
    String remediationAction = args.containsKey("remediationAction") ? args.remediationAction : "null"
    

    def customProperties = args.containsKey("customProperties") ? args.customProperties : [ ]

    // check minimum required params
    if(tagRule == "" ) {
        echo "tagRule is a mandatory parameter!"
        return -1
    }
 
    String eventType = "CUSTOM_DEPLOYMENT"

    int errorCode = 0

    // build the curl command
    int numberOfTags = tagRule[0].tags.size()
    int numberOfProperties = customProperties.size()

    // set Dynatrace URL, API Token and Event Type.
    String curlCmd = "curl -X POST \"${dtTenantUrl}/api/v1/events?Api-Token=${dtApiToken}\" -H \"accept: application/json\" -H \"Content-Type: application/json\" -d \"{" 
    curlCmd += " \\\"eventType\\\": \\\"${eventType}\\\","
    curlCmd += " \\\"attachRules\\\": { \\\"tagRule\\\" : [{ \\\"meTypes\\\" : [\\\"${tagRule[0].meTypes[0].meType}\\\"],"

    // attach tag rules
    curlCmd += " \\\"tags\\\" : [ "
    tagRule[0].tags.eachWithIndex { tag, i ->
        curlCmd += "{ \\\"context\\\" : \\\"${tag.context}\\\", \\\"key\\\" : \\\"${tag.key}\\\", \\\"value\\\" : \\\"${tag.value}\\\" }"
        if(i < (numberOfTags - 1)) { curlCmd += ", " }
    }
    curlCmd += " ] }] },"

    // set deploymentName, deploymentVersion, deploymentProject, ciBackLink, source
    if (deploymentName != "null")
      curlCmd += " \\\"deploymentName\\\":\\\"${deploymentName}\\\","
    
    if (deploymentVersion != "null")
      curlCmd += " \\\"deploymentVersion\\\":\\\"${deploymentVersion}\\\","
       
    if (deploymentProject != "null")
      curlCmd += " \\\"deploymentProject\\\":\\\"${deploymentProject}\\\","

    if (ciBackLink != "null") 
      curlCmd += " \\\"ciBackLink\\\":\\\"${ciBackLink}\\\","

    if (remediationAction != "null") 
      curlCmd += " \\\"remediationAction\\\":\\\"${remediationAction}\\\","
  
    curlCmd += " \\\"source\\\":\\\"Jenkins\\\","
    
    // set custom properties
    curlCmd += " \\\"customProperties\\\": { "
    customProperties.eachWithIndex { property, i ->
        curlCmd += "\\\"${property.key}\\\": \\\"${property.value}\\\""
        if(i < (numberOfProperties - 1)) { curlCmd += ", " }
    }
    curlCmd += "} }\" "

    // push the event
    sh "${curlCmd}"

    return errorCode
}
