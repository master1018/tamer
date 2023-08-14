public class RSLContextInvalidationTest {
    public static void main(String[] args) {
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        VolatileImage vi = gc.createCompatibleVolatileImage(100, 100);
        vi.validate(gc);
        VolatileImage vi1 = gc.createCompatibleVolatileImage(100, 100);
        vi1.validate(gc);
        if (!(vi instanceof DestSurfaceProvider)) {
            System.out.println("Test considered PASSED: no HW acceleration");
            return;
        }
        DestSurfaceProvider p = (DestSurfaceProvider)vi;
        Surface s = p.getDestSurface();
        if (!(s instanceof AccelSurface)) {
            System.out.println("Test considered PASSED: no HW acceleration");
            return;
        }
        AccelSurface dst = (AccelSurface)s;
        Graphics g = vi.createGraphics();
        g.drawImage(vi1, 95, 95, null);
        g.setColor(Color.red);
        g.fillRect(0, 0, 100, 100);
        g.setColor(Color.black);
        g.fillRect(0, 0, 100, 100);
        RenderQueue rq = dst.getContext().getRenderQueue();
        rq.lock();
        try {
            dst.getContext().saveState();
            dst.getContext().restoreState();
        } finally {
            rq.unlock();
        }
        g.drawImage(vi1, 95, 95, null);
        g.setColor(Color.black);
        g.fillRect(0, 0, 100, 100);
        BufferedImage bi = vi.getSnapshot();
        if (bi.getRGB(50, 50) != Color.black.getRGB()) {
            throw new RuntimeException("Test FAILED: found color="+
                Integer.toHexString(bi.getRGB(50, 50))+" instead of "+
                Integer.toHexString(Color.black.getRGB()));
        }
        System.out.println("Test PASSED.");
    }
}
