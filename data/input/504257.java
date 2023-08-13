public class EmptyLaunchAction implements IAndroidLaunchAction {
    public boolean doLaunchAction(DelayedLaunchInfo info, IDevice device) {
        String msg = String.format("%1$s installed on device",
                info.getPackageFile().getFullPath().toOSString());
        AdtPlugin.printToConsole(info.getProject(), msg, "Done!");
        return false;
    }
    public String getLaunchDescription() {
        return "sync";
    }
}
