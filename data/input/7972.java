public class DrawByteBinary {
    public static void main(String args[]) {
        int w = 100, h = 30;
        int x = 10;
        byte[] arr = {(byte)0xff, (byte)0x0, (byte)0x00};
        IndexColorModel newCM = new IndexColorModel(1, 2, arr, arr, arr);
        BufferedImage orig = new BufferedImage(w, h, TYPE_BYTE_BINARY, newCM);
        Graphics2D g2d = orig.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, w, h);
        g2d.setColor(Color.black);
        g2d.drawLine(x, 0, x, h);
        g2d.dispose();
        IndexColorModel origCM = (IndexColorModel)orig.getColorModel();
        BufferedImage test = new BufferedImage(w, h, TYPE_BYTE_BINARY,origCM);
        g2d = test.createGraphics();
        g2d.drawImage(orig, 0, 0, null);
        g2d.dispose();
        int y = h / 2;
        if (test.getRGB(x - 1, y) != 0xffffffff) {
            throw new RuntimeException("Invalid color outside the line.");
        }
        if (test.getRGB(x, y) != 0xff000000) {
            throw new RuntimeException("Invalid color on the line.");
        }
    }
}
