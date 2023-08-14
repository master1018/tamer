public class AnnotationTypeWriterImpl extends SubWriterHolderWriter
        implements AnnotationTypeWriter {
    protected AnnotationTypeDoc annotationType;
    protected Type prev;
    protected Type next;
    public AnnotationTypeWriterImpl (AnnotationTypeDoc annotationType,
            Type prevType, Type nextType)
    throws Exception {
        super(ConfigurationImpl.getInstance(),
              DirectoryManager.getDirectoryPath(annotationType.containingPackage()),
              annotationType.name() + ".html",
              DirectoryManager.getRelativePath(annotationType.containingPackage().name()));
        this.annotationType = annotationType;
        configuration.currentcd = annotationType.asClassDoc();
        this.prev = prevType;
        this.next = nextType;
    }
    protected Content getNavLinkPackage() {
        Content linkContent = getHyperLink("package-summary.html", "",
                packageLabel);
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
    protected Content getNavLinkClass() {
        Content li = HtmlTree.LI(HtmlStyle.navBarCell1Rev, classLabel);
        return li;
    }
    protected Content getNavLinkClassUse() {
        Content linkContent = getHyperLink("class-use/" + filename, "", useLabel);
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
    public Content getNavLinkPrevious() {
        Content li;
        if (prev != null) {
            Content prevLink = new RawHtml(getLink(new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_CLASS, prev.asClassDoc(), "",
                    configuration.getText("doclet.Prev_Class"), true)));
            li = HtmlTree.LI(prevLink);
        }
        else
            li = HtmlTree.LI(prevclassLabel);
        return li;
    }
    public Content getNavLinkNext() {
        Content li;
        if (next != null) {
            Content nextLink = new RawHtml(getLink(new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_CLASS, next.asClassDoc(), "",
                    configuration.getText("doclet.Next_Class"), true)));
            li = HtmlTree.LI(nextLink);
        }
        else
            li = HtmlTree.LI(nextclassLabel);
        return li;
    }
    public Content getHeader(String header) {
        String pkgname = (annotationType.containingPackage() != null)?
            annotationType.containingPackage().name(): "";
        String clname = annotationType.name();
        Content bodyTree = getBody(true, getWindowTitle(clname));
        addTop(bodyTree);
        addNavLinks(true, bodyTree);
        bodyTree.addContent(HtmlConstants.START_OF_CLASS_DATA);
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.addStyle(HtmlStyle.header);
        if (pkgname.length() > 0) {
            Content pkgNameContent = new StringContent(pkgname);
            Content pkgNameDiv = HtmlTree.DIV(HtmlStyle.subTitle, pkgNameContent);
            div.addContent(pkgNameDiv);
        }
        LinkInfoImpl linkInfo = new LinkInfoImpl(
                LinkInfoImpl.CONTEXT_CLASS_HEADER, annotationType, false);
        Content headerContent = new StringContent(header);
        Content heading = HtmlTree.HEADING(HtmlConstants.CLASS_PAGE_HEADING, true,
                HtmlStyle.title, headerContent);
        heading.addContent(new RawHtml(getTypeParameterLinks(linkInfo)));
        div.addContent(heading);
        bodyTree.addContent(div);
        return bodyTree;
    }
    public Content getAnnotationContentHeader() {
        return getContentHeader();
    }
    public void addFooter(Content contentTree) {
        contentTree.addContent(HtmlConstants.END_OF_CLASS_DATA);
        addNavLinks(false, contentTree);
        addBottom(contentTree);
    }
    public void printDocument(Content contentTree) {
        printHtmlDocument(configuration.metakeywords.getMetaKeywords(annotationType),
                true, contentTree);
    }
    public Content getAnnotationInfoTreeHeader() {
        return getMemberTreeHeader();
    }
    public Content getAnnotationInfo(Content annotationInfoTree) {
        return getMemberTree(HtmlStyle.description, annotationInfoTree);
    }
    public void addAnnotationTypeSignature(String modifiers, Content annotationInfoTree) {
        annotationInfoTree.addContent(new HtmlTree(HtmlTag.BR));
        Content pre = new HtmlTree(HtmlTag.PRE);
        addAnnotationInfo(annotationType, pre);
        pre.addContent(modifiers);
        LinkInfoImpl linkInfo = new LinkInfoImpl(
                LinkInfoImpl.CONTEXT_CLASS_SIGNATURE, annotationType, false);
        Content annotationName = new StringContent(annotationType.name());
        Content parameterLinks = new RawHtml(getTypeParameterLinks(linkInfo));
        if (configuration().linksource) {
            addSrcLink(annotationType, annotationName, pre);
            pre.addContent(parameterLinks);
        } else {
            Content span = HtmlTree.SPAN(HtmlStyle.strong, annotationName);
            span.addContent(parameterLinks);
            pre.addContent(span);
        }
        annotationInfoTree.addContent(pre);
    }
    public void addAnnotationTypeDescription(Content annotationInfoTree) {
        if(!configuration.nocomment) {
            if (annotationType.inlineTags().length > 0) {
                addInlineComment(annotationType, annotationInfoTree);
            }
        }
    }
    public void addAnnotationTypeTagInfo(Content annotationInfoTree) {
        if(!configuration.nocomment) {
            addTagsInfo(annotationType, annotationInfoTree);
        }
    }
    public void addAnnotationTypeDeprecationInfo(Content annotationInfoTree) {
        Content hr = new HtmlTree(HtmlTag.HR);
        annotationInfoTree.addContent(hr);
        Tag[] deprs = annotationType.tags("deprecated");
        if (Util.isDeprecated(annotationType)) {
            Content strong = HtmlTree.STRONG(deprecatedPhrase);
            Content div = HtmlTree.DIV(HtmlStyle.block, strong);
            if (deprs.length > 0) {
                Tag[] commentTags = deprs[0].inlineTags();
                if (commentTags.length > 0) {
                    div.addContent(getSpace());
                    addInlineDeprecatedComment(annotationType, deprs[0], div);
                }
            }
            annotationInfoTree.addContent(div);
        }
    }
    public void addAnnotationDetailsMarker(Content memberDetails) {
        memberDetails.addContent(HtmlConstants.START_OF_ANNOTATION_TYPE_DETAILS);
    }
    protected Content getNavLinkTree() {
        Content treeLinkContent = getHyperLink("package-tree.html",
                "", treeLabel, "", "");
        Content li = HtmlTree.LI(treeLinkContent);
        return li;
    }
    protected void addSummaryDetailLinks(Content subDiv) {
        try {
            Content div = HtmlTree.DIV(getNavSummaryLinks());
            div.addContent(getNavDetailLinks());
            subDiv.addContent(div);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DocletAbortException();
        }
    }
    protected Content getNavSummaryLinks() throws Exception {
        Content li = HtmlTree.LI(summaryLabel);
        li.addContent(getSpace());
        Content ulNav = HtmlTree.UL(HtmlStyle.subNavList, li);
        MemberSummaryBuilder memberSummaryBuilder = (MemberSummaryBuilder)
                configuration.getBuilderFactory().getMemberSummaryBuilder(this);
        Content liNavReq = new HtmlTree(HtmlTag.LI);
        addNavSummaryLink(memberSummaryBuilder,
                "doclet.navAnnotationTypeRequiredMember",
                VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED, liNavReq);
        addNavGap(liNavReq);
        ulNav.addContent(liNavReq);
        Content liNavOpt = new HtmlTree(HtmlTag.LI);
        addNavSummaryLink(memberSummaryBuilder,
                "doclet.navAnnotationTypeOptionalMember",
                VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL, liNavOpt);
        ulNav.addContent(liNavOpt);
        return ulNav;
    }
    protected void addNavSummaryLink(MemberSummaryBuilder builder,
            String label, int type, Content liNav) {
        AbstractMemberWriter writer = ((AbstractMemberWriter) builder.
                getMemberSummaryWriter(type));
        if (writer == null) {
            liNav.addContent(getResource(label));
        } else {
            liNav.addContent(writer.getNavSummaryLink(null,
                    ! builder.getVisibleMemberMap(type).noVisibleMembers()));
        }
    }
    protected Content getNavDetailLinks() throws Exception {
        Content li = HtmlTree.LI(detailLabel);
        li.addContent(getSpace());
        Content ulNav = HtmlTree.UL(HtmlStyle.subNavList, li);
        MemberSummaryBuilder memberSummaryBuilder = (MemberSummaryBuilder)
                configuration.getBuilderFactory().getMemberSummaryBuilder(this);
        AbstractMemberWriter writerOptional =
                ((AbstractMemberWriter) memberSummaryBuilder.
                getMemberSummaryWriter(VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL));
        AbstractMemberWriter writerRequired =
                ((AbstractMemberWriter) memberSummaryBuilder.
                getMemberSummaryWriter(VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED));
        if (writerOptional != null){
            Content liNavOpt = new HtmlTree(HtmlTag.LI);
            writerOptional.addNavDetailLink(annotationType.elements().length > 0, liNavOpt);
            ulNav.addContent(liNavOpt);
        } else if (writerRequired != null){
            Content liNavReq = new HtmlTree(HtmlTag.LI);
            writerRequired.addNavDetailLink(annotationType.elements().length > 0, liNavReq);
            ulNav.addContent(liNavReq);
        } else {
            Content liNav = HtmlTree.LI(getResource("doclet.navAnnotationTypeMember"));
            ulNav.addContent(liNav);
        }
        return ulNav;
    }
    protected void addNavGap(Content liNav) {
        liNav.addContent(getSpace());
        liNav.addContent("|");
        liNav.addContent(getSpace());
    }
    public AnnotationTypeDoc getAnnotationTypeDoc() {
        return annotationType;
    }
}
