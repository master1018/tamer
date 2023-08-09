public class IconManager {
    public static Icon MBEAN =
            getSmallIcon(getImage("mbean.gif"));
    public static Icon MBEANSERVERDELEGATE =
            getSmallIcon(getImage("mbeanserverdelegate.gif"));
    public static Icon DEFAULT_XOBJECT =
            getSmallIcon(getImage("xobject.gif"));
    private static ImageIcon getImage(String img) {
        return new ImageIcon(JConsole.class.getResource("resources/" + img));
    }
    private static ImageIcon getSmallIcon(ImageIcon icon) {
        return new ImageIcon(
                icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
    }
}
