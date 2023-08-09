public class NewXmlFileAction extends OpenWizardAction {
    @Override
    protected IWorkbenchWizard instanciateWizard(IAction action) {
        return new NewXmlFileWizard();
    }
}
