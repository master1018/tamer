public abstract class ContinuousDistribution implements Distribution {
    private static final long serialVersionUID = 6775492269986383673L;
    public final boolean isDiscrete() {
        return false;
    }
    public final boolean isContinuous() {
        return true;
    }
    public abstract double getProbability(double x);
    public abstract double getLowerBound();
    public abstract double getUpperBound();
    public String mapValue(double value) {
        return Tools.formatNumber(value);
    }
}
