public class DrawImageBgTest {
    public static void main(String[] args) {
        GraphicsConfiguration gc =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
        if (gc.getColorModel().getPixelSize() <= 8) {
            System.out.println("8-bit color model, test considered passed");
            return;
        }
        VolatileImage vImg = null;
        BufferedImage readBackBImg;
        byte r[] = { 0x00, (byte)0xff};
        byte g[] = { 0x00, (byte)0xff};
        byte b[] = { 0x00, (byte)0xff};
        IndexColorModel icm = new IndexColorModel(8, 2, r, g, b, 0);
        WritableRaster wr = icm.createCompatibleWritableRaster(25, 25);
        BufferedImage tImg = new BufferedImage(icm, wr, false, null);
        do {
            if (vImg == null ||
                vImg.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE)
            {
                vImg = gc.createCompatibleVolatileImage(tImg.getWidth(),
                                                        tImg.getHeight());
            }
            Graphics viG = vImg.getGraphics();
            viG.setColor(Color.red);
            viG.fillRect(0, 0, vImg.getWidth(), vImg.getHeight());
            viG.drawImage(tImg, 0, 0, Color.green, null);
            viG.fillRect(0, 0, vImg.getWidth(), vImg.getHeight());
            viG.drawImage(tImg, 0, 0, Color.white, null);
            readBackBImg = vImg.getSnapshot();
        } while (vImg.contentsLost());
        for (int x = 0; x < readBackBImg.getWidth(); x++) {
            for (int y = 0; y < readBackBImg.getHeight(); y++) {
                int currPixel = readBackBImg.getRGB(x, y);
                if (currPixel != Color.white.getRGB()) {
                    String fileName = "DrawImageBgTest.png";
                    try {
                        ImageIO.write(readBackBImg, "png", new File(fileName));
                        System.err.println("Dumped image to " + fileName);
                    } catch (IOException ex) {}
                    throw new
                        RuntimeException("Test Failed: found wrong color: 0x"+
                                         Integer.toHexString(currPixel));
                }
            }
        }
        System.out.println("Test Passed.");
    }
}
