## List of needed files:
- intent schema = flow of logic in Voice User Interface
- sample utterances = spoken phrases to illicit responses
- list of values for any custom slot types

### intents: 
	- ListStatesWithFluOutbreakIntent
	- GetFluLevelByStateIntent
	- AskToGetFluLevelByStateIntent

## TODO:
- figure out null pointer bug in the getStateReport
- After publishing, Sign up to get AWS Hoodie: https://developer.amazon.com/alexa-skills-kit/alexa-developer-skill-promotion
- Diagram the Voice Interface
- rename the project
- push to gitHub, but first:
	- create .gitignore
		- add target IDE and .DS files 
	- change the remote and origin to my gitHub account

      
- *xml parser example:* http://seleniummaster.com/sitecontent/index.php/java-tutorial/java-xml/291-parse-xml-file-java-jackson

### COMPLETED
- Research CDC open URL APIs for getting Flu and Illness Data
- Add intents to the SampleUtterances.txt and create methods for them.
- Make call to CDC webservice.  Caching possible?  Maybe a daily polling(how often does the information change?)
- In XMLPAXstleParseTesting:
	- write logic to get only the most recent node.
- Write logic to call only once per day and then cache the response for parsing
 	- Decide, do in memory cache or do a DB setup.
    	- For DB: set up DB, check for entry in DB, parse
    	- For InMem:
     		- Parse  parse the Date time.
     		- If date time > 24 hours or Null, call the service to retrieve the response.
      		- set up a reportDataPOJO to hold the string and a date/time stamp. make getter and setters for each element in report data
      		- Make call to webservice to get report data
      		- set report data to reportDataPOJO and Set the date/time to present.
 

      