public class LinkFactoryImpl extends LinkFactory {
    private HtmlDocletWriter m_writer;
    public LinkFactoryImpl(HtmlDocletWriter writer) {
        m_writer = writer;
    }
    protected LinkOutput getOutputInstance() {
        return new LinkOutputImpl();
    }
    protected LinkOutput getClassLink(LinkInfo linkInfo) {
        LinkInfoImpl classLinkInfo = (LinkInfoImpl) linkInfo;
        boolean noLabel = linkInfo.label == null || linkInfo.label.length() == 0;
        ClassDoc classDoc = classLinkInfo.classDoc;
        String title =
            (classLinkInfo.where == null || classLinkInfo.where.length() == 0) ?
                getClassToolTip(classDoc,
                    classLinkInfo.type != null &&
                    !classDoc.qualifiedTypeName().equals(classLinkInfo.type.qualifiedTypeName())) :
            "";
        StringBuffer label = new StringBuffer(
            classLinkInfo.getClassLinkLabel(m_writer.configuration));
        classLinkInfo.displayLength += label.length();
        Configuration configuration = ConfigurationImpl.getInstance();
        LinkOutputImpl linkOutput = new LinkOutputImpl();
        if (classDoc.isIncluded()) {
            if (configuration.isGeneratedDoc(classDoc)) {
                String filename = pathString(classLinkInfo);
                if (linkInfo.linkToSelf ||
                                !(linkInfo.classDoc.name() + ".html").equals(m_writer.filename)) {
                        linkOutput.append(m_writer.getHyperLinkString(filename,
                            classLinkInfo.where, label.toString(),
                            classLinkInfo.isStrong, classLinkInfo.styleName,
                            title, classLinkInfo.target));
                        if (noLabel && !classLinkInfo.excludeTypeParameterLinks) {
                            linkOutput.append(getTypeParameterLinks(linkInfo).toString());
                        }
                        return linkOutput;
                }
            }
        } else {
            String crossLink = m_writer.getCrossClassLink(
                classDoc.qualifiedName(), classLinkInfo.where,
                label.toString(), classLinkInfo.isStrong, classLinkInfo.styleName,
                true);
            if (crossLink != null) {
                linkOutput.append(crossLink);
                if (noLabel && !classLinkInfo.excludeTypeParameterLinks) {
                    linkOutput.append(getTypeParameterLinks(linkInfo).toString());
                }
                return linkOutput;
            }
        }
        linkOutput.append(label.toString());
        if (noLabel && !classLinkInfo.excludeTypeParameterLinks) {
            linkOutput.append(getTypeParameterLinks(linkInfo).toString());
        }
        return linkOutput;
    }
    protected LinkOutput getTypeParameterLink(LinkInfo linkInfo,
        Type typeParam) {
        LinkInfoImpl typeLinkInfo = new LinkInfoImpl(linkInfo.getContext(),
            typeParam);
        typeLinkInfo.excludeTypeBounds = linkInfo.excludeTypeBounds;
        typeLinkInfo.excludeTypeParameterLinks = linkInfo.excludeTypeParameterLinks;
        typeLinkInfo.linkToSelf = linkInfo.linkToSelf;
        LinkOutput output = getLinkOutput(typeLinkInfo);
        ((LinkInfoImpl) linkInfo).displayLength += typeLinkInfo.displayLength;
        return output;
    }
    private String getClassToolTip(ClassDoc classDoc, boolean isTypeLink) {
        Configuration configuration = ConfigurationImpl.getInstance();
        if (isTypeLink) {
            return configuration.getText("doclet.Href_Type_Param_Title",
            classDoc.name());
        } else if (classDoc.isInterface()){
            return configuration.getText("doclet.Href_Interface_Title",
                Util.getPackageName(classDoc.containingPackage()));
        } else if (classDoc.isAnnotationType()) {
            return configuration.getText("doclet.Href_Annotation_Title",
                Util.getPackageName(classDoc.containingPackage()));
        } else if (classDoc.isEnum()) {
            return configuration.getText("doclet.Href_Enum_Title",
                Util.getPackageName(classDoc.containingPackage()));
        } else {
            return configuration.getText("doclet.Href_Class_Title",
                Util.getPackageName(classDoc.containingPackage()));
        }
    }
    private String pathString(LinkInfoImpl linkInfo) {
        if (linkInfo.context == LinkInfoImpl.PACKAGE_FRAME) {
            return linkInfo.classDoc.name() + ".html";
        }
        StringBuffer buf = new StringBuffer(m_writer.relativePath);
        buf.append(DirectoryManager.getPathToPackage(
            linkInfo.classDoc.containingPackage(),
            linkInfo.classDoc.name() + ".html"));
        return buf.toString();
    }
}
