public class bug6883341 {
    private static void createGui() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem());
        menu.setVisible(true);
        menu.setVisible(false);
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                bug6883341.createGui();
            }
        });
    }
}
