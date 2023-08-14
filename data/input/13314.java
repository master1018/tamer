public class TestHtmlDocument {
    private static final String BUGID = "6851834";
    private static final String BUGNAME = "TestHtmlDocument";
    private static final String FS = System.getProperty("file.separator");
    private static final String LS = System.getProperty("line.separator");
    private static String srcdir = System.getProperty("test.src", ".");
    public static void main(String[] args) throws IOException {
        if (generateHtmlTree().equals(readFileToString(srcdir + FS + "testMarkup.html"))) {
            System.out.println("\nTest passed for bug " + BUGID + " (" + BUGNAME + ")\n");
        } else {
            throw new Error("\nTest failed for bug " + BUGID + " (" + BUGNAME + ")\n");
        }
    }
    public static String generateHtmlTree() {
        DocType htmlDocType = DocType.Transitional();
        HtmlTree html = new HtmlTree(HtmlTag.HTML);
        HtmlTree head = new HtmlTree(HtmlTag.HEAD);
        HtmlTree title = new HtmlTree(HtmlTag.TITLE);
        StringContent titleContent = new StringContent("Markup test");
        title.addContent(titleContent);
        head.addContent(title);
        HtmlTree meta = new HtmlTree(HtmlTag.META);
        meta.addAttr(HtmlAttr.NAME, "keywords");
        meta.addAttr(HtmlAttr.CONTENT, "testContent");
        head.addContent(meta);
        HtmlTree invmeta = new HtmlTree(HtmlTag.META);
        head.addContent(invmeta);
        HtmlTree link = new HtmlTree(HtmlTag.LINK);
        link.addAttr(HtmlAttr.REL, "testRel");
        link.addAttr(HtmlAttr.HREF, "testLink.html");
        head.addContent(link);
        HtmlTree invlink = new HtmlTree(HtmlTag.LINK);
        head.addContent(invlink);
        html.addContent(head);
        Comment bodyMarker = new Comment("======== START OF BODY ========");
        html.addContent(bodyMarker);
        HtmlTree body = new HtmlTree(HtmlTag.BODY);
        Comment pMarker = new Comment("======== START OF PARAGRAPH ========");
        body.addContent(pMarker);
        HtmlTree p = new HtmlTree(HtmlTag.P);
        StringContent bodyContent = new StringContent(
                "This document is generated from sample source code and HTML " +
                "files with examples of a wide variety of Java language constructs: packages, " +
                "subclasses, subinterfaces, nested classes, nested interfaces," +
                "inheriting from other packages, constructors, fields," +
                "methods, and so forth. ");
        p.addContent(bodyContent);
        StringContent anchorContent = new StringContent("Click Here");
        p.addContent(HtmlTree.A("testLink.html", anchorContent));
        StringContent pContent = new StringContent(" to <test> out a link.");
        p.addContent(pContent);
        body.addContent(p);
        HtmlTree p1 = new HtmlTree(HtmlTag.P);
        HtmlTree anchor = new HtmlTree(HtmlTag.A);
        anchor.addAttr(HtmlAttr.HREF, "testLink.html");
        anchor.addAttr(HtmlAttr.NAME, "Another version of a tag");
        p1.addContent(anchor);
        body.addContent(p1);
        HtmlTree dl = new HtmlTree(HtmlTag.DL);
        html.addContent(dl);
        HtmlTree dlTree = new HtmlTree(HtmlTag.DL);
        dlTree.addContent(new HtmlTree(HtmlTag.DT));
        dlTree.addContent(new HtmlTree (HtmlTag.DD));
        html.addContent(dlTree);
        HtmlTree dlDisplay = new HtmlTree(HtmlTag.DL);
        dlDisplay.addContent(new HtmlTree(HtmlTag.DT));
        HtmlTree dd = new HtmlTree (HtmlTag.DD);
        StringContent ddContent = new StringContent("Test DD");
        dd.addContent(ddContent);
        dlDisplay.addContent(dd);
        body.addContent(dlDisplay);
        StringContent emptyString = new StringContent("");
        body.addContent(emptyString);
        Comment emptyComment = new Comment("");
        body.addContent(emptyComment);
        HtmlTree hr = new HtmlTree(HtmlTag.HR);
        body.addContent(hr);
        html.addContent(body);
        HtmlDocument htmlDoc = new HtmlDocument(htmlDocType, html);
        return htmlDoc.toString();
    }
    public static String readFileToString(String filename) throws IOException {
        File file = new File(filename);
        if ( !file.exists() ) {
            System.out.println("\nFILE DOES NOT EXIST: " + filename);
        }
        BufferedReader in = new BufferedReader(new FileReader(file));
        StringBuilder fileString = new StringBuilder();
        try {
            String line;
            while ((line = in.readLine()) != null) {
                fileString.append(line);
                fileString.append(LS);
            }
        } finally {
            in.close();
        }
        return fileString.toString();
    }
}
