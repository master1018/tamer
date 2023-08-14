public class BufferedImageDevice extends GraphicsDevice
{
    GraphicsConfiguration gc;
    public BufferedImageDevice(BufferedImageGraphicsConfig gc) {
        this.gc = gc;
    }
    public int getType() {
        return GraphicsDevice.TYPE_IMAGE_BUFFER;
    }
    public String getIDstring() {
        return ("BufferedImage");
    }
    public GraphicsConfiguration[] getConfigurations() {
        return new GraphicsConfiguration[] { gc };
    }
    public GraphicsConfiguration getDefaultConfiguration() {
        return gc;
    }
}
