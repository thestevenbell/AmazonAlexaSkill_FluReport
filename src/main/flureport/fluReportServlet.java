package main.flureport;

import com.amazon.speech.speechlet.servlet.SpeechletServlet;

public class fluReportServlet extends SpeechletServlet
{
    /**
     * 
     */
    private static final long serialVersionUID = 6998188851979224629L;

    static
    {
        System.setProperty("com.amazon.speech.speechlet.servlet.disableRequestSignatureCheck", "true");
    }
    
    public fluReportServlet()
    {
        this.setSpeechlet(new FluReportSpeechlet());
        
    }
}
