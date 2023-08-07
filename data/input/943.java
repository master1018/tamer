public abstract class DefaultFF implements FeatureFunction<MapFFState> {
    private boolean stateful = true;
    protected double weight = 0.0;
    public DefaultFF(double weight_) {
        this.weight = weight_;
    }
    public final void setStateless() {
        this.stateful = false;
    }
    public final boolean isStateful() {
        return this.stateful;
    }
    public final double getWeight() {
        return this.weight;
    }
    public void putWeight(final double weight_) {
        this.weight = weight_;
    }
    public double estimate(final Rule rule) {
        if (this.stateful) {
            return 0.0;
        } else {
            final FFState state = this.transition(rule, null, -1, -1);
            if (null == state) {
                return 0.0;
            } else {
                return state.getTransitionCost();
            }
        }
    }
    public MapFFState transition(final Rule rule, final ArrayList<MapFFState> previous_states, final int i, final int j) {
        MapFFState state = new MapFFState();
        state.putTransitionCost(this.estimate(rule));
        return state;
    }
    public double finalTransition(final MapFFState state) {
        return 0.0;
    }
}
