public class ColumnFormat extends OptionFormat {
    private int number;
    private int width;
    private Alignment align = Alignment.CENTER;
    private Scale scale = Scale.RAW;
    private String format;
    private String header;
    private Expression expression;
    private Object previousValue;
    public ColumnFormat(int number) {
        super("Column" + number);
        this.number = number;
    }
    public void validate() throws ParserException {
        if (expression == null) {
            throw new ParserException("Missing data statement in column " + number);
        }
        if (header == null) {
            throw new ParserException("Missing header statement in column " + number);
        }
        if (format == null) {
            format="0";
        }
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public void setAlignment(Alignment align) {
        this.align = align;
    }
    public void setScale(Scale scale) {
        this.scale = scale;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    public void setHeader(String header) {
        this.header = header;
    }
    public String getHeader() {
        return header;
    }
    public String getFormat() {
        return format;
    }
    public int getWidth() {
        return width;
    }
    public Alignment getAlignment() {
        return align;
    }
    public Scale getScale() {
        return scale;
    }
    public Expression getExpression() {
        return expression;
    }
    public void setExpression(Expression e) {
        this.expression = e;
    }
    public void setPreviousValue(Object o) {
        this.previousValue = o;
    }
    public Object getPreviousValue() {
        return previousValue;
    }
    public void printFormat(int indentLevel) {
        String indentAmount = "  ";
        StringBuilder indent = new StringBuilder("");
        for (int j = 0; j < indentLevel; j++) {
            indent.append(indentAmount);
        }
        System.out.println(indent + name + " {");
        System.out.println(indent + indentAmount + "name=" + name
                + ";data=" + expression.toString() + ";header=" + header
                + ";format=" + format + ";width=" + width
                + ";scale=" + scale.toString() + ";align=" + align.toString());
        for (Iterator i = children.iterator();  i.hasNext(); ) {
            OptionFormat of = (OptionFormat)i.next();
            of.printFormat(indentLevel+1);
        }
        System.out.println(indent + "}");
    }
    public String getValue() {
        return null;
    }
}
