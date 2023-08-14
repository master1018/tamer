public class LaunchConfigTabGroup extends AbstractLaunchConfigurationTabGroup {
    public LaunchConfigTabGroup() {
    }
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                new MainLaunchConfigTab(),
                new EmulatorConfigTab(),
                new CommonTab()
            };
            setTabs(tabs);
    }
}
