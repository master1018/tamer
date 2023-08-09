public class NativeHeapView extends TableView {
    public static final String ID =
        "com.android.ide.eclipse.ddms.views.NativeHeapView"; 
    private NativeHeapPanel mPanel;
    public NativeHeapView() {
    }
    @Override
    public void createPartControl(Composite parent) {
        mPanel = new NativeHeapPanel();
        mPanel.createPanel(parent);
        setSelectionDependentPanel(mPanel);
        setupTableFocusListener(mPanel, parent);
    }
    @Override
    public void setFocus() {
        mPanel.setFocus();
    }
}
