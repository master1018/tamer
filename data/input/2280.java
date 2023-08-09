class MotifMenuMouseMotionListener implements MouseMotionListener {
    public void mouseDragged(MouseEvent e) {
        MenuSelectionManager.defaultManager().processMouseEvent(e);
    }
    public void mouseMoved(MouseEvent e) {
        MenuSelectionManager.defaultManager().processMouseEvent(e);
    }
}
