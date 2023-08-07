public class Recognizer implements Configurable {
    public static final String PROP_DECODER = "decoder";
    public static final String PROP_MONITORS = "monitors";
    private String name;
    private Decoder decoder;
    private RecognizerState currentState = RecognizerState.DEALLOCATED;
    private List stateListeners = Collections.synchronizedList(new ArrayList());
    private List monitors;
    public void register(String name, Registry registry) throws PropertyException {
        this.name = name;
        registry.register(PROP_DECODER, PropertyType.COMPONENT);
        registry.register(PROP_MONITORS, PropertyType.COMPONENT_LIST);
    }
    public void newProperties(PropertySheet ps) throws PropertyException {
        decoder = (Decoder) ps.getComponent(PROP_DECODER, Decoder.class);
        monitors = ps.getComponentList(PROP_MONITORS, Configurable.class);
    }
    public Result recognize(String referenceText) throws IllegalStateException {
        Result result = null;
        checkState(RecognizerState.READY);
        try {
            setState(RecognizerState.RECOGNIZING);
            result = decoder.decode(referenceText);
        } finally {
            setState(RecognizerState.READY);
        }
        return result;
    }
    public Result recognize() throws IllegalStateException {
        return recognize(null);
    }
    private void checkState(RecognizerState desiredState) {
        if (currentState != desiredState) {
            throw new IllegalStateException("Expected state " + desiredState + " actual state " + currentState);
        }
    }
    private void setState(RecognizerState newState) {
        currentState = newState;
        synchronized (stateListeners) {
            for (Iterator i = stateListeners.iterator(); i.hasNext(); ) {
                StateListener stateListener = (StateListener) i.next();
                stateListener.statusChanged(currentState);
            }
        }
    }
    public void allocate() throws IllegalStateException, IOException {
        checkState(RecognizerState.DEALLOCATED);
        setState(RecognizerState.ALLOCATING);
        decoder.allocate();
        setState(RecognizerState.ALLOCATED);
        setState(RecognizerState.READY);
    }
    public void deallocate() throws IllegalStateException {
        checkState(RecognizerState.READY);
        setState(RecognizerState.DEALLOCATING);
        decoder.deallocate();
        setState(RecognizerState.DEALLOCATED);
    }
    public RecognizerState getState() {
        return currentState;
    }
    public void resetMonitors() {
        for (Iterator i = monitors.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (o instanceof Resetable) {
                Resetable r = (Resetable) o;
                r.reset();
            }
        }
    }
    public void addResultListener(ResultListener resultListener) {
        decoder.addResultListener(resultListener);
    }
    public void addStateListener(StateListener stateListener) {
        stateListeners.add(stateListener);
    }
    public void removeResultListener(ResultListener resultListener) {
        decoder.removeResultListener(resultListener);
    }
    public void removeStateListener(StateListener stateListener) {
        stateListeners.remove(stateListener);
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return "Recognizer: " + getName() + " State: " + currentState;
    }
}
