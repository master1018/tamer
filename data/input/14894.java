final class ViewOptionsPanelController extends OptionsPanelController {
    private ViewPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    public void update() {
        getPanel().load();
        changed = false;
    }
    public void applyChanges() {
        getPanel().store();
        changed = false;
    }
    public void cancel() {
    }
    public boolean isValid() {
        return getPanel().valid();
    }
    public boolean isChanged() {
        return changed;
    }
    public HelpCtx getHelpCtx() {
        return null; 
    }
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    private ViewPanel getPanel() {
        if (panel == null) {
            panel = new ViewPanel(this);
        }
        return panel;
    }
    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
