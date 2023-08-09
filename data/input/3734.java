public class WindowsDesktopPaneUI extends BasicDesktopPaneUI
{
    public static ComponentUI createUI(JComponent c) {
        return new WindowsDesktopPaneUI();
    }
    protected void installDesktopManager() {
        desktopManager = desktop.getDesktopManager();
        if(desktopManager == null) {
            desktopManager = new WindowsDesktopManager();
            desktop.setDesktopManager(desktopManager);
        }
    }
    protected void installDefaults() {
        super.installDefaults();
    }
    protected void installKeyboardActions() {
        super.installKeyboardActions();
        if(!desktop.requestDefaultFocus()) {
            desktop.requestFocus();
        }
    }
}
