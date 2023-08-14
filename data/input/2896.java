public class JDesktopPane extends JLayeredPane implements Accessible
{
    private static final String uiClassID = "DesktopPaneUI";
    transient DesktopManager desktopManager;
    private transient JInternalFrame selectedFrame = null;
    public static final int LIVE_DRAG_MODE = 0;
    public static final int OUTLINE_DRAG_MODE = 1;
    private int dragMode = LIVE_DRAG_MODE;
    private boolean dragModeSet = false;
    private transient List<JInternalFrame> framesCache;
    private boolean componentOrderCheckingEnabled = true;
    private boolean componentOrderChanged = false;
    public JDesktopPane() {
        setUIProperty("opaque", Boolean.TRUE);
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
            public Component getDefaultComponent(Container c) {
                JInternalFrame jifArray[] = getAllFrames();
                Component comp = null;
                for (JInternalFrame jif : jifArray) {
                    comp = jif.getFocusTraversalPolicy().getDefaultComponent(jif);
                    if (comp != null) {
                        break;
                    }
                }
                return comp;
            }
        });
        updateUI();
    }
    public DesktopPaneUI getUI() {
        return (DesktopPaneUI)ui;
    }
    public void setUI(DesktopPaneUI ui) {
        super.setUI(ui);
    }
    public void setDragMode(int dragMode) {
        int oldDragMode = this.dragMode;
        this.dragMode = dragMode;
        firePropertyChange("dragMode", oldDragMode, this.dragMode);
        dragModeSet = true;
     }
     public int getDragMode() {
         return dragMode;
     }
    public DesktopManager getDesktopManager() {
        return desktopManager;
    }
    public void setDesktopManager(DesktopManager d) {
        DesktopManager oldValue = desktopManager;
        desktopManager = d;
        firePropertyChange("desktopManager", oldValue, desktopManager);
    }
    public void updateUI() {
        setUI((DesktopPaneUI)UIManager.getUI(this));
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public JInternalFrame[] getAllFrames() {
        int i, count;
        JInternalFrame[] results;
        Vector<JInternalFrame> vResults = new Vector<JInternalFrame>(10);
        count = getComponentCount();
        for(i = 0; i < count; i++) {
            Component next = getComponent(i);
            if(next instanceof JInternalFrame)
                vResults.addElement((JInternalFrame) next);
            else if(next instanceof JInternalFrame.JDesktopIcon)  {
                JInternalFrame tmp = ((JInternalFrame.JDesktopIcon)next).getInternalFrame();
                if(tmp != null)
                    vResults.addElement(tmp);
            }
        }
        results = new JInternalFrame[vResults.size()];
        vResults.copyInto(results);
        return results;
    }
    public JInternalFrame getSelectedFrame() {
      return selectedFrame;
    }
    public void setSelectedFrame(JInternalFrame f) {
      selectedFrame = f;
    }
    public JInternalFrame[] getAllFramesInLayer(int layer) {
        int i, count;
        JInternalFrame[] results;
        Vector<JInternalFrame> vResults = new Vector<JInternalFrame>(10);
        count = getComponentCount();
        for(i = 0; i < count; i++) {
            Component next = getComponent(i);
            if(next instanceof JInternalFrame) {
                if(((JInternalFrame)next).getLayer() == layer)
                    vResults.addElement((JInternalFrame) next);
            } else if(next instanceof JInternalFrame.JDesktopIcon)  {
                JInternalFrame tmp = ((JInternalFrame.JDesktopIcon)next).getInternalFrame();
                if(tmp != null && tmp.getLayer() == layer)
                    vResults.addElement(tmp);
            }
        }
        results = new JInternalFrame[vResults.size()];
        vResults.copyInto(results);
        return results;
    }
    private List<JInternalFrame> getFrames() {
        Component c;
        Set<ComponentPosition> set = new TreeSet<ComponentPosition>();
        for (int i = 0; i < getComponentCount(); i++) {
            c = getComponent(i);
            if (c instanceof JInternalFrame) {
                set.add(new ComponentPosition((JInternalFrame)c, getLayer(c),
                    i));
            }
            else if (c instanceof JInternalFrame.JDesktopIcon)  {
                c = ((JInternalFrame.JDesktopIcon)c).getInternalFrame();
                set.add(new ComponentPosition((JInternalFrame)c, getLayer(c),
                    i));
            }
        }
        List<JInternalFrame> frames = new ArrayList<JInternalFrame>(
                set.size());
        for (ComponentPosition position : set) {
            frames.add(position.component);
        }
        return frames;
   }
    private static class ComponentPosition implements
        Comparable<ComponentPosition> {
        private final JInternalFrame component;
        private final int layer;
        private final int zOrder;
        ComponentPosition(JInternalFrame component, int layer, int zOrder) {
            this.component = component;
            this.layer = layer;
            this.zOrder = zOrder;
        }
        public int compareTo(ComponentPosition o) {
            int delta = o.layer - layer;
            if (delta == 0) {
                return zOrder - o.zOrder;
            }
            return delta;
        }
    }
    private JInternalFrame getNextFrame(JInternalFrame f, boolean forward) {
        verifyFramesCache();
        if (f == null) {
            return getTopInternalFrame();
        }
        int i = framesCache.indexOf(f);
        if (i == -1 || framesCache.size() == 1) {
            return null;
        }
        if (forward) {
            if (++i == framesCache.size()) {
                i = 0;
            }
        }
        else {
            if (--i == -1) {
                i = framesCache.size() - 1;
            }
        }
        return framesCache.get(i);
    }
    JInternalFrame getNextFrame(JInternalFrame f) {
        return getNextFrame(f, true);
    }
    private JInternalFrame getTopInternalFrame() {
        if (framesCache.size() == 0) {
            return null;
        }
        return framesCache.get(0);
    }
    private void updateFramesCache() {
        framesCache = getFrames();
    }
    private void verifyFramesCache() {
        if (componentOrderChanged) {
            componentOrderChanged = false;
            updateFramesCache();
        }
    }
    public JInternalFrame selectFrame(boolean forward) {
        JInternalFrame selectedFrame = getSelectedFrame();
        JInternalFrame frameToSelect = getNextFrame(selectedFrame, forward);
        if (frameToSelect == null) {
            return null;
        }
        setComponentOrderCheckingEnabled(false);
        if (forward && selectedFrame != null) {
            selectedFrame.moveToBack();  
        }
        try { frameToSelect.setSelected(true);
        } catch (PropertyVetoException pve) {}
        setComponentOrderCheckingEnabled(true);
        return frameToSelect;
    }
    void setComponentOrderCheckingEnabled(boolean enable) {
        componentOrderCheckingEnabled = enable;
    }
    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
        if (componentOrderCheckingEnabled) {
            if (comp instanceof JInternalFrame ||
                comp instanceof JInternalFrame.JDesktopIcon) {
                componentOrderChanged = true;
            }
        }
    }
    public void remove(int index) {
        if (componentOrderCheckingEnabled) {
            Component comp = getComponent(index);
            if (comp instanceof JInternalFrame ||
                comp instanceof JInternalFrame.JDesktopIcon) {
                componentOrderChanged = true;
            }
        }
        super.remove(index);
    }
    public void removeAll() {
        if (componentOrderCheckingEnabled) {
            int count = getComponentCount();
            for (int i = 0; i < count; i++) {
                Component comp = getComponent(i);
                if (comp instanceof JInternalFrame ||
                    comp instanceof JInternalFrame.JDesktopIcon) {
                    componentOrderChanged = true;
                    break;
                }
            }
        }
        super.removeAll();
    }
    public void setComponentZOrder(Component comp, int index) {
        super.setComponentZOrder(comp, index);
        if (componentOrderCheckingEnabled) {
            if (comp instanceof JInternalFrame ||
                comp instanceof JInternalFrame.JDesktopIcon) {
                componentOrderChanged = true;
            }
        }
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
    }
    void setUIProperty(String propertyName, Object value) {
        if (propertyName == "dragMode") {
            if (!dragModeSet) {
                setDragMode(((Integer)value).intValue());
                dragModeSet = false;
            }
        } else {
            super.setUIProperty(propertyName, value);
        }
    }
    protected String paramString() {
        String desktopManagerString = (desktopManager != null ?
                                       desktopManager.toString() : "");
        return super.paramString() +
        ",desktopManager=" + desktopManagerString;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJDesktopPane();
        }
        return accessibleContext;
    }
    protected class AccessibleJDesktopPane extends AccessibleJComponent {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.DESKTOP_PANE;
        }
    }
}
