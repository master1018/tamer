public abstract class HtmlDocWriter extends HtmlWriter {
    public HtmlDocWriter(Configuration configuration,
                         String filename) throws IOException {
        super(configuration,
              null, configuration.destDirName + filename,
              configuration.docencoding);
        configuration.message.notice("doclet.Generating_0",
            new File(configuration.destDirName, filename));
    }
    public HtmlDocWriter(Configuration configuration,
                         String path, String filename) throws IOException {
        super(configuration,
              configuration.destDirName + path, filename,
              configuration.docencoding);
        configuration.message.notice("doclet.Generating_0",
            new File(configuration.destDirName,
                    ((path.length() > 0)? path + File.separator: "") + filename));
    }
    public abstract Configuration configuration();
    public void printHyperLink(String link, String where,
                               String label, boolean strong) {
        print(getHyperLinkString(link, where, label, strong, "", "", ""));
    }
    public void printHyperLink(String link, String where, String label) {
        printHyperLink(link, where, label, false);
    }
    public void printHyperLink(String link, String where,
                               String label, boolean strong,
                               String stylename) {
        print(getHyperLinkString(link, where, label, strong, stylename, "", ""));
    }
    public String getHyperLinkString(String link, String where,
                               String label, boolean strong) {
        return getHyperLinkString(link, where, label, strong, "", "", "");
    }
    public String getHyperLinkString(String link, String where,
                               String label, boolean strong,
                               String stylename) {
        return getHyperLinkString(link, where, label, strong, stylename, "", "");
    }
    public Content getHyperLink(String link, String where,
                               Content label) {
        return getHyperLink(link, where, label, "", "");
    }
    public String getHyperLinkString(String link, String where,
                               String label, boolean strong,
                               String stylename, String title, String target) {
        StringBuffer retlink = new StringBuffer();
        retlink.append("<a href=\"");
        retlink.append(link);
        if (where != null && where.length() != 0) {
            retlink.append("#");
            retlink.append(where);
        }
        retlink.append("\"");
        if (title != null && title.length() != 0) {
            retlink.append(" title=\"" + title + "\"");
        }
        if (target != null && target.length() != 0) {
            retlink.append(" target=\"" + target + "\"");
        }
        retlink.append(">");
        if (stylename != null && stylename.length() != 0) {
            retlink.append("<FONT CLASS=\"");
            retlink.append(stylename);
            retlink.append("\">");
        }
        if (strong) {
            retlink.append("<span class=\"strong\">");
        }
        retlink.append(label);
        if (strong) {
            retlink.append("</span>");
        }
        if (stylename != null && stylename.length() != 0) {
            retlink.append("</FONT>");
        }
        retlink.append("</a>");
        return retlink.toString();
    }
    public Content getHyperLink(String link, String where,
            Content label, String title, String target) {
        if (where != null && where.length() != 0) {
            link += "#" + where;
        }
        HtmlTree anchor = HtmlTree.A(link, label);
        if (title != null && title.length() != 0) {
            anchor.addAttr(HtmlAttr.TITLE, title);
        }
        if (target != null && target.length() != 0) {
            anchor.addAttr(HtmlAttr.TARGET, target);
        }
        return anchor;
    }
    public Content getHyperLink(String link, Content label) {
        return getHyperLink(link, "", label);
    }
    public String getHyperLinkString(String link, String label) {
        return getHyperLinkString(link, "", label, false);
    }
    public void printPkgName(ClassDoc cd) {
        print(getPkgName(cd));
    }
    public String getPkgName(ClassDoc cd) {
        String pkgName = cd.containingPackage().name();
        if (pkgName.length() > 0) {
            pkgName += ".";
            return pkgName;
        }
        return "";
    }
    public void printMemberDetailsListStartTag () {
        if (!getMemberDetailsListPrinted()) {
            dl();
            memberDetailsListPrinted = true;
        }
    }
    public void printMemberDetailsListEndTag () {
        if (getMemberDetailsListPrinted()) {
            dlEnd();
            memberDetailsListPrinted = false;
        }
    }
    public boolean getMemberDetailsListPrinted() {
        return memberDetailsListPrinted;
    }
    public void printFramesetDocument(String title, boolean noTimeStamp,
            Content frameset) {
        Content htmlDocType = DocType.Frameset();
        Content htmlComment = new Comment(configuration.getText("doclet.New_Page"));
        Content head = new HtmlTree(HtmlTag.HEAD);
        if (! noTimeStamp) {
            Content headComment = new Comment("Generated by javadoc on " + today());
            head.addContent(headComment);
        }
        if (configuration.charset.length() > 0) {
            Content meta = HtmlTree.META("Content-Type", "text/html",
                    configuration.charset);
            head.addContent(meta);
        }
        Content windowTitle = HtmlTree.TITLE(new StringContent(title));
        head.addContent(windowTitle);
        head.addContent(getFramesetJavaScript());
        Content htmlTree = HtmlTree.HTML(configuration.getLocale().getLanguage(),
                head, frameset);
        Content htmlDocument = new HtmlDocument(htmlDocType,
                htmlComment, htmlTree);
        print(htmlDocument.toString());
    }
    public String spaces(int len) {
        String space = "";
        for (int i = 0; i < len; i++) {
            space += " ";
        }
        return space;
    }
    public void printBodyHtmlEnd() {
        println();
        bodyEnd();
        htmlEnd();
    }
    public void printFooter() {
        printBodyHtmlEnd();
    }
    public void printFrameFooter() {
        htmlEnd();
    }
    public void printNbsps() {
        print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    }
    public String today() {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        return calendar.getTime().toString();
    }
}
