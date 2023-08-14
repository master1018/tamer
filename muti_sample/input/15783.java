public class Test4425885 {
    private static final String PROPERTY = "property"; 
    public static void main(String[] args) {
        CheckListener first = new CheckListener();
        VetoListener second = new VetoListener();
        CheckListener third = new CheckListener();
        VetoableChangeSupport vcs = new VetoableChangeSupport(Test4425885.class);
        vcs.addVetoableChangeListener(PROPERTY, first);
        vcs.addVetoableChangeListener(PROPERTY, second);
        vcs.addVetoableChangeListener(PROPERTY, third);
        try {
            vcs.fireVetoableChange(PROPERTY, 0, 1);
        } catch (PropertyVetoException exception) {
            if (first.odd)
                throw new Error("no undo for the first listener", exception);
            if (third.odd)
                throw new Error("no undo for the third listener", exception);
            return; 
        }
        throw new Error("exception should be thrown");
    }
    private static class CheckListener implements VetoableChangeListener {
        private boolean odd; 
        public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
            this.odd = !this.odd;
        }
    }
    private static class VetoListener implements VetoableChangeListener {
        public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
            throw new PropertyVetoException("disable all changes", event);
        }
    }
}
