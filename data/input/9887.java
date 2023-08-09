public class LegacyTaglet implements Taglet {
    private com.sun.tools.doclets.Taglet legacyTaglet;
    public LegacyTaglet(com.sun.tools.doclets.Taglet t) {
        legacyTaglet = t;
    }
    public boolean inField() {
        return legacyTaglet.isInlineTag() || legacyTaglet.inField();
    }
    public boolean inConstructor() {
        return legacyTaglet.isInlineTag() || legacyTaglet.inConstructor();
    }
    public boolean inMethod() {
        return legacyTaglet.isInlineTag() || legacyTaglet.inMethod();
    }
    public boolean inOverview() {
        return legacyTaglet.isInlineTag() || legacyTaglet.inOverview();
    }
    public boolean inPackage() {
        return legacyTaglet.isInlineTag() || legacyTaglet.inPackage();
    }
    public boolean inType() {
        return legacyTaglet.isInlineTag() || legacyTaglet.inType();
    }
    public boolean isInlineTag() {
        return legacyTaglet.isInlineTag();
    }
    public String getName() {
        return legacyTaglet.getName();
    }
    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer)
            throws IllegalArgumentException {
        TagletOutput output = writer.getOutputInstance();
        output.setOutput(legacyTaglet.toString(tag));
        return output;
    }
    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer)
            throws IllegalArgumentException {
        TagletOutput output = writer.getOutputInstance();
        output.setOutput(legacyTaglet.toString(holder.tags(getName())));
        return output;
    }
}
