public class HtmlSerialMethodWriter extends MethodWriterImpl implements
        SerializedFormWriter.SerialMethodWriter{
    public HtmlSerialMethodWriter(SubWriterHolderWriter writer,
            ClassDoc classdoc) {
        super(writer, classdoc);
    }
    public Content getSerializableMethodsHeader() {
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        return ul;
    }
    public Content getMethodsContentHeader(boolean isLastContent) {
        HtmlTree li = new HtmlTree(HtmlTag.LI);
        if (isLastContent)
            li.addStyle(HtmlStyle.blockListLast);
        else
            li.addStyle(HtmlStyle.blockList);
        return li;
    }
    public Content getSerializableMethods(String heading, Content serializableMethodContent) {
        Content li = HtmlTree.LI(HtmlStyle.blockList, writer.getMarkerAnchor(
                "serialized_methods"));
        Content headingContent = new StringContent(heading);
        Content serialHeading = HtmlTree.HEADING(HtmlConstants.SERIALIZED_MEMBER_HEADING,
                headingContent);
        li.addContent(serialHeading);
        li.addContent(serializableMethodContent);
        return li;
    }
    public Content getNoCustomizationMsg(String msg) {
        Content noCustomizationMsg = new StringContent(msg);
        return noCustomizationMsg;
    }
    public void addMemberHeader(MethodDoc member, Content methodsContentTree) {
        methodsContentTree.addContent(writer.getMarkerAnchor(
                writer.getAnchor(member)));
        methodsContentTree.addContent(getHead(member));
        methodsContentTree.addContent(getSignature(member));
    }
    public void addDeprecatedMemberInfo(MethodDoc member, Content methodsContentTree) {
        addDeprecatedInfo(member, methodsContentTree);
    }
    public void addMemberDescription(MethodDoc member, Content methodsContentTree) {
        addComment(member, methodsContentTree);
    }
    public void addMemberTags(MethodDoc member, Content methodsContentTree) {
        TagletOutputImpl output = new TagletOutputImpl("");
        TagletManager tagletManager =
            ConfigurationImpl.getInstance().tagletManager;
        TagletWriter.genTagOuput(tagletManager, member,
            tagletManager.getSerializedFormTags(),
            writer.getTagletWriterInstance(false), output);
        String outputString = output.toString().trim();
        Content dlTags = new HtmlTree(HtmlTag.DL);
        if (!outputString.isEmpty()) {
            Content tagContent = new RawHtml(outputString);
            dlTags.addContent(tagContent);
        }
        methodsContentTree.addContent(dlTags);
        MethodDoc method = member;
        if (method.name().compareTo("writeExternal") == 0
                && method.tags("serialData").length == 0) {
            serialWarning(member.position(), "doclet.MissingSerialDataTag",
                method.containingClass().qualifiedName(), method.name());
        }
    }
    protected void printTypeLinkNoDimension(Type type) {
        ClassDoc cd = type.asClassDoc();
        if (type.isPrimitive() || cd.isPackagePrivate()) {
            print(type.typeName());
        } else {
            writer.printLink(new LinkInfoImpl(
                LinkInfoImpl.CONTEXT_SERIAL_MEMBER,type));
        }
    }
}
