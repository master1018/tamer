 abstract class OpenWizardAction
    implements IWorkbenchWindowActionDelegate, IObjectActionDelegate {
    private static final int SIZING_WIZARD_WIDTH = 500;
    private static final int SIZING_WIZARD_HEIGHT = 500;
    private IWorkbenchWizard mWizard;
    private int mDialogResult;
    private ISelection mSelection;
    private IWorkbench mWorkbench;
    public IWorkbenchWizard getWizard() {
        return mWizard;
    }
    public int getDialogResult() {
        return mDialogResult;
    }
    public void dispose() {
    }
    public void init(IWorkbenchWindow window) {
    }
    public void run(IAction action) {
        IWorkbench workbench = mWorkbench != null ? mWorkbench : PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        ISelection selection = mSelection;
        if (selection == null) {
            selection = window.getSelectionService().getSelection();
        }
        IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
        if (selection instanceof IStructuredSelection) {
            selectionToPass = (IStructuredSelection) selection;
        } else {
            IWorkbenchPart part = window.getPartService().getActivePart();
            if (part instanceof IEditorPart) {
                IEditorInput input = ((IEditorPart) part).getEditorInput();
                Class<?> fileClass = LegacyResourceSupport.getFileClass();
                if (input != null && fileClass != null) {
                    Object file = Util.getAdapter(input, fileClass);
                    if (file != null) {
                        selectionToPass = new StructuredSelection(file);
                    }
                }
            }
        }
        mWizard = instanciateWizard(action);
        mWizard.init(workbench, selectionToPass);
        Shell parent = window.getShell();
        WizardDialogEx dialog = new WizardDialogEx(parent, mWizard);
        dialog.create();
        if (mWizard instanceof IUpdateWizardDialog) {
            ((IUpdateWizardDialog) mWizard).updateWizardDialog(dialog);
        }
        Point defaultSize = dialog.getShell().getSize();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH, defaultSize.x),
                Math.max(SIZING_WIZARD_HEIGHT, defaultSize.y));
        window.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
                IWorkbenchHelpContextIds.NEW_WIZARD_SHORTCUT);
        mDialogResult = dialog.open();
    }
    protected abstract IWorkbenchWizard instanciateWizard(IAction action);
    public void selectionChanged(IAction action, ISelection selection) {
        mSelection = selection;
    }
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        mWorkbench = targetPart.getSite().getWorkbenchWindow().getWorkbench();
    }
}
