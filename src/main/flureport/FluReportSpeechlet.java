/**
 Original form of this file modified by Steven Bell.  2016.
 */
// Original Copyright
/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package main.flureport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;

import sun.java2d.pipe.NullPipe;

import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

// TODO - update the below docs descriptor
/**
 * This sample shows how to create a Lambda function for handling Alexa Skill requests that:
 * 
 * <ul>
 * <li><b>Web service</b>: communicate with an external web service to get the flu report for specified state 
 * or the states with flu outbreaks. 
 * </li>
 * </li>
 * <p>
 * <li><b>Dialog and Session state</b>: Handles two models, both a one-shot ask and tell model, and
 * a multi-turn dialog model</li>
 * <li><b>SSML</b>: Using SSML tags to control how Alexa renders the text-to-speech</li>
 * </ul>
 * <p>
 * <h2>Examples</h2>
 * <p>
 * <b>One-shot model</b>
 * <p>
 * User: "Alexa, ask Flu Report what the flu status is for Puerto Rico."
 * <p>
 * Alexa: "The CDC report that flu is Widespread in Puerto Rico . Wanna know about another state?"
 * <p>
 * User: "No."
 * <p>
 * Alexa: "Good bye!"
 * <p>
 * 
 * <b>Dialog model</b>
 * <p>
 * User: "Alexa, open Flu Report"
 * <p>
 * Alexa: ""Hi, Flu Report can tell you the level of flu in each state or get you a list of 
 * states where the CDC has classified the level of flu as widespread.  Would you like to get 
 * a State flu report or to know which states have widespread flu?";"
 * <p>
 * User: "I would like a State flu report for Guam."
 * <p>
 * Alexa: "The CDC reports that the flu is Widespread in Guam. Would you like the list of states where 
 * the flu is widespread?"
 * <p>
 * User: "Yes."
 * <p>
 * Alexa: "The flu is reported by the CDC to be widespread in Guam, Puerto Rico and the Virgin Islands. 
 * Would you like to get a flu report for a specific state?"
 * <p>
 * User: "No."
 * <p>
 * Alexa: "Ok, Flu Report signing out. Good bye!"
 * <p>
 */
public class FluReportSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(FluReportSpeechlet.class);

   
    /**
     * Constant defining session attribute key for the event index.
     */
    private static final String SESSION_INDEX = "index";

    /**
     * Constant defining session attribute key for the event text key for date of events.
     */
    private static final String SESSION_TEXT = "text";

    /**
     * Constant defining session attribute key for the intent slot key for the State for which a Flu Report is requested.
     */
    private static final String SLOT_STATE = "State";
    
    private static fluReportCDCXML fluReport = fluReportCDCXML.getInstance();
    private static Document doc;

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        System.out.println("onIntent requestId={ "+  request.getRequestId() +" }, sessionId={" + session.getSessionId() + "}" );

        Intent intent = request.getIntent();
        String intentName = intent.getName();
        System.out.println("INTENT NAME:" + intentName);
// TODO populate the else if below with intents
// intent factory, list the intents created here 
        if ("ListStatesWithFluOutbreakIntent".equals(intentName)) {
        	System.out.println("INTENT ListStatesWithFluOutbreakIntent:" + intentName);
            return ListStatesWithFluOutbreakIntent(session);
        } else if ("GetFluLevelByStateIntent".equals(intentName)) {
        	System.out.println("INTENT GetFluLevelByStateIntent:" + intentName);
            return GetFluLevelByStateIntent(intent, session, intent.getSlot(SLOT_STATE));
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
        	
            // Create the plain text output.
            String speechOutput =
        		"Flu Report can give you the states where the CDC reports that the flu is widespread."
                + "To get this report say something like, where is the flu or in which states is the "
                + "flu widespread.  Flu Report can also tell you the prevelance of Flu in any state as "
                + "reported by the CDC. Ask for this by saying, give me the flu report for Florida or "
                + "what's the flu prevalence in California.";
            String repromptText = "Would you like to get a state specific report or a list of states with widespread flu?";

            return newAskResponse(speechOutput, false, repromptText, false);
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    private SpeechletResponse GetFluLevelByStateIntent(Intent intent, Session session, Slot slot) {
    	String stateSlotString = slot.getValue().toString().toUpperCase();
    	System.out.println("************************");
    	System.out.println("**********stateSlotString"+stateSlotString);
    	String standardizedStateName = null;
    	try{
    		standardizedStateName = convertStateIntentToStateEnum(slot);
    		System.out.println("***standardizedStateName***" + standardizedStateName);
    	}catch( NullPointerException npe){
    		System.out.println("The stateSlotString string from the StateSlot param in "
    				+ "convertStateIntentToStateEnum is null. Exception: " + npe);
    	}
    	try{
    		doc = fluReport.getFluReport();
    	}catch(NullPointerException npe){
    		System.out.println("fluReport.getFluReport(): " + npe);
    		
    	}
    	
    	if(doc.equals(null)){System.out.println("$$$$$NULL FLU REPORT 196");};
    	NodeList listOfTimeperiods = doc.getElementsByTagName("timeperiod");
        Node mostRecentReportNode = XMLPAXParseWebCall.getMostRecentNode(listOfTimeperiods);
        String subtitle = mostRecentReportNode.getAttributes().getNamedItem("subtitle").getNodeValue().toString();
    	String fluLevelforState = XMLPAXParseWebCall.getStateReport( standardizedStateName, mostRecentReportNode);
    	System.out.println("list of Prevalence of Flue in "+ standardizedStateName + " is " + fluLevelforState);
        String speechPrefixContent = "<p>For the " + subtitle + "</p> ";
        String cardPrefixContent = "For the State/Territory for " + standardizedStateName + 
        		", The CDC report the prevalence of the flu as " + fluLevelforState;
        String cardTitle = "Flu Report"; 
        String speechOutput = "For the State/Territory for " + standardizedStateName + 
        		", The CDC report the prevalence of the flu as " + fluLevelforState;  
        String repromptText = "Flu Report can give you the states where the CDC reports that the flu is widespread."
                + "To get this report say something like, where is the flu or in which states is the "
                + "flu widespread.  Flu Report can also tell you the prevelance of Flu in any state as "
                + "reported by the CDC. Ask for this by saying, give me the flu report for Florida or "
                + "what's the flu prevalence in California." + "Would you like to get a state specific "
        		+ "report or a list of states with widespread flu?";
        String askForNextStep = " Would you like to get a state specific "
        		+ "report or a list of states with widespread flu?";
        if (fluLevelforState.isEmpty()) {
            speechOutput =
                    "There is a problem communicating with the CDC at this time."
                            + " Please try again later.";
            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            StringBuilder speechOutputBuilder = new StringBuilder();
            speechOutputBuilder.append(speechPrefixContent);
            speechOutputBuilder.append(speechOutput);
            speechOutputBuilder.append(askForNextStep);
            speechOutput = speechOutputBuilder.toString();
            
            StringBuilder cardOutputBuilder = new StringBuilder();
            cardOutputBuilder.append(cardPrefixContent);
            cardOutputBuilder.append(askForNextStep);
      
            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle(cardTitle);
            card.setContent(cardOutputBuilder.toString());
            SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
            response.setCard(card);
            return response;
        }
	}

	private SpeechletResponse ListStatesWithFluOutbreakIntent(Session session) {
    	doc = fluReport.getFluReport();
    	NodeList listOfTimeperiods = doc.getElementsByTagName("timeperiod");
    	Node mostRecentReportNode = XMLPAXParseWebCall.getMostRecentNode(listOfTimeperiods);
        String listOfStatesWithWidespreadFlu = XMLPAXParseWebCall.getListStatesWithFlu(mostRecentReportNode).toString();
        listOfStatesWithWidespreadFlu = listOfStatesWithWidespreadFlu.replace("[", "");
        listOfStatesWithWidespreadFlu = listOfStatesWithWidespreadFlu.replace("]", "");
        String subtitle = mostRecentReportNode.getAttributes().getNamedItem("subtitle").getNodeValue().toString();

        String speechPrefixContent = "<p>For the " + subtitle + "</p> ";
        String cardPrefixContent = "The CDC reports that the prevalence of the flu is widespread "
        		+ "in the following states " + listOfStatesWithWidespreadFlu;
        String cardTitle = "Flu Report, States with widespread flu"; 
        String speechOutput = "The CDC reports that the prevalence of the flu is widespread "
        		+ "in the following states " + listOfStatesWithWidespreadFlu;
        String repromptText = "Flu Report can give you the states where the CDC reports that the flu is widespread."
                + "To get this report say something like, where is the flu or in which states is the "
                + "flu widespread.  Flu Report can also tell you the prevelance of Flu in any state as "
                + "reported by the CDC. Ask for this by saying, give me the flu report for Florida or "
                + "what's the flu prevalence in California." + "Would you like to get a state specific "
        		+ "report or a list of states with widespread flu?";
        String askForNextStep = " Would you like to get a state specific "
        		+ "report or a list of states with widespread flu?";
        if (listOfStatesWithWidespreadFlu.isEmpty()) {
            speechOutput =
                    "There is a problem communicating with the CDC at this time."
                            + " Please try again later.";
            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            StringBuilder speechOutputBuilder = new StringBuilder();
            speechOutputBuilder.append(speechPrefixContent);
            speechOutputBuilder.append(speechOutput);
            speechOutputBuilder.append(askForNextStep);
            speechOutput = speechOutputBuilder.toString();
            
            StringBuilder cardOutputBuilder = new StringBuilder();
            cardOutputBuilder.append(cardPrefixContent);
            cardOutputBuilder.append(askForNextStep);
      
            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle(cardTitle);
            card.setContent(cardOutputBuilder.toString());
            SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
            response.setCard(card);
            return response;
        }
	}

	@Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any session cleanup logic would go here
    }

    /**
     * Function to handle the onLaunch skill behavior.
     * 
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechOutput = "Hi, Flu Report can tell you the level of flu in each state or "
        		+ "get you a list of states where the CDC has classified the level of flu as "
        		+ "widespread.  Would you like to get a State flu report or to know which states"
        		+ "have widespread flu?";
        // If the user either does not reply to the welcome message or says something that is not
        // understood, they will be prompted again with this text.
        String repromptText =
                "Flu Report can give you the states where the CDC reports that the flu is widespread."
                + "To get this report say something like, where is the flu or in which states is the "
                + "flu widespread.  Flu Report can also tell you the prevelance of Flu in any state as "
                + "reported by the CDC. Ask for this by saying, give me the flu report for Florida or "
                + "what's the flu prevalence in California.";

        return newAskResponse(speechOutput, false, repromptText, false);
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
    
    private String convertStateIntentToStateEnum(Slot stateSlot) throws NullPointerException {
    	String stateSlotString = stateSlot.getValue().toString().toUpperCase();
    	log.info("stateSlotString"+stateSlotString);
    	if(!stateSlotString.isEmpty()){
    		String state = States.parse(stateSlotString).toString().toUpperCase();
    		log.info("state"+state);
    		return state;
    	}else{
    		return stateSlot.getValue().toString().toUpperCase();
    	}
    }
   
    /**
     * Wrapper for creating the Ask response from the input strings.
     * 
     * @param stringOutput
     *            the output to be spoken
     * @param isOutputSsml
     *            whether the output text is of type SSML
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @param isRepromptSsml
     *            whether the reprompt text is of type SSML
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
            String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

}
