public class JmolPanel extends JPanel {
    JmolViewer viewer;
    JmolAdapter adapter;
    JmolPopup jmolpopup;
    JmolStatusListener statuslistener;
    public JmolPanel() {
        super();
        adapter = new SmarterJmolAdapter();
        viewer = JmolViewer.allocateViewer(this, adapter);
        viewer.evalString("set scriptQueue on;");
        jmolpopup = JmolPopup.newJmolPopup(viewer, true, null, true);
    }
    public JmolViewer getViewer() {
        return viewer;
    }
    final Dimension currentSize = new Dimension();
    final Rectangle rectClip = new Rectangle();
    public void paint(Graphics g) {
        getSize(currentSize);
        g.getClipBounds(rectClip);
        viewer.renderScreenImage(g, currentSize, rectClip);
    }
}
