public class AvdManagerAction implements IWorkbenchWindowActionDelegate, IObjectActionDelegate {
    public void dispose() {
    }
    public void init(IWorkbenchWindow window) {
    }
    public void run(IAction action) {
        Sdk sdk = Sdk.getCurrent();
        if (sdk != null) {
            UpdaterWindow window = new UpdaterWindow(
                    AdtPlugin.getDisplay().getActiveShell(),
                    new AdtConsoleSdkLog(),
                    sdk.getSdkLocation(),
                    false );
            window.addListeners(new UpdaterWindow.ISdkListener() {
                public void onSdkChange(boolean init) {
                    if (init == false) { 
                        AdtPlugin.getDefault().reparseSdk();
                    }
                }
            });
            window.open();
        }
    }
    public void selectionChanged(IAction action, ISelection selection) {
    }
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }
}
