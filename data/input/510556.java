class InstrumentationRunnerValidator {
    private final IJavaProject mJavaProject;
    private String[] mInstrumentationNames = null;
    private boolean mHasRunnerLibrary = false;
    static final String INSTRUMENTATION_OK = null;
    InstrumentationRunnerValidator(IJavaProject javaProject) {
        mJavaProject = javaProject;
        try {
            AndroidManifestParser manifestParser = AndroidManifestParser.parse(javaProject, 
                    null , true , false );
            init(manifestParser);
        } catch (CoreException e) {
            AdtPlugin.printErrorToConsole(javaProject.getProject(), LaunchMessages.ParseFileFailure_s,
                    AndroidConstants.FN_ANDROID_MANIFEST);
        }
    }
    InstrumentationRunnerValidator(IProject project) throws CoreException {
        this(BaseProjectHelper.getJavaProject(project));
    }
    InstrumentationRunnerValidator(IJavaProject javaProject, AndroidManifestParser manifestParser) {
        mJavaProject = javaProject;
        init(manifestParser);
    }
    private void init(AndroidManifestParser manifestParser) {
        Instrumentation[] instrumentations = manifestParser.getInstrumentations();
        mInstrumentationNames = new String[instrumentations.length];
        for (int i = 0; i < instrumentations.length; i++) {
            mInstrumentationNames[i] = instrumentations[i].getName();
        }
        mHasRunnerLibrary = hasTestRunnerLibrary(manifestParser);
    }
    private boolean hasTestRunnerLibrary(AndroidManifestParser manifestParser) {
       for (String lib : manifestParser.getUsesLibraries()) {
           if (lib.equals(AndroidConstants.LIBRARY_TEST_RUNNER)) {
               return true;
           }
       }
       return false;
    }
    String[] getInstrumentationNames() {
        return mInstrumentationNames;
    }
    String getValidInstrumentationTestRunner() {
        for (String instrumentation : getInstrumentationNames()) {
            if (validateInstrumentationRunner(instrumentation) == INSTRUMENTATION_OK) {
                return instrumentation;
            }
        }
        return null;
    }
    String validateInstrumentationRunner(String instrumentation) {
        if (!mHasRunnerLibrary) {
            return String.format(LaunchMessages.InstrValidator_NoTestLibMsg_s,
                    AndroidConstants.LIBRARY_TEST_RUNNER);
        }
        if (!instrumentation.equals(AndroidConstants.CLASS_INSTRUMENTATION_RUNNER)) {
            String result = BaseProjectHelper.testClassForManifest(mJavaProject,
                    instrumentation, AndroidConstants.CLASS_INSTRUMENTATION_RUNNER, true);
             if (result != BaseProjectHelper.TEST_CLASS_OK) {
                return String.format(
                        LaunchMessages.InstrValidator_WrongRunnerTypeMsg_s,
                        AndroidConstants.CLASS_INSTRUMENTATION_RUNNER);
             }
        }
        return INSTRUMENTATION_OK;
    }
}
