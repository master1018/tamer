public class basefont extends SinglePartElement implements Printable {
    {
        setElementType("basefont");
        setCase(LOWERCASE);
        setAttributeQuote(true);
        setBeginEndModifier('/');
    }
    public basefont() {
    }
    public basefont(String face) {
        setFace(face);
    }
    public basefont(String face, String color) {
        setFace(face);
        setColor(color);
    }
    public basefont(String face, String color, int size) {
        setFace(face);
        setColor(color);
        setSize(size);
    }
    public basefont(int size, String face) {
        setSize(size);
        setFace(face);
    }
    public basefont(String color, int size) {
        setSize(size);
        setColor(color);
    }
    public basefont setFace(String face) {
        addAttribute("face", face);
        return (this);
    }
    public basefont setColor(String color) {
        addAttribute("color", HtmlColor.convertColor(color));
        return (this);
    }
    public basefont setSize(int size) {
        addAttribute("size", Integer.toString(size));
        return (this);
    }
    public basefont setSize(String size) {
        addAttribute("size", size);
        return (this);
    }
    public Element setLang(String lang) {
        addAttribute("lang", lang);
        addAttribute("xml:lang", lang);
        return this;
    }
    public basefont addElement(String hashcode, Element element) {
        addElementToRegistry(hashcode, element);
        return (this);
    }
    public basefont addElement(String hashcode, String element) {
        addElementToRegistry(hashcode, element);
        return (this);
    }
    public basefont addElement(Element element) {
        addElementToRegistry(element);
        return (this);
    }
    public basefont addElement(String element) {
        addElementToRegistry(element);
        return (this);
    }
    public basefont removeElement(String hashcode) {
        removeElementFromRegistry(hashcode);
        return (this);
    }
}
