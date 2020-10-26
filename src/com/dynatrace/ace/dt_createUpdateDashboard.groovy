@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' )
 
import groovyx.net.http.HTTPBuilder
import groovy.json.JsonOutput
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

/***************************\
  This function assumes we run on a Jenkins Agent that has curl command available.
  Returns either 0(=no errors), 1(=create/update dashboard failed)
\***************************/
@NonCPS
def call( Map args ) 
    
    /*  String dtTenantUrl, 
        String dtApiToken 
        String dashboardName
        String dashboardManagementZoneName
        String dashboardManagementZoneId
        String dashboardTimeframe
        String dashboardShared
        String dashboardLinkShared
        String dashboardPublished
        dtDashboardTiles
    */
{
    // check input arguments
    String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
    String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
    String dashboardName = args.containsKey("dashboardName") ? args.dashboardName : ""
    String dashboardManagementZoneName = args.containsKey("dashboardManagementZoneName") ? args.dashboardManagementZoneName : ""
    String dashboardManagementZoneId = args.containsKey("dashboardManagementZoneId") ? args.dashboardManagementZoneId : ""
    String dashboardTimeframe = args.containsKey("dashboardTimeframe") ? args.dashboardTimeframe : ""
    Boolean dashboardShared = args.containsKey("dashboardShared") ? args.dashboardShared : ""
    Boolean dashboardLinkShared = args.containsKey("dashboardLinkShared") ? args.dashboardLinkShared : ""
    Boolean dashboardPublished = args.containsKey("dashboardPublished") ? args.dashboardPublished : ""
    def dtDashboardTiles = args.containsKey("dtDashboardTiles") ? args.dtDashboardTiles : ""
    String dtDashboardId = ""

    // check minimum required params
    if(dashboardName == "" ) {
        echo "dashboardName is a mandatory parameter!"
        return -1
    }
    if(dashboardManagementZoneName == "" ) {
        echo "dashboardManagementZoneName is a mandatory parameter!"
        return -1
    }
    if(dashboardManagementZoneId == "" ) {
        echo "dashboardManagementZoneId is a mandatory parameter!"
        return -1
    }
    if(dashboardShared == "" ) {
        echo "dashboardShared is a mandatory parameter!"
        return -1
    }
    if(dashboardLinkShared == "" ) {
        echo "dashboardLinkShared is a mandatory parameter!"
        return -1
    }
    if(dashboardPublished == "" ) {
        echo "dashboardPublished is a mandatory parameter!"
        return -1
    }
    if(dashboardTimeframe == "" ) {
        echo "dashboardTimeframe is a mandatory parameter!"
        return -1
    }
    if(dtDashboardTiles == "" ) {
        echo "dtDashboardTiles is a mandatory parameter!"
        return -1
    }

    int errorCode = 0

    def http = new HTTPBuilder( dtTenantUrl + '/api/config/v1/dashboards' )
 
    http.request( GET, JSON ) { req ->
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      response.success = { resp, json ->
        println "Dashboards retrieved successfully! ${resp.status}"
        Integer arraySize = json.dashboards.size()

        for (i=0; i < arraySize; i++) {
          if (json.dashboards[i].name == dashboardName){
            dtDashboardId = json.dashboards[i].id
            println "Found dashboard ${json.dashboards[i].name} with id ${dtDashboardId}"
          }
        }
      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item GET: uri: " + uri + "\n" +
            "  Unknown error trying to get item: ${resp.status}, not getting Item." +
            "\njson = ${json}")
      }
    }

  if (dtDashboardId == ""){

      http.request( POST, JSON ) { req ->
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      headers.'Content-Type' = 'application/json'
      body = [
        dashboardMetadata: [
            name: dashboardName,
            shared: dashboardShared,
            sharingDetails: [
                linkShared: dashboardLinkShared,
                published: dashboardPublished
            ],
            dashboardFilter: [
                timeframe: dashboardTimeframe,
                managementZone: [
                    id: dashboardManagementZoneId,
                    name: dashboardManagementZoneName
                ]
            ]
        ],
        tiles: 
            dtDashboardTiles
        
      ]

      response.success = { resp, json ->
      println "Dashboard created successfully! ${resp.status}"

      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
            "   Unknown error trying to create item: ${resp.status}, not creating Item." +
            "\njson = ${json}")
      }
    }
  }

  if (dtDashboardId != ""){

      http.request( PUT, JSON ) { req ->
      uri.path = '/api/config/v1/dashboards/' + dtDashboardId
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      headers.'Content-Type' = 'application/json'
      body = [
        id : dtDashboardId,
        dashboardMetadata: [
            name: dashboardName,
            shared: dashboardShared,
            sharingDetails: [
                linkShared: dashboardLinkShared,
                published: dashboardPublished
            ],
            dashboardFilter: [
                timeframe: dashboardTimeframe,
                managementZone: [
                    id: dashboardManagementZoneId,
                    name: dashboardManagementZoneName
                ]
            ]
        ],
        tiles: 
            dtDashboardTiles
        
      ]

      response.success = { resp, json ->
        println "Dashboard updated successfully! ${resp.status}"

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