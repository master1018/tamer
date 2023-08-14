public class DefaultPolicyChange_AWT {
    public static void main(String []s) {
        DefaultPolicyChange_AWT.runTestAWT();
    }
    private static void runTestAWT(){
        KeyboardFocusManager currentKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        FocusTraversalPolicy defaultFTP = currentKFM.getDefaultFocusTraversalPolicy();
        ContainerOrderFocusTraversalPolicy newFTP = new ContainerOrderFocusTraversalPolicy();
        Frame frame = new Frame();
        Window window = new Window(frame);
        FocusTraversalPolicy resultFTP = window.getFocusTraversalPolicy();
        Sysout.println("FocusTraversalPolicy on window = " + resultFTP);
        Sysout.println("Now will set another policy.");
        currentKFM.setDefaultFocusTraversalPolicy(newFTP);
        resultFTP = window.getFocusTraversalPolicy();
        if (!resultFTP.equals(defaultFTP)) {
            Sysout.println("Failure! FocusTraversalPolicy should not change");
            Sysout.println("Was: " + defaultFTP);
            Sysout.println("Become: " + resultFTP);
            throw new RuntimeException("Failure! FocusTraversalPolicy should not change");
        }
    }
}
