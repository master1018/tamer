public class TailMasterPanel extends JPanel {
    private long panelId;
    protected TailMasterTextArea textArea;
    protected JScrollPane scroller;
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(1, 1, 1, 1));
        setPanelId(System.currentTimeMillis());
        TabRegistry.INSTANCE.addTab(getPanelId(), new TabData(panelId));
        addComponentListener(new PanelComponentListener());
        scroller = new JScrollPane();
        scroller.setAutoscrolls(true);
        add(scroller);
    }
    public long getPanelId() {
        return panelId;
    }
    public void setPanelId(long panelId) {
        this.panelId = panelId;
    }
    public TailMasterTextArea getTextArea() {
        return textArea;
    }
    protected class PanelComponentListener implements ComponentListener {
        @Override
        public void componentResized(ComponentEvent e) {
        }
        @Override
        public void componentMoved(ComponentEvent e) {
        }
        @Override
        public void componentShown(ComponentEvent e) {
            TailMasterPanel panel = (TailMasterPanel) e.getComponent();
            boolean playing = TabRegistry.INSTANCE.getTabData(panel.getPanelId()).isPlaying();
            TailMasterToolBar toolBar = (TailMasterToolBar) TailMasterFrame.getInstance().getToolBar();
            toolBar.togglePauseButtonText(playing);
        }
        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }
}
