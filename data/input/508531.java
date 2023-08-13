public class PointF {
    public float x;
    public float y;
    public PointF() {}
    public PointF(float x, float y) {
        this.x = x;
        this.y = y; 
    }
    public PointF(Point p) { 
        this.x = p.x;
        this.y = p.y;
    }
    public final void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public final void set(PointF p) { 
        this.x = p.x;
        this.y = p.y;
    }
    public final void negate() { 
        x = -x;
        y = -y; 
    }
    public final void offset(float dx, float dy) {
        x += dx;
        y += dy;
    }
    public final boolean equals(float x, float y) { 
        return this.x == x && this.y == y; 
    }
    public final float length() { 
        return length(x, y); 
    }
    public static float length(float x, float y) {
        return FloatMath.sqrt(x * x + y * y);
    }
}
