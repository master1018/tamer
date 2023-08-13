abstract class ShadowEffect extends Effect {
    protected Color color = Color.BLACK;
    protected float opacity = 0.75f;
    protected int angle = 135;
    protected int distance = 5;
    protected int spread = 0;
    protected int size = 5;
    Color getColor() {
        return color;
    }
    void setColor(Color color) {
        Color old = getColor();
        this.color = color;
    }
    float getOpacity() {
        return opacity;
    }
    void setOpacity(float opacity) {
        float old = getOpacity();
        this.opacity = opacity;
    }
    int getAngle() {
        return angle;
    }
    void setAngle(int angle) {
        int old = getAngle();
        this.angle = angle;
    }
    int getDistance() {
        return distance;
    }
    void setDistance(int distance) {
        int old = getDistance();
        this.distance = distance;
    }
    int getSpread() {
        return spread;
    }
    void setSpread(int spread) {
        int old = getSpread();
        this.spread = spread;
    }
    int getSize() {
        return size;
    }
    void setSize(int size) {
        int old = getSize();
        this.size = size;
    }
}
