public class ColorUIResource extends Color implements UIResource
{
    @ConstructorProperties({"red", "green", "blue"})
    public ColorUIResource(int r, int g, int b) {
        super(r, g, b);
    }
    public ColorUIResource(int rgb) {
        super(rgb);
    }
    public ColorUIResource(float r, float g, float b) {
        super(r, g, b);
    }
    public ColorUIResource(Color c) {
        super(c.getRGB(), (c.getRGB() & 0xFF000000) != 0xFF000000);
    }
}
