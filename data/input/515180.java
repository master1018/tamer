public class ArcShape extends RectShape {
    private float mStart;
    private float mSweep;
    public ArcShape(float startAngle, float sweepAngle) {
        mStart = startAngle;
        mSweep = sweepAngle;
    }
    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawArc(rect(), mStart, mSweep, true, paint);
    }
}
