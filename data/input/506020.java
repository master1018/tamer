public class RasterizerSpan extends CharacterStyle implements UpdateAppearance {
	private Rasterizer mRasterizer;
	public RasterizerSpan(Rasterizer r) {
		mRasterizer = r;
	}
	public Rasterizer getRasterizer() {
		return mRasterizer;
	}
	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setRasterizer(mRasterizer);
	}
}
