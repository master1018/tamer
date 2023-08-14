public final class WizardDialogEx extends WizardDialog {
    public WizardDialogEx(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
    }
    public Button getCancelButtonEx() {
        return getButton(IDialogConstants.CANCEL_ID);
    }
}
