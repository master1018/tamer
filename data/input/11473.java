public class XRenderBlitsTest {
    private static final int w = 10;
    private static final int h = 10;
    public static void main(String[] args) {
        final CountDownLatch done = new CountDownLatch(1);
        final ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
        int type = BufferedImage.TYPE_INT_RGB;
        do {
            BufferedImage img = new BufferedImage(w, h, type++);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.pink);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
            images.add(img);
        } while (type <= BufferedImage.TYPE_BYTE_INDEXED);
        Frame f = new Frame("Draw images");
        Component c = new Component() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(w * images.size(), h);
            }
            @Override
            public void paint(Graphics g) {
                int x = 0;
                for (BufferedImage img : images) {
                    System.out.println("Draw image " + img.getType());
                    g.drawImage(img, x, 0, this);
                    x += w;
                }
                done.countDown();
            }
        };
        f.add("Center", c);
        f.pack();
        f.setVisible(true);
        try {
        done.await();
        } catch (InterruptedException e) {
        }
        System.out.println("Test passed");
        f.dispose();
    }
}
