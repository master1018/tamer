public class bug6675802 {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new JMenuItem("Click"));
        popupMenu.show(null, 0, 0);
        System.out.println("Test passed");
    }
}
