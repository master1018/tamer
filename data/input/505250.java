public class Kernel implements Cloneable {
    private final int xOrigin;
    private final int yOrigin;
    private int width;
    private int height;
    float data[];
    public Kernel(int width, int height, float[] data) {
        int dataLength = width * height;
        if (data.length < dataLength) {
            throw new IllegalArgumentException(Messages.getString("awt.22B")); 
        }
        this.width = width;
        this.height = height;
        this.data = new float[dataLength];
        System.arraycopy(data, 0, this.data, 0, dataLength);
        xOrigin = (width - 1) / 2;
        yOrigin = (height - 1) / 2;
    }
    public final int getWidth() {
        return width;
    }
    public final int getHeight() {
        return height;
    }
    public final float[] getKernelData(float[] data) {
        if (data == null) {
            data = new float[this.data.length];
        }
        System.arraycopy(this.data, 0, data, 0, this.data.length);
        return data;
    }
    public final int getXOrigin() {
        return xOrigin;
    }
    public final int getYOrigin() {
        return yOrigin;
    }
    @Override
    public Object clone() {
        return new Kernel(width, height, data);
    }
}
