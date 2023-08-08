public abstract class AbstractWizardPanel implements WizardDescriptor.Panel {
    private final Set listeners = new HashSet(1);
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator itr;
        synchronized (listeners) {
            itr = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (itr.hasNext()) {
        }
    }
    public abstract boolean isPanelProcessingComplete();
    public abstract void terminateProcessing();
}
