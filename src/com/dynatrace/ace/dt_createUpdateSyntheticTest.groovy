@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' )
 
import groovyx.net.http.HTTPBuilder
import groovy.json.JsonOutput
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

/***************************\
  This function assumes we run on a Jenkins Agent that has curl command available.
  Returns either 0(=no errors), 1(=create/update syntthetic monitor failed)
\***************************/
@NonCPS
def call( Map args ) 
    
    /*  String dtTenantUrl, 
        String dtApiToken 
        String testName 
        String url
        String method
        String frequency
        String location
    */
{
    // check input arguments
    String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
    String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
    String testName = args.containsKey("testName") ? args.testName : ""
    String url = args.containsKey("url") ? args.url : ""
    String method = args.containsKey("method") ? args.method : "GET"
    String frequency = args.containsKey("frequency") ? args.frequency : 1
    String location = args.containsKey("location") ? args.location : ""
    String dtSyntheticMonitorId = ""
    
    // check minimum required params
    if(testName == "" ) {
        echo "testName is a mandatory parameter!"
        return -1
    }
    if(url == "" ) {
        echo "url is a mandatory parameter!"
        return -1
    }
    if(location == "" ) {
        echo "location is a mandatory parameter!"
        return -1
    }
 
    int errorCode = 0

    def http = new HTTPBuilder( dtTenantUrl + '/api/v1/synthetic/monitors' )
 
    http.request( GET, JSON ) { req ->
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      response.success = { resp, json ->
        println "Monitors retrieved successfully! ${resp.status}"
        Integer arraySize = json.monitors.size()

        for (i=0; i < arraySize; i++) {
          if (json.monitors[i].name == testName){
            dtSyntheticMonitorId = json.monitors[i].entityId
            println "Found monitor ${json.monitors[i].name} with id ${dtSyntheticMonitorId}"
          }
        }
      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item GET: uri: " + uri + "\n" +
            "  Unknown error trying to get item: ${resp.status}, not getting Item." +
            "\njson = ${json}")
      }
    }
    
  if (dtSyntheticMonitorId == ""){

      http.request( POST, JSON ) { req ->
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      headers.'Content-Type' = 'application/json'
      body = [
        name: testName,
        frequencyMin: frequency,
        enabled: true,
        type: "HTTP",
        script: [
          version: "1.0",
          requests: [
            [
              description: testName,
              url: url,
              method: method
            ]
          ]
        ],
        locations: [
          location
        ]
      ]
  
      response.success = { resp, json ->
      println "Monitor created successfully! ${resp.status}"

      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
            "   Unknown error trying to create item: ${resp.status}, not creating Item." +
            "\njson = ${json}")
      }
    }
  }

  if (dtSyntheticMonitorId != ""){

      http.request( PUT, JSON ) { req ->
      uri.path = '/api/v1/synthetic/monitors/' + dtSyntheticMonitorId
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      headers.'Content-Type' = 'application/json'
      body = [
        name: testName,
        frequencyMin: frequency,
        enabled: true,
        type: "HTTP",
        script: [
          version: "1.0",
          requests: [
            [
              description: testName,
              url: url,
              method: method
            ]
          ]
        ],
        locations: [
          location
        ]
      ]
  
      response.success = { resp, json ->
        println "Monitor updated successfully! ${resp.status}"

      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item PUT: uri: " + uri + "\n" +
            "   Unknown error trying to update item: ${resp.status}, not updating Item." +
            "\njson = ${json}")
      }
    }
  }

    return errorCode
}
