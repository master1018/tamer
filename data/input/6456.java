class XRepaintArea extends RepaintArea {
    public XRepaintArea() {
    }
    protected void updateComponent(Component comp, Graphics g) {
        if (comp != null) {
            ComponentPeer peer = comp.getPeer();
            if (peer != null) {
                peer.paint(g);
            }
            super.updateComponent(comp, g);
        }
    }
    protected void paintComponent(Component comp, Graphics g) {
        if (comp != null) {
            ComponentPeer peer = comp.getPeer();
            if (peer != null) {
                peer.paint(g);
            }
            super.paintComponent(comp, g);
        }
    }
}
