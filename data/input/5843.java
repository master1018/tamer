public class ImageIconUIResource extends ImageIcon implements UIResource {
    public ImageIconUIResource(byte[] imageData) {
        super(imageData);
    }
    public ImageIconUIResource(Image image) {
        super(image);
    }
}
