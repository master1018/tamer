public class bug6501991 {
    public static void main(String[] args) {
        try {
            JLabel l = new JLabel("\u0634\u0634\u0634\u0634\u0634\u0634\u0634");
            l.setSize(5, 22);
            BufferedImage image =
                new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            l.paint(image.createGraphics());
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("failed");
        }
    }
}
