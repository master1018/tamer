public class InheritDocTaglet extends BaseInlineTaglet {
    public static final String INHERIT_DOC_INLINE_TAG = "{@inheritDoc}";
    public InheritDocTaglet () {
        name = "inheritDoc";
    }
    public boolean inField() {
        return false;
    }
    public boolean inConstructor() {
        return false;
    }
    public boolean inOverview() {
        return false;
    }
    public boolean inPackage() {
        return false;
    }
    public boolean inType() {
        return false;
    }
    private TagletOutput retrieveInheritedDocumentation(TagletWriter writer,
            MethodDoc md, Tag holderTag, boolean isFirstSentence) {
        TagletOutput replacement = writer.getTagletOutputInstance();
        Taglet inheritableTaglet = holderTag == null ?
            null : writer.configuration().tagletManager.getTaglet(holderTag.name());
        if (inheritableTaglet != null &&
            !(inheritableTaglet instanceof InheritableTaglet)) {
                writer.configuration().message.warning(md.position(),
                "doclet.noInheritedDoc", md.name() + md.flatSignature());
         }
        DocFinder.Output inheritedDoc =
            DocFinder.search(new DocFinder.Input(md,
                (InheritableTaglet) inheritableTaglet, holderTag,
                isFirstSentence, true));
        if (inheritedDoc.isValidInheritDocTag == false) {
            writer.configuration().message.warning(md.position(),
                "doclet.noInheritedDoc", md.name() + md.flatSignature());
        } else if (inheritedDoc.inlineTags.length > 0) {
            replacement = writer.commentTagsToOutput(inheritedDoc.holderTag,
                inheritedDoc.holder, inheritedDoc.inlineTags, isFirstSentence);
        }
        return replacement;
    }
    public TagletOutput getTagletOutput(Tag tag, TagletWriter tagletWriter) {
        if (! (tag.holder() instanceof MethodDoc)) {
            return tagletWriter.getOutputInstance();
        }
        return tag.name().equals("@inheritDoc") ?
                retrieveInheritedDocumentation(tagletWriter, (MethodDoc) tag.holder(), null, tagletWriter.isFirstSentence) :
                retrieveInheritedDocumentation(tagletWriter, (MethodDoc) tag.holder(), tag, tagletWriter.isFirstSentence);
    }
}
