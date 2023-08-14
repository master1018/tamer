public class AgentInitializationException extends Exception {
    static final long serialVersionUID = -1508756333332806353L;
    private int returnValue;
    public AgentInitializationException() {
        super();
        this.returnValue = 0;
    }
    public AgentInitializationException(String s) {
        super(s);
        this.returnValue = 0;
    }
    public AgentInitializationException(String s, int returnValue) {
        super(s);
        this.returnValue = returnValue;
    }
    public int returnValue() {
        return returnValue;
    }
}
