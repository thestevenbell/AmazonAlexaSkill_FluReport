package main.flureport;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

    
public class XMLPAXParseWebCall {
	private static final Logger log = LoggerFactory.getLogger(FluReportSpeechlet.class);
	
//	for testing XML logic, uncomment main and run as java application
	static fluReportCDCXML fluReport = fluReportCDCXML.getInstance();
	public static void main(String[] args){
		// surrogate for main method (no main needed as the handler is the starting point for 
		// code execution.
		Document doc = fluReport.getFluReport();
		NodeList listOfTimeperiods = doc.getElementsByTagName("timeperiod");
	    Node mostRecentReportNode = XMLPAXParseWebCall.getMostRecentNode(listOfTimeperiods);
	    String subtitle = mostRecentReportNode.getAttributes().getNamedItem("subtitle").getNodeValue().toString();
	    System.out.println("Subtitle: " + subtitle);
	    String state = "PR";
	    System.out.println("Prevalence of Flu in "+state + ". "+ XMLPAXParseWebCall.getStateReport( state, mostRecentReportNode) );
	    String listOfStatesWithWidespreadFlu = XMLPAXParseWebCall.getListStatesWithFlu(mostRecentReportNode).toString();
	    listOfStatesWithWidespreadFlu = listOfStatesWithWidespreadFlu.replace("[", "");
	    listOfStatesWithWidespreadFlu = listOfStatesWithWidespreadFlu.replace("]", "");
	}
	
    public static List<String> getListStatesWithFlu(Node mostRecentReportNode){
		List<String> listOfStatesWithFlu = new ArrayList<String>();
		Element timePeriodElement = (Element)mostRecentReportNode;

	    NodeList firstNameList = timePeriodElement.getElementsByTagName("abbrev");
	    NodeList labelList = timePeriodElement.getElementsByTagName("label");
	    int numOfNodes = timePeriodElement.getElementsByTagName("abbrev").getLength();
	    for(int i=0; i < numOfNodes ;i++){
	    	Element fluStatusElement = (Element)labelList.item(i);
	    	NodeList labelChildNodeList = fluStatusElement.getChildNodes();
		    if((labelChildNodeList.item(0)).getNodeValue().trim().toString().equalsIgnoreCase("Widespread")){
		    	Element stateElement = (Element)firstNameList.item(i);
		    	NodeList textStateAbbrevList = stateElement.getChildNodes();
			    log.debug("State/abbrev : " +  ((Node)textStateAbbrevList.item(0)).getNodeValue().trim());
			    String abbrevStateWithWidespreadFlu = ((Node)textStateAbbrevList.item(0)).getNodeValue().trim();
			    String fullNameStateWithWidespreadFlu = convertStateIntentToStateEnum(abbrevStateWithWidespreadFlu);
		    	listOfStatesWithFlu.add(fullNameStateWithWidespreadFlu);
		    }
	    }
		return listOfStatesWithFlu;
    }

	public static String getStateReport(String state, Node mostRecentReportNode){
    	String fluStatus = "not found";
    	Element timePeriodElement = (Element)mostRecentReportNode;
    	NodeList abbrevStateList = timePeriodElement.getElementsByTagName("abbrev");
		int numOfNodes = timePeriodElement.getElementsByTagName("abbrev").getLength();
//    	log.debug("num of nodes: " + numOfNodes);
    	for(int i=0; i < numOfNodes ;i++){
    		// check for the listing matching the state and return the flustatus attribute.  
	        Element stateElement = (Element)abbrevStateList.item(i);
	        NodeList textStateAbbrevList = stateElement.getChildNodes();
	        String stateAbbrev = ((Node)textStateAbbrevList.item(0)).getNodeValue().trim().toString();
//	        log.debug("State Abbrev : " + stateAbbrev);
	        String standardizedStateNameFromXML = convertStateIntentToStateEnum(stateAbbrev).trim() ;
			if(standardizedStateNameFromXML.equalsIgnoreCase(state)){
				System.out.println("standardizedStateNameFromXML.equals(state): " + state );
	        	NodeList labelList = timePeriodElement.getElementsByTagName("label");
	            Element ageElement = (Element)labelList.item(0);
	            NodeList textlabelList = ageElement.getChildNodes();
	            fluStatus = ((Node)textlabelList.item(0)).getNodeValue().trim().toString();
//	            log.debug("Label(X) : " + ((Node)textlabelList.item(0)).getNodeValue().trim().toString());
//	            log.debug("State(X): " + state);
	        }
    	}
    		 
    	return fluStatus;
    }

    /*
     *returns the node containing the most recent report, checks timestamp field to determine if the in memory report is 
     *greater than 24 hours old, if so, a new call will be made to update the report.  This logic was implemented to 
     *keep web traffic to a minimum.  
     *@param Nodelist nodelist
    */
    public static Node getMostRecentNode ( NodeList nodelist ) {
    	Node mostRecentNode;
    	int indexOfMostRecentNodeCurrentYear = 0;
    	int indexOfMostRecentNodeLastYear = 0;
    	List<Integer> weekANDyear = getCurrentWeekOfYear();
    	Integer currentYear = weekANDyear.get(1);
    	int latestWeekReportedThisYear = 0;
    	int latestWeekReportedLastYear = 0;
		
    	for(int s=0; s < nodelist.getLength() ; s++){
        	Node timePeriodNode = nodelist.item(s);
        	String year = timePeriodNode.getAttributes().getNamedItem("year").getNodeValue();
        	String reportWeekNumber = timePeriodNode.getAttributes().getNamedItem("number").getNodeValue();
        	int reportForWeekNumber = Integer.parseInt(reportWeekNumber);
        	int yearInt = Integer.parseInt(year);
            if(timePeriodNode.getNodeType() == Node.ELEMENT_NODE){
            	if(currentYear == yearInt){
//            		log.debug("currentYear == year");
            		
            		if(reportForWeekNumber > latestWeekReportedThisYear){
            			latestWeekReportedThisYear = reportForWeekNumber;
            			indexOfMostRecentNodeCurrentYear = s;
//            			log.debug("indexOfMostRecentNodeCurrentYear:" + indexOfMostRecentNodeCurrentYear);
            		}//end of if clause
            	}
            	if((currentYear-1) == yearInt) {
            		if(reportForWeekNumber > latestWeekReportedLastYear){
            			latestWeekReportedThisYear = reportForWeekNumber;
            			indexOfMostRecentNodeCurrentYear = s;
//            			log.debug("indexOfMostRecentNodeCurrentYear; " + indexOfMostRecentNodeCurrentYear);
            		}//end of if clause
            	}
            }
    	 }//end of for loop with s var
    	if(indexOfMostRecentNodeCurrentYear > 0){
    		mostRecentNode = nodelist.item(indexOfMostRecentNodeCurrentYear);
    	}else if(indexOfMostRecentNodeLastYear > 0){
			mostRecentNode = nodelist.item(indexOfMostRecentNodeLastYear);
		}else{
			mostRecentNode = nodelist.item(nodelist.getLength()-1);
		}
    	log.debug("indexOfMostRecentNodeCurrentYear: " + indexOfMostRecentNodeCurrentYear);
    	log.debug("latestWeekReportedThisYear: " + latestWeekReportedThisYear);
    	return mostRecentNode;
    }
    
    private static List<Integer> getCurrentWeekOfYear (){
    	List<Integer> currentWeekAndYear = new ArrayList<>();
    	ZoneId zoneId = ZoneId.of( "America/Montreal" );
    	ZonedDateTime now = ZonedDateTime.now( zoneId );
    	int week = now.get ( IsoFields.WEEK_OF_WEEK_BASED_YEAR );
    	currentWeekAndYear.add(week);
    	int weekYear = now.get ( IsoFields.WEEK_BASED_YEAR );
    	currentWeekAndYear.add(weekYear);
//    	log.debug( "now: " + now + " is week: " + week + " of weekYear: " + weekYear );
    	return currentWeekAndYear;
    }
    
    /**
     * Function to accept an intent containing a State slot (string representing a US State) and return the String 
     * representing the ENUM value of that slot value. The State ENUM class accepts three formats for input 1) the 
     * full name of  the state in all caps, 2) the uppercase two letter code example: GA, 3) the uppercase two letter 
     * code preceded by US- example: US-GA
     * 
     * @param intent
     *            the intent object containing the state slot
     * @return the States ENUM representation of that slot state value
     */
    
    private static String convertStateIntentToStateEnum(String stateAbbrev)  {
    	String stateSlotString = stateAbbrev.toString().toUpperCase();
    	if(!stateSlotString.equals(null)){
    		String fullStateName = States.parse(stateSlotString).toString().toUpperCase();
    		fullStateName = fullStateName.replace("_", " ");
    		return fullStateName;
    	}else{
    		return stateAbbrev.toString().toUpperCase();
    	}
    }

}
