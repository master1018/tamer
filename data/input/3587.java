public class Test6630275 {
    private static final String PROPERTY = "property"; 
    public static void main(String[] args) {
        CheckListener first = new CheckListener(false);
        CheckListener second = new CheckListener(true);
        CheckListener third = new CheckListener(false);
        VetoableChangeSupport vcs = new VetoableChangeSupport(Test6630275.class);
        vcs.addVetoableChangeListener(first);
        vcs.addVetoableChangeListener(PROPERTY, first);
        vcs.addVetoableChangeListener(PROPERTY, second);
        vcs.addVetoableChangeListener(PROPERTY, third);
        try {
            vcs.fireVetoableChange(PROPERTY, true, false);
        } catch (PropertyVetoException exception) {
            first.validate();
            second.validate();
            third.validate();
            return; 
        }
        throw new Error("exception should be thrown");
    }
    private static class CheckListener implements VetoableChangeListener {
        private final boolean veto;
        private boolean odd; 
        private CheckListener(boolean veto) {
            this.veto = veto;
        }
        private void validate() {
            if (this.veto != this.odd)
                throw new Error(this.odd
                        ? "undo event expected"
                        : "unexpected undo event");
        }
        public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
            this.odd = !this.odd;
            if (this.veto)
                throw new PropertyVetoException("disable all changes", event);
        }
    }
}
