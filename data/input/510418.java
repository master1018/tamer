public class Any implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = -3743894206806704049L;
    public static final Any ANY = new Any();    
    private Any() {
    }
    public boolean matches(Object actual) {
        return true;
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("<any>");
    }
}
