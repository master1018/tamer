public class MotifDesktopPaneUI extends javax.swing.plaf.basic.BasicDesktopPaneUI
{
    public static ComponentUI createUI(JComponent d)    {
        return new MotifDesktopPaneUI();
    }
    public MotifDesktopPaneUI() {
    }
    protected void installDesktopManager() {
        desktopManager = desktop.getDesktopManager();
        if(desktopManager == null) {
            desktopManager = new MotifDesktopManager();
            desktop.setDesktopManager(desktopManager);
            ((MotifDesktopManager)desktopManager).adjustIcons(desktop);
        }
    }
    public Insets getInsets(JComponent c) {return new Insets(0,0,0,0);}
    private class DragPane extends JComponent {
        public void paint(Graphics g) {
            g.setColor(Color.darkGray);
            g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        }
    };
    private class MotifDesktopManager extends DefaultDesktopManager implements Serializable, UIResource {
        JComponent dragPane;
        boolean usingDragPane = false;
        private transient JLayeredPane layeredPaneForDragPane;
        int iconWidth, iconHeight;
    public void setBoundsForFrame(JComponent f, int newX, int newY,
                        int newWidth, int newHeight) {
        if(!usingDragPane) {
            boolean didResize;
            didResize = (f.getWidth() != newWidth || f.getHeight() != newHeight);
            Rectangle r = f.getBounds();
            f.setBounds(newX, newY, newWidth, newHeight);
            SwingUtilities.computeUnion(newX, newY, newWidth, newHeight, r);
            f.getParent().repaint(r.x, r.y, r.width, r.height);
            if(didResize) {
                f.validate();
            }
        } else {
            Rectangle r = dragPane.getBounds();
            dragPane.setBounds(newX, newY, newWidth, newHeight);
            SwingUtilities.computeUnion(newX, newY, newWidth, newHeight, r);
            dragPane.getParent().repaint(r.x, r.y, r.width, r.height);
        }
    }
    public void beginDraggingFrame(JComponent f) {
        usingDragPane = false;
        if(f.getParent() instanceof JLayeredPane) {
            if(dragPane == null)
                dragPane = new DragPane();
            layeredPaneForDragPane = (JLayeredPane)f.getParent();
            layeredPaneForDragPane.setLayer(dragPane, Integer.MAX_VALUE);
            dragPane.setBounds(f.getX(), f.getY(), f.getWidth(), f.getHeight());
            layeredPaneForDragPane.add(dragPane);
            usingDragPane = true;
        }
    }
    public void dragFrame(JComponent f, int newX, int newY) {
        setBoundsForFrame(f, newX, newY, f.getWidth(), f.getHeight());
    }
    public void endDraggingFrame(JComponent f) {
        if(usingDragPane) {
            layeredPaneForDragPane.remove(dragPane);
            usingDragPane = false;
            if (f instanceof JInternalFrame) {
                setBoundsForFrame(f, dragPane.getX(), dragPane.getY(),
                        dragPane.getWidth(), dragPane.getHeight());
            } else if (f instanceof JInternalFrame.JDesktopIcon) {
                adjustBoundsForIcon((JInternalFrame.JDesktopIcon)f,
                        dragPane.getX(), dragPane.getY());
            }
        }
    }
    public void beginResizingFrame(JComponent f, int direction) {
        usingDragPane = false;
        if(f.getParent() instanceof JLayeredPane) {
            if(dragPane == null)
                dragPane = new DragPane();
            JLayeredPane p = (JLayeredPane)f.getParent();
            p.setLayer(dragPane, Integer.MAX_VALUE);
            dragPane.setBounds(f.getX(), f.getY(),
                                f.getWidth(), f.getHeight());
            p.add(dragPane);
            usingDragPane = true;
        }
    }
    public void resizeFrame(JComponent f, int newX, int newY,
                                int newWidth, int newHeight) {
        setBoundsForFrame(f, newX, newY, newWidth, newHeight);
    }
    public void endResizingFrame(JComponent f) {
        if(usingDragPane) {
            JLayeredPane p = (JLayeredPane)f.getParent();
            p.remove(dragPane);
            usingDragPane = false;
            setBoundsForFrame(f, dragPane.getX(), dragPane.getY(),
                                dragPane.getWidth(), dragPane.getHeight());
        }
    }
        public void iconifyFrame(JInternalFrame f) {
            JInternalFrame.JDesktopIcon icon = f.getDesktopIcon();
            Point p = icon.getLocation();
            adjustBoundsForIcon(icon, p.x, p.y);
            super.iconifyFrame(f);
        }
        protected void adjustIcons(JDesktopPane desktop) {
            JInternalFrame.JDesktopIcon icon = new JInternalFrame.JDesktopIcon(
                    new JInternalFrame());
            Dimension iconSize = icon.getPreferredSize();
            iconWidth = iconSize.width;
            iconHeight = iconSize.height;
            JInternalFrame[] frames = desktop.getAllFrames();
            for (int i=0; i<frames.length; i++) {
                icon = frames[i].getDesktopIcon();
                Point ip = icon.getLocation();
                adjustBoundsForIcon(icon, ip.x, ip.y);
            }
        }
        protected void adjustBoundsForIcon(JInternalFrame.JDesktopIcon icon,
                int x, int y) {
            JDesktopPane c = icon.getDesktopPane();
            int maxy = c.getHeight();
            int w = iconWidth;
            int h = iconHeight;
            c.repaint(x, y, w, h);
            x = x < 0 ? 0 : x;
            y = y < 0 ? 0 : y;
            y = y >= maxy ? (maxy - 1) : y;
            int lx = (x / w) * w;
            int ygap = maxy % h;
            int ly = ((y-ygap) / h) * h + ygap;
            int dx = x - lx;
            int dy = y - ly;
            x = dx < w/2 ? lx: lx + w;
            y = dy < h/2 ? ly: ((ly + h) < maxy ? ly + h: ly);
            while (getIconAt(c, icon, x, y) != null) {
                x += w;
            }
            if (x > c.getWidth()) {
                return;
            }
            if (icon.getParent() != null) {
                setBoundsForFrame(icon, x, y, w, h);
            } else {
                icon.setLocation(x, y);
            }
        }
        protected JInternalFrame.JDesktopIcon getIconAt(JDesktopPane desktop,
            JInternalFrame.JDesktopIcon icon, int x, int y) {
            JInternalFrame.JDesktopIcon currentIcon = null;
            Component[] components = desktop.getComponents();
            for (int i=0; i<components.length; i++) {
                Component comp = components[i];
                if (comp instanceof JInternalFrame.JDesktopIcon &&
                    comp != icon) {
                    Point p = comp.getLocation();
                    if (p.x == x && p.y == y) {
                        return (JInternalFrame.JDesktopIcon)comp;
                    }
                }
            }
            return null;
        }
    }; 
}
