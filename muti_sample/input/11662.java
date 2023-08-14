public class TextTag implements Tag {
    protected final String text;
    protected final String name = "Text";
    protected final Doc holder;
    public TextTag(Doc holder, String text) {
        super();
        this.holder = holder;
        this.text = text;
    }
    public String name() {
        return name;
    }
    public Doc holder() {
        return holder;
    }
    public String kind() {
        return name;
    }
    public String text() {
        return text;
    }
    public String toString() {
        return name + ":" + text;
    }
    public Tag[] inlineTags() {
        return new Tag[] {this};
    }
    public Tag[] firstSentenceTags() {
        return new Tag[] {this};
    }
    public SourcePosition position() {
        return holder.position();
    }
}
