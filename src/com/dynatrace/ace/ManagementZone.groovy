package com.dynatrace.ace

@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' )
 
import groovyx.net.http.HTTPBuilder
import groovy.json.JsonOutput
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

/***************************\
  This function assumes we run on a Jenkins Agent that has curl command available.
  Returns either 0(=no errors), 1(=create/update management zone failed)
\***************************/
@NonCPS
def createUpdateManagementZone( Map args ) 
    
    /*  String dtTenantUrl, 
        String dtApiToken 
        String managementZoneName 
        String ruleType
        String managementZoneConditions
    */
{
    // check input arguments
    String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
    String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
    String managementZoneName = args.containsKey("managementZoneName") ? args.managementZoneName  : ""
    String ruleType = args.containsKey("ruleType") ? args.ruleType : ""
    def managementZoneConditions = args.containsKey("managementZoneConditions") ? args.managementZoneConditions : ""
    String dtManagementZoneId = ""

    // check minimum required params
    if(managementZoneName == "" ) {
        echo "managementZoneName is a mandatory parameter!"
        return -1
    }
    if(ruleType == "" ) {
        echo "ruleType is a mandatory parameter!"
        return -1
    }
    if(managementZoneConditions == "" ) {
        echo "managementZoneConditions is a mandatory parameter!"
        return -1
    }

    int errorCode = 0

    def http = new HTTPBuilder( dtTenantUrl + '/api/config/v1/managementZones' )
 
    http.request( GET, JSON ) { req ->
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      response.success = { resp, json ->
        println "Management zones retrieved successfully! ${resp.status}"
        Integer arraySize = json.values.size()

        for (i=0; i < arraySize; i++) {
          if (json.values[i].name == managementZoneName){
            dtManagementZoneId = json.values[i].id
            println "Found management zone ${json.values[i].name} with id ${dtManagementZoneId}"
            
          }
        }

      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item GET: uri: " + uri + "\n" +
            "  Unknown error trying to get item: ${resp.status}, not getting Item." +
            "\njson = ${json}")
      }
    }

    if (dtManagementZoneId == ""){

        http.request( POST, JSON ) { req ->
        headers.'Authorization' = 'Api-Token ' + dtApiToken
        headers.'Content-Type' = 'application/json'
        body = [
            name: managementZoneName,
            rules: [
                [
                    type: ruleType,
                    enabled: true,
                    conditions: 
                        managementZoneConditions
                ]
            ]
        ]
        response.success = { resp, json ->
            println "Management zone created successfully! ${resp.status}"
            dtManagementZoneId = json.id

        }
        response.failure = { resp, json ->
            throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
                "   Unknown error trying to create item: ${resp.status}, not creating Item." +
                "\njson = ${json}")
        }
      }
   }

    if (dtManagementZoneId != ""){

        http.request( PUT, JSON ) { req ->
        uri.path = '/api/config/v1/managementZones/' + dtManagementZoneId
        headers.'Authorization' = 'Api-Token ' + dtApiToken
        headers.'Content-Type' = 'application/json'
        body = [
            name: managementZoneName,
            rules: [
                [
                    type: ruleType,
                    enabled: true,
                    conditions: 
                        managementZoneConditions
                ]
            ]
        ]
        response.success = { resp, json ->
            println "Management zone updated successfully! ${resp.status}"

        }
        response.failure = { resp, json ->
            throw new Exception("Stopping at item PUT: uri: " + uri + "\n" +
                "   Unknown error trying to update item: ${resp.status}, not updating Item." +
                "\njson = ${json}")
        }
      }
   }
    return [ errorCode, dtManagementZoneId ]

}

return this