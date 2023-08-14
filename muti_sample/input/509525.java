public class NumericSprite {
    public NumericSprite() {
        mText = "";
        mLabelMaker = null;
    }
    public void initialize(GL10 gl, Paint paint) {
        int height = roundUpPower2((int) paint.getFontSpacing());
        final float interDigitGaps = 9 * 1.0f;
        int width = roundUpPower2((int) (interDigitGaps + paint.measureText(sStrike)));
        mLabelMaker = new LabelMaker(true, width, height);
        mLabelMaker.initialize(gl);
        mLabelMaker.beginAdding(gl);
        for (int i = 0; i < 10; i++) {
            String digit = sStrike.substring(i, i+1);
            mLabelId[i] = mLabelMaker.add(gl, digit, paint);
            mWidth[i] = (int) Math.ceil(mLabelMaker.getWidth(i));
        }
        mLabelMaker.endAdding(gl);
    }
    public void shutdown(GL10 gl) {
        mLabelMaker.shutdown(gl);
        mLabelMaker = null;
    }
    private int roundUpPower2(int x) {
        x = x - 1;
        x = x | (x >> 1);
        x = x | (x >> 2);
        x = x | (x >> 4);
        x = x | (x >> 8);
        x = x | (x >>16);
        return x + 1;
    }
    public void setValue(int value) {
        mText = format(value);
    }
    public void draw(GL10 gl, float x, float y,
            float viewWidth, float viewHeight) {
        int length = mText.length();
        mLabelMaker.beginDrawing(gl, viewWidth, viewHeight);
        for(int i = 0; i < length; i++) {
            char c = mText.charAt(i);
            int digit = c - '0';
            mLabelMaker.draw(gl, x, y, mLabelId[digit]);
            x += mWidth[digit];
        }
        mLabelMaker.endDrawing(gl);
    }
    public float width() {
        float width = 0.0f;
        int length = mText.length();
        for(int i = 0; i < length; i++) {
            char c = mText.charAt(i);
            width += mWidth[c - '0'];
        }
        return width;
    }
    private String format(int value) {
        return Integer.toString(value);
    }
    private LabelMaker mLabelMaker;
    private String mText;
    private int[] mWidth = new int[10];
    private int[] mLabelId = new int[10];
    private final static String sStrike = "0123456789";
}
