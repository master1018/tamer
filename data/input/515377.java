public abstract class MetricAffectingSpan
extends CharacterStyle
implements UpdateLayout {
	public abstract void updateMeasureState(TextPaint p);
    @Override
    public MetricAffectingSpan getUnderlying() {
        return this;
    }
     static class Passthrough extends MetricAffectingSpan {
        private MetricAffectingSpan mStyle;
        public Passthrough(MetricAffectingSpan cs) {
            mStyle = cs;
        }
        @Override
        public void updateDrawState(TextPaint tp) {
            mStyle.updateDrawState(tp);
        }
        @Override
        public void updateMeasureState(TextPaint tp) {
            mStyle.updateMeasureState(tp);
        }
        @Override
        public MetricAffectingSpan getUnderlying() {
            return mStyle.getUnderlying();
        }
    }
}
