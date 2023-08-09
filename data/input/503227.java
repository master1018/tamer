public class AndroidJUnitLaunchShortcut extends JUnitLaunchShortcut {
    @Override
    protected String getLaunchConfigurationTypeId() {
        return "com.android.ide.eclipse.adt.junit.launchConfigurationType"; 
    }
    @Override
    protected ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element)
            throws CoreException {
        ILaunchConfigurationWorkingCopy config = super.createLaunchConfiguration(element);
        String instrumentation = new InstrumentationRunnerValidator(element.getJavaProject()).
                getValidInstrumentationTestRunner();
        if (instrumentation != null) {
            config.setAttribute(AndroidJUnitLaunchConfigDelegate.ATTR_INSTR_NAME, 
                    instrumentation);
        }
        AndroidJUnitLaunchConfigDelegate.setJUnitDefaults(config);
        return config;
    }
}
