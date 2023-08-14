public class MaskFilterSpan extends CharacterStyle implements UpdateAppearance {
	private MaskFilter mFilter;
	public MaskFilterSpan(MaskFilter filter) {
		mFilter = filter;
	}
	public MaskFilter getMaskFilter() {
		return mFilter;
	}
	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setMaskFilter(mFilter);
	}
}
