public class Element {
    private String name;
    private HashMap<String, TypedContent> attributes;
    private TypedContent content;
    private Vector<Element> children = new Vector<Element>();
    public Element(String nameToSet) {
        this.name = new String(nameToSet);
    }
    public Element(String nameToSet, TypedContent contentToSet) {
        this.name = new String(nameToSet);
        this.content = contentToSet;
    }
    public String getName() {
        return name;
    }
    public void setName(String nameToSet) {
        this.name = nameToSet;
    }
    public HashMap<String, TypedContent> getAttributes() {
        return attributes;
    }
    public void setAttributes(HashMap<String, TypedContent> attributesToSet) {
        this.attributes = attributesToSet;
    }
    public TypedContent getContent() {
        return content;
    }
    public void setContent(TypedContent contentToSet) {
        this.content = contentToSet;
    }
    public Vector<Element> getChildren() {
        return children;
    }
    public void setChildren(Vector<Element> children) {
        this.children = children;
    }
    public void setChild(Element child) {
        this.children.add(child);
    }
    public Element clone() {
        Element toReturn;
        if (this.content == null) {
            toReturn = new Element(this.name);
        } else {
            toReturn = new Element(this.name, this.content.clone());
        }
        HashMap<String, TypedContent> tempAttributes = new HashMap<String, TypedContent>();
        if (this.attributes != null) {
            Iterator<String> tempIterator = attributes.keySet().iterator();
            while (tempIterator.hasNext()) {
                tempAttributes.put(tempIterator.next(), this.attributes.get(tempIterator.next()).clone());
            }
            toReturn.setAttributes(tempAttributes);
        }
        Vector<Element> tempElements = new Vector<Element>();
        if (this.children != null) {
            for (Element e : this.children) {
                tempElements.add(e.clone());
            }
            toReturn.setChildren(tempElements);
        }
        return toReturn;
    }
}
