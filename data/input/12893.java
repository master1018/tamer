public class PrintColorUIResource extends ColorUIResource {
    private Color printColor;
    public PrintColorUIResource(int rgb, Color printColor) {
        super(rgb);
        this.printColor = printColor;
    }
    public Color getPrintColor() {
        return ((printColor != null) ? printColor : this);
    }
    private Object writeReplace() {
        return new ColorUIResource(this);
    }
}
