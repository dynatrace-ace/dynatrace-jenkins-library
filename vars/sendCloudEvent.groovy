import groovy.json.JsonOutput

/***************************\
  This function assumes we run on a Jenkins Agent that has curl command available.

  Returns either 0(=no errors), 1(=pushing event failed)
\***************************/
def call( Map args ) 
    
    /*  String receiver,
        String type,
        String data,
    */
{
    // check input arguments
    def receiver = args.containsKey("receiver") ? args.receiver : ""
    def type = args.containsKey("type") ? args.type : ""
    def source = args.containsKey("source") ? args.source : ""
    def shkeptncontext = args.containsKey("shkeptncontext") ? args.shkeptncontext : ""
    def data = args.containsKey("data") ? args.data : [ ]

    // check minimum required params
    if(receiver == "" || type == "" || source == "") {
        echo "Receiver, type and source are mandatory parameters!"
        return -1
    }

    def now = new Date()
    
    String specversion = "0.2"
    String id = "1234"
    String time = now.format("yyyyMMdd-HH:mm:ss.SSS", TimeZone.getTimeZone('UTC'))
    String datacontenttype = "application/json"

    int errorCode = 0

    // build the curl command
    int numberOfItems = data.size()

    // set Dynatrace URL, API Token and Event Type.
    String curlCmd = "curl -X POST \"${receiver}\" -H \"accept: application/json\" -H \"Content-Type: application/json\" -d \"{" 
    curlCmd += " \\\"specversion\\\": \\\"${specversion}\\\","
    curlCmd += " \\\"type\\\": \\\"${type}\\\","
    curlCmd += " \\\"source\\\": \\\"${source}\\\","
    curlCmd += " \\\"id\\\": \\\"${id}\\\","
    curlCmd += " \\\"time\\\": \\\"${time}\\\","
    curlCmd += " \\\"datacontenttype\\\": \\\"${datacontenttype}\\\","
    curlCmd += " \\\"shkeptncontext\\\": \\\"${shkeptncontext}\\\","
    
    // set data block 
    curlCmd += " \\\"data\\\": { "
    data.eachWithIndex { item, i ->
        curlCmd += "\\\"${item.key}\\\": \\\"${item.value}\\\""
        if(i < (numberOfItems - 1)) { curlCmd += ", " }
    }
    curlCmd += "} }\" "

    // send the event
    sh "${curlCmd}"

    return errorCode
}