@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' )
 
import groovyx.net.http.HTTPBuilder
import groovy.json.JsonOutput
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

/***************************\
  This function assumes we run on a Jenkins Agent that has curl command available.
  Returns either 0(=no errors), 1(=create/update app detection rule failed)
\***************************/
@NonCPS
def call( Map args ) 
    
    /*  String dtTenantUrl, 
        String dtApiToken 
        String dtAppName
        String pattern
        String applicationMatchType
        String applicationMatchTarget

    */
{
    // check input arguments
    String dtTenantUrl = args.containsKey("dtTenantUrl") ? args.dtTenantUrl : "${DT_TENANT_URL}"
    String dtApiToken = args.containsKey("dtApiToken") ? args.dtApiToken : "${DT_API_TOKEN}"
    String dtAppName = args.containsKey("dtAppName") ? args.dtAppName : ""
    String pattern = args.containsKey("pattern") ? args.pattern : ""
    String applicationMatchType = args.containsKey("applicationMatchType") ? args.applicationMatchType : ""
    String applicationMatchTarget = args.containsKey("applicationMatchTarget") ? args.applicationMatchTarget : ""
    String dtAppId = ""
    String dtDetectionRuleId = ""
    String newDtAppId = ""
    String dtAppRuleId = ""

    // check minimum required params
    if(dtAppName == "" ) {
        echo "dtAppName is a mandatory parameter!"
        return -1
    }
    if(pattern == "" ) {
        echo "pattern is a mandatory parameter!"
        return -1
    }
    if(applicationMatchType == "" ) {
        echo "applicationMatchType is a mandatory parameter!"
        return -1
    }
    if(applicationMatchTarget == "" ) {
        echo "applicationMatchType is a mandatory parameter!"
        return -1
    }

    int errorCode = 0

    def http = new HTTPBuilder( dtTenantUrl + '/api/config/v1/applications/web' )
 
    http.request( GET, JSON ) { req ->
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      response.success = { resp, json ->
        println "Web Apps retrieved successfully! ${resp.status}"
        Integer arraySize = json.values.size()

        for (i=0; i < arraySize; i++) {
          if (json.values[i].name == dtAppName){
            dtAppId = json.values[i].id
            println "Found DT web app ${json.values[i].name} with id ${dtAppId}"
          }
        }

      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item GET: uri: " + uri + "\n" +
            "  Unknown error trying to get item: ${resp.status}, not getting Item." +
            "\njson = ${json}")
      }
    }

    http.request( GET, JSON ) { req ->
      uri.path = '/api/config/v1/applicationDetectionRules'
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      response.success = { resp, json ->
        println "Application rules retrieved successfully! ${resp.status}"
        Integer arraySize = json.values.size()

        for (i=0; i < arraySize; i++) {
          if (json.values[i].name == dtAppName){
            dtAppRuleId = json.values[i].id
            println "Found DT app rule ${json.values[i].name} with id ${dtAppRuleId}"
          }
        }

      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item GET: uri: " + uri + "\n" +
            "  Unknown error trying to get item: ${resp.status}, not getting Item." +
            "\njson = ${json}")
      }
    }

    if (dtAppId == "" && dtAppRuleId == ""){

      http.request( POST, JSON ) { req ->
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      headers.'Content-Type' = 'application/json'
      body = [
        name: dtAppName,
        type: "AUTO_INJECTED",
        realUserMonitoringEnabled: true,
        costControlUserSessionPercentage: 100.000,
        loadActionKeyPerformanceMetric: "VISUALLY_COMPLETE",
        xhrActionKeyPerformanceMetric: "VISUALLY_COMPLETE",
        loadActionApdexSettings: [
            threshold: 3.0,
            toleratedThreshold: 3000,
            frustratingThreshold: 12000,
            toleratedFallbackThreshold: 3000,
            frustratingFallbackThreshold: 12000,
            considerJavaScriptErrors: true
        ],
        xhrActionApdexSettings: [
            threshold: 3.0,
            toleratedThreshold: 3000,
            frustratingThreshold: 12000,
            toleratedFallbackThreshold: 3000,
            frustratingFallbackThreshold: 12000,
            considerJavaScriptErrors: true
        ],
        customActionApdexSettings: [
            threshold: 3.0,
            toleratedThreshold: 3000,
            frustratingThreshold: 12000,
            toleratedFallbackThreshold: 3000,
            frustratingFallbackThreshold: 12000,
            considerJavaScriptErrors: true
        ],
        waterfallSettings: [
            uncompressedResourcesThreshold: 860,
            resourcesThreshold: 100000,
            resourceBrowserCachingThreshold: 50,
            slowFirstPartyResourcesThreshold: 200000,
            slowThirdPartyResourcesThreshold: 200000,
            slowCdnResourcesThreshold: 200000,
            speedIndexVisuallyCompleteRatioThreshold: 50
        ],
        monitoringSettings: [
            fetchRequests: false,
            xmlHttpRequest: false,
            javaScriptFrameworkSupport: [
                angular: false,
                dojo: false,
                extJS: false,
                icefaces: false,
                jQuery: false,
                mooTools: false,
                prototype: false,
                activeXObject: false
            ],
            contentCapture: [
                resourceTimingSettings: [
                    w3cResourceTimings: true,
                    nonW3cResourceTimings: false,
                    nonW3cResourceTimingsInstrumentationDelay: 50,
                    resourceTimingCaptureType: "CAPTURE_FULL_DETAILS",
                    resourceTimingsDomainLimit: 10
                ],
                javaScriptErrors: true,
                timeoutSettings: [
                    timedActionSupport: false,
                    temporaryActionLimit: 0,
                    temporaryActionTotalTimeout: 100
                ],
                visuallyCompleteAndSpeedIndex: true
            ],
            excludeXhrRegex: "",
            injectionMode: "JAVASCRIPT_TAG",
            addCrossOriginAnonymousAttribute: true,
            scriptTagCacheDurationInHours: 1,
            libraryFileLocation: "",
            monitoringDataPath: "",
            customConfigurationProperties: "",
            serverRequestPathId: "",
            secureCookieAttribute: false,
            cookiePlacementDomain: "",
            cacheControlHeaderOptimizations: true,
            advancedJavaScriptTagSettings: [
                syncBeaconFirefox: false,
                syncBeaconInternetExplorer: false,
                instrumentUnsupportedAjaxFrameworks: false,
                specialCharactersToEscape: "",
                maxActionNameLength: 100,
                maxErrorsToCapture: 10,
                additionalEventHandlers: [
                    userMouseupEventForClicks: false,
                    clickEventHandler: false,
                    mouseupEventHandler: false,
                    blurEventHandler: false,
                    changeEventHandler: false,
                    toStringMethod: false,
                    maxDomNodesToInstrument: 5000
                ],
                eventWrapperSettings: [
                    click: false,
                    mouseUp: false,
                    change: false,
                    blur: false,
                    touchStart: false,
                    touchEnd: false
                ],
                globalEventCaptureSettings: [
                    mouseUp: true,
                    mouseDown: true,
                    click: true,
                    doubleClick: true,
                    keyUp: true,
                    keyDown: true,
                    scroll: true,
                    additionalEventCapturedAsUserInput: ""
                ]
            ]
        ],
        userActionNamingSettings: [
            ignoreCase: true,
            splitUserActionsByDomain: true
        ]
    ]
  
      response.success = { resp, json ->
      println "Web App created successfully! ${resp.status}"
      newDtAppId = json.id

      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
            "   Unknown error trying to create item: ${resp.status}, not creating Item." +
            "\njson = ${json}")
      }
    }

    http.request( POST, JSON ) { req ->
      uri.path = '/api/config/v1/applicationDetectionRules'
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      headers.'Content-Type' = 'application/json'
      body = [   
        applicationIdentifier: newDtAppId,
        filterConfig: [
            pattern: pattern,
            applicationMatchType: applicationMatchType,
            applicationMatchTarget: applicationMatchTarget
        ]
     ]
      response.success = { resp, json ->
      println "Application detection rule created successfully! ${resp.status}"
      }
      response.failure = { resp, json ->
        throw new Exception("Stopping at item POST: uri: " + uri + "\n" +
            "   Unknown error trying to create item: ${resp.status}, not creating Item." +
            "\njson = ${json}")
      }
    }
  }

    if (dtAppId != "" && dtAppRuleId != ""){

    http.request( PUT, JSON ) { req ->
      uri.path = '/api/config/v1/applicationDetectionRules/' + dtAppRuleId
      headers.'Authorization' = 'Api-Token ' + dtApiToken
      headers.'Content-Type' = 'application/json'
      body = [   
        applicationIdentifier: dtAppId,
        filterConfig: [
            pattern: pattern,
            applicationMatchType: applicationMatchType,
            applicationMatchTarget: applicationMatchTarget
        ]
     ]
      response.success = { resp, json ->
      println "Application detection rule updated successfully! ${resp.status}"

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