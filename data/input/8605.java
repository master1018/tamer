public abstract class TagletWriter {
    protected boolean isFirstSentence = false;
    public abstract TagletOutput getOutputInstance();
    protected abstract TagletOutput getDocRootOutput();
    protected abstract TagletOutput deprecatedTagOutput(Doc doc);
    protected abstract MessageRetriever getMsgRetriever();
    protected abstract TagletOutput getParamHeader(String header);
    protected abstract TagletOutput paramTagOutput(ParamTag paramTag,
        String paramName);
    protected abstract TagletOutput returnTagOutput(Tag returnTag);
    protected abstract TagletOutput seeTagOutput(Doc holder, SeeTag[] seeTags);
    protected abstract TagletOutput simpleTagOutput(Tag[] simpleTags,
        String header);
    protected abstract TagletOutput simpleTagOutput(Tag simpleTag, String header);
    protected abstract TagletOutput getThrowsHeader();
    protected abstract TagletOutput throwsTagOutput(ThrowsTag throwsTag);
    protected abstract TagletOutput throwsTagOutput(Type throwsType);
    protected abstract TagletOutput valueTagOutput(FieldDoc field,
        String constantVal, boolean includeLink);
    public static void genTagOuput(TagletManager tagletManager, Doc doc,
            Taglet[] taglets, TagletWriter writer, TagletOutput output) {
        tagletManager.checkTags(doc, doc.tags(), false);
        tagletManager.checkTags(doc, doc.inlineTags(), true);
        TagletOutput currentOutput = null;
        for (int i = 0; i < taglets.length; i++) {
            if (doc instanceof ClassDoc && taglets[i] instanceof ParamTaglet) {
                continue;
            }
            if (taglets[i] instanceof DeprecatedTaglet) {
                continue;
            }
            try {
                currentOutput = taglets[i].getTagletOutput(doc, writer);
            } catch (IllegalArgumentException e) {
                Tag[] tags = doc.tags(taglets[i].getName());
                if (tags.length > 0) {
                    currentOutput = taglets[i].getTagletOutput(tags[0], writer);
                }
            }
            if (currentOutput != null) {
                tagletManager.seenCustomTag(taglets[i].getName());
                output.appendOutput(currentOutput);
            }
        }
    }
    public static TagletOutput getInlineTagOuput(TagletManager tagletManager,
            Tag holderTag, Tag inlineTag, TagletWriter tagletWriter) {
        Taglet[] definedTags = tagletManager.getInlineCustomTags();
        for (int j = 0; j < definedTags.length; j++) {
            if (("@"+definedTags[j].getName()).equals(inlineTag.name())) {
                tagletManager.seenCustomTag(definedTags[j].getName());
                TagletOutput output = definedTags[j].getTagletOutput(
                    holderTag != null &&
                        definedTags[j].getName().equals("inheritDoc") ?
                            holderTag : inlineTag, tagletWriter);
                return output;
            }
        }
        return null;
    }
    public abstract TagletOutput commentTagsToOutput(Tag holderTag, Tag[] tags);
    public abstract TagletOutput commentTagsToOutput(Doc holderDoc, Tag[] tags);
    public abstract TagletOutput commentTagsToOutput(Tag holderTag,
        Doc holderDoc, Tag[] tags, boolean isFirstSentence);
    public abstract Configuration configuration();
    public abstract TagletOutput getTagletOutputInstance();
}
