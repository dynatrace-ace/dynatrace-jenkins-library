/***************************\
   Helper function to execute a shell script and to return the output as string.
  
  shellCommand: The shell command to execute that you wish to capture te output of
\***************************/
def call(String shellCommand)
{
    sh "${shellCommand} > shelloutput.txt"
    def returnString=readFile("shelloutput.txt").trim()
    sh "rm -f shelloutput.txt"
    return returnString 
}