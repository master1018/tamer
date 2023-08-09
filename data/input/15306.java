public class EmptyFileTest {
    public static void main(String[] args) throws IOException {
        String format = "MY_IMG";
        File out = new File("output.myimg");
        System.out.printf("File %s: %s\n", out.getAbsolutePath(),
                          out.exists() ? "EXISTS" : "NEW");
        BufferedImage img = createTestImage();
        boolean status = false;
        try {
            status = ImageIO.write(img, format, out);
        } catch (IOException e) {
            throw new RuntimeException("Test FAILED: unexpected exception", e);
        }
        if (status) {
            throw new RuntimeException("Test FAILED: Format " +
                                       format + " is supported.");
        }
        if (out.exists()) {
            throw new RuntimeException("Test FAILED.");
        }
        System.out.println("Test PASSED.");
    }
    private static BufferedImage createTestImage() {
        return new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    }
}
