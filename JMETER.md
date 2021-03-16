# JMETER Examples

Supporting function that allows to run jmeter tests from Jenkins. This function assumes we run on a Jenkins Agent that has JMeter installed 

# Library functions

| Class | Library Function | Description |
| --- | --- | --- |
| Jmeter | executeJmeter | Execute a Jmeter test from a Jenkins pipeline |

# Example 

Below is what Dynatrace events look like for this example:

```
    stage('Run performance test') {
        steps {
            container('jmeter') {
                script {
                    def status = jmeter.executeJmeterTest ( 
                        scriptName: "jmeter/simplenodeservice_load.jmx",
                        resultsDir: "perfCheck_MYAPP_staging_${BUILD_NUMBER}",
                        serverUrl: "myappurl", 
                        serverPort: 80,
                        checkPath: '/health',
                        vuCount: 1,
                        loopCount: 10,
                        LTN: "perfCheck_MYAPP_${BUILD_NUMBER}",
                        funcValidation: false,
                        avgRtValidation: 4000
                    )
                    if (status != 0) {
                        currentBuild.result = 'FAILED'
                        error "Performance test in staging failed."
                    }
                }
            }
        }
    }
```

# Archive artefact

The script will also archive the output, *.tlf files to Jenkins
