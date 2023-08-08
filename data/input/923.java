public class Warning extends BasicCarabinerMessage {
    public Warning(Component component, String name, String message) {
        this(component, name, message, new ImageIcon(DataUtils.class.getResource("warning.jpg")), null);
    }
    public Warning(Component component, String name, String message, Color highlightColor) {
        this(component, name, message, new ImageIcon(DataUtils.class.getResource("warning.jpg")), highlightColor);
    }
    public Warning(Component component, String name, String message, Icon icon, Color highlightColor) {
        super(component, name, message, icon, highlightColor);
    }
}
