public class StubProjectWizard extends NewProjectWizard {
    private final String mProjectName;
    private final String mProjectLocation;
    private final IAndroidTarget mTarget;
    public StubProjectWizard(String projectName, String projectLocation, IAndroidTarget target) {
        this.mProjectName = projectName;
        this.mProjectLocation = projectLocation;
        this.mTarget = target;
    }
    @Override
    protected NewProjectCreationPage createMainPage() {
        return new StubProjectCreationPage(mProjectName, mProjectLocation, mTarget);
    }
    @Override
    protected NewTestProjectCreationPage createTestPage() {
        return null;
    }
    @Override
    public IWizardContainer getContainer() {
        return new IWizardContainer() {
            public IWizardPage getCurrentPage() {
                return null;
            }
            public Shell getShell() {
                return null;
            }
            public void showPage(IWizardPage page) {
            }
            public void updateButtons() {
            }
            public void updateMessage() {
            }
            public void updateTitleBar() {
            }
            public void updateWindowTitle() {
            }
            public void run(boolean fork, boolean cancelable,
                    IRunnableWithProgress runnable)
                    throws InvocationTargetException, InterruptedException {
                runnable.run(new NullProgressMonitor());
            }
        };
    }
}
