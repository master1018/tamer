public class bug6474153 {
    public static void main(String... args) throws Exception {
        checkArray(LookAndFeel.makeKeyBindings(new Object[] {"UP", DefaultEditorKit.upAction} ));
        checkArray(LookAndFeel.makeKeyBindings(new Object[] {"UP", DefaultEditorKit.upAction, "PAGE_UP"} ));
    }
    private static void checkArray(JTextComponent.KeyBinding[] keyActionArray) {
        if (keyActionArray.length != 1) {
            throw new RuntimeException("Wrong array lenght!");
        }
        if (!DefaultEditorKit.upAction.equals(keyActionArray[0].actionName)) {
            throw new RuntimeException("Wrong action name!");
        }
        if (!KeyStroke.getKeyStroke("UP").equals(keyActionArray[0].key)) {
            throw new RuntimeException("Wrong keystroke!");
        }
    }
}
