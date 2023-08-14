public class GrabOnUnfocusableToplevel {
    public static void main(String[] args) {
        Robot r = Util.createRobot();
        JWindow w = new JWindow();
        w.setSize(100, 100);
        w.setVisible(true);
        Util.waitForIdle(r);
        final JPopupMenu menu = new JPopupMenu();
        JButton item = new JButton("A button in popup");
        menu.add(item);
        w.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent me) {
                menu.show(me.getComponent(), me.getX(), me.getY());
                System.out.println("Showing menu at " + menu.getLocationOnScreen() +
                                   " isVisible: " + menu.isVisible() +
                                   " isValid: " + menu.isValid());
                }
            });
        Util.clickOnComp(w, r);
        Util.waitForIdle(r);
        if (!menu.isVisible()) {
            throw new RuntimeException("menu was not shown");
        }
        menu.hide();
        System.out.println("Test passed.");
    }
}
