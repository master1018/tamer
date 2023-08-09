public class SampleData extends Object {
    protected Font font;
    protected Color color;
    protected String string;
    public SampleData(Font newFont, Color newColor, String newString) {
        font = newFont;
        color = newColor;
        string = newString;
    }
    public void setFont(Font newFont) {
        font = newFont;
    }
    public Font getFont() {
        return font;
    }
    public void setColor(Color newColor) {
        color = newColor;
    }
    public Color getColor() {
        return color;
    }
    public void setString(String newString) {
        string = newString;
    }
    public String string() {
        return string;
    }
    @Override
    public String toString() {
        return string;
    }
}
