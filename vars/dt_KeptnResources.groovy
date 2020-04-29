
@NonCPS
def updateProjectResource() {
}

@NonCPS
def updateKepnResource() {

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

enum resourceTypes {
    PROJECT, STAGE, SERVICE
}