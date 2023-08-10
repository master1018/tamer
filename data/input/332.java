public class ReconnectSourceCommand extends Command {
    protected Activity source;
    protected Activity target;
    protected Transition transition;
    protected Activity oldSource;
    public boolean canExecute() {
        if (transition.target.equals(source)) return false;
        List transitions = source.getOutgoingTransitions();
        for (int i = 0; i < transitions.size(); i++) {
            Transition trans = ((Transition) (transitions.get(i)));
            if (trans.target.equals(target) && !trans.source.equals(oldSource)) return false;
        }
        return true;
    }
    public void execute() {
        if (source != null) {
            oldSource.removeOutput(transition);
            transition.source = source;
            source.addOutput(transition);
        }
    }
    public Activity getSource() {
        return source;
    }
    public Activity getTarget() {
        return target;
    }
    public Transition getTransition() {
        return transition;
    }
    public void setSource(Activity activity) {
        source = activity;
    }
    public void setTarget(Activity activity) {
        target = activity;
    }
    public void setTransition(Transition trans) {
        transition = trans;
        target = trans.target;
        oldSource = trans.source;
    }
    public void undo() {
        source.removeOutput(transition);
        transition.source = oldSource;
        oldSource.addOutput(transition);
    }
}
