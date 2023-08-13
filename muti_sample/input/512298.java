public abstract class GridDialog extends Dialog {
    private final int mNumColumns;
    private final boolean mMakeColumnsEqualWidth;
    public GridDialog(Shell parentShell, int numColumns, boolean makeColumnsEqualWidth) {
        super(parentShell);
        mNumColumns = numColumns;
        mMakeColumnsEqualWidth = makeColumnsEqualWidth;
    }
    public abstract void createDialogContent(Composite parent);
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(mNumColumns, mMakeColumnsEqualWidth);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(
                IDialogConstants.HORIZONTAL_SPACING);
        top.setLayout(layout);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        createDialogContent(top);
        applyDialogFont(top);
        return top;
    }
}
