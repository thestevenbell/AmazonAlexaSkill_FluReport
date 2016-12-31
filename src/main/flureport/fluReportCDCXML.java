package main.flureport;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class fluReportCDCXML  {
	private static final Logger log = LoggerFactory.getLogger(FluReportSpeechlet.class);
	private static fluReportCDCXML instance = null;
	private static DateTime timeOfLastCDCAPICall;
	private static Document doc;
		
    protected fluReportCDCXML() {
       // Exists only to defeat instantiation.
    }
    
    // returns same instance for each thread
    public static fluReportCDCXML getInstance() {
       if(instance == null) {
          instance = new fluReportCDCXML();
       }
       return instance;
    }
    
    /**
     * Makes a HTTPS web call to the CDC API to retrieve the flu report .xml file.
     * 
     * @return Document
     */
    public Document getFluReport() {
    	try {
    		System.out.println(" compare time: isGreaterThan24Hours(" + isGreaterThan24Hours(timeOfLastCDCAPICall) + ")");
    		if(doc == null || isGreaterThan24Hours(timeOfLastCDCAPICall)){
    			timeOfLastCDCAPICall = DateTime.now();
    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    			factory.setNamespaceAware(true);
    			//	return factory.newDocumentBuilder().parse(new File("src/test/input.xml"));
    			//	TODO - uncomment below for production  
    			System.out.println("fetching new Flu Report");
    			doc = factory.newDocumentBuilder().parse(new URL("https://www.cdc.gov/flu/weekly/flureport.xml").openStream());
    		}
    		if(doc != null){
    			// normalize text representation
    			doc.getDocumentElement().normalize();
    			System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());
    			NodeList listOfTimeperiods = doc.getElementsByTagName("timeperiod");
    			int NumberTimePeriods = listOfTimeperiods.getLength();
    			System.out.println("Total no of timeperiods : " + NumberTimePeriods);
    		}

    	}catch (SAXParseException err) {
    		System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId());
    		System.out.println("Error message: " + err.getMessage());
    		err.printStackTrace();
    	}catch (SAXException e) {
    		Exception x = e.getException ();
    		((x == null) ? e : x).printStackTrace ();
    	}catch (Throwable t) {
    		t.printStackTrace ();
    	}
    	return doc;
    }
	
    /**	
     * Function to determine if the last time the CDC API was accessed.
     * If the time of the last access is null or greater than 24 hours
     * then a new call will be made so that the report information
     * will not be older than 24 hours.  
     * 
     * @param timeOfLastCDCAPICall
     * @return Boolean
     */
    private static Boolean isGreaterThan24Hours(DateTime timeOfLastCDCAPICall){
//			uncomment for testing (sets the time to greater than 24 hours ago to force new api call
//	    	DateTime TESTINGtimeOfLastCDCAPICall = DateTime.now().minusHours(27);
//			returns the system date time in the default timezone.
    	DateTime endTime = DateTime.now();
    	System.out.println("dateTime from the system is:" + endTime);
    	// For testing > 24 hours
//			Period p = new Period(TESTINGtimeOfLastCDCAPICall, endTime);
		Period p = new Period(timeOfLastCDCAPICall, endTime);
		System.out.println("Period: " + p.getHours() );
		if(timeOfLastCDCAPICall == null || endTime == null){
			return false;
		}
		if (p.getHours() >= 1 ){
			return true;
		}else{
			return false;
		}
    }
	   
}