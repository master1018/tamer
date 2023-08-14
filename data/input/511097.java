public abstract class CharacterStyle {
	public abstract void updateDrawState(TextPaint tp);
    public static CharacterStyle wrap(CharacterStyle cs) {
        if (cs instanceof MetricAffectingSpan) {
            return new MetricAffectingSpan.Passthrough((MetricAffectingSpan) cs);
        } else {
            return new Passthrough(cs);
        }
    }
    public CharacterStyle getUnderlying() {
        return this;
    }
    private static class Passthrough extends CharacterStyle {
        private CharacterStyle mStyle;
        public Passthrough(CharacterStyle cs) {
            mStyle = cs;
        }
        @Override
        public void updateDrawState(TextPaint tp) {
            mStyle.updateDrawState(tp);
        }
        @Override
        public CharacterStyle getUnderlying() {
            return mStyle.getUnderlying();
        }
    }
}
