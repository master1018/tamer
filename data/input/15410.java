public class ReturnTaglet extends BaseExecutableMemberTaglet
        implements InheritableTaglet {
    public ReturnTaglet() {
        name = "return";
    }
    public void inherit(DocFinder.Input input, DocFinder.Output output) {
       Tag[] tags = input.method.tags("return");
        if (tags.length > 0) {
            output.holder = input.method;
            output.holderTag = tags[0];
            output.inlineTags = input.isFirstSentence ?
                tags[0].firstSentenceTags() : tags[0].inlineTags();
        }
    }
    public boolean inConstructor() {
        return false;
    }
    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) {
        Type returnType = ((MethodDoc) holder).returnType();
        Tag[] tags = holder.tags(name);
        if (returnType.isPrimitive() && returnType.typeName().equals("void")) {
            if (tags.length > 0) {
                writer.getMsgRetriever().warning(holder.position(),
                    "doclet.Return_tag_on_void_method");
            }
            return null;
        }
        if (tags.length == 0) {
            DocFinder.Output inheritedDoc =
                DocFinder.search(new DocFinder.Input((MethodDoc) holder, this));
            tags = inheritedDoc.holderTag == null ? tags : new Tag[] {inheritedDoc.holderTag};
        }
        return tags.length > 0 ? writer.returnTagOutput(tags[0]) : null;
    }
}
