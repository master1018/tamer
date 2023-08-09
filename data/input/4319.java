public class CloneConfigsTest {
    public static void main(String[] args) {
        GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
        GraphicsConfiguration c = new TestConfig();
        for (GraphicsDevice gd : devices) {
            System.out.println("Device: " + gd);
            GraphicsConfiguration[] configs = gd.getConfigurations();
            for (int i = 0; i < configs.length; i++) {
                GraphicsConfiguration gc  = configs[i];
                System.out.println("\tConfig: " + gc);
                configs[i] = c;
            }
            configs = gd.getConfigurations();
            for (GraphicsConfiguration gc : configs) {
                if (gc == c) {
                    throw new RuntimeException("Test failed.");
                }
            }
            System.out.println("Test passed.");
        }
    }
    private static class TestConfig extends GraphicsConfiguration {
        @Override
        public GraphicsDevice getDevice() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public BufferedImage createCompatibleImage(int width, int height) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public ColorModel getColorModel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public ColorModel getColorModel(int transparency) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public AffineTransform getDefaultTransform() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public AffineTransform getNormalizingTransform() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Rectangle getBounds() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
