public class UIDefault<T> {
    @XmlAttribute private String name;
    private T value;
    public String getName() {
        return name;
    }
    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        this.value = value;
    }
}
class UIColor extends UIDefault<Matte> {
    @XmlElement
    public void setMatte(Matte m) {
        setValue(m);
    }
    public String write() {
        return String.format("        addColor(d, \"%s\", %s);\n",
                             getName(), getValue().write());
    }
}
class UIFont extends UIDefault<Typeface> {
    @XmlElement
    public void setTypeface(Typeface t) {
        setValue(t);
    }
    public String write() {
        return String.format("        d.put(\"%s\", %s);\n",
                             getName(), getValue().write());
    }
}
class UIProperty extends UIDefault<String> {
    public static enum PropertyType {
        BOOLEAN, INT, FLOAT, DOUBLE, STRING, FONT, COLOR, INSETS, DIMENSION, BORDER
    }
    @XmlAttribute private PropertyType type;
    @XmlElement private Border border;
    @XmlElement private Dimension dimension;
    @XmlElement private Insets insets;
    @XmlElement private Matte matte;
    @XmlElement private Typeface typeface;
    @XmlAttribute
    @Override public void setValue(String value) {
        super.setValue(value);
    }
    public String write(String prefix) {
        switch (type) {
            case BOOLEAN:
                return String.format("        d.put(\"%s%s\", Boolean.%s);\n",
                                     prefix, getName(), getValue().toUpperCase());  
            case STRING:
                return String.format("        d.put(\"%s%s\", \"%s\");\n",
                                     prefix, getName(), getValue());
            case INT:
                return String.format("        d.put(\"%s%s\", new Integer(%s));\n",
                                     prefix, getName(), getValue());
            case FLOAT:
                return String.format("        d.put(\"%s%s\", new Float(%sf));\n",
                                     prefix, getName(), getValue());
            case DOUBLE:
                return String.format("        d.put(\"%s%s\", new Double(%s));\n",
                                     prefix, getName(), getValue());
            case COLOR:
                return String.format("        addColor(d, \"%s%s\", %s);\n",
                                     prefix, getName(), matte.write());
            case FONT:
                return String.format("        d.put(\"%s%s\", %s);\n",
                                     prefix, getName(), typeface.write());
            case INSETS:
                return String.format("        d.put(\"%s%s\", %s);\n",
                                     prefix, getName(), insets.write(true));
            case DIMENSION:
                return String.format("        d.put(\"%s%s\", new DimensionUIResource(%d, %d));\n",
                                     prefix, getName(), dimension.width, dimension.height);
            case BORDER:
                return String.format("        d.put(\"%s%s\", new BorderUIResource(%s));\n",
                                     prefix, getName(), border.write());
            default:
                return "###  Look, something's wrong with UIProperty.write()  $$$";
        }
    }
}
