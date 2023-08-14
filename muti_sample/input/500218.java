public class NewProjectAction extends OpenWizardAction {
    @Override
    protected IWorkbenchWizard instanciateWizard(IAction action) {
        return new NewProjectWizard();
    }
}
