package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import main.flureport.States;
import main.flureport.XMLPAXParseWebCall;
import main.flureport.fluReportCDCXML;

public class StateEnumTest {
	
	fluReportCDCXML fluReport = fluReportCDCXML.getInstance();

	@Test
	public void StateEnumTest() {
		States alabama = States.parse("AL");
		String alabamaString = alabama.toString();
		States waffle = States.parse("waffle");
		assertEquals("ALABAMA",States.ALABAMA.toString() );
		assertEquals(alabamaString, "ALABAMA");
		assertEquals(waffle, null);
	}

	@Test
	public void StateEnumTest2() {
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
	

}
