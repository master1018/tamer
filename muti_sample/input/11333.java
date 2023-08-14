public class HtmlSerialFieldWriter extends FieldWriterImpl
    implements SerializedFormWriter.SerialFieldWriter {
    ProgramElementDoc[] members = null;
    private boolean printedOverallAnchor = false;
    public HtmlSerialFieldWriter(SubWriterHolderWriter writer,
                                    ClassDoc classdoc) {
        super(writer, classdoc);
    }
    public List<FieldDoc> members(ClassDoc cd) {
        return Arrays.asList(cd.serializableFields());
    }
    protected void printTypeLinkNoDimension(Type type) {
        ClassDoc cd = type.asClassDoc();
        if (type.isPrimitive() || cd.isPackagePrivate()) {
            print(type.typeName());
        } else {
            writer.printLink(new LinkInfoImpl(
                LinkInfoImpl.CONTEXT_SERIAL_MEMBER, type));
        }
    }
    public Content getSerializableFieldsHeader() {
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        return ul;
    }
    public Content getFieldsContentHeader(boolean isLastContent) {
        HtmlTree li = new HtmlTree(HtmlTag.LI);
        if (isLastContent)
            li.addStyle(HtmlStyle.blockListLast);
        else
            li.addStyle(HtmlStyle.blockList);
        return li;
    }
    public Content getSerializableFields(String heading, Content serializableFieldsTree) {
        HtmlTree li = new HtmlTree(HtmlTag.LI);
        li.addStyle(HtmlStyle.blockList);
        if (serializableFieldsTree.isValid()) {
            if (!printedOverallAnchor) {
                li.addContent(writer.getMarkerAnchor("serializedForm"));
                printedOverallAnchor = true;
            }
            Content headingContent = new StringContent(heading);
            Content serialHeading = HtmlTree.HEADING(HtmlConstants.SERIALIZED_MEMBER_HEADING,
                    headingContent);
            li.addContent(serialHeading);
            li.addContent(serializableFieldsTree);
        }
        return li;
    }
    public void addMemberHeader(ClassDoc fieldType, String fieldTypeStr,
            String fieldDimensions, String fieldName, Content contentTree) {
        Content nameContent = new RawHtml(fieldName);
        Content heading = HtmlTree.HEADING(HtmlConstants.MEMBER_HEADING, nameContent);
        contentTree.addContent(heading);
        Content pre = new HtmlTree(HtmlTag.PRE);
        if (fieldType == null) {
            pre.addContent(fieldTypeStr);
        } else {
            Content fieldContent = new RawHtml(writer.getLink(new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_SERIAL_MEMBER, fieldType)));
            pre.addContent(fieldContent);
        }
        pre.addContent(fieldDimensions + " ");
        pre.addContent(fieldName);
        contentTree.addContent(pre);
    }
    public void addMemberDeprecatedInfo(FieldDoc field, Content contentTree) {
        addDeprecatedInfo(field, contentTree);
    }
    public void addMemberDescription(FieldDoc field, Content contentTree) {
        if (field.inlineTags().length > 0) {
            writer.addInlineComment(field, contentTree);
        }
        Tag[] tags = field.tags("serial");
        if (tags.length > 0) {
            writer.addInlineComment(field, tags[0], contentTree);
        }
    }
    public void addMemberDescription(SerialFieldTag serialFieldTag, Content contentTree) {
        String serialFieldTagDesc = serialFieldTag.description().trim();
        if (!serialFieldTagDesc.isEmpty()) {
            Content serialFieldContent = new RawHtml(serialFieldTagDesc);
            Content div = HtmlTree.DIV(HtmlStyle.block, serialFieldContent);
            contentTree.addContent(div);
        }
    }
    public void addMemberTags(FieldDoc field, Content contentTree) {
        TagletOutputImpl output = new TagletOutputImpl("");
        TagletWriter.genTagOuput(configuration().tagletManager, field,
                configuration().tagletManager.getCustomTags(field),
                writer.getTagletWriterInstance(false), output);
        String outputString = output.toString().trim();
        Content dlTags = new HtmlTree(HtmlTag.DL);
        if (!outputString.isEmpty()) {
            Content tagContent = new RawHtml(outputString);
            dlTags.addContent(tagContent);
        }
        contentTree.addContent(dlTags);
    }
    public boolean shouldPrintOverview(FieldDoc field) {
        if (!configuration().nocomment) {
            if(!field.commentText().isEmpty() ||
                    writer.hasSerializationOverviewTags(field))
                return true;
        }
        if (field.tags("deprecated").length > 0)
            return true;
        return false;
    }
}
