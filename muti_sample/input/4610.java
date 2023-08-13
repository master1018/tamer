public class AbstractIndexWriter extends HtmlDocletWriter {
    protected IndexBuilder indexbuilder;
    protected AbstractIndexWriter(ConfigurationImpl configuration,
                                  String path, String filename,
                                  String relpath, IndexBuilder indexbuilder)
                                  throws IOException {
        super(configuration, path, filename, relpath);
        this.indexbuilder = indexbuilder;
    }
    protected AbstractIndexWriter(ConfigurationImpl configuration,
                                  String filename, IndexBuilder indexbuilder)
                                  throws IOException {
        super(configuration, filename);
        this.indexbuilder = indexbuilder;
    }
    protected Content getNavLinkIndex() {
        Content li = HtmlTree.LI(HtmlStyle.navBarCell1Rev, indexLabel);
        return li;
    }
    protected void addContents(Character unicode, List<? extends Doc> memberlist,
            Content contentTree) {
        contentTree.addContent(getMarkerAnchor("_" + unicode + "_"));
        Content headContent = new StringContent(unicode.toString());
        Content heading = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING, false,
                HtmlStyle.title, headContent);
        contentTree.addContent(heading);
        int memberListSize = memberlist.size();
        if (memberListSize > 0) {
            Content dl = new HtmlTree(HtmlTag.DL);
            for (int i = 0; i < memberListSize; i++) {
                Doc element = memberlist.get(i);
                if (element instanceof MemberDoc) {
                    addDescription((MemberDoc)element, dl);
                } else if (element instanceof ClassDoc) {
                    addDescription((ClassDoc)element, dl);
                } else if (element instanceof PackageDoc) {
                    addDescription((PackageDoc)element, dl);
                }
            }
            contentTree.addContent(dl);
        }
    }
    protected void addDescription(PackageDoc pkg, Content dlTree) {
        Content link = getPackageLink(pkg, new StringContent(Util.getPackageName(pkg)));
        Content dt = HtmlTree.DT(link);
        dt.addContent(" - ");
        dt.addContent(getResource("doclet.package"));
        dt.addContent(" " + pkg.name());
        dlTree.addContent(dt);
        Content dd = new HtmlTree(HtmlTag.DD);
        addSummaryComment(pkg, dd);
        dlTree.addContent(dd);
    }
    protected void addDescription(ClassDoc cd, Content dlTree) {
        Content link = new RawHtml(
                getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_INDEX, cd, true)));
        Content dt = HtmlTree.DT(link);
        dt.addContent(" - ");
        addClassInfo(cd, dt);
        dlTree.addContent(dt);
        Content dd = new HtmlTree(HtmlTag.DD);
        addComment(cd, dd);
        dlTree.addContent(dd);
    }
    protected void addClassInfo(ClassDoc cd, Content contentTree) {
        contentTree.addContent(getResource("doclet.in",
                Util.getTypeName(configuration, cd, false),
                getPackageLinkString(cd.containingPackage(),
                Util.getPackageName(cd.containingPackage()), false)));
    }
    protected void addDescription(MemberDoc member, Content dlTree) {
        String name = (member instanceof ExecutableMemberDoc)?
            member.name() + ((ExecutableMemberDoc)member).flatSignature() :
            member.name();
        if (name.indexOf("<") != -1 || name.indexOf(">") != -1) {
                name = Util.escapeHtmlChars(name);
        }
        Content span = HtmlTree.SPAN(HtmlStyle.strong,
                getDocLink(LinkInfoImpl.CONTEXT_INDEX, member, name));
        Content dt = HtmlTree.DT(span);
        dt.addContent(" - ");
        addMemberDesc(member, dt);
        dlTree.addContent(dt);
        Content dd = new HtmlTree(HtmlTag.DD);
        addComment(member, dd);
        dlTree.addContent(dd);
    }
    protected void addComment(ProgramElementDoc element, Content contentTree) {
        Tag[] tags;
        Content span = HtmlTree.SPAN(HtmlStyle.strong, deprecatedPhrase);
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.addStyle(HtmlStyle.block);
        if (Util.isDeprecated(element)) {
            div.addContent(span);
            if ((tags = element.tags("deprecated")).length > 0)
                addInlineDeprecatedComment(element, tags[0], div);
            contentTree.addContent(div);
        } else {
            ClassDoc cont = element.containingClass();
            while (cont != null) {
                if (Util.isDeprecated(cont)) {
                    div.addContent(span);
                    contentTree.addContent(div);
                    break;
                }
                cont = cont.containingClass();
            }
            addSummaryComment(element, contentTree);
        }
    }
    protected void addMemberDesc(MemberDoc member, Content contentTree) {
        ClassDoc containing = member.containingClass();
        String classdesc = Util.getTypeName(
                configuration, containing, true) + " ";
        if (member.isField()) {
            if (member.isStatic()) {
                contentTree.addContent(
                        getResource("doclet.Static_variable_in", classdesc));
            } else {
                contentTree.addContent(
                        getResource("doclet.Variable_in", classdesc));
            }
        } else if (member.isConstructor()) {
            contentTree.addContent(
                    getResource("doclet.Constructor_for", classdesc));
        } else if (member.isMethod()) {
            if (member.isStatic()) {
                contentTree.addContent(
                        getResource("doclet.Static_method_in", classdesc));
            } else {
                contentTree.addContent(
                        getResource("doclet.Method_in", classdesc));
            }
        }
        addPreQualifiedClassLink(LinkInfoImpl.CONTEXT_INDEX, containing,
                false, contentTree);
    }
}
