public class DefaultRessourceProvider implements IRessourceProvider {
    @Override
    public Color getColor(int r, int g, int b) {
        return new Color(Display.getDefault(), r, g, b);
    }
    @Override
    public Color getColor(RGB rgb) {
        return new Color(Display.getDefault(), rgb);
    }
    @Override
    public Font getFont(FontData data) {
        return new Font(Display.getDefault(), data);
    }
    @Override
    public Font getFont(String name, int height, int style) {
        return new Font(Display.getDefault(), name, height, style);
    }
}
