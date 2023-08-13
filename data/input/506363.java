public class EmulatorControlView extends SelectionDependentViewPart {
    public static final String ID =
        "com.android.ide.eclipse.ddms.views.EmulatorControlView"; 
    private EmulatorControlPanel mPanel;
    @Override
    public void createPartControl(Composite parent) {
        mPanel = new EmulatorControlPanel(DdmsPlugin.getImageLoader());
        mPanel.createPanel(parent);
        setSelectionDependentPanel(mPanel);
    }
    @Override
    public void setFocus() {
        mPanel.setFocus();
    }
}
