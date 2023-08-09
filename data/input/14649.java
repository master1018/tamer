public class AccelTypedVolatileImage extends SunVolatileImage {
    public AccelTypedVolatileImage(GraphicsConfiguration graphicsConfig,
                                   int width, int height, int transparency,
                                   int accType)
    {
        super(null, graphicsConfig, width, height, null, transparency,
              null, accType);
    }
    @Override
    public Graphics2D createGraphics() {
        if (getForcedAccelSurfaceType() == TEXTURE) {
            throw new UnsupportedOperationException("Can't render " +
                                                    "to a non-RT Texture");
        }
        return super.createGraphics();
    }
}
