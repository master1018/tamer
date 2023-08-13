public class FontMetricsImpl extends FontMetrics {
	private static final long serialVersionUID = 844695615201925138L;
	private int ascent;
	private int descent;
	private int leading;
	private int maxAscent;
	private int maxDescent;
	private int maxAdvance;
	private int[] widths = new int[256];
	private transient FontPeerImpl peer;
	private float scaleX = 1;
	public AndroidGraphics2D mSg;
	private Font mFn;
	private float scaleY = 1;
	public FontMetricsImpl(Font fnt) {
		super(fnt);
		this.mFn = fnt;
		mSg = AndroidGraphics2D.getInstance();
		Paint p = mSg.getAndroidPaint();
		this.ascent = (int)-p.ascent();
		this.descent = (int)p.descent();
		this.leading = p.getFontMetricsInt().leading;
		AffineTransform at = fnt.getTransform();
		if (!at.isIdentity()) {
			scaleX = (float) at.getScaleX();
			scaleY = (float) at.getScaleY();
		}
	}
	private void initWidths() {
		this.widths = new int[256];
		for (int chr = 0; chr < 256; chr++) {
			widths[chr] = (int) (getFontPeer().charWidth((char) chr) * scaleX);
		}
	}
	@Override
	public int getAscent() {
		return this.ascent;
	}
	@Override
	public int getDescent() {
		return this.descent;
	}
	@Override
	public int getLeading() {
		return this.leading;
	}
	@Override
	public int charWidth(int ch) {
		if (ch < 256) {
			return widths[ch];
		}
		return getFontPeer().charWidth((char) ch);
	}
	@Override
	public int charWidth(char ch) {
		if (ch < 256) {
			return widths[ch];
		}
		return (int) (getFontPeer().charWidth(ch) * scaleX);
	}
	@Override
	public int getMaxAdvance() {
		return this.maxAdvance;
	}
	@Override
	public int getMaxAscent() {
		return this.maxAscent;
	}
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public int getMaxDecent() {
		return this.maxDescent;
	}
	@Override
	public int getMaxDescent() {
		return this.maxDescent;
	}
	@Override
	public int[] getWidths() {
		return this.widths;
	}
	@Override
	public int stringWidth(String str) {
		int width = 0;
		char chr;
		for (int i = 0; i < str.length(); i++) {
			chr = str.charAt(i);
			width += charWidth(chr);
		}
		return width;
	}
	@SuppressWarnings("deprecation")
	public FontPeerImpl getFontPeer() {
		if (peer == null) {
			peer = (FontPeerImpl) font.getPeer();
		}
		return peer;
	}
}
