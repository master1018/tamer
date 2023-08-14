public final class Test6501431 extends AbstractTest<JMenuItem> {
    public static void main(String[] args) {
        new Test6501431().test(true);
    }
    protected JMenuItem getObject() {
        JMenuItem menu = new JMenuItem();
        menu.setAccelerator(KeyStroke.getKeyStroke("ctrl F2"));
        return menu;
    }
    protected JMenuItem getAnotherObject() {
        return new JMenuItem();
    }
}
