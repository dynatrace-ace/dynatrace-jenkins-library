import groovy.json.JsonOutput

/***************************\
  This function assumes we run on a Jenkins Agent that has curl command available.

  Returns either 0(=no errors), 1(=pushing event failed)
\***************************/
def call( Map args )

    /*  String dtTenantUrl,
        String dtApiToken
        def tagRule

        String description
        String source
        String configuration

        def customProperties
    */
{

  echo "Method pushDynatraceInfoEvent is deprecated! Please use dt_pushDynatraceInfoEvent instead!"

    // check input arguments
    String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
    String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
    def tagRule = args.containsKey("tagRule") ? args.tagRule : ""

    String description = args.containsKey("description") ? args.description : ""
    String source = args.containsKey("source") ? args.source : ""
    String title = args.containsKey("title") ? args.title : ""

    def customProperties = args.containsKey("customProperties") ? args.customProperties : [ ]

    // check minimum required params
    if(tagRule == "" ) {
        echo "tagRule is a mandatory parameter!"
        return -1
    }

    String eventType = "CUSTOM_INFO"

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

    // set description, source, configuration
    curlCmd += " \\\"description\\\":\\\"${description}\\\", \\\"source\\\":\\\"${source}\\\", \\\"title\\\":\\\"${title}\\\", "

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
