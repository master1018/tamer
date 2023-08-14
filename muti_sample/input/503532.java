public class NewTestProjectAction extends OpenWizardAction {
    @Override
    protected IWorkbenchWizard instanciateWizard(IAction action) {
        return new NewTestProjectWizard();
    }
}
