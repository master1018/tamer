public class HtmlWriter extends PrintWriter {
    protected final String htmlFilename;
    protected String winTitle;
    public static final String fileseparator =
         DirectoryManager.URL_FILE_SEPARATOR;
    protected Configuration configuration;
    protected boolean memberDetailsListPrinted;
    protected final String[] packageTableHeader;
    protected final String useTableSummary;
    protected final String modifierTypeHeader;
    public final Content overviewLabel;
    public final Content defaultPackageLabel;
    public final Content packageLabel;
    public final Content useLabel;
    public final Content prevLabel;
    public final Content nextLabel;
    public final Content prevclassLabel;
    public final Content nextclassLabel;
    public final Content summaryLabel;
    public final Content detailLabel;
    public final Content framesLabel;
    public final Content noframesLabel;
    public final Content treeLabel;
    public final Content classLabel;
    public final Content deprecatedLabel;
    public final Content deprecatedPhrase;
    public final Content allclassesLabel;
    public final Content indexLabel;
    public final Content helpLabel;
    public final Content seeLabel;
    public final Content descriptionLabel;
    public final Content prevpackageLabel;
    public final Content nextpackageLabel;
    public final Content packagesLabel;
    public final Content methodDetailsLabel;
    public final Content annotationTypeDetailsLabel;
    public final Content fieldDetailsLabel;
    public final Content constructorDetailsLabel;
    public final Content enumConstantsDetailsLabel;
    public final Content specifiedByLabel;
    public final Content overridesLabel;
    public final Content descfrmClassLabel;
    public final Content descfrmInterfaceLabel;
    public HtmlWriter(Configuration configuration,
                      String path, String filename, String docencoding)
                      throws IOException, UnsupportedEncodingException {
        super(Util.genWriter(configuration, path, filename, docencoding));
        this.configuration = configuration;
        htmlFilename = filename;
        this.memberDetailsListPrinted = false;
        packageTableHeader = new String[] {
            configuration.getText("doclet.Package"),
            configuration.getText("doclet.Description")
        };
        useTableSummary = configuration.getText("doclet.Use_Table_Summary",
                configuration.getText("doclet.packages"));
        modifierTypeHeader = configuration.getText("doclet.0_and_1",
                configuration.getText("doclet.Modifier"),
                configuration.getText("doclet.Type"));
        overviewLabel = getResource("doclet.Overview");
        defaultPackageLabel = new RawHtml(
                DocletConstants.DEFAULT_PACKAGE_NAME);
        packageLabel = getResource("doclet.Package");
        useLabel = getResource("doclet.navClassUse");
        prevLabel = getResource("doclet.Prev");
        nextLabel = getResource("doclet.Next");
        prevclassLabel = getResource("doclet.Prev_Class");
        nextclassLabel = getResource("doclet.Next_Class");
        summaryLabel = getResource("doclet.Summary");
        detailLabel = getResource("doclet.Detail");
        framesLabel = getResource("doclet.Frames");
        noframesLabel = getResource("doclet.No_Frames");
        treeLabel = getResource("doclet.Tree");
        classLabel = getResource("doclet.Class");
        deprecatedLabel = getResource("doclet.navDeprecated");
        deprecatedPhrase = getResource("doclet.Deprecated");
        allclassesLabel = getResource("doclet.All_Classes");
        indexLabel = getResource("doclet.Index");
        helpLabel = getResource("doclet.Help");
        seeLabel = getResource("doclet.See");
        descriptionLabel = getResource("doclet.Description");
        prevpackageLabel = getResource("doclet.Prev_Package");
        nextpackageLabel = getResource("doclet.Next_Package");
        packagesLabel = getResource("doclet.Packages");
        methodDetailsLabel = getResource("doclet.Method_Detail");
        annotationTypeDetailsLabel = getResource("doclet.Annotation_Type_Member_Detail");
        fieldDetailsLabel = getResource("doclet.Field_Detail");
        constructorDetailsLabel = getResource("doclet.Constructor_Detail");
        enumConstantsDetailsLabel = getResource("doclet.Enum_Constant_Detail");
        specifiedByLabel = getResource("doclet.Specified_By");
        overridesLabel = getResource("doclet.Overrides");
        descfrmClassLabel = getResource("doclet.Description_From_Class");
        descfrmInterfaceLabel = getResource("doclet.Description_From_Interface");
    }
    public Content getResource(String key) {
        return new StringContent(configuration.getText(key));
    }
    public Content getResource(String key, String a1) {
        return new RawHtml(configuration.getText(key, a1));
    }
    public Content getResource(String key, String a1, String a2) {
        return new RawHtml(configuration.getText(key, a1, a2));
    }
    public void html() {
        println("<HTML lang=\"" + configuration.getLocale().getLanguage() + "\">");
    }
    public void htmlEnd() {
        println("</HTML>");
    }
    protected void printWinTitleScript(String winTitle){
        if(winTitle != null && winTitle.length() > 0) {
            script();
            println("function windowTitle()");
            println("{");
            println("    if (location.href.indexOf('is-external=true') == -1) {");
            println("        parent.document.title=\"" + winTitle + "\";");
            println("    }");
            println("}");
            scriptEnd();
            noScript();
            noScriptEnd();
        }
    }
    protected HtmlTree getWinTitleScript(){
        HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
        if(winTitle != null && winTitle.length() > 0) {
            script.addAttr(HtmlAttr.TYPE, "text/javascript");
            String scriptCode = "<!--" + DocletConstants.NL +
                    "    if (location.href.indexOf('is-external=true') == -1) {" + DocletConstants.NL +
                    "        parent.document.title=\"" + winTitle + "\";" + DocletConstants.NL +
                    "    }" + DocletConstants.NL +
                    "
            RawHtml scriptContent = new RawHtml(scriptCode);
            script.addContent(scriptContent);
        }
        return script;
    }
    protected Content getFramesetJavaScript(){
        HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
        script.addAttr(HtmlAttr.TYPE, "text/javascript");
        String scriptCode = DocletConstants.NL + "    targetPage = \"\" + window.location.search;" + DocletConstants.NL +
                "    if (targetPage != \"\" && targetPage != \"undefined\")" + DocletConstants.NL +
                "        targetPage = targetPage.substring(1);" + DocletConstants.NL +
                "    if (targetPage.indexOf(\":\") != -1)" + DocletConstants.NL +
                "        targetPage = \"undefined\";" + DocletConstants.NL +
                "    function loadFrames() {" + DocletConstants.NL +
                "        if (targetPage != \"\" && targetPage != \"undefined\")" + DocletConstants.NL +
                "             top.classFrame.location = top.targetPage;" + DocletConstants.NL +
                "    }" + DocletConstants.NL;
        RawHtml scriptContent = new RawHtml(scriptCode);
        script.addContent(scriptContent);
        return script;
    }
    public void script() {
        println("<SCRIPT type=\"text/javascript\">");
    }
    public void scriptEnd() {
        println("</SCRIPT>");
    }
    public void noScript() {
        println("<NOSCRIPT>");
    }
    public void noScriptEnd() {
        println("</NOSCRIPT>");
    }
    protected String getWindowTitleOnload(){
        if(winTitle != null && winTitle.length() > 0) {
            return " onload=\"windowTitle();\"";
        } else {
            return "";
        }
    }
    public void body(String bgcolor, boolean includeScript) {
        print("<BODY BGCOLOR=\"" + bgcolor + "\"");
        if (includeScript) {
            print(getWindowTitleOnload());
        }
        println(">");
    }
    public HtmlTree getBody(boolean includeScript, String title) {
        HtmlTree body = new HtmlTree(HtmlTag.BODY);
        this.winTitle = title;
        if (includeScript) {
            body.addContent(getWinTitleScript());
            Content noScript = HtmlTree.NOSCRIPT(
                    HtmlTree.DIV(getResource("doclet.No_Script_Message")));
            body.addContent(noScript);
        }
        return body;
    }
    public void bodyEnd() {
        println("</BODY>");
    }
    public void title() {
        println("<TITLE>");
    }
    public void title(String winTitle) {
        this.winTitle = winTitle;
        title();
    }
    public HtmlTree getTitle() {
        HtmlTree title = HtmlTree.TITLE(new StringContent(winTitle));
        return title;
    }
    public void titleEnd() {
        println("</TITLE>");
    }
    public void ul() {
        println("<UL>");
    }
    public void ulEnd() {
        println("</UL>");
    }
    public void li() {
        print("<LI>");
    }
    public void li(String type) {
        print("<LI TYPE=\"" + type + "\">");
    }
    public void h1() {
        println("<H1>");
    }
    public void h1End() {
        println("</H1>");
    }
    public void h1(String text) {
        h1();
        println(text);
        h1End();
    }
    public void h2() {
        println("<H2>");
    }
    public void h2(String text) {
        h2();
        println(text);
        h2End();
    }
    public void h2End() {
        println("</H2>");
    }
    public void h3() {
        println("<H3>");
    }
    public void h3(String text) {
        h3();
        println(text);
        h3End();
    }
    public void h3End() {
        println("</H3>");
    }
    public void h4() {
        println("<H4>");
    }
    public void h4End() {
        println("</H4>");
    }
    public void h4(String text) {
        h4();
        println(text);
        h4End();
    }
    public void h5() {
        println("<H5>");
    }
    public void h5End() {
        println("</H5>");
    }
    public void img(String imggif, String imgname, int width, int height) {
        println("<IMG SRC=\"images/" + imggif + ".gif\""
              + " WIDTH=\"" + width + "\" HEIGHT=\"" + height
              + "\" ALT=\"" + imgname + "\">");
    }
    public void menu() {
        println("<MENU>");
    }
    public void menuEnd() {
        println("</MENU>");
    }
    public void pre() {
        println("<PRE>");
    }
    public void preNoNewLine() {
        print("<PRE>");
    }
    public void preEnd() {
        println("</PRE>");
    }
    public void hr() {
        println("<HR>");
    }
    public void hr(int size, int widthPercent) {
        println("<HR SIZE=\"" + size + "\" WIDTH=\"" + widthPercent + "%\">");
    }
    public void hr(int size, String noshade) {
        println("<HR SIZE=\"" + size + "\" NOSHADE>");
    }
    public String getStrong() {
        return "<STRONG>";
    }
    public String getStrongEnd() {
        return "</STRONG>";
    }
    public void strong() {
        print("<STRONG>");
    }
    public void strongEnd() {
        print("</STRONG>");
    }
    public void strong(String text) {
        strong();
        print(text);
        strongEnd();
    }
    public void italics(String text) {
        print("<I>");
        print(text);
        println("</I>");
    }
    public String italicsText(String text) {
        return "<i>" + text + "</i>";
    }
    public String codeText(String text) {
        return "<code>" + text + "</code>";
    }
    public void space() {
        print("&nbsp;");
    }
    public Content getSpace() {
        return RawHtml.nbsp;
    }
    public void dl() {
        println("<DL>");
    }
    public void dlEnd() {
        println("</DL>");
    }
    public void dt() {
        print("<DT>");
    }
    public void dtEnd() {
        print("</DT>");
    }
    public void dd() {
        print("<DD>");
    }
    public void ddEnd() {
        println("</DD>");
    }
    public void sup() {
        println("<SUP>");
    }
    public void supEnd() {
        println("</SUP>");
    }
    public void font(String size) {
        println("<FONT SIZE=\"" + size + "\">");
    }
    public void fontNoNewLine(String size) {
        print("<FONT SIZE=\"" + size + "\">");
    }
    public void fontStyle(String stylename) {
        print("<FONT CLASS=\"" + stylename + "\">");
    }
    public void fontSizeStyle(String size, String stylename) {
        println("<FONT size=\"" + size + "\" CLASS=\"" + stylename + "\">");
    }
    public void fontEnd() {
        print("</FONT>");
    }
    public String getFontColor(String color) {
        return "<FONT COLOR=\"" + color + "\">";
    }
    public String getFontEnd() {
        return "</FONT>";
    }
    public void center() {
        println("<CENTER>");
    }
    public void centerEnd() {
        println("</CENTER>");
    }
    public void aName(String name) {
        print("<A NAME=\"" + name + "\">");
    }
    public void aEnd() {
        print("</A>");
    }
    public void italic() {
        print("<I>");
    }
    public void italicEnd() {
        print("</I>");
    }
    public void anchor(String name, String content) {
        aName(name);
        print(content);
        aEnd();
    }
    public void anchor(String name) {
        anchor(name, "<!-- -->");
    }
    public void p() {
        println();
        println("<P>");
    }
    public void pEnd() {
        println();
        println("</P>");
    }
    public void br() {
        println();
        println("<BR>");
    }
    public void address() {
        println("<ADDRESS>");
    }
    public void addressEnd() {
        println("</ADDRESS>");
    }
    public void head() {
        println("<HEAD>");
    }
    public void headEnd() {
        println("</HEAD>");
    }
    public void code() {
        print("<CODE>");
    }
    public void codeEnd() {
        print("</CODE>");
    }
    public void em() {
        println("<EM>");
    }
    public void emEnd() {
        println("</EM>");
    }
    public void table(int border, String width, int cellpadding,
                      int cellspacing) {
        println(DocletConstants.NL +
                "<TABLE BORDER=\"" + border +
                "\" WIDTH=\"" + width +
                "\" CELLPADDING=\"" + cellpadding +
                "\" CELLSPACING=\"" + cellspacing +
                "\" SUMMARY=\"\">");
    }
    public void table(int border, String width, int cellpadding,
                      int cellspacing, String summary) {
        println(DocletConstants.NL +
                "<TABLE BORDER=\"" + border +
                "\" WIDTH=\"" + width +
                "\" CELLPADDING=\"" + cellpadding +
                "\" CELLSPACING=\"" + cellspacing +
                "\" SUMMARY=\"" + summary + "\">");
    }
    public void table(int border, int cellpadding, int cellspacing) {
        println(DocletConstants.NL +
                "<TABLE BORDER=\"" + border +
                "\" CELLPADDING=\"" + cellpadding +
                "\" CELLSPACING=\"" + cellspacing +
                "\" SUMMARY=\"\">");
    }
    public void table(int border, int cellpadding, int cellspacing, String summary) {
        println(DocletConstants.NL +
                "<TABLE BORDER=\"" + border +
                "\" CELLPADDING=\"" + cellpadding +
                "\" CELLSPACING=\"" + cellspacing +
                "\" SUMMARY=\"" + summary + "\">");
    }
    public void table(int border, String width) {
        println(DocletConstants.NL +
                "<TABLE BORDER=\"" + border +
                "\" WIDTH=\"" + width +
                "\" SUMMARY=\"\">");
    }
    public void table() {
        table(0, "100%");
    }
    public void tableEnd() {
        println("</TABLE>");
    }
    public void tr() {
        println("<TR>");
    }
    public void trEnd() {
        println("</TR>");
    }
    public void td() {
        print("<TD>");
    }
    public void tdNowrap() {
        print("<TD NOWRAP>");
    }
    public void tdWidth(String width) {
        print("<TD WIDTH=\"" + width + "\">");
    }
    public void tdEnd() {
        println("</TD>");
    }
    public void link(String str) {
        println("<LINK " + str + ">");
    }
    public void commentStart() {
         print("<!-- ");
    }
    public void commentEnd() {
         println("-->");
    }
    public void captionStyle(String stylename) {
        println("<CAPTION CLASS=\"" + stylename + "\">");
    }
    public void captionEnd() {
        println("</CAPTION>");
    }
    public void trBgcolorStyle(String color, String stylename) {
        println("<TR BGCOLOR=\"" + color + "\" CLASS=\"" + stylename + "\">");
    }
    public void trBgcolor(String color) {
        println("<TR BGCOLOR=\"" + color + "\">");
    }
    public void trAlignVAlign(String align, String valign) {
        println("<TR ALIGN=\"" + align + "\" VALIGN=\"" + valign + "\">");
    }
    public void thAlign(String align) {
        print("<TH ALIGN=\"" + align + "\">");
    }
    public void thScopeNoWrap(String stylename, String scope) {
        print("<TH CLASS=\"" + stylename + "\" SCOPE=\"" + scope + "\" NOWRAP>");
    }
    public String getModifierTypeHeader() {
        return modifierTypeHeader;
    }
    public void thAlignColspan(String align, int i) {
        print("<TH ALIGN=\"" + align + "\" COLSPAN=\"" + i + "\">");
    }
    public void thAlignNowrap(String align) {
        print("<TH ALIGN=\"" + align + "\" NOWRAP>");
    }
    public void thEnd() {
        println("</TH>");
    }
    public void tdColspan(int i) {
        print("<TD COLSPAN=" + i + ">");
    }
    public void tdBgcolorStyle(String color, String stylename) {
        print("<TD BGCOLOR=\"" + color + "\" CLASS=\"" + stylename + "\">");
    }
    public void tdColspanBgcolorStyle(int i, String color, String stylename) {
        print("<TD COLSPAN=" + i + " BGCOLOR=\"" + color + "\" CLASS=\"" +
              stylename + "\">");
    }
    public void tdAlign(String align) {
        print("<TD ALIGN=\"" + align + "\">");
    }
    public void tdVAlignClass(String align, String stylename) {
        print("<TD VALIGN=\"" + align + "\" CLASS=\"" + stylename + "\">");
    }
    public void tdVAlign(String valign) {
        print("<TD VALIGN=\"" + valign + "\">");
    }
    public void tdAlignVAlign(String align, String valign) {
        print("<TD ALIGN=\"" + align + "\" VALIGN=\"" + valign + "\">");
    }
    public void tdAlignRowspan(String align, int rowspan) {
        print("<TD ALIGN=\"" + align + "\" ROWSPAN=" + rowspan + ">");
    }
    public void tdAlignVAlignRowspan(String align, String valign,
                                     int rowspan) {
        print("<TD ALIGN=\"" + align + "\" VALIGN=\"" + valign
                + "\" ROWSPAN=" + rowspan + ">");
    }
    public void blockquote() {
        println("<BLOCKQUOTE>");
    }
    public void blockquoteEnd() {
        println("</BLOCKQUOTE>");
    }
    public String getCode() {
        return "<code>";
    }
    public String getCodeEnd() {
        return "</code>";
    }
    public void noFrames() {
        println("<NOFRAMES>");
    }
    public void noFramesEnd() {
        println("</NOFRAMES>");
    }
}
