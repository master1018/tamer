public class HTMLAnalysisResultsEvent extends EventObject {
    private String m_htmlText;
    private HTMLAnalysisResultsEvent(Object source) {
        super(source);
    }
    private HTMLAnalysisResultsEvent(Object source, String htmlText) {
        super(source);
        m_htmlText = htmlText;
    }
    public String getHTMLText() {
        return m_htmlText;
    }
    public static final void notifyRequestHTMLText(Object source) {
        HTMLAnalysisResultsEvent event = new HTMLAnalysisResultsEvent(source);
        List listenerList = ListenerList.getListeners(HTMLAnalysisResultsEventListener.class);
        Iterator listeners = listenerList.iterator();
        while (listeners.hasNext()) {
            HTMLAnalysisResultsEventListener listener;
            listener = (HTMLAnalysisResultsEventListener) listeners.next();
            listener.requestHTMLAnalysisResults(event);
        }
    }
    public static final void notifyReturnedHTMLText(Object source, String htmlText) {
        HTMLAnalysisResultsEvent event = new HTMLAnalysisResultsEvent(source, htmlText);
        List listenerList = ListenerList.getListeners(HTMLAnalysisResultsEventListener.class);
        Iterator listeners = listenerList.iterator();
        while (listeners.hasNext()) {
            HTMLAnalysisResultsEventListener listener;
            listener = (HTMLAnalysisResultsEventListener) listeners.next();
            listener.returnedHTMLAnalysisResults(event);
        }
    }
}
