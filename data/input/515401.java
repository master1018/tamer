public class AndroidJUnitLaunchConfigDelegate extends LaunchConfigDelegate {
    static final String ATTR_INSTR_NAME = AdtPlugin.PLUGIN_ID + ".instrumentation"; 
    private static final String EMPTY_STRING = ""; 
    @Override
    protected void doLaunch(final ILaunchConfiguration configuration, final String mode,
            IProgressMonitor monitor, IProject project, final AndroidLaunch androidLaunch,
            AndroidLaunchConfiguration config, AndroidLaunchController controller,
            IFile applicationPackage, AndroidManifestParser manifestParser) {
        String runner = getRunner(project, configuration, manifestParser);
        if (runner == null) {
            AdtPlugin.displayError(LaunchMessages.LaunchDialogTitle,
                    String.format(LaunchMessages.AndroidJUnitDelegate_NoRunnerMsg_s,
                            project.getName()));
            androidLaunch.stopLaunch();
            return;
        }
        String targetAppPackage = getTargetPackage(manifestParser, runner); 
        if (targetAppPackage == null) {
            AdtPlugin.displayError(LaunchMessages.LaunchDialogTitle,
                    String.format(LaunchMessages.AndroidJUnitDelegate_NoTargetMsg_3s,
                            project.getName(), runner, AndroidConstants.FN_ANDROID_MANIFEST));
            androidLaunch.stopLaunch();
            return; 
        }
        String testAppPackage = manifestParser.getPackage();
        AndroidJUnitLaunchInfo junitLaunchInfo = new AndroidJUnitLaunchInfo(project, 
                testAppPackage, runner);
        junitLaunchInfo.setTestClass(getTestClass(configuration));
        junitLaunchInfo.setTestPackage(getTestPackage(configuration));
        junitLaunchInfo.setTestMethod(getTestMethod(configuration));
        junitLaunchInfo.setLaunch(androidLaunch);
        IAndroidLaunchAction junitLaunch = new AndroidJUnitLaunchAction(junitLaunchInfo);
        controller.launch(project, mode, applicationPackage, testAppPackage, targetAppPackage,
                manifestParser.getDebuggable(), manifestParser.getApiLevelRequirement(),
                junitLaunch, config, androidLaunch, monitor);
    }
    private String getTargetPackage(AndroidManifestParser manifestParser, String runner) {
        for (Instrumentation instr : manifestParser.getInstrumentations()) {
            if (instr.getName().equals(runner)) {
                return instr.getTargetPackage();
            }
        }
        return null;
    }
    private String getTestPackage(ILaunchConfiguration configuration) {
        String containerHandle = getStringLaunchAttribute(
                JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, configuration);
        if (containerHandle != null && containerHandle.length() > 0) {
            IJavaElement element = JavaCore.create(containerHandle);
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                return element.getElementName();
            }
        }
        return null;
    }
    private String getTestClass(ILaunchConfiguration configuration) {
        return getStringLaunchAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                configuration);
    }
    private String getTestMethod(ILaunchConfiguration configuration) {
        return getStringLaunchAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME,
                configuration);
    }
    private String getRunner(IProject project, ILaunchConfiguration configuration,
            AndroidManifestParser manifestParser) {
        try {
            String runner = getRunnerFromConfig(configuration);
            if (runner != null) {
                return runner;
            }
            final InstrumentationRunnerValidator instrFinder = new InstrumentationRunnerValidator(
                    BaseProjectHelper.getJavaProject(project), manifestParser);
            runner = instrFinder.getValidInstrumentationTestRunner();
            if (runner != null) {
                AdtPlugin.printErrorToConsole(project, String.format(
                        LaunchMessages.AndroidJUnitDelegate_NoRunnerConfigMsg_s, runner));
                return runner;
            }
            AdtPlugin.printErrorToConsole(project, String.format(
                    LaunchMessages.AndroidJUnitDelegate_NoRunnerConsoleMsg_4s,
                    project.getName(),
                    AndroidConstants.CLASS_INSTRUMENTATION_RUNNER, 
                    AndroidConstants.LIBRARY_TEST_RUNNER,
                    AndroidConstants.FN_ANDROID_MANIFEST));
            return null;
        } catch (CoreException e) {
            AdtPlugin.log(e, "Error when retrieving instrumentation info"); 
        }
        return null;
    }
    private String getRunnerFromConfig(ILaunchConfiguration configuration) throws CoreException {
        return getStringLaunchAttribute(ATTR_INSTR_NAME, configuration);
    }
    private String getStringLaunchAttribute(String attributeName,
            ILaunchConfiguration configuration) {
        try {
            String attrValue = configuration.getAttribute(attributeName, EMPTY_STRING);
            if (attrValue.length() < 1) {
                return null;
            }
            return attrValue;
        } catch (CoreException e) {
            AdtPlugin.log(e, String.format("Error when retrieving launch info %1$s",  
                    attributeName));
        }
        return null;
    }
    static void setJUnitDefaults(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND, 
                TestKindRegistry.JUNIT3_TEST_KIND_ID);
    }
}
