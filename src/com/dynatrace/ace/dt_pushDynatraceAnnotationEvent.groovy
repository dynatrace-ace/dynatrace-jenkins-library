import groovy.json.JsonOutput

@NonCPS
def call( Map args ) {
  // check input arguments
  String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
  String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
  def tagRule = args.containsKey("tagRule") ? args.tagRule : ""
  String source = args.containsKey("source") ? args.source : "Jenkins"

  String description = args.containsKey("description") ? args.description : ""
  String annotationDescription = args.containsKey("annotationDescription") ? args.annotationDescription : ""
  String annotationType = args.containsKey("annotationType") ? args.annotationType : ""

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

  String eventType = "CUSTOM_ANNOTATION"

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

  def createEventResponse = httpRequest contentType: 'APPLICATION_JSON', 
    customHeaders: [[maskValue: true, name: 'Authorization', value: "Api-Token ${dtApiToken}"]], 
    httpMode: 'POST',
    requestBody: JsonOutput.toJson(postBody),
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
