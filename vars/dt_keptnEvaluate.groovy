@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1' )
 
import groovyx.net.http.HTTPBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

/*
 * This method is used to SEND or GET a keptn event
 * The result will always contain two key:value pairs.
 * The first K/V pair is 'result' which is either 'success' or 'fail'.
 * The second K/V pair is 'data' which is either:
 *   - The keptn return data (for 'result: success) or
 *   - The error message (for 'result: fail)
 *
 * Also note that there is a convenience / helper return K/V to judge the Keptn result.
 * This is only returned for the GET calls and only when the call didn't fail.
 * This Key for this K/V is 'keptnResult'. It provides the output of the Keptn evaluation (pass, warning or fail)
 * Note that the raw Keptn data is still returned in the 'data' Key.
 */

@NonCPS
def processEvent( Map args) {
    echo args.toString();

/* Inputs
   'keptn_url'
   'keptn_api_token'
   'keptn_project' // eg. 'website'
   'keptn_service' // eg. 'front-end'
   'keptn_stage'   // eg. 'quality'
   'start_time' // Format: "2020-03-20T11:36:31"
   'end_time' // Format: "2020-03-20T11:36:31"
   'timeframe' // Minimum is 2m (2 minutes)
   'debug_mode' // boolean
   'timeout' // timeout in seconds. Default = 30s
   'retries' // number of retries for getting the evaluation results. Default = 30
   'wait' // time between each retry for getting the evaluation results. Default = 10s
*/

    def returnValue;
    String strKeptnURL, strKeptnAPIToken;
    strKeptnURL = strKeptnAPIToken = "";

    try {
    strKeptnURL = args.containsKey("keptn_url") ? args.keptn_url : "${KEPTN_URL}";
    strKeptnAPIToken = args.containsKey("keptn_api_token") ? args.keptn_api_token : "${KEPTN_API_TOKEN}";
    }
    catch (Exception e) {
    echo "[dt_processEvent.groovy] Missing mandatory parameters. KEPTN_URL and KEPTN_API_TOKEN are mandatory";
    returnValue = [ "result": "fail", "data": "ERROR: Missing input parameters. See log." ];
    }

    String strKeptnProject = args.containsKey("keptn_project") ? args.keptn_project : "${KEPTN_PROJECT}";
    String strKeptnService = args.containsKey("keptn_service") ? args.keptn_service : "${KEPTN_SERVICE}";
    String strKeptnStage = args.containsKey("keptn_stage") ? args.keptn_stage : "${KEPTN_STAGE}";
    //String strKeptnEventType = args.containsKey("keptn_event_type") ? args.keptn_event_type : "";
    //String strKeptnEventMethod = args.containsKey("keptn_event_method") ? args.keptn_event_method : "";
    //String strKeptnContext = args.containsKey("keptn_context") ? args.keptn_context : "";
    String strStartTime = args.containsKey("start_time") ? args.start_time : "";
    String strEndTime = args.containsKey("end_time") ? args.end_time : "";
    String strTimeframe = args.containsKey("timeframe") ? args.timeframe : "";
    int iTimeout = args.containsKey("timeout") ? args.timeout : 30; // Default timeout is 30 seconds
    int iRetries = args.containsKey("retries") ? args.retries : 30; // Default retries is 30
    int iWait = args.containsKey("wait") ? args.wait : 10; // Default wait is 10 seconds
    boolean bDebug = args.containsKey("debug_mode") ? args.debug_mode : false;

    echo "[dt_processEvent.groovy] Debug Mode: " + bDebug;
    
    if (bDebug) {
    echo "[dt_processEvent.groovy] Keptn URL is: " + strKeptnURL;
    echo "[dt_processEvent.groovy] Keptn API Token is: " + strKeptnAPIToken;
    echo "[dt_processEvent.groovy] Keptn Project is: " + strKeptnProject;
    echo "[dt_processEvent.groovy] Keptn Service is: " + strKeptnService;
    echo "[dt_processEvent.groovy] Keptn Stage is: " + strKeptnStage;
    /*echo "[dt_processEvent.groovy] Keptn Event Type is: " + strKeptnEventType;
    echo "[dt_processEvent.groovy] Keptn Event Method is: " + strKeptnEventMethod;
    echo "[dt_processEvent.groovy] Keptn Context is: " + strKeptnContext;*/
    echo "[dt_processEvent.groovy] Start Time is: " + strStartTime;
    echo "[dt_processEvent.groovy] End Time is: " + strEndTime;
    echo "[dt_processEvent.groovy] Timeframe is: " + strTimeframe;
    echo "[dt_processEvent.groovy] Timeout is: " + iTimeout;
    }
    
    if(strKeptnURL == "") {
    echo "KEPTN_URL is a mandatory parameter!"
    returnValue = [ "result": "fail", "data": "ERROR: Missing input parameters. See log." ];
    }
    if(strKeptnAPIToken == "" ) {
    echo "KEPTN_API_TOKEN is a mandatory parameter!"
    returnValue = [ "result": "fail", "data": "ERROR: Missing input parameters. See log." ];
    }
    if(strKeptnProject == "" ) {
    echo "keptn_project is a mandatory parameter!"
    returnValue = [ "result": "fail", "data": "ERROR: Missing input parameters. See log." ];
    }
    if(strKeptnService == "" ) {
    echo "keptn_service is a mandatory parameter!"
    returnValue = [ "result": "fail", "data": "ERROR: Missing input parameters. See log." ];
    }
    if(strKeptnStage == "" ) {
    echo "keptn_stage is a mandatory parameter!"
    returnValue = [ "result": "fail", "data": "ERROR: Missing input parameters. See log." ];
    }
    /*if(strKeptnEventType == "" ) {
    echo "keptn_event_type is a mandatory parameter!"
    returnValue = [ "result": "fail", "data": "ERROR: Missing input parameters. See log." ];
    }
    if(strKeptnEventMethod == "" ) {
    echo "keptn_event_method is a mandatory parameter!"
    returnValue = [ "result": "fail", "data": "ERROR: Missing input parameters. See log." ];
    }*/

    returnValue = sendStartEvaluationEvent(strKeptnURL, strKeptnAPIToken, strKeptnProject, strKeptnService, strKeptnStage, strStartTime, strEndTime, bDebug);
    if(returnValue.result == "fail") return returnValue;
    if(returnValue.data == null || returnValue.data == "") return [ "result": "fail", "data": "ERROR: Invalid keptnContext returned from sending evaluation event." ];
    //if (bDebug) echo "[dt_processEvent.groovy] Keptn Project is: " + keptn_context; 
    returnValue = getEvaluationResults(strKeptnURL, strKeptnAPIToken, returnValue.data, iRetries, iWait, bDebug);
    //echo (returnValue.toString());
    //echo (returnValue.data);
    //Class type = returnValue.data.getClass();
    //println(type);
    //returnValue= buildEvaluationResult(returnValue, bDebug);



    return returnValue;
} 

@NonCPS
def sendStartEvaluationEvent(String keptn_url, String keptn_api_token, String keptn_project, String keptn_service, String keptn_stage, String start_time, String end_time, boolean bDebug) {
    if (bDebug) echo "[dt_processEvent.groovy] ENTER sendStartEvaluationEvent";
    def http = new HTTPBuilder( keptn_url + '/v1/event' );
    //if (bDebug) http.ignoreSSLIssues();
    if (bDebug) {
        echo "[dt_processEvent.groovy] Keptn URL is: " + keptn_url;
        echo "[dt_processEvent.groovy] Keptn API Token is: " + keptn_api_token;
        echo "[dt_processEvent.groovy] Keptn Project is: " + keptn_project;
        echo "[dt_processEvent.groovy] Keptn Service is: " + keptn_service;
        echo "[dt_processEvent.groovy] Keptn Stage is: " + keptn_stage;
        echo "[dt_processEvent.groovy] Eval start is: " + start_time;
        echo "[dt_processEvent.groovy] Eval end is: " + end_time;
    }
       
    String strKeptnEventType="sh.keptn.event.start-evaluation";

    try {
        http.request( POST, JSON ) { req ->
            headers.'x-token' = keptn_api_token
            headers.'Content-Type' = 'application/json'
            body = [
                type: strKeptnEventType,
                source: "Pipeline",
                data: [
                    start: start_time,
                    end: end_time,
                    project: keptn_project,
                    service: keptn_service,
                    stage: keptn_stage,
                    teststrategy: "manual"
                ]
            ]
            response.success = { resp, json ->
                if (bDebug) echo "[dt_processEvent.groovy] Success: ${json} ++ Keptn Context: ${json.keptnContext}";
                returnValue = [ "result": "success", "data": "${json.keptnContext}" ];
            }
            
            response.failure = { resp, json ->
                println "Failure: ${resp} ++ ${json}";
                if (bDebug) echo "[dt_processEvent.groovy] Setting returnValue to: 'ERROR: SEND KEPTN EVENT FAILED'";
                returnValue = [ "result": "fail", "data": "ERROR: SEND KEPTN EVENT FAILED" ];
            }
        }
    }
        catch (Exception e) {
        echo "[dt_processEvent.groovy] SEND EVENT: Exception caught: " + e.getMessage();
        returnValue = [ "result": "fail", "data": "ERROR: " + e.getMessage() ];
        
        }
    if (bDebug) echo "[dt_processEvent.groovy] EXIT sendStartEvaluationEvent";
    return returnValue;
}

@NonCPS
def getEvaluationResults(String keptn_url, String keptn_api_token, String keptn_context, int retries, int wait, boolean bDebug)
{
    if (bDebug) echo "[dt_processEvent.groovy] ENTER getEvaluationResults";
    if (bDebug) {
        echo "[dt_processEvent.groovy] Keptn URL is: " + keptn_url;
        echo "[dt_processEvent.groovy] Keptn API Token is: " + keptn_api_token;
        echo "[dt_processEvent.groovy] Keptn Context is: " + keptn_context;
        echo "[dt_processEvent.groovy] Retries: " + retries;
        echo "[dt_processEvent.groovy] Wait: " + wait;
    }
    def http;
    
    //if (bDebug) http.ignoreSSLIssues();
       
    String strKeptnEventType="sh.keptn.events.evaluation-done";

    for(int i=1;i<=retries;i++){
        echo "[dt_processEvent.groovy] Waiting for evaluation results, try " + i + " of " + retries;
        http = new HTTPBuilder( keptn_url + '/v1/event' );
        try {
            http.request( GET, JSON ) { req ->
                headers.'x-token' = keptn_api_token
                headers.'Content-Type' = 'application/json'
                uri.query = [
                    type: strKeptnEventType,
                    keptnContext: keptn_context
                ]
                
                response.success = { resp, json ->
                    if (bDebug) echo "[dt_processEvent.groovy] Success: ${json} ++ Keptn Context: ${keptn_context}";
                    //if (json.data.result) evaluated = true;
                    Class type = json.getClass();
                    println(type);
                    returnValue = [ "result": "success", "data": "${json}" ];
                    def evalResult = buildEvaluationResult(json, bDebug);
                    return returnValue;
                }
                
                response.failure = { resp, json ->
                    println "Failure: ${resp} ++ ${json} ++ ${req}";
                    //if (bDebug) echo "[dt_processEvent.groovy] Setting returnValue to: 'ERROR: SEND KEPTN EVENT FAILED'";
                    if (bDebug) echo "[dt_processEvent.groovy] response code: " + json.code
                    if(json.code.toString().equals("500")) {
                        echo "[dt_processEvent.groovy] No evaluation results found yet, retrying..."
                        returnValue = [ "result": "pending", "data": "No evaluation results found yet for ${keptn_context}, retrying..." ];
                    }
                    else {
                        echo "[dt_processEvent.groovy] Techncal error when attempting to evaluate, break from loop...";
                        returnValue = [ "result": "fail", "data": "ERROR: SEND KEPTN EVENT FAILED" ];
                        return returnValue;
                    }
                }
                
            }
        }
        catch (Exception e) {
            echo "[dt_processEvent.groovy] SEND EVENT: Exception caught: " + e.getMessage();
            returnValue = [ "result": "fail", "data": "ERROR: " + e.getMessage() ];
            return returnValue; 
        }
        if(returnValue.result.toString().equals("success") || returnValue.result.toString().equals("fail")) break;
        
        Thread.sleep(wait*1000);
    }
    if(!returnValue.result.toString().equals("success") && !returnValue.result.toString().equals("fail"))
    returnValue = [ "result": "fail", "data": "ERROR: NO EVALUATION RESULT AFTER ${retries} retries of ${wait} seconds." ];

    if (bDebug) echo "[dt_processEvent.groovy] EXIT getEvaluationResults";
    return returnValue;
}

@NonCPS
def buildEvaluationResult (groovy.json.internal.LazyMap evaluationData, bDebug)
{
    if (bDebug) echo "[dt_processEvent.groovy] ENTER buildEvaluationResult";
    if (bDebug) echo "[dt_processEvent.groovy] Evaluation Data: " + evaluationData;
    
    def returnValue;
    for(def indicator : evaluationData.data.evaluationdetails.indicatorResults){
        echo indicator.value.metric;
    }

    returnValue.data = evaluationData.
//https://examples.javacodegeeks.com/jvm-languages/groovy/groovy-map-example/
    returnValue = evaluationData

    return returnValue;
    if (bDebug) echo "[dt_processEvent.groovy] ENTER buildEvaluationResult";
}

