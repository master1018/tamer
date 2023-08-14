public class GesturePoint {
    public final float x;
    public final float y;
    public final long timestamp;
    public GesturePoint(float x, float y, long t) {
        this.x = x;
        this.y = y;
        timestamp = t;
    }
    static GesturePoint deserialize(DataInputStream in) throws IOException {
        final float x = in.readFloat();
        final float y = in.readFloat();
        final long timeStamp = in.readLong();
        return new GesturePoint(x, y, timeStamp);
    }
    @Override
    public Object clone() {
        return new GesturePoint(x, y, timestamp);
    }
}
