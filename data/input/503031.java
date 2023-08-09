public class AndroidJUnitTabGroup extends AbstractLaunchConfigurationTabGroup {
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                new AndroidJUnitLaunchConfigurationTab(),
                new EmulatorConfigTab(),
                new CommonTab()
        };
        setTabs(tabs);
    }
}
