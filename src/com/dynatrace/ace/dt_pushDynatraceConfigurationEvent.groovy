/***************************\
  This function assumes we run on a standard Jenkins Agent.

  Returns either 0(=no errors), 1(=pushing event failed)
\***************************/

@NonCPS
def call(Map args){
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
