public class TextAttribute {
    public int start;
    public int length;
    public TextAlignment align;
    public FontSize size;
    public boolean bold;
    public boolean italic;
    public boolean underlined;
    public boolean strikeThrough;
    public TextColor color;
    public TextAttribute(int start, int length, TextAlignment align,
            FontSize size, boolean bold, boolean italic, boolean underlined,
            boolean strikeThrough, TextColor color) {
        this.start = start;
        this.length = length;
        this.align = align;
        this.size = size;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikeThrough = strikeThrough;
        this.color = color;
    }
}
