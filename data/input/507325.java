public class AllocTrackerView extends TableView {
    public static final String ID = "com.android.ide.eclipse.ddms.views.AllocTrackerView"; 
    private AllocationPanel mPanel;
    public AllocTrackerView() {
    }
    @Override
    public void createPartControl(Composite parent) {
        mPanel = new AllocationPanel();
        mPanel.createPanel(parent);
        setSelectionDependentPanel(mPanel);
        setupTableFocusListener(mPanel, parent);
    }
    @Override
    public void setFocus() {
        mPanel.setFocus();
    }
}
