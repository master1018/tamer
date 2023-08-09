public class HeapView extends TableView {
    public static final String ID = "com.android.ide.eclipse.ddms.views.HeapView"; 
    private HeapPanel mPanel;
    public HeapView() {
    }
    @Override
    public void createPartControl(Composite parent) {
        mPanel = new HeapPanel();
        mPanel.createPanel(parent);
        setSelectionDependentPanel(mPanel);
        setupTableFocusListener(mPanel, parent);
    }
    @Override
    public void setFocus() {
        mPanel.setFocus();
    }
}
