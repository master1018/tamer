public class SourceToHTMLConverter {
    private static final int NUM_BLANK_LINES = 60;
    private static final Content NEW_LINE = new RawHtml(DocletConstants.NL);
    private static String relativePath = "";
    private SourceToHTMLConverter() {}
    public static void convertRoot(ConfigurationImpl configuration, RootDoc rd,
            String outputdir) {
        if (rd == null || outputdir == null) {
            return;
        }
        PackageDoc[] pds = rd.specifiedPackages();
        for (int i = 0; i < pds.length; i++) {
            if (!(configuration.nodeprecated && Util.isDeprecated(pds[i])))
                convertPackage(configuration, pds[i], outputdir);
        }
        ClassDoc[] cds = rd.specifiedClasses();
        for (int i = 0; i < cds.length; i++) {
            if (!(configuration.nodeprecated &&
                    (Util.isDeprecated(cds[i]) || Util.isDeprecated(cds[i].containingPackage()))))
                convertClass(configuration, cds[i],
                        getPackageOutputDir(outputdir, cds[i].containingPackage()));
        }
    }
    public static void convertPackage(ConfigurationImpl configuration, PackageDoc pd,
            String outputdir) {
        if (pd == null || outputdir == null) {
            return;
        }
        String classOutputdir = getPackageOutputDir(outputdir, pd);
        ClassDoc[] cds = pd.allClasses();
        for (int i = 0; i < cds.length; i++) {
            if (!(configuration.nodeprecated && Util.isDeprecated(cds[i])))
                convertClass(configuration, cds[i], classOutputdir);
        }
    }
    private static String getPackageOutputDir(String outputDir, PackageDoc pd) {
        return outputDir + File.separator +
            DirectoryManager.getDirectoryPath(pd) + File.separator;
    }
    public static void convertClass(ConfigurationImpl configuration, ClassDoc cd,
            String outputdir) {
        if (cd == null || outputdir == null) {
            return;
        }
        try {
            SourcePosition sp = cd.position();
            if (sp == null)
                return;
            Reader r;
            if (sp instanceof com.sun.tools.javadoc.SourcePositionImpl) {
                FileObject fo = ((com.sun.tools.javadoc.SourcePositionImpl) sp).fileObject();
                if (fo == null)
                    return;
                r = fo.openReader(true);
            } else {
                File file = sp.file();
                if (file == null)
                    return;
                r = new FileReader(file);
            }
            LineNumberReader reader = new LineNumberReader(r);
            int lineno = 1;
            String line;
            relativePath = DirectoryManager.getRelativePath(DocletConstants.SOURCE_OUTPUT_DIR_NAME) +
                    DirectoryManager.getRelativePath(cd.containingPackage());
            Content body = getHeader();
            Content pre = new HtmlTree(HtmlTag.PRE);
            try {
                while ((line = reader.readLine()) != null) {
                    addLineNo(pre, lineno);
                    addLine(pre, line, configuration.sourcetab, lineno);
                    lineno++;
                }
            } finally {
                reader.close();
            }
            addBlankLines(pre);
            Content div = HtmlTree.DIV(HtmlStyle.sourceContainer, pre);
            body.addContent(div);
            writeToFile(body, outputdir, cd.name(), configuration);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void writeToFile(Content body, String outputDir,
            String className, ConfigurationImpl configuration) throws IOException {
        Content htmlDocType = DocType.Transitional();
        Content head = new HtmlTree(HtmlTag.HEAD);
        head.addContent(HtmlTree.TITLE(new StringContent(
                configuration.getText("doclet.Window_Source_title"))));
        head.addContent(getStyleSheetProperties(configuration));
        Content htmlTree = HtmlTree.HTML(configuration.getLocale().getLanguage(),
                head, body);
        Content htmlDocument = new HtmlDocument(htmlDocType, htmlTree);
        File dir = new File(outputDir);
        dir.mkdirs();
        File newFile = new File(dir, className + ".html");
        configuration.message.notice("doclet.Generating_0", newFile.getPath());
        FileOutputStream fout = new FileOutputStream(newFile);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fout));
        bw.write(htmlDocument.toString());
        bw.close();
        fout.close();
    }
    public static HtmlTree getStyleSheetProperties(ConfigurationImpl configuration) {
        String filename = configuration.stylesheetfile;
        if (filename.length() > 0) {
            File stylefile = new File(filename);
            String parent = stylefile.getParent();
            filename = (parent == null)?
                filename:
                filename.substring(parent.length() + 1);
        } else {
            filename = "stylesheet.css";
        }
        filename = relativePath + filename;
        HtmlTree link = HtmlTree.LINK("stylesheet", "text/css", filename, "Style");
        return link;
    }
    private static Content getHeader() {
        return new HtmlTree(HtmlTag.BODY);
    }
    private static void addLineNo(Content pre, int lineno) {
        HtmlTree span = new HtmlTree(HtmlTag.SPAN);
        span.addStyle(HtmlStyle.sourceLineNo);
        if (lineno < 10) {
            span.addContent("00" + Integer.toString(lineno));
        } else if (lineno < 100) {
            span.addContent("0" + Integer.toString(lineno));
        } else {
            span.addContent(Integer.toString(lineno));
        }
        pre.addContent(span);
    }
    private static void addLine(Content pre, String line, int tabLength,
            int currentLineNo) {
        if (line != null) {
            StringBuilder lineBuffer = new StringBuilder(Util.escapeHtmlChars(line));
            Util.replaceTabs(tabLength, lineBuffer);
            pre.addContent(new RawHtml(lineBuffer.toString()));
            Content anchor = HtmlTree.A_NAME("line." + Integer.toString(currentLineNo));
            pre.addContent(anchor);
            pre.addContent(NEW_LINE);
        }
    }
    private static void addBlankLines(Content pre) {
        for (int i = 0; i < NUM_BLANK_LINES; i++) {
            pre.addContent(NEW_LINE);
        }
    }
    public static String getAnchorName(Doc d) {
        return "line." + d.position().line();
    }
}
