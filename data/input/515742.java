public class ImageCapabilities implements Cloneable {
    private final boolean accelerated;
    public ImageCapabilities(boolean accelerated) {
        this.accelerated = accelerated;
    }
    @Override
    public Object clone() {
        return new ImageCapabilities(accelerated);
    }
    public boolean isAccelerated() {
        return accelerated;
    }
    public boolean isTrueVolatile() {
        return true;
    }
}
