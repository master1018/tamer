public class ActionEvent extends AWTEvent {
    private static final long serialVersionUID = -7671078796273832149L;
    public static final int SHIFT_MASK = 1;
    public static final int CTRL_MASK = 2;
    public static final int META_MASK = 4;
    public static final int ALT_MASK = 8;
    public static final int ACTION_FIRST = 1001;
    public static final int ACTION_LAST = 1001;
    public static final int ACTION_PERFORMED = 1001;
    private long when;
    private int modifiers;
    private String command;
    public ActionEvent(Object source, int id, String command) {
        this(source, id, command, 0);
    }
    public ActionEvent(Object source, int id, String command, int modifiers) {
        this(source, id, command, 0l, modifiers);
    }
    public ActionEvent(Object source, int id, String command, long when, int modifiers) {
        super(source, id);
        this.command = command;
        this.when = when;
        this.modifiers = modifiers;
    }
    public int getModifiers() {
        return modifiers;
    }
    public String getActionCommand() {
        return command;
    }
    public long getWhen() {
        return when;
    }
    @Override
    public String paramString() {
        String idString = (id == ACTION_PERFORMED) ? 
                          "ACTION_PERFORMED" : "unknown type"; 
        String modifiersString = ""; 
        if ((modifiers & SHIFT_MASK) > 0) {
            modifiersString += "Shift"; 
        }
        if ((modifiers & CTRL_MASK) > 0) {
            modifiersString += modifiersString.length() == 0 ? "Ctrl" : "+Ctrl"; 
        }
        if ((modifiers & META_MASK) > 0) {
            modifiersString += modifiersString.length() == 0 ? "Meta" : "+Meta"; 
        }
        if ((modifiers & ALT_MASK) > 0) {
            modifiersString += modifiersString.length() == 0 ? "Alt" : "+Alt"; 
        }
        return (idString + ",cmd=" + command + ",when=" + when +  
                ",modifiers=" + modifiersString); 
    }
}
