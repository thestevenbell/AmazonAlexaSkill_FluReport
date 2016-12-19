## TODO:
- rework the question at the end of the intro to be more clear about what the user should ask next, as it sounds now
it begs a yes/no response.  Decide if a yes/no is what is wanted, if so, then prompt after yes for the user to then 
ask for a state specific report or the list.  Otherwise, make the intro explicitly ask the user to ask for either report.
- Rework this diagram in Powerpoint.  Diagram the Voice Interface
- Think about renaming the project as Alexa is confusing report for a weather report.

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
- push to gitHub, but first:
	- create .gitignore
		- add target IDE and .DS files 
	- change the remote and origin to my gitHub account

 

      