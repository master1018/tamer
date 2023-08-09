public class OvalShape extends RectShape {
    public OvalShape() {}
    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawOval(rect(), paint);
    }
}
