public final class java_awt_MenuShortcut extends AbstractTest<MenuShortcut> {
    public static void main(String[] args) {
        new java_awt_MenuShortcut().test(true);
    }
    protected MenuShortcut getObject() {
        return new MenuShortcut(KeyEvent.VK_A);
    }
    protected MenuShortcut getAnotherObject() {
        return new MenuShortcut(KeyEvent.VK_Z, true);
    }
}
