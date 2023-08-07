public class DefaultWizardModel implements WizardModel, StateChangeListener {
    public static final String PROP_CURRENTPANEL = "currentPanel";
    private static final String PREV_PANEL_STATE_NAME = "_prevPanel";
    protected Map<String, WizardPanel> panels;
    private DefaultStateMachine<WizardPanel> stateMachine;
    private EventListenerList eventListeners;
    public DefaultWizardModel() {
        this.panels = new HashMap<String, WizardPanel>();
        this.stateMachine = new DefaultStateMachine<WizardPanel>();
        this.eventListeners = new EventListenerList();
        stateMachine.addStateChangeListener(this);
    }
    public void add(WizardPanel panel) {
        stateMachine.addState(new DefaultState<WizardPanel>(panel.getId(), panel));
        panels.put(panel.getId(), panel);
    }
    public void add(WizardPanel panel, boolean initial) {
        stateMachine.addState(new DefaultState<WizardPanel>(panel.getId(), panel), initial);
        panels.put(panel.getId(), panel);
    }
    public void add(WizardPanel startPanel, WizardPanel endPanel, String transitionName) {
        State<WizardPanel> startState = new DefaultState<WizardPanel>(startPanel.getId(), startPanel);
        State<WizardPanel> endState = new DefaultState<WizardPanel>(endPanel.getId(), endPanel);
        stateMachine.addTransition(new DefaultStateTransition<WizardPanel>(transitionName, startState, endState));
        stateMachine.addTransition(new DefaultStateTransition<WizardPanel>(PREV_PANEL_STATE_NAME, endState, startState));
    }
    public WizardPanel getCurrentPanel() {
        return stateMachine.getCurrentState().getValue();
    }
    public boolean hasPreviousPanel() {
        return stateMachine.canTransitionTo(PREV_PANEL_STATE_NAME);
    }
    public void goToNextPanel(String panelId) throws IllegalArgumentException {
        try {
            stateMachine.transitionToNewState(panelId);
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException(npe);
        } catch (IllegalStateException ise) {
            throw new IllegalArgumentException(ise);
        }
    }
    public void goToPreviousPanel() {
        try {
            stateMachine.transitionToNewState(PREV_PANEL_STATE_NAME);
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException(npe);
        } catch (IllegalStateException ise) {
            throw new IllegalArgumentException(ise);
        }
    }
    public WizardPanel getPanel(String id) {
        return panels.get(id);
    }
    public boolean remove(WizardPanel panel) {
        if (panels.containsKey(panel.getId()) && panels.get(panel.getId()).equals(panel)) {
            panels.remove(panel.getId());
            return true;
        } else {
            return false;
        }
    }
    public void reset() {
        stateMachine.reset();
    }
    public Iterator<WizardPanel> iterator() {
        return new WizardPanelIterator();
    }
    public void stateChanging(StateChangeEvent evt) {
        fireWizardModelChangeListenerModelIsChanging(new WizardModelEvent(this));
    }
    public void stateChanged(StateChangeEvent evt) {
        fireWizardModelChangeListenerModelChanged(new WizardModelEvent(this));
    }
    public void addWizardModelChangeListener(WizardModelChangeListener listener) {
        eventListeners.add(WizardModelChangeListener.class, listener);
    }
    public void removeWizardModelChangeListener(WizardModelChangeListener listener) {
        eventListeners.remove(WizardModelChangeListener.class, listener);
    }
    protected void fireWizardModelChangeListenerModelIsChanging(WizardModelEvent event) {
        WizardModelChangeListener[] listeners = eventListeners.getListeners(WizardModelChangeListener.class);
        for (int index = 0; index < listeners.length; index++) {
            listeners[index].modelIsChanging(event);
        }
    }
    protected void fireWizardModelChangeListenerModelChanged(WizardModelEvent event) {
        WizardModelChangeListener[] listeners = eventListeners.getListeners(WizardModelChangeListener.class);
        for (int index = 0; index < listeners.length; index++) {
            listeners[index].modelChanged(event);
        }
    }
    protected void fireWizardModelChangeListenerWizardPanelAdded(WizardModelEvent event) {
        WizardModelChangeListener[] listeners = eventListeners.getListeners(WizardModelChangeListener.class);
        for (int index = 0; index < listeners.length; index++) {
            listeners[index].wizardPanelAdded(event);
        }
    }
    protected void fireWizardModelChangeListenerWizardPanelRemoved(WizardModelEvent event) {
        WizardModelChangeListener[] listeners = eventListeners.getListeners(WizardModelChangeListener.class);
        for (int index = 0; index < listeners.length; index++) {
            listeners[index].wizardPanelRemoved(event);
        }
    }
    private class WizardPanelIterator implements Iterator<WizardPanel> {
        private Iterator<State<WizardPanel>> backingIterator;
        public WizardPanelIterator() {
            backingIterator = stateMachine.iterator();
        }
        public boolean hasNext() {
            return backingIterator.hasNext();
        }
        public WizardPanel next() {
            State<WizardPanel> nextState = backingIterator.next();
            return nextState.getValue();
        }
        public void remove() {
            throw new UnsupportedOperationException("Removal of wizard panels not permitted via iterator");
        }
    }
}
