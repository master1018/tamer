public class HtmlDocletWriter extends HtmlDocWriter {
    public String relativePath = "";
    public String relativepathNoSlash = "";
    public String path = "";
    public String filename = "";
    public int displayLength = 0;
    public ConfigurationImpl configuration;
    protected boolean printedAnnotationHeading = false;
    public HtmlDocletWriter(ConfigurationImpl configuration,
                              String filename) throws IOException {
        super(configuration, filename);
        this.configuration = configuration;
        this.filename = filename;
    }
    public HtmlDocletWriter(ConfigurationImpl configuration,
                              String path, String filename,
                              String relativePath) throws IOException {
        super(configuration, path, filename);
        this.configuration = configuration;
        this.path = path;
        this.relativePath = relativePath;
        this.relativepathNoSlash =
            DirectoryManager.getPathNoTrailingSlash(this.relativePath);
        this.filename = filename;
    }
    public String replaceDocRootDir(String htmlstr) {
        int index = htmlstr.indexOf("{@");
        if (index < 0) {
            return htmlstr;
        }
        String lowerHtml = htmlstr.toLowerCase();
        index = lowerHtml.indexOf("{@docroot}", index);
        if (index < 0) {
            return htmlstr;
        }
        StringBuilder buf = new StringBuilder();
        int previndex = 0;
        while (true) {
            if (configuration.docrootparent.length() > 0) {
                index = lowerHtml.indexOf("{@docroot}/..", previndex);
                if (index < 0) {
                    buf.append(htmlstr.substring(previndex));
                    break;
                }
                buf.append(htmlstr.substring(previndex, index));
                previndex = index + 13;  
                buf.append(configuration.docrootparent);
                if (previndex < htmlstr.length() && htmlstr.charAt(previndex) != '/') {
                    buf.append(DirectoryManager.URL_FILE_SEPARATOR);
                }
            } else {
                index = lowerHtml.indexOf("{@docroot}", previndex);
                if (index < 0) {
                    buf.append(htmlstr.substring(previndex));
                    break;
                }
                buf.append(htmlstr.substring(previndex, index));
                previndex = index + 10;  
                buf.append(relativepathNoSlash);
                if (relativepathNoSlash.length() > 0 && previndex < htmlstr.length() &&
                        htmlstr.charAt(previndex) != '/') {
                    buf.append(DirectoryManager.URL_FILE_SEPARATOR);
                }
            }
        }
        return buf.toString();
    }
    public void printNoFramesTargetHyperLink(String link, String where,
                                               String target, String label,
                                               boolean strong) {
        script();
        println("  <!--");
        println("  if(window==top) {");
        println("    document.writeln('"
            + getHyperLinkString(link, where, label, strong, "", "", target) + "');");
        println("  }");
        println("  
        scriptEnd();
        noScript();
        println("  " + getHyperLinkString(link, where, label, strong, "", "", target));
        noScriptEnd();
        println(DocletConstants.NL);
    }
    public Content getAllClassesLinkScript(String id) {
        HtmlTree script = new HtmlTree(HtmlTag.SCRIPT);
        script.addAttr(HtmlAttr.TYPE, "text/javascript");
        String scriptCode = "<!--" + DocletConstants.NL +
                "  allClassesLink = document.getElementById(\"" + id + "\");" + DocletConstants.NL +
                "  if(window==top) {" + DocletConstants.NL +
                "    allClassesLink.style.display = \"block\";" + DocletConstants.NL +
                "  }" + DocletConstants.NL +
                "  else {" + DocletConstants.NL +
                "    allClassesLink.style.display = \"none\";" + DocletConstants.NL +
                "  }" + DocletConstants.NL +
                "  
        Content scriptContent = new RawHtml(scriptCode);
        script.addContent(scriptContent);
        Content div = HtmlTree.DIV(script);
        return div;
    }
    private void addMethodInfo(MethodDoc method, Content dl) {
        ClassDoc[] intfacs = method.containingClass().interfaces();
        MethodDoc overriddenMethod = method.overriddenMethod();
        if ((intfacs.length > 0 &&
                new ImplementedMethods(method, this.configuration).build().length > 0) ||
                overriddenMethod != null) {
            MethodWriterImpl.addImplementsInfo(this, method, dl);
            if (overriddenMethod != null) {
                MethodWriterImpl.addOverridden(this,
                        method.overriddenType(), overriddenMethod, dl);
            }
        }
    }
    protected void addTagsInfo(Doc doc, Content htmltree) {
        if (configuration.nocomment) {
            return;
        }
        Content dl = new HtmlTree(HtmlTag.DL);
        if (doc instanceof MethodDoc) {
            addMethodInfo((MethodDoc) doc, dl);
        }
        TagletOutputImpl output = new TagletOutputImpl("");
        TagletWriter.genTagOuput(configuration.tagletManager, doc,
            configuration.tagletManager.getCustomTags(doc),
                getTagletWriterInstance(false), output);
        String outputString = output.toString().trim();
        if (!outputString.isEmpty()) {
            Content resultString = new RawHtml(outputString);
            dl.addContent(resultString);
        }
        htmltree.addContent(dl);
    }
    protected boolean hasSerializationOverviewTags(FieldDoc field) {
        TagletOutputImpl output = new TagletOutputImpl("");
        TagletWriter.genTagOuput(configuration.tagletManager, field,
            configuration.tagletManager.getCustomTags(field),
                getTagletWriterInstance(false), output);
        return (!output.toString().trim().isEmpty());
    }
    public TagletWriter getTagletWriterInstance(boolean isFirstSentence) {
        return new TagletWriterImpl(this, isFirstSentence);
    }
    protected void printTagsInfoHeader() {
        dl();
    }
    protected void printTagsInfoFooter() {
        dlEnd();
    }
    public Content getTargetPackageLink(PackageDoc pd, String target,
            Content label) {
        return getHyperLink(pathString(pd, "package-summary.html"), "", label, "", target);
    }
    public void printHtmlHeader(String title, String[] metakeywords,
            boolean includeScript) {
        println("<!DOCTYPE HTML PUBLIC \"-
                    "Transitional
                    "\"http:
        println("<!--NewPage-->");
        html();
        head();
        if (! configuration.notimestamp) {
            print("<!-- Generated by javadoc (build " + ConfigurationImpl.BUILD_DATE + ") on ");
            print(today());
            println(" -->");
        }
        if (configuration.charset.length() > 0) {
            println("<META http-equiv=\"Content-Type\" content=\"text/html; "
                        + "charset=" + configuration.charset + "\">");
        }
        if ( configuration.windowtitle.length() > 0 ) {
            title += " (" + configuration.windowtitle  + ")";
        }
        title(title);
        println(title);
        titleEnd();
        println("");
        if (! configuration.notimestamp) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                println("<META NAME=\"date\" "
                                    + "CONTENT=\"" + dateFormat.format(new Date()) + "\">");
        }
        if ( metakeywords != null ) {
            for ( int i=0; i < metakeywords.length; i++ ) {
                println("<META NAME=\"keywords\" "
                            + "CONTENT=\"" + metakeywords[i] + "\">");
            }
        }
        println("");
        printStyleSheetProperties();
        println("");
        if (includeScript) {
            printWinTitleScript(title);
        }
        println("");
        headEnd();
        println("");
        body("white", includeScript);
    }
    public void printHtmlDocument(String[] metakeywords, boolean includeScript,
            Content body) {
        Content htmlDocType = DocType.Transitional();
        Content htmlComment = new Comment(configuration.getText("doclet.New_Page"));
        Content head = new HtmlTree(HtmlTag.HEAD);
        if (!configuration.notimestamp) {
            Content headComment = new Comment("Generated by javadoc (version " +
                    ConfigurationImpl.BUILD_DATE + ") on " + today());
            head.addContent(headComment);
        }
        if (configuration.charset.length() > 0) {
            Content meta = HtmlTree.META("Content-Type", "text/html",
                    configuration.charset);
            head.addContent(meta);
        }
        head.addContent(getTitle());
        if (!configuration.notimestamp) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Content meta = HtmlTree.META("date", dateFormat.format(new Date()));
            head.addContent(meta);
        }
        if (metakeywords != null) {
            for (int i=0; i < metakeywords.length; i++) {
                Content meta = HtmlTree.META("keywords", metakeywords[i]);
                head.addContent(meta);
            }
        }
        head.addContent(getStyleSheetProperties());
        Content htmlTree = HtmlTree.HTML(configuration.getLocale().getLanguage(),
                head, body);
        Content htmlDocument = new HtmlDocument(htmlDocType,
                htmlComment, htmlTree);
        print(htmlDocument.toString());
    }
    public String getWindowTitle(String title) {
        if (configuration.windowtitle.length() > 0) {
            title += " (" + configuration.windowtitle  + ")";
        }
        return title;
    }
    public void printUserHeaderFooter(boolean header) {
        em();
        if (header) {
            print(replaceDocRootDir(configuration.header));
        } else {
            if (configuration.footer.length() != 0) {
                print(replaceDocRootDir(configuration.footer));
            } else {
                print(replaceDocRootDir(configuration.header));
            }
        }
        emEnd();
    }
    public Content getUserHeaderFooter(boolean header) {
        String content;
        if (header) {
            content = replaceDocRootDir(configuration.header);
        } else {
            if (configuration.footer.length() != 0) {
                content = replaceDocRootDir(configuration.footer);
            } else {
                content = replaceDocRootDir(configuration.header);
            }
        }
        Content rawContent = new RawHtml(content);
        Content em = HtmlTree.EM(rawContent);
        return em;
    }
    public void printTop() {
        print(replaceDocRootDir(configuration.top));
        hr();
    }
    public void addTop(Content body) {
        Content top = new RawHtml(replaceDocRootDir(configuration.top));
        body.addContent(top);
    }
    public void printBottom() {
        hr();
        print(replaceDocRootDir(configuration.bottom));
    }
    public void addBottom(Content body) {
        Content bottom = new RawHtml(replaceDocRootDir(configuration.bottom));
        Content small = HtmlTree.SMALL(bottom);
        Content p = HtmlTree.P(HtmlStyle.legalCopy, small);
        body.addContent(p);
    }
    protected void navLinks(boolean header) {
        println("");
        if (!configuration.nonavbar) {
            if (header) {
                println(DocletConstants.NL + "<!-- ========= START OF TOP NAVBAR ======= -->");
                anchor("navbar_top");
                println();
                print(getHyperLinkString("", "skip-navbar_top", "", false, "",
                    configuration.getText("doclet.Skip_navigation_links"), ""));
            } else {
                println(DocletConstants.NL + "<!-- ======= START OF BOTTOM NAVBAR ====== -->");
                anchor("navbar_bottom");
                println();
                print(getHyperLinkString("", "skip-navbar_bottom", "", false, "",
                    configuration.getText("doclet.Skip_navigation_links"), ""));
            }
            table(0, "100%", 1, 0);
            tr();
            tdColspanBgcolorStyle(2, "#EEEEFF", "NavBarCell1");
            println("");
            if (header) {
                anchor("navbar_top_firstrow");
            } else {
                anchor("navbar_bottom_firstrow");
            }
            table(0, 0, 3);
            print("  ");
            trAlignVAlign("center", "top");
            if (configuration.createoverview) {
                navLinkContents();
            }
            if (configuration.packages.length == 1) {
                navLinkPackage(configuration.packages[0]);
            } else if (configuration.packages.length > 1) {
                navLinkPackage();
            }
            navLinkClass();
            if(configuration.classuse) {
                navLinkClassUse();
            }
            if(configuration.createtree) {
                navLinkTree();
            }
            if(!(configuration.nodeprecated ||
                     configuration.nodeprecatedlist)) {
                navLinkDeprecated();
            }
            if(configuration.createindex) {
                navLinkIndex();
            }
            if (!configuration.nohelp) {
                navLinkHelp();
            }
            print("  ");
            trEnd();
            tableEnd();
            tdEnd();
            tdAlignVAlignRowspan("right", "top", 3);
            printUserHeaderFooter(header);
            tdEnd();
            trEnd();
            println("");
            tr();
            tdBgcolorStyle("white", "NavBarCell2");
            font("-2");
            space();
            navLinkPrevious();
            space();
            println("");
            space();
            navLinkNext();
            fontEnd();
            tdEnd();
            tdBgcolorStyle("white", "NavBarCell2");
            font("-2");
            print("  ");
            navShowLists();
            print("  ");
            space();
            println("");
            space();
            navHideLists(filename);
            print("  ");
            space();
            println("");
            space();
            navLinkClassIndex();
            fontEnd();
            tdEnd();
            trEnd();
            printSummaryDetailLinks();
            tableEnd();
            if (header) {
                aName("skip-navbar_top");
                aEnd();
                println(DocletConstants.NL + "<!-- ========= END OF TOP NAVBAR ========= -->");
            } else {
                aName("skip-navbar_bottom");
                aEnd();
                println(DocletConstants.NL + "<!-- ======== END OF BOTTOM NAVBAR ======= -->");
            }
            println("");
        }
    }
    protected void addNavLinks(boolean header, Content body) {
        if (!configuration.nonavbar) {
            String allClassesId = "allclasses_";
            HtmlTree navDiv = new HtmlTree(HtmlTag.DIV);
            if (header) {
                body.addContent(HtmlConstants.START_OF_TOP_NAVBAR);
                navDiv.addStyle(HtmlStyle.topNav);
                allClassesId += "navbar_top";
                Content a = getMarkerAnchor("navbar_top");
                navDiv.addContent(a);
                Content skipLinkContent = getHyperLink("",
                        "skip-navbar_top", HtmlTree.EMPTY, configuration.getText(
                        "doclet.Skip_navigation_links"), "");
                navDiv.addContent(skipLinkContent);
            } else {
                body.addContent(HtmlConstants.START_OF_BOTTOM_NAVBAR);
                navDiv.addStyle(HtmlStyle.bottomNav);
                allClassesId += "navbar_bottom";
                Content a = getMarkerAnchor("navbar_bottom");
                navDiv.addContent(a);
                Content skipLinkContent = getHyperLink("",
                        "skip-navbar_bottom", HtmlTree.EMPTY, configuration.getText(
                        "doclet.Skip_navigation_links"), "");
                navDiv.addContent(skipLinkContent);
            }
            if (header) {
                navDiv.addContent(getMarkerAnchor("navbar_top_firstrow"));
            } else {
                navDiv.addContent(getMarkerAnchor("navbar_bottom_firstrow"));
            }
            HtmlTree navList = new HtmlTree(HtmlTag.UL);
            navList.addStyle(HtmlStyle.navList);
            navList.addAttr(HtmlAttr.TITLE, "Navigation");
            if (configuration.createoverview) {
                navList.addContent(getNavLinkContents());
            }
            if (configuration.packages.length == 1) {
                navList.addContent(getNavLinkPackage(configuration.packages[0]));
            } else if (configuration.packages.length > 1) {
                navList.addContent(getNavLinkPackage());
            }
            navList.addContent(getNavLinkClass());
            if(configuration.classuse) {
                navList.addContent(getNavLinkClassUse());
            }
            if(configuration.createtree) {
                navList.addContent(getNavLinkTree());
            }
            if(!(configuration.nodeprecated ||
                     configuration.nodeprecatedlist)) {
                navList.addContent(getNavLinkDeprecated());
            }
            if(configuration.createindex) {
                navList.addContent(getNavLinkIndex());
            }
            if (!configuration.nohelp) {
                navList.addContent(getNavLinkHelp());
            }
            navDiv.addContent(navList);
            Content aboutDiv = HtmlTree.DIV(HtmlStyle.aboutLanguage, getUserHeaderFooter(header));
            navDiv.addContent(aboutDiv);
            body.addContent(navDiv);
            Content ulNav = HtmlTree.UL(HtmlStyle.navList, getNavLinkPrevious());
            ulNav.addContent(getNavLinkNext());
            Content subDiv = HtmlTree.DIV(HtmlStyle.subNav, ulNav);
            Content ulFrames = HtmlTree.UL(HtmlStyle.navList, getNavShowLists());
            ulFrames.addContent(getNavHideLists(filename));
            subDiv.addContent(ulFrames);
            HtmlTree ulAllClasses = HtmlTree.UL(HtmlStyle.navList, getNavLinkClassIndex());
            ulAllClasses.addAttr(HtmlAttr.ID, allClassesId.toString());
            subDiv.addContent(ulAllClasses);
            subDiv.addContent(getAllClassesLinkScript(allClassesId.toString()));
            addSummaryDetailLinks(subDiv);
            if (header) {
                subDiv.addContent(getMarkerAnchor("skip-navbar_top"));
                body.addContent(subDiv);
                body.addContent(HtmlConstants.END_OF_TOP_NAVBAR);
            } else {
                subDiv.addContent(getMarkerAnchor("skip-navbar_bottom"));
                body.addContent(subDiv);
                body.addContent(HtmlConstants.END_OF_BOTTOM_NAVBAR);
            }
        }
    }
    protected void navLinkNext() {
        navLinkNext(null);
    }
    protected Content getNavLinkNext() {
        return getNavLinkNext(null);
    }
    protected void navLinkPrevious() {
        navLinkPrevious(null);
    }
    protected Content getNavLinkPrevious() {
        return getNavLinkPrevious(null);
    }
    protected void printSummaryDetailLinks() {
    }
    protected void addSummaryDetailLinks(Content navDiv) {
    }
    protected void navLinkContents() {
        navCellStart();
        printHyperLink(relativePath + "overview-summary.html", "",
                       configuration.getText("doclet.Overview"), true, "NavBarFont1");
        navCellEnd();
    }
    protected Content getNavLinkContents() {
        Content linkContent = getHyperLink(relativePath +
                "overview-summary.html", "", overviewLabel, "", "");
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
    protected void navCellStart() {
        print("  ");
        tdBgcolorStyle("#EEEEFF", "NavBarCell1");
        print("    ");
    }
    protected void navCellRevStart() {
        print("  ");
        tdBgcolorStyle("#FFFFFF", "NavBarCell1Rev");
        print(" ");
        space();
    }
    protected void navCellEnd() {
        space();
        tdEnd();
    }
    protected void navLinkPackage(PackageDoc pkg) {
        navCellStart();
        printPackageLink(pkg, configuration.getText("doclet.Package"), true,
            "NavBarFont1");
        navCellEnd();
    }
    protected Content getNavLinkPackage(PackageDoc pkg) {
        Content linkContent = getPackageLink(pkg,
                packageLabel);
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
    protected void navLinkPackage() {
        navCellStart();
        fontStyle("NavBarFont1");
        printText("doclet.Package");
        fontEnd();
        navCellEnd();
    }
    protected Content getNavLinkPackage() {
        Content li = HtmlTree.LI(packageLabel);
        return li;
    }
    protected void navLinkClassUse() {
        navCellStart();
        fontStyle("NavBarFont1");
        printText("doclet.navClassUse");
        fontEnd();
        navCellEnd();
    }
    protected Content getNavLinkClassUse() {
        Content li = HtmlTree.LI(useLabel);
        return li;
    }
    public void navLinkPrevious(String prev) {
        String tag = configuration.getText("doclet.Prev");
        if (prev != null) {
            printHyperLink(prev, "", tag, true) ;
        } else {
            print(tag);
        }
    }
    public Content getNavLinkPrevious(String prev) {
        Content li;
        if (prev != null) {
            li = HtmlTree.LI(getHyperLink(prev, "", prevLabel, "", ""));
        }
        else
            li = HtmlTree.LI(prevLabel);
        return li;
    }
    public void navLinkNext(String next) {
        String tag = configuration.getText("doclet.Next");
        if (next != null) {
            printHyperLink(next, "", tag, true);
        } else {
            print(tag);
        }
    }
    public Content getNavLinkNext(String next) {
        Content li;
        if (next != null) {
            li = HtmlTree.LI(getHyperLink(next, "", nextLabel, "", ""));
        }
        else
            li = HtmlTree.LI(nextLabel);
        return li;
    }
    protected void navShowLists(String link) {
        print(getHyperLinkString(link + "?" + path + filename, "",
            configuration.getText("doclet.FRAMES"), true, "", "", "_top"));
    }
    protected Content getNavShowLists(String link) {
        Content framesContent = getHyperLink(link + "?" + path +
                filename, "", framesLabel, "", "_top");
        Content li = HtmlTree.LI(framesContent);
        return li;
    }
    protected void navShowLists() {
        navShowLists(relativePath + "index.html");
    }
    protected Content getNavShowLists() {
        return getNavShowLists(relativePath + "index.html");
    }
    protected void navHideLists(String link) {
        print(getHyperLinkString(link, "", configuration.getText("doclet.NO_FRAMES"),
            true, "", "", "_top"));
    }
    protected Content getNavHideLists(String link) {
        Content noFramesContent = getHyperLink(link, "", noframesLabel, "", "_top");
        Content li = HtmlTree.LI(noFramesContent);
        return li;
    }
    protected void navLinkTree() {
        navCellStart();
        PackageDoc[] packages = configuration.root.specifiedPackages();
        if (packages.length == 1 && configuration.root.specifiedClasses().length == 0) {
            printHyperLink(pathString(packages[0], "package-tree.html"), "",
                           configuration.getText("doclet.Tree"), true, "NavBarFont1");
        } else {
            printHyperLink(relativePath + "overview-tree.html", "",
                           configuration.getText("doclet.Tree"), true, "NavBarFont1");
        }
        navCellEnd();
    }
    protected Content getNavLinkTree() {
        Content treeLinkContent;
        PackageDoc[] packages = configuration.root.specifiedPackages();
        if (packages.length == 1 && configuration.root.specifiedClasses().length == 0) {
            treeLinkContent = getHyperLink(pathString(packages[0],
                    "package-tree.html"), "", treeLabel,
                    "", "");
        } else {
            treeLinkContent = getHyperLink(relativePath + "overview-tree.html",
                    "", treeLabel, "", "");
        }
        Content li = HtmlTree.LI(treeLinkContent);
        return li;
    }
    protected Content getNavLinkMainTree(String label) {
        Content mainTreeContent = getHyperLink(relativePath + "overview-tree.html",
                new StringContent(label));
        Content li = HtmlTree.LI(mainTreeContent);
        return li;
    }
    protected void navLinkClass() {
        navCellStart();
        fontStyle("NavBarFont1");
        printText("doclet.Class");
        fontEnd();
        navCellEnd();
    }
    protected Content getNavLinkClass() {
        Content li = HtmlTree.LI(classLabel);
        return li;
    }
    protected void navLinkDeprecated() {
        navCellStart();
        printHyperLink(relativePath + "deprecated-list.html", "",
                       configuration.getText("doclet.navDeprecated"), true, "NavBarFont1");
        navCellEnd();
    }
    protected Content getNavLinkDeprecated() {
        Content linkContent = getHyperLink(relativePath +
                "deprecated-list.html", "", deprecatedLabel, "", "");
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
    protected void navLinkClassIndex() {
        printNoFramesTargetHyperLink(relativePath +
                AllClassesFrameWriter.OUTPUT_FILE_NAME_NOFRAMES,
            "", "", configuration.getText("doclet.All_Classes"), true);
    }
    protected Content getNavLinkClassIndex() {
        Content allClassesContent = getHyperLink(relativePath +
                AllClassesFrameWriter.OUTPUT_FILE_NAME_NOFRAMES, "",
                allclassesLabel, "", "");
        Content li = HtmlTree.LI(allClassesContent);
        return li;
    }
    protected void navLinkIndex() {
        navCellStart();
        printHyperLink(relativePath +
                           (configuration.splitindex?
                                DirectoryManager.getPath("index-files") +
                                fileseparator: "") +
                           (configuration.splitindex?
                                "index-1.html" : "index-all.html"), "",
                       configuration.getText("doclet.Index"), true, "NavBarFont1");
        navCellEnd();
    }
    protected Content getNavLinkIndex() {
        Content linkContent = getHyperLink(relativePath +(configuration.splitindex?
            DirectoryManager.getPath("index-files") + fileseparator: "") +
            (configuration.splitindex?"index-1.html" : "index-all.html"), "",
            indexLabel, "", "");
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
    protected void navLinkHelp() {
        String helpfilenm = configuration.helpfile;
        if (helpfilenm.equals("")) {
            helpfilenm = "help-doc.html";
        } else {
            int lastsep;
            if ((lastsep = helpfilenm.lastIndexOf(File.separatorChar)) != -1) {
                helpfilenm = helpfilenm.substring(lastsep + 1);
            }
        }
        navCellStart();
        printHyperLink(relativePath + helpfilenm, "",
                       configuration.getText("doclet.Help"), true, "NavBarFont1");
        navCellEnd();
    }
    protected Content getNavLinkHelp() {
        String helpfilenm = configuration.helpfile;
        if (helpfilenm.equals("")) {
            helpfilenm = "help-doc.html";
        } else {
            int lastsep;
            if ((lastsep = helpfilenm.lastIndexOf(File.separatorChar)) != -1) {
                helpfilenm = helpfilenm.substring(lastsep + 1);
            }
        }
        Content linkContent = getHyperLink(relativePath + helpfilenm, "",
                helpLabel, "", "");
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
    protected void navDetail() {
        printText("doclet.Detail");
    }
    protected void navSummary() {
        printText("doclet.Summary");
    }
    public void tableIndexSummary() {
        table(1, "100%", 3, 0);
    }
    public void tableIndexSummary(String summary) {
        table(1, "100%", 3, 0, summary);
    }
    public void tableIndexDetail() {
        table(1, "100%", 3, 0);
    }
    public void tdIndex() {
        print("<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"1%\">");
    }
    public void tableCaptionStart() {
        captionStyle("TableCaption");
    }
    public void tableSubCaptionStart() {
        captionStyle("TableSubCaption");
    }
    public void tableCaptionEnd() {
        captionEnd();
    }
    public void summaryTableHeader(String[] header, String scope) {
        tr();
        for ( int i=0; i < header.length; i++ ) {
            thScopeNoWrap("TableHeader", scope);
            print(header[i]);
            thEnd();
        }
        trEnd();
    }
    public Content getSummaryTableHeader(String[] header, String scope) {
        Content tr = new HtmlTree(HtmlTag.TR);
        int size = header.length;
        Content tableHeader;
        if (size == 1) {
            tableHeader = new StringContent(header[0]);
            tr.addContent(HtmlTree.TH(HtmlStyle.colOne, scope, tableHeader));
            return tr;
        }
        for (int i = 0; i < size; i++) {
            tableHeader = new StringContent(header[i]);
            if(i == 0)
                tr.addContent(HtmlTree.TH(HtmlStyle.colFirst, scope, tableHeader));
            else if(i == (size - 1))
                tr.addContent(HtmlTree.TH(HtmlStyle.colLast, scope, tableHeader));
            else
                tr.addContent(HtmlTree.TH(scope, tableHeader));
        }
        return tr;
    }
    public Content getTableCaption(String rawText) {
        Content title = new RawHtml(rawText);
        Content captionSpan = HtmlTree.SPAN(title);
        Content space = getSpace();
        Content tabSpan = HtmlTree.SPAN(HtmlStyle.tabEnd, space);
        Content caption = HtmlTree.CAPTION(captionSpan);
        caption.addContent(tabSpan);
        return caption;
    }
    public Content getMarkerAnchor(String anchorName) {
        return getMarkerAnchor(anchorName, null);
    }
    public Content getMarkerAnchor(String anchorName, Content anchorContent) {
        if (anchorContent == null)
            anchorContent = new Comment(" ");
        Content markerAnchor = HtmlTree.A_NAME(anchorName, anchorContent);
        return markerAnchor;
    }
    public Content getPackageName(PackageDoc packageDoc) {
        return packageDoc == null || packageDoc.name().length() == 0 ?
            defaultPackageLabel :
            getPackageLabel(packageDoc.name());
    }
    public Content getPackageLabel(String packageName) {
        return new StringContent(packageName);
    }
    protected void addPackageDeprecatedAPI(List<Doc> deprPkgs, String headingKey,
            String tableSummary, String[] tableHeader, Content contentTree) {
        if (deprPkgs.size() > 0) {
            Content table = HtmlTree.TABLE(0, 3, 0, tableSummary,
                    getTableCaption(configuration().getText(headingKey)));
            table.addContent(getSummaryTableHeader(tableHeader, "col"));
            Content tbody = new HtmlTree(HtmlTag.TBODY);
            for (int i = 0; i < deprPkgs.size(); i++) {
                PackageDoc pkg = (PackageDoc) deprPkgs.get(i);
                HtmlTree td = HtmlTree.TD(HtmlStyle.colOne,
                        getPackageLink(pkg, getPackageName(pkg)));
                if (pkg.tags("deprecated").length > 0) {
                    addInlineDeprecatedComment(pkg, pkg.tags("deprecated")[0], td);
                }
                HtmlTree tr = HtmlTree.TR(td);
                if (i % 2 == 0) {
                    tr.addStyle(HtmlStyle.altColor);
                } else {
                    tr.addStyle(HtmlStyle.rowColor);
                }
                tbody.addContent(tr);
            }
            table.addContent(tbody);
            Content li = HtmlTree.LI(HtmlStyle.blockList, table);
            Content ul = HtmlTree.UL(HtmlStyle.blockList, li);
            contentTree.addContent(ul);
        }
    }
    public void tableHeaderStart(String color, int span) {
        trBgcolorStyle(color, "TableHeadingColor");
        thAlignColspan("left", span);
        font("+2");
    }
    public void tableInheritedHeaderStart(String color) {
        trBgcolorStyle(color, "TableSubHeadingColor");
        thAlign("left");
    }
    public void tableUseInfoHeaderStart(String color) {
        trBgcolorStyle(color, "TableSubHeadingColor");
        thAlignColspan("left", 2);
    }
    public void tableHeaderStart(String color) {
        tableHeaderStart(color, 2);
    }
    public void tableHeaderStart(int span) {
        tableHeaderStart("#CCCCFF", span);
    }
    public void tableHeaderStart() {
        tableHeaderStart(2);
    }
    public void tableHeaderEnd() {
        fontEnd();
        thEnd();
        trEnd();
    }
    public void tableInheritedHeaderEnd() {
        thEnd();
        trEnd();
    }
    public void summaryRow(int width) {
        if (width != 0) {
            tdWidth(width + "%");
        } else {
            td();
        }
    }
    public void summaryRowEnd() {
        tdEnd();
    }
    public void printIndexHeading(String str) {
        h2();
        print(str);
        h2End();
    }
    public void frameSet(String arg) {
        println("<FRAMESET " + arg + ">");
    }
    public void frameSetEnd() {
        println("</FRAMESET>");
    }
    public void frame(String arg) {
        println("<FRAME " + arg + ">");
    }
    public void frameEnd() {
        println("</FRAME>");
    }
    protected String pathToClass(ClassDoc cd) {
        return pathString(cd.containingPackage(), cd.name() + ".html");
    }
    protected String pathString(ClassDoc cd, String name) {
        return pathString(cd.containingPackage(), name);
    }
    protected String pathString(PackageDoc pd, String name) {
        StringBuffer buf = new StringBuffer(relativePath);
        buf.append(DirectoryManager.getPathToPackage(pd, name));
        return buf.toString();
    }
    public void printPackageLink(PackageDoc pkg, String label, boolean isStrong) {
        print(getPackageLinkString(pkg, label, isStrong));
    }
    public void printPackageLink(PackageDoc pkg, String label, boolean isStrong,
            String style) {
        print(getPackageLinkString(pkg, label, isStrong, style));
    }
    public String getPackageLinkString(PackageDoc pkg, String label,
                                 boolean isStrong) {
        return getPackageLinkString(pkg, label, isStrong, "");
    }
    public String getPackageLinkString(PackageDoc pkg, String label, boolean isStrong,
            String style) {
        boolean included = pkg != null && pkg.isIncluded();
        if (! included) {
            PackageDoc[] packages = configuration.packages;
            for (int i = 0; i < packages.length; i++) {
                if (packages[i].equals(pkg)) {
                    included = true;
                    break;
                }
            }
        }
        if (included || pkg == null) {
            return getHyperLinkString(pathString(pkg, "package-summary.html"),
                                "", label, isStrong, style);
        } else {
            String crossPkgLink = getCrossPackageLink(Util.getPackageName(pkg));
            if (crossPkgLink != null) {
                return getHyperLinkString(crossPkgLink, "", label, isStrong, style);
            } else {
                return label;
            }
        }
    }
    public Content getPackageLink(PackageDoc pkg, Content label) {
        boolean included = pkg != null && pkg.isIncluded();
        if (! included) {
            PackageDoc[] packages = configuration.packages;
            for (int i = 0; i < packages.length; i++) {
                if (packages[i].equals(pkg)) {
                    included = true;
                    break;
                }
            }
        }
        if (included || pkg == null) {
            return getHyperLink(pathString(pkg, "package-summary.html"),
                                "", label);
        } else {
            String crossPkgLink = getCrossPackageLink(Util.getPackageName(pkg));
            if (crossPkgLink != null) {
                return getHyperLink(crossPkgLink, "", label);
            } else {
                return label;
            }
        }
    }
    public String italicsClassName(ClassDoc cd, boolean qual) {
        String name = (qual)? cd.qualifiedName(): cd.name();
        return (cd.isInterface())?  italicsText(name): name;
    }
    public void printSrcLink(ProgramElementDoc d, String label) {
        if (d == null) {
            return;
        }
        ClassDoc cd = d.containingClass();
        if (cd == null) {
            cd = (ClassDoc) d;
        }
        String href = relativePath + DocletConstants.SOURCE_OUTPUT_DIR_NAME
            + DirectoryManager.getDirectoryPath(cd.containingPackage())
            + cd.name() + ".html#" + SourceToHTMLConverter.getAnchorName(d);
        printHyperLink(href, "", label, true);
    }
    public void addSrcLink(ProgramElementDoc doc, Content label, Content htmltree) {
        if (doc == null) {
            return;
        }
        ClassDoc cd = doc.containingClass();
        if (cd == null) {
            cd = (ClassDoc) doc;
        }
        String href = relativePath + DocletConstants.SOURCE_OUTPUT_DIR_NAME
                + DirectoryManager.getDirectoryPath(cd.containingPackage())
                + cd.name() + ".html#" + SourceToHTMLConverter.getAnchorName(doc);
        Content linkContent = getHyperLink(href, "", label, "", "");
        htmltree.addContent(linkContent);
    }
    public String getLink(LinkInfoImpl linkInfo) {
        LinkFactoryImpl factory = new LinkFactoryImpl(this);
        String link = ((LinkOutputImpl) factory.getLinkOutput(linkInfo)).toString();
        displayLength += linkInfo.displayLength;
        return link;
    }
    public String getTypeParameterLinks(LinkInfoImpl linkInfo) {
        LinkFactoryImpl factory = new LinkFactoryImpl(this);
        return ((LinkOutputImpl)
            factory.getTypeParameterLinks(linkInfo, false)).toString();
    }
    public void printLink(LinkInfoImpl linkInfo) {
        print(getLink(linkInfo));
    }
    public String getCrossClassLink(String qualifiedClassName, String refMemName,
                                    String label, boolean strong, String style,
                                    boolean code) {
        String className = "",
            packageName = qualifiedClassName == null ? "" : qualifiedClassName;
        int periodIndex;
        while((periodIndex = packageName.lastIndexOf('.')) != -1) {
            className = packageName.substring(periodIndex + 1, packageName.length()) +
                (className.length() > 0 ? "." + className : "");
            String defaultLabel = code ? getCode() + className + getCodeEnd() : className;
            packageName = packageName.substring(0, periodIndex);
            if (getCrossPackageLink(packageName) != null) {
                return getHyperLinkString(
                    configuration.extern.getExternalLink(packageName, relativePath,
                                className + ".html?is-external=true"),
                    refMemName == null ? "" : refMemName,
                    label == null || label.length() == 0 ? defaultLabel : label,
                    strong, style,
                    configuration.getText("doclet.Href_Class_Or_Interface_Title", packageName),
                    "");
            }
        }
        return null;
    }
    public boolean isClassLinkable(ClassDoc cd) {
        if (cd.isIncluded()) {
            return configuration.isGeneratedDoc(cd);
        }
        return configuration.extern.isExternal(cd);
    }
    public String getCrossPackageLink(String pkgName) {
        return configuration.extern.getExternalLink(pkgName, relativePath,
            "package-summary.html?is-external=true");
    }
    public Content getQualifiedClassLink(int context, ClassDoc cd) {
        return new RawHtml(getLink(new LinkInfoImpl(context, cd,
                configuration.getClassName(cd), "")));
    }
    public void addPreQualifiedClassLink(int context, ClassDoc cd, Content contentTree) {
        addPreQualifiedClassLink(context, cd, false, contentTree);
    }
    public String getPreQualifiedClassLink(int context,
            ClassDoc cd, boolean isStrong) {
        String classlink = "";
        PackageDoc pd = cd.containingPackage();
        if(pd != null && ! configuration.shouldExcludeQualifier(pd.name())) {
            classlink = getPkgName(cd);
        }
        classlink += getLink(new LinkInfoImpl(context, cd, cd.name(), isStrong));
        return classlink;
    }
    public void addPreQualifiedClassLink(int context,
            ClassDoc cd, boolean isStrong, Content contentTree) {
        PackageDoc pd = cd.containingPackage();
        if(pd != null && ! configuration.shouldExcludeQualifier(pd.name())) {
            contentTree.addContent(getPkgName(cd));
        }
        contentTree.addContent(new RawHtml(getLink(new LinkInfoImpl(
                context, cd, cd.name(), isStrong))));
    }
    public void addPreQualifiedStrongClassLink(int context, ClassDoc cd, Content contentTree) {
        addPreQualifiedClassLink(context, cd, true, contentTree);
    }
    public void printText(String key) {
        print(configuration.getText(key));
    }
    public void printText(String key, String a1) {
        print(configuration.getText(key, a1));
    }
    public void printText(String key, String a1, String a2) {
        print(configuration.getText(key, a1, a2));
    }
    public void strongText(String key) {
        strong(configuration.getText(key));
    }
    public void strongText(String key, String a1) {
        strong(configuration.getText(key, a1));
    }
    public void strongText(String key, String a1, String a2) {
        strong(configuration.getText(key, a1, a2));
    }
    public Content getDocLink(int context, MemberDoc doc, String label) {
        return getDocLink(context, doc.containingClass(), doc, label);
    }
    public void printDocLink(int context, ClassDoc classDoc, MemberDoc doc,
            String label, boolean strong) {
        print(getDocLink(context, classDoc, doc, label, strong));
    }
    public String getDocLink(int context, MemberDoc doc, String label,
                boolean strong) {
        return getDocLink(context, doc.containingClass(), doc, label, strong);
    }
    public String getDocLink(int context, ClassDoc classDoc, MemberDoc doc,
        String label, boolean strong) {
        if (! (doc.isIncluded() ||
            Util.isLinkable(classDoc, configuration()))) {
            return label;
        } else if (doc instanceof ExecutableMemberDoc) {
            ExecutableMemberDoc emd = (ExecutableMemberDoc)doc;
            return getLink(new LinkInfoImpl(context, classDoc,
                getAnchor(emd), label, strong));
        } else if (doc instanceof MemberDoc) {
            return getLink(new LinkInfoImpl(context, classDoc,
                doc.name(), label, strong));
        } else {
            return label;
        }
    }
    public Content getDocLink(int context, ClassDoc classDoc, MemberDoc doc,
        String label) {
        if (! (doc.isIncluded() ||
            Util.isLinkable(classDoc, configuration()))) {
            return new StringContent(label);
        } else if (doc instanceof ExecutableMemberDoc) {
            ExecutableMemberDoc emd = (ExecutableMemberDoc)doc;
            return new RawHtml(getLink(new LinkInfoImpl(context, classDoc,
                getAnchor(emd), label, false)));
        } else if (doc instanceof MemberDoc) {
            return new RawHtml(getLink(new LinkInfoImpl(context, classDoc,
                doc.name(), label, false)));
        } else {
            return new StringContent(label);
        }
    }
    public void anchor(ExecutableMemberDoc emd) {
        anchor(getAnchor(emd));
    }
    public String getAnchor(ExecutableMemberDoc emd) {
        StringBuilder signature = new StringBuilder(emd.signature());
        StringBuilder signatureParsed = new StringBuilder();
        int counter = 0;
        for (int i = 0; i < signature.length(); i++) {
            char c = signature.charAt(i);
            if (c == '<') {
                counter++;
            } else if (c == '>') {
                counter--;
            } else if (counter == 0) {
                signatureParsed.append(c);
            }
        }
        return emd.name() + signatureParsed.toString();
    }
    public String seeTagToString(SeeTag see) {
        String tagName = see.name();
        if (! (tagName.startsWith("@link") || tagName.equals("@see"))) {
            return "";
        }
        StringBuffer result = new StringBuffer();
        boolean isplaintext = tagName.toLowerCase().equals("@linkplain");
        String label = see.label();
        label = (label.length() > 0)?
            ((isplaintext) ? label :
                 getCode() + label + getCodeEnd()):"";
        String seetext = replaceDocRootDir(see.text());
        if (seetext.startsWith("<") || seetext.startsWith("\"")) {
            result.append(seetext);
            return result.toString();
        }
        String text = (isplaintext) ? seetext : getCode() + seetext + getCodeEnd();
        ClassDoc refClass = see.referencedClass();
        String refClassName = see.referencedClassName();
        MemberDoc refMem = see.referencedMember();
        String refMemName = see.referencedMemberName();
        if (refClass == null) {
            PackageDoc refPackage = see.referencedPackage();
            if (refPackage != null && refPackage.isIncluded()) {
                String packageName = isplaintext ? refPackage.name() :
                    getCode() + refPackage.name() + getCodeEnd();
                result.append(getPackageLinkString(refPackage,
                    label.length() == 0 ? packageName : label, false));
            } else {
                String classCrossLink, packageCrossLink = getCrossPackageLink(refClassName);
                if (packageCrossLink != null) {
                    result.append(getHyperLinkString(packageCrossLink, "",
                        (label.length() == 0)? text : label, false));
                } else if ((classCrossLink = getCrossClassLink(refClassName,
                        refMemName, label, false, "", ! isplaintext)) != null) {
                    result.append(classCrossLink);
                } else {
                    configuration.getDocletSpecificMsg().warning(see.position(), "doclet.see.class_or_package_not_found",
                            tagName, seetext);
                    result.append((label.length() == 0)? text: label);
                }
            }
        } else if (refMemName == null) {
            if (label.length() == 0) {
                label = (isplaintext) ? refClass.name() : getCode() + refClass.name() + getCodeEnd();
                result.append(getLink(new LinkInfoImpl(refClass, label)));
            } else {
                result.append(getLink(new LinkInfoImpl(refClass, label)));
            }
        } else if (refMem == null) {
            result.append((label.length() == 0)? text: label);
        } else {
            ClassDoc containing = refMem.containingClass();
            if (see.text().trim().startsWith("#") &&
                ! (containing.isPublic() ||
                Util.isLinkable(containing, configuration()))) {
                if (this instanceof ClassWriterImpl) {
                    containing = ((ClassWriterImpl) this).getClassDoc();
                } else if (!containing.isPublic()){
                    configuration.getDocletSpecificMsg().warning(
                        see.position(), "doclet.see.class_or_package_not_accessible",
                        tagName, containing.qualifiedName());
                } else {
                    configuration.getDocletSpecificMsg().warning(
                        see.position(), "doclet.see.class_or_package_not_found",
                        tagName, seetext);
                }
            }
            if (configuration.currentcd != containing) {
                refMemName = containing.name() + "." + refMemName;
            }
            if (refMem instanceof ExecutableMemberDoc) {
                if (refMemName.indexOf('(') < 0) {
                    refMemName += ((ExecutableMemberDoc)refMem).signature();
                }
            }
            text = (isplaintext) ?
                refMemName : getCode() + Util.escapeHtmlChars(refMemName) + getCodeEnd();
            result.append(getDocLink(LinkInfoImpl.CONTEXT_SEE_TAG, containing,
                refMem, (label.length() == 0)? text: label, false));
        }
        return result.toString();
    }
    public void printInlineComment(Doc doc, Tag tag) {
        printCommentTags(doc, tag.inlineTags(), false, false);
    }
    public void addInlineComment(Doc doc, Tag tag, Content htmltree) {
        addCommentTags(doc, tag.inlineTags(), false, false, htmltree);
    }
    public void printInlineDeprecatedComment(Doc doc, Tag tag) {
        printCommentTags(doc, tag.inlineTags(), true, false);
    }
    public void addInlineDeprecatedComment(Doc doc, Tag tag, Content htmltree) {
        addCommentTags(doc, tag.inlineTags(), true, false, htmltree);
    }
    public void printSummaryComment(Doc doc) {
        printSummaryComment(doc, doc.firstSentenceTags());
    }
    public void addSummaryComment(Doc doc, Content htmltree) {
        addSummaryComment(doc, doc.firstSentenceTags(), htmltree);
    }
    public void printSummaryComment(Doc doc, Tag[] firstSentenceTags) {
        printCommentTags(doc, firstSentenceTags, false, true);
    }
    public void addSummaryComment(Doc doc, Tag[] firstSentenceTags, Content htmltree) {
        addCommentTags(doc, firstSentenceTags, false, true, htmltree);
    }
    public void printSummaryDeprecatedComment(Doc doc) {
        printCommentTags(doc, doc.firstSentenceTags(), true, true);
    }
    public void printSummaryDeprecatedComment(Doc doc, Tag tag) {
        printCommentTags(doc, tag.firstSentenceTags(), true, true);
    }
    public void addSummaryDeprecatedComment(Doc doc, Tag tag, Content htmltree) {
        addCommentTags(doc, tag.firstSentenceTags(), true, true, htmltree);
    }
    public void printInlineComment(Doc doc) {
        printCommentTags(doc, doc.inlineTags(), false, false);
        p();
    }
    public void addInlineComment(Doc doc, Content htmltree) {
        addCommentTags(doc, doc.inlineTags(), false, false, htmltree);
    }
    public void printInlineDeprecatedComment(Doc doc) {
        printCommentTags(doc, doc.inlineTags(), true, false);
    }
    private void printCommentTags(Doc doc, Tag[] tags, boolean depr, boolean first) {
        if(configuration.nocomment){
            return;
        }
        if (depr) {
            italic();
        }
        String result = commentTagsToString(null, doc, tags, first);
        print(result);
        if (depr) {
            italicEnd();
        }
        if (tags.length == 0) {
            space();
        }
    }
    private void addCommentTags(Doc doc, Tag[] tags, boolean depr,
            boolean first, Content htmltree) {
        if(configuration.nocomment){
            return;
        }
        Content div;
        Content result = new RawHtml(commentTagsToString(null, doc, tags, first));
        if (depr) {
            Content italic = HtmlTree.I(result);
            div = HtmlTree.DIV(HtmlStyle.block, italic);
            htmltree.addContent(div);
        }
        else {
            div = HtmlTree.DIV(HtmlStyle.block, result);
            htmltree.addContent(div);
        }
        if (tags.length == 0) {
            htmltree.addContent(getSpace());
        }
    }
    public String commentTagsToString(Tag holderTag, Doc doc, Tag[] tags,
            boolean isFirstSentence) {
        StringBuilder result = new StringBuilder();
        boolean textTagChange = false;
        configuration.tagletManager.checkTags(doc, tags, true);
        for (int i = 0; i < tags.length; i++) {
            Tag tagelem = tags[i];
            String tagName = tagelem.name();
            if (tagelem instanceof SeeTag) {
                result.append(seeTagToString((SeeTag)tagelem));
            } else if (! tagName.equals("Text")) {
                int originalLength = result.length();
                TagletOutput output = TagletWriter.getInlineTagOuput(
                    configuration.tagletManager, holderTag,
                    tagelem, getTagletWriterInstance(isFirstSentence));
                result.append(output == null ? "" : output.toString());
                if (originalLength == 0 && isFirstSentence && tagelem.name().equals("@inheritDoc") && result.length() > 0) {
                    break;
                } else if (configuration.docrootparent.length() > 0 &&
                        tagelem.name().equals("@docRoot") &&
                        ((tags[i + 1]).text()).startsWith("/..")) {
                    textTagChange = true;
                    continue;
                } else {
                    continue;
                }
            } else {
                String text = tagelem.text();
                if (textTagChange) {
                    text = text.replaceFirst("/..", "");
                    textTagChange = false;
                }
                text = redirectRelativeLinks(tagelem.holder(), text);
                text = replaceDocRootDir(text);
                if (isFirstSentence) {
                    text = removeNonInlineHtmlTags(text);
                }
                StringTokenizer lines = new StringTokenizer(text, "\r\n", true);
                StringBuffer textBuff = new StringBuffer();
                while (lines.hasMoreTokens()) {
                    StringBuilder line = new StringBuilder(lines.nextToken());
                    Util.replaceTabs(configuration.sourcetab, line);
                    textBuff.append(line.toString());
                }
                result.append(textBuff);
            }
        }
        return result.toString();
    }
    private boolean shouldNotRedirectRelativeLinks() {
        return  this instanceof AnnotationTypeWriter ||
                this instanceof ClassWriter ||
                this instanceof PackageSummaryWriter;
    }
    private String redirectRelativeLinks(Doc doc, String text) {
        if (doc == null || shouldNotRedirectRelativeLinks()) {
            return text;
        }
        String redirectPathFromRoot;
        if (doc instanceof ClassDoc) {
            redirectPathFromRoot = DirectoryManager.getDirectoryPath(((ClassDoc) doc).containingPackage());
        } else if (doc instanceof MemberDoc) {
            redirectPathFromRoot = DirectoryManager.getDirectoryPath(((MemberDoc) doc).containingPackage());
        } else if (doc instanceof PackageDoc) {
            redirectPathFromRoot = DirectoryManager.getDirectoryPath((PackageDoc) doc);
        } else {
            return text;
        }
        if (! redirectPathFromRoot.endsWith(DirectoryManager.URL_FILE_SEPARATOR)) {
            redirectPathFromRoot += DirectoryManager.URL_FILE_SEPARATOR;
        }
        int end, begin = text.toLowerCase().indexOf("<a");
        if(begin >= 0){
            StringBuffer textBuff = new StringBuffer(text);
            while(begin >=0){
                if (textBuff.length() > begin + 2 && ! Character.isWhitespace(textBuff.charAt(begin+2))) {
                    begin = textBuff.toString().toLowerCase().indexOf("<a", begin + 1);
                    continue;
                }
                begin = textBuff.indexOf("=", begin) + 1;
                end = textBuff.indexOf(">", begin +1);
                if(begin == 0){
                    configuration.root.printWarning(
                        doc.position(),
                        configuration.getText("doclet.malformed_html_link_tag", text));
                    break;
                }
                if (end == -1) {
                    break;
                }
                if(textBuff.substring(begin, end).indexOf("\"") != -1){
                    begin = textBuff.indexOf("\"", begin) + 1;
                    end = textBuff.indexOf("\"", begin +1);
                    if(begin == 0 || end == -1){
                        break;
                    }
                }
                String relativeLink = textBuff.substring(begin, end);
                if(!(relativeLink.toLowerCase().startsWith("mailto:") ||
                     relativeLink.toLowerCase().startsWith("http:") ||
                     relativeLink.toLowerCase().startsWith("https:") ||
                     relativeLink.toLowerCase().startsWith("file:"))){
                     relativeLink = "{@"+(new DocRootTaglet()).getName() + "}"
                        + redirectPathFromRoot
                        + relativeLink;
                    textBuff.replace(begin, end, relativeLink);
                }
                begin = textBuff.toString().toLowerCase().indexOf("<a", begin + 1);
            }
            return textBuff.toString();
        }
        return text;
    }
    public String removeNonInlineHtmlTags(String text) {
        if (text.indexOf('<') < 0) {
            return text;
        }
        String noninlinetags[] = { "<ul>", "</ul>", "<ol>", "</ol>",
                "<dl>", "</dl>", "<table>", "</table>",
                "<tr>", "</tr>", "<td>", "</td>",
                "<th>", "</th>", "<p>", "</p>",
                "<li>", "</li>", "<dd>", "</dd>",
                "<dir>", "</dir>", "<dt>", "</dt>",
                "<h1>", "</h1>", "<h2>", "</h2>",
                "<h3>", "</h3>", "<h4>", "</h4>",
                "<h5>", "</h5>", "<h6>", "</h6>",
                "<pre>", "</pre>", "<menu>", "</menu>",
                "<listing>", "</listing>", "<hr>",
                "<blockquote>", "</blockquote>",
                "<center>", "</center>",
                "<UL>", "</UL>", "<OL>", "</OL>",
                "<DL>", "</DL>", "<TABLE>", "</TABLE>",
                "<TR>", "</TR>", "<TD>", "</TD>",
                "<TH>", "</TH>", "<P>", "</P>",
                "<LI>", "</LI>", "<DD>", "</DD>",
                "<DIR>", "</DIR>", "<DT>", "</DT>",
                "<H1>", "</H1>", "<H2>", "</H2>",
                "<H3>", "</H3>", "<H4>", "</H4>",
                "<H5>", "</H5>", "<H6>", "</H6>",
                "<PRE>", "</PRE>", "<MENU>", "</MENU>",
                "<LISTING>", "</LISTING>", "<HR>",
                "<BLOCKQUOTE>", "</BLOCKQUOTE>",
                "<CENTER>", "</CENTER>"
        };
        for (int i = 0; i < noninlinetags.length; i++) {
            text = replace(text, noninlinetags[i], "");
        }
        return text;
    }
    public String replace(String text, String tobe, String by) {
        while (true) {
            int startindex = text.indexOf(tobe);
            if (startindex < 0) {
                return text;
            }
            int endindex = startindex + tobe.length();
            StringBuilder replaced = new StringBuilder();
            if (startindex > 0) {
                replaced.append(text.substring(0, startindex));
            }
            replaced.append(by);
            if (text.length() > endindex) {
                replaced.append(text.substring(endindex));
            }
            text = replaced.toString();
        }
    }
    public void printStyleSheetProperties() {
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
        link("REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"" +
                 filename + "\" " + "TITLE=\"Style\"");
    }
    public HtmlTree getStyleSheetProperties() {
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
    public boolean isCoreClass(ClassDoc cd) {
        return cd.containingClass() == null || cd.isStatic();
    }
    public void writeAnnotationInfo(PackageDoc packageDoc) {
        writeAnnotationInfo(packageDoc, packageDoc.annotations());
    }
    public void addAnnotationInfo(PackageDoc packageDoc, Content htmltree) {
        addAnnotationInfo(packageDoc, packageDoc.annotations(), htmltree);
    }
    public void writeAnnotationInfo(ProgramElementDoc doc) {
        writeAnnotationInfo(doc, doc.annotations());
    }
    public void addAnnotationInfo(ProgramElementDoc doc, Content htmltree) {
        addAnnotationInfo(doc, doc.annotations(), htmltree);
    }
    public boolean writeAnnotationInfo(int indent, Doc doc, Parameter param) {
        return writeAnnotationInfo(indent, doc, param.annotations(), false);
    }
    public boolean addAnnotationInfo(int indent, Doc doc, Parameter param,
            Content tree) {
        return addAnnotationInfo(indent, doc, param.annotations(), false, tree);
    }
    private void writeAnnotationInfo(Doc doc, AnnotationDesc[] descList) {
        writeAnnotationInfo(0, doc, descList, true);
    }
    private void addAnnotationInfo(Doc doc, AnnotationDesc[] descList,
            Content htmltree) {
        addAnnotationInfo(0, doc, descList, true, htmltree);
    }
    private boolean writeAnnotationInfo(int indent, Doc doc, AnnotationDesc[] descList, boolean lineBreak) {
        List<String> annotations = getAnnotations(indent, descList, lineBreak);
        if (annotations.size() == 0) {
            return false;
        }
        fontNoNewLine("-1");
        for (Iterator<String> iter = annotations.iterator(); iter.hasNext();) {
            print(iter.next());
        }
        fontEnd();
        return true;
    }
    private boolean addAnnotationInfo(int indent, Doc doc,
            AnnotationDesc[] descList, boolean lineBreak, Content htmltree) {
        List<String> annotations = getAnnotations(indent, descList, lineBreak);
        if (annotations.size() == 0) {
            return false;
        }
        Content annotationContent;
        for (Iterator<String> iter = annotations.iterator(); iter.hasNext();) {
            annotationContent = new RawHtml(iter.next());
            htmltree.addContent(annotationContent);
        }
        return true;
    }
    private List<String> getAnnotations(int indent, AnnotationDesc[] descList, boolean linkBreak) {
        List<String> results = new ArrayList<String>();
        StringBuffer annotation;
        for (int i = 0; i < descList.length; i++) {
            AnnotationTypeDoc annotationDoc = descList[i].annotationType();
            if (! Util.isDocumentedAnnotation(annotationDoc)){
                continue;
            }
            annotation = new StringBuffer();
            LinkInfoImpl linkInfo = new LinkInfoImpl(
                LinkInfoImpl.CONTEXT_ANNOTATION, annotationDoc);
            linkInfo.label = "@" + annotationDoc.name();
            annotation.append(getLink(linkInfo));
            AnnotationDesc.ElementValuePair[] pairs = descList[i].elementValues();
            if (pairs.length > 0) {
                annotation.append('(');
                for (int j = 0; j < pairs.length; j++) {
                    if (j > 0) {
                        annotation.append(",");
                        if (linkBreak) {
                            annotation.append(DocletConstants.NL);
                            int spaces = annotationDoc.name().length() + 2;
                            for (int k = 0; k < (spaces + indent); k++) {
                                annotation.append(' ');
                            }
                        }
                    }
                    annotation.append(getDocLink(LinkInfoImpl.CONTEXT_ANNOTATION,
                        pairs[j].element(), pairs[j].element().name(), false));
                    annotation.append('=');
                    AnnotationValue annotationValue = pairs[j].value();
                    List<AnnotationValue> annotationTypeValues = new ArrayList<AnnotationValue>();
                    if (annotationValue.value() instanceof AnnotationValue[]) {
                        AnnotationValue[] annotationArray =
                            (AnnotationValue[]) annotationValue.value();
                        for (int k = 0; k < annotationArray.length; k++) {
                            annotationTypeValues.add(annotationArray[k]);
                        }
                    } else {
                        annotationTypeValues.add(annotationValue);
                    }
                    annotation.append(annotationTypeValues.size() == 1 ? "" : "{");
                    for (Iterator<AnnotationValue> iter = annotationTypeValues.iterator(); iter.hasNext(); ) {
                        annotation.append(annotationValueToString(iter.next()));
                        annotation.append(iter.hasNext() ? "," : "");
                    }
                    annotation.append(annotationTypeValues.size() == 1 ? "" : "}");
                }
                annotation.append(")");
            }
            annotation.append(linkBreak ? DocletConstants.NL : "");
            results.add(annotation.toString());
        }
        return results;
    }
    private String annotationValueToString(AnnotationValue annotationValue) {
        if (annotationValue.value() instanceof Type) {
            Type type = (Type) annotationValue.value();
            if (type.asClassDoc() != null) {
                LinkInfoImpl linkInfo = new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_ANNOTATION, type);
                    linkInfo.label = (type.asClassDoc().isIncluded() ?
                        type.typeName() :
                        type.qualifiedTypeName()) + type.dimension() + ".class";
                return getLink(linkInfo);
            } else {
                return type.typeName() + type.dimension() + ".class";
            }
        } else if (annotationValue.value() instanceof AnnotationDesc) {
            List<String> list = getAnnotations(0,
                new AnnotationDesc[]{(AnnotationDesc) annotationValue.value()},
                    false);
            StringBuffer buf = new StringBuffer();
            for (Iterator<String> iter = list.iterator(); iter.hasNext(); ) {
                buf.append(iter.next());
            }
            return buf.toString();
        } else if (annotationValue.value() instanceof MemberDoc) {
            return getDocLink(LinkInfoImpl.CONTEXT_ANNOTATION,
                (MemberDoc) annotationValue.value(),
                ((MemberDoc) annotationValue.value()).name(), false);
         } else {
            return annotationValue.toString();
         }
    }
    public Configuration configuration() {
        return configuration;
    }
}
