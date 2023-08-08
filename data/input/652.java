public class Command extends AbstractAction {
    public static String IMAGE_DIR = "/org/apache/fop/viewer/Images/";
    public Command(String name) {
        this(name, (ImageIcon) null);
    }
    public Command(String name, ImageIcon anIcon) {
        super(name, anIcon);
    }
    public Command(String name, String iconName) {
        super(name);
        String path = IMAGE_DIR + iconName + ".gif";
        URL url = getClass().getResource(path);
        if (url == null) {
            MessageHandler.errorln("Icon not found: " + path);
        } else putValue(SMALL_ICON, new ImageIcon(url));
    }
    public void actionPerformed(ActionEvent e) {
        doit();
    }
    public void doit() {
        MessageHandler.errorln("Not implemented.");
    }
    public void undoit() {
        MessageHandler.errorln("Not implemented.");
    }
}
