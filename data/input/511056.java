public class AndroidLaunch extends Launch {
    public AndroidLaunch(ILaunchConfiguration launchConfiguration, String mode,
            ISourceLocator locator) {
        super(launchConfiguration, mode, locator);
    }
    public void stopLaunch() {
        ILaunchManager mgr = getLaunchManager();
        if (canTerminate()) {
            try {
                terminate();
            } catch (DebugException e) {
            }
        }
        mgr.removeLaunch(this);
    }
}
