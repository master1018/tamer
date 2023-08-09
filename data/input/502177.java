public abstract class TableView extends SelectionDependentViewPart {
    IFocusedTableActivator mActivator = null;
    private Clipboard mClipboard;
    private Action mCopyAction;
    private Action mSelectAllAction;
    void setupTableFocusListener(TablePanel panel, Composite parent) {
        panel.setTableFocusListener(new ITableFocusListener() {
            public void focusGained(IFocusedTableActivator activator) {
                mActivator = activator;
                mCopyAction.setEnabled(true);
                mSelectAllAction.setEnabled(true);
            }
            public void focusLost(IFocusedTableActivator activator) {
                if (activator == mActivator) {
                    mActivator = null;
                    mCopyAction.setEnabled(false);
                    mSelectAllAction.setEnabled(false);
                }
            }
        });
        mClipboard = new Clipboard(parent.getDisplay());
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
                mCopyAction = new Action("Copy") {
            @Override
            public void run() {
                if (mActivator != null) {
                    mActivator.copy(mClipboard);
                }
            }
        });
        actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
                mSelectAllAction = new Action("Select All") {
            @Override
            public void run() {
                if (mActivator != null) {
                    mActivator.selectAll();
                }
            }
        });
    }
    @Override
    public void dispose() {
        super.dispose();
        mClipboard.dispose();
    }
}
