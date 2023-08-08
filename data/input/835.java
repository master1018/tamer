public class ImportWizard extends Wizard implements IImportWizard {
    ImportWizardPage mainPage;
    public ImportWizard() {
        super();
    }
    public boolean performFinish() {
        IFile file = mainPage.createNewFile();
        if (file == null) return false;
        return true;
    }
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("Symfony Import Wizard");
        setNeedsProgressMonitor(true);
        mainPage = new ImportWizardPage("SymfonyProject", selection);
    }
    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }
}
