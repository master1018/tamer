public class OpaqueImageToSurfaceBlitTest {
    public static void main(String[] args) {
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        VolatileImage vi = gc.createCompatibleVolatileImage(16, 16);
        vi.validate(gc);
        BufferedImage bi =
            new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        int data[] = ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
        data[0] = 0x0000007f;
        data[1] = 0x0000007f;
        data[2] = 0xff00007f;
        data[3] = 0xff00007f;
        Graphics2D g = vi.createGraphics();
        g.setComposite(AlphaComposite.SrcOver.derive(0.999f));
        g.drawImage(bi, 0, 0, null);
        bi = vi.getSnapshot();
        if (bi.getRGB(0, 0) != bi.getRGB(1, 1)) {
            throw new RuntimeException("Test FAILED: color at 0x0 ="+
                Integer.toHexString(bi.getRGB(0, 0))+" differs from 1x1 ="+
                Integer.toHexString(bi.getRGB(1,1)));
        }
        System.out.println("Test PASSED.");
    }
}
