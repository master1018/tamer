public class bug4300666 extends JApplet {
    public void init() {
        UIDefaults d = UIManager.getDefaults();
        d.toString();
    }
}
