# Flu Report, an Alexa Skill to inform user of Flu prevalence by US state
#### [AWS Lambda](http://aws.amazon.com/lambda) function written in Java and hosted as an Amazon Web Services Lambda.
Derived from the Alexa SDK found here : https://github.com/amzn/alexa-skills-kit-java.
## Home Repo: 
> Note: This project can be used a scaffolding for future project by simply replacing the flureport package with your own.  

### File Descriptions:
- intentSchema.json, defines the intents that the skill will have and the variable fields from user input
- sampleUtterances.txt, spoken phrases to illicit intents.
- list of values for any custom slot types.(not used in this project as Amazon has a standard US States Slot type already defined)
- States Enum, this used to take user slot in put and the parsed state fields from the CDC API and parse them into the same format.  
This file is derived from https://github.com/AustinC/UnitedStates/blob/master/src/main/java/unitedstates/US.java . Thanks AustinC for
making this openly available. The list did not include teritories, so Guam, Puerto Rico and the Virgin Islands were added to the list.  
- FluReportSpeechlet.java, this is where the majority of the logic for flow of the Skill resides.  It handles the input of intents and 
slots and returns the appropriate speech text or terminates the session as necessary.  
- FluReportSpeechletRequestStreamHandler.java, provides the entry point to application from an outside application like the associated
 Alexa Skill app Flu Report.  The name of this class must be provided during the Lambda setup so that the platform can map to it.  Improper 
 reference to the class or any malformation of this class will error out the Lambda.  
- fluReportCDCXML.java, set up as a singleton so that there is only one instance of the class holding the CDC fle report.  This class
makes a call to get the flu report and saves is as type Document.  The document is parsed as in PAX style XML.  In a normal server hosted
application this architecture for the webservice class would allow the instance of the Document to persist in memory acting as a cache
to hold the report data.  The getFluReport() method checks for a timestamp field that is populated at the time of the API call effectively
giving a reference for how old the document is.  getFluReport() checks the timestamp and if the time of the last web call was greater 24 
hours prior the API is hit again to update the report.  In this way application performance is increased and web traffic is reduced.  As 
this application is run serverless as an AWS Lambda this behavior is negated somewhat as their is no persistant memory held in a permanent server 
environment.  Never-the-less, if the instance of the Lambda is alive for more than one transaction in 24 hours some performance gain 
may be realized. 
- fluReportServlet.java, exists to set the system property:
```   
static
    {
        System.setProperty("com.amazon.speech.speechlet.servlet.disableRequestSignatureCheck", "true");
    }
```
 See this https://forums.developer.amazon.com/questions/29011/lambda-speechletrequesthandlerexception.html forum entry for more info.  I was
 never able to map the Alexa Skill key to my Lambda, the workaround in this class.  Not ideal, I know. Therefore, the handler function has not been populated with the supportedApplicationIds as it should be to prevent rando's from hitting the endpoint triggering the funcation.  
- XMLPAXParseWebCall.java - contains a main method which is unused and unneeded for use as a Lambda project but has been retained to aid in local
development and testing.  Also contains the core logic to return the states with widespread flu and to return the level of flu prevalence for the 
given State slot.  

#### Skill Description:
Flu Report provides the prevalence of the flu for any user specified US state and the list of states where the flu is widespread. Information is provided by the Center for Disease Control at the following address https://www.cdc.gov/flu/weekly/.  No special hardware or accounts are needed to use the app.  Simply follow the voice prompts to ask for a report naming the state you are interested in knowing more about.  You may also ask for a list of states where the CDC reports the flu as widespread.  The application is designed to provide the most recent information available from the CDC but no guarantee is made that the information is up to date or accurate.  This tool does not provide medical advice, and is for informational and educational purposes only, and is not a substitute for professional medical advice, treatment or diagnosis. Call your doctor to receive medical advice. If you think you may have a medical emergency, please dial 911.

  This sample shows how to create a Lambda function for handling Alexa Skill requests that:
  
  <ul>
  <li><b>Web service</b>: communicate with an external web service to get the flu report for specified state 
  or the states with flu outbreaks. 
  </li>
  </li>
  <p>
  <li><b>Dialog and Session state</b>: Handles two models, both a one-shot ask and tell model, and
  a multi-turn dialog model</li>
  <li><b>SSML</b>: Using SSML tags to control how Alexa renders the text-to-speech</li>
  </ul>
  <p>
  <h2>Examples</h2>
  <p>
  <b>One-shot model</b>
  <p>
  User: "Alexa, ask Flu Report what the flu status is for Puerto Rico."
  <p>
  Alexa: "The CDC report that flu is Widespread in Puerto Rico . Wanna know about another state?"
  <p>
  User: "No."
  <p>
  Alexa: "Good bye!"
  <p>
  
  <b>Dialog model</b>
  <p>
  User: "Alexa, open Flu Report"
  <p>
  Alexa: ""Hi, Flu Report can tell you the level of flu in each state or get you a list of 
  states where the CDC has classified the level of flu as widespread.  Would you like to get 
  a State flu report or to know which states have widespread flu?";"
  <p>
  User: "I would like a State flu report for Guam."
  <p>
  Alexa: "The CDC reports that the flu is Widespread in Guam. Would you like the list of states where 
  the flu is widespread?"
  <p>
  User: "Yes."
  <p>
  Alexa: "The flu is reported by the CDC to be widespread in Guam, Puerto Rico and the Virgin Islands. 
  Would you like to get a flu report for a specific state?"
  <p>
  User: "No."
  <p>
  Alexa: "Ok, Flu Report signing out. Good bye!"
  <p>
 
### Setup
To run this example skill you need to do two things. The first is to deploy the example code in lambda, and the second is to configure an Alexa skill to interact with this Lambda.

### Fire Up the Code in AWS Lambda
1. Go to the AWS Console and click on the Lambda link. Note: ensure you are in us-east or you wont be able to use Alexa with Lambda.
2. Click on the Create a Lambda Function or Get Started Now button.
3. Skip the blueprint
4. Name the Lambda Function "FluReport-Skill".
5. Select the runtime as Java 8
6. Go to the the root directory containing pom.xml, and run 'mvn assembly:assembly -DdescriptorId=jar-with-dependencies package'. This will generate a zip file named "alexa-skills-kit-samples-1.0-jar-with-dependencies.jar" in the target directory. Note the version number may be different if the jar has been created before for this project.
7. Select Code entry type as "Upload a .ZIP file" and then upload the "alexa-skills-kit-samples-1.0-jar-with-dependencies.jar" file from the build directory to Lambda
8. Set the Handler as  main.FluReport.FluReportSpeechletRequestStreamHandler (this refers to the package where handler resides and the RequestStreamHandler class/file in the zip).
9. Create a basic execution role and click create.
10. Leave the Advanced settings as the defaults.
11. Click "Next" and review the settings then click "Create Function"
12. Click the "Event Sources" tab and select "Add event source"
13. Set the Event Source type as Alexa Skills kit and Enable it now. Click Submit.
14. Copy the ARN from the top right to be used later in the Alexa Skill Setup.

### Alexa Skill Setup
1. Go to the [Alexa Console](https://developer.amazon.com/edw/home.html) and click Add a New Skill.
2. Set "Flu Report" as the skill name and "flu report" as the invocation name, this is what is used to activate your skill. For example you would say: "Alexa, Ask Flu Report what the prevalence of flu in Arizona is."
3. Copy the Intent Schema from the included IntentSchema.json.
4. Copy the Sample Utterances from the included SampleUtterances.txt. Click Next.
5. Select the Lambda ARN for the skill Endpoint and paste the ARN copied from above. Click Next.
6. Go back to the skill Information tab and copy the appId. Paste the appId into the FluReportSpeechletRequestStreamHandler.java file for the variable supportedApplicationIds,
   then update the lambda source zip file with this change and upload to lambda again, this step makes sure the lambda function only serves request from authorized source.
7. You are now able to start testing your sample skill! You should be able to go to the [Echo webpage](http://echo.amazon.com/#skills) and see your skill enabled.
8. In order to test it, try to say some of the Sample Utterances from the Examples section below.
9. Your skill is now saved and once you are finished testing you can continue to publish your skill.
 
#### Notes:
- /src/test/testObjectForUseInLambdaConsole.json,  Use a similar in the Lamda Console to test
 your Lambda function. This object was captured from the Alexa Developers Console 
under the testing tab. It is created when an Intent string
is processed from test imput.  Nabbing this object is much
easier than trying to configure one from scratch as all of 
Id fields are already poputed with valid input.
- Source Data API: http://www.cdc.gov/flu/weekly/flureport.xml
- The Sysout/Console logs in the code have been left intact to aid others in picking up the flow of the code.   
- 


### GENERAL ALEXA STUFF
#### Alexa Skills Kit Documentation
The documentation for the Alexa Skills Kit is available on the [Amazon Apps and Services Developer Portal](https://developer.amazon.com/appsandservices/solutions/alexa/alexa-skills-kit/).

#### Resources
Here are a few direct links to our documentation:
- [Using the Alexa Skills Kit Samples](https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/using-the-alexa-skills-kit-samples)
- [Getting Started](https://developer.amazon.com/appsandservices/solutions/alexa/alexa-skills-kit/getting-started-guide)
- [Invocation Name Guidelines](https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/choosing-the-invocation-name-for-an-alexa-skill)
- [Developing an Alexa Skill as an AWS Lambda Function](https://developer.amazon.com/appsandservices/solutions/alexa/alexa-skills-kit/docs/developing-an-alexa-skill-as-a-lambda-function)
