class Diff {
    static String saveDocDiffs(String pkgName, String className, 
                               String oldDoc, String newDoc, 
                               String id, String title) {
        if (noDocDiffs)
            return "Documentation changed from ";
        if (oldDoc == null || newDoc == null) {
            return "Documentation changed from ";
        }
        generateDiffs(pkgName, className, oldDoc, newDoc, id, title);
        return "Documentation <a href=\"" + diffFileName + pkgName +
            HTMLReportGenerator.reportFileExt + "#" + id + 
            "\">changed</a> from ";
    }
    static void generateDiffs(String pkgName, String className,
                              String oldDoc, String newDoc, 
                              String id, String title) {
        String[] oldDocWords = parseDoc(oldDoc);
        String[] newDocWords = parseDoc(newDoc);
        DiffMyers diff = new DiffMyers(oldDocWords, newDocWords);
        DiffMyers.change script = diff.diff_2(false);
        script = mergeDiffs(oldDocWords, newDocWords, script);
        String text = "<A NAME=\"" + id + "\"></A>" + title + "<br><br>";
        text += "<blockquote>";
        text = addDiffs(oldDocWords, newDocWords, script, text);
        text += "</blockquote>";
        docDiffs.add(new DiffOutput(pkgName, className, id, title, text));
    }
    static String[] parseDoc(String doc) {
        String delimiters = " .,;:?!(){}[]\"'~@#$%^&*+=_-|\\<>/";
        StringTokenizer st = new StringTokenizer(doc, delimiters, true);
        List docList = new ArrayList();
        boolean inTag = false;
        String tag = null;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (!inTag) {
                if (tok.compareTo("<") == 0) {
                    tag = tok;
                    if (st.hasMoreTokens()) {
                        tok = st.nextToken();
                        char ch = tok.charAt(0);
                        if (Character.isLetter(ch) || ch == '/') {
                            inTag = true;
                            tag += tok;
                        }
                    }
                    if (!inTag)
                      docList.add(tag);
                } else { 
                    docList.add(tok);
                }
            } else {
                if (tok.compareTo(">") == 0) {
                    inTag = false;
                    tag += tok;
                    docList.add(tag);
                } else { 
                    tag += tok;
                }
            }
        }   
        if (inTag) {
            docList.add(tag);
        }
        String[] docWords = new String[docList.size()];
        docWords = (String[])docList.toArray(docWords);
        return docWords;
    }
    static DiffMyers.change mergeDiffs(String[] oldDocWords, String[] newDocWords, 
                                       DiffMyers.change script) {
        if (script.link == null)
            return script; 
        DiffMyers.change hunk = script;
        DiffMyers.change lasthunk = null; 
        int startOld = 0;
        for (; hunk != null; hunk = hunk.link) {
            int deletes = hunk.deleted;
            int inserts = hunk.inserted;
            if (lasthunk == null) {
                if (deletes == 1 && inserts == 1) {
                    lasthunk = hunk;
                } 
                continue;
            } else {
                int first0 = hunk.line0; 
                int first1 = hunk.line1; 
                if (deletes == 1 && inserts == 1 && 
                    oldDocWords[first0 - 1].compareTo(" ") == 0 && 
                    newDocWords[first1 - 1].compareTo(" ") == 0 &&
                    first0 == lasthunk.line0 + lasthunk.deleted + 1 &&
                    first1 == lasthunk.line1 + lasthunk.inserted + 1) {
                    lasthunk.deleted += 2;
                    lasthunk.inserted += 2;
                    lasthunk.link = hunk.link;
                } else {
                    lasthunk = null;
                }
            }
        }            
        return script;
    }
    static String addDiffs(String[] oldDocWords, String[] newDocWords, 
                           DiffMyers.change script, String text) {
        String res = text;
        DiffMyers.change hunk = script;
        int startOld = 0;
        if (trace) {
            System.out.println("Old Text:");
            for (int i = 0; i < oldDocWords.length; i++) {
                System.out.print(oldDocWords[i]);
            }
            System.out.println(":END");
            System.out.println("New Text:");
            for (int i = 0; i < newDocWords.length; i++) {
                System.out.print(newDocWords[i]);
            }
            System.out.println(":END");
        }
        for (; hunk != null; hunk = hunk.link) {
            int deletes = hunk.deleted;
            int inserts = hunk.inserted;
            if (deletes == 0 && inserts == 0) {
                continue; 
            }
            int first0 = hunk.line0; 
            int last0 = hunk.line0 + hunk.deleted - 1; 
            int first1 = hunk.line1; 
            int last1 = hunk.line1 + hunk.inserted - 1;
            if (trace) {
                System.out.println("HUNK: ");
                System.out.println("inserts: " + inserts);
                System.out.println("deletes: " + deletes);
                System.out.println("first0: " + first0);
                System.out.println("last0: " + last0);
                System.out.println("first1: " + first1);
                System.out.println("last1: " + last1);
            }
            for (int i = startOld; i < first0; i++) {
                res += oldDocWords[i];
            }
            startOld = last0 + 1;
            if (deletes != 0) {
                boolean inStrike = false;
                for (int i = first0; i <= last0; i++) {
                    if (!oldDocWords[i].startsWith("<") && 
                        !oldDocWords[i].endsWith(">")) {
                        if (!inStrike) {
                            if (deleteEffect == 0)
                                res += "<strike>";
                            else if (deleteEffect == 1)
                                res += "<span style=\"background: #FFCCCC\">";
                            inStrike = true;
                        }
                        res += oldDocWords[i];
                    }
                }
                if (inStrike) {
                    if (deleteEffect == 0)
                        res += "</strike>";
                    else if (deleteEffect == 1)
                        res += "</span>";
                }
            }
            if (inserts != 0) {
                boolean inEmph = false;
                for (int i = first1; i <= last1; i++) {
                    if (!newDocWords[i].startsWith("<") && 
                        !newDocWords[i].endsWith(">")) {
                        if (!inEmph) {
                            if (insertEffect == 0)
                                res += "<font color=\"red\">";
                            else if (insertEffect == 1)
                                res += "<span style=\"background: #FFFF00\">";
                            inEmph = true;
                        }
                    }
                    res += newDocWords[i];
                }
                if (inEmph) {
                    if (insertEffect == 0)
                        res += "</font>";
                    else if (insertEffect == 1)
                        res += "</span>";
                }
            }
        } 
        for (int i = startOld; i < oldDocWords.length; i++) {
            res += oldDocWords[i];
        }
        return res;
    }
    static void emitDocDiffs(String fullReportFileName) {
        Collections.sort(docDiffs);
        DiffOutput[] docDiffsArr = new DiffOutput[docDiffs.size()];
        docDiffsArr = (DiffOutput[])docDiffs.toArray(docDiffsArr);
        for (int i = 0; i < docDiffsArr.length; i++) {
            DiffOutput diffOutput = docDiffsArr[i];
            if (currPkgName == null || 
                currPkgName.compareTo(diffOutput.pkgName_) != 0) {
                if (currPkgName != null)
                    closeDiffFile(); 
                String prevPkgName = currPkgName;
                if (currPkgName != null) {
                    prevPkgName = diffFileName + docDiffsArr[i-1].pkgName_ +
                    HTMLReportGenerator.reportFileExt;
                }
                currPkgName = diffOutput.pkgName_;
                String nextPkgName = null;
                for (int j = i; j < docDiffsArr.length; j++) {
                    if (currPkgName.compareTo(docDiffsArr[j].pkgName_) != 0) {
                        nextPkgName = diffFileName + docDiffsArr[j].pkgName_ +
                            HTMLReportGenerator.reportFileExt;
                        break;
                    }
                }
                String fullDiffFileName = fullReportFileName + 
                    JDiff.DIR_SEP + diffFileName + currPkgName +
                    HTMLReportGenerator.reportFileExt;
                try {
                    FileOutputStream fos = new FileOutputStream(fullDiffFileName);
                    diffFile = new PrintWriter(fos);
                    diffFile.println("<!DOCTYPE HTML PUBLIC \"-
                    diffFile.println("<HTML>");
                    diffFile.println("<HEAD>");
                    diffFile.println("<meta name=\"generator\" content=\"JDiff v" + JDiff.version + "\">");
                    diffFile.println("<!-- Generated by the JDiff Javadoc doclet -->");
                    diffFile.println("<!-- (" + JDiff.jDiffLocation + ") -->");
                    diffFile.println("<meta name=\"description\" content=\"" + JDiff.jDiffDescription + "\">");
                    diffFile.println("<meta name=\"keywords\" content=\"" + JDiff.jDiffKeywords + "\">");
                    diffFile.println("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"" + "../" + "stylesheet-jdiff.css\" TITLE=\"Style\">");
                    diffFile.println("<TITLE>");
                    diffFile.println(currPkgName + " Documentation Differences");
                    diffFile.println("</TITLE>");
                    diffFile.println("</HEAD>");
                    diffFile.println("<BODY>");
                    diffFile.println("<!-- Start of nav bar -->");
                    diffFile.println("<TABLE summary=\"Navigation bar\" BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\">");
                    diffFile.println("<TR>");
                    diffFile.println("<TD COLSPAN=2 BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">");
                    diffFile.println("  <TABLE summary=\"Navigation bar\" BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\">");
                    diffFile.println("    <TR ALIGN=\"center\" VALIGN=\"top\">");
                    String pkgRef = currPkgName;
                    pkgRef = pkgRef.replace('.', '/');
                    pkgRef = HTMLReportGenerator.newDocPrefix + pkgRef + "/package-summary";
                    diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"" + pkgRef + ".html\" target=\"_top\"><FONT CLASS=\"NavBarFont1\"><B><tt>" + APIDiff.newAPIName_ + "</tt></B></FONT></A>&nbsp;</TD>");
                    diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"" + HTMLReportGenerator.reportFileName + "-summary" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Overview</B></FONT></A>&nbsp;</TD>");
                    diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> &nbsp;<FONT CLASS=\"NavBarFont1\">Package</FONT>&nbsp;</TD>");
                    diffFile.println("      <TD BGCOLOR=\"#FFFFFF\" CLASS=\"NavBarCell1\"> &nbsp;<FONT CLASS=\"NavBarFont1\">Class</FONT>&nbsp;</TD>");
                    if (!Diff.noDocDiffs) {
                        diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"" + Diff.diffFileName + "index" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Text Changes</B></FONT></A>&nbsp;</TD>");
                    }
                    if (HTMLReportGenerator.doStats) {
                        diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"jdiff_statistics" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Statistics</B></FONT></A>&nbsp;</TD>");
                    }
                    diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"jdiff_help" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Help</B></FONT></A>&nbsp;</TD>");
                    diffFile.println("    </TR>");
                    diffFile.println("  </TABLE>");
                    diffFile.println("</TD>");
                    diffFile.println("<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM><b>Generated by<br><a href=\"" + JDiff.jDiffLocation + "\" class=\"staysblack\" target=\"_top\">JDiff</a></b></EM></TD>");
                    diffFile.println("</TR>");
                    diffFile.println("<TR>");
                    diffFile.println("  <TD BGCOLOR=\"" + HTMLReportGenerator.bgcolor + "\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">");
                    if (prevPkgName != null)
                        diffFile.println("  <A HREF=\"" + prevPkgName + "\"><B>PREV PACKAGE</B></A>  &nbsp;");
                    else
                        diffFile.println("  <B>PREV PACKAGE</B>  &nbsp;");
                    if (nextPkgName != null)
                        diffFile.println("  &nbsp;<A HREF=\"" + nextPkgName + "\"><B>NEXT PACKAGE</B></A>");
                    else
                        diffFile.println("  &nbsp;<B>NEXT PACKAGE</B>");
                    diffFile.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                    diffFile.println("  <A HREF=\"" + "../" + HTMLReportGenerator.reportFileName + HTMLReportGenerator.reportFileExt + "\" TARGET=\"_top\"><B>FRAMES</B></A>  &nbsp;");
                    diffFile.println("  &nbsp;<A HREF=\"" + diffFileName + currPkgName + HTMLReportGenerator.reportFileExt + "\" TARGET=\"_top\"><B>NO FRAMES</B></A></FONT></TD>");
                    diffFile.println("  <TD BGCOLOR=\"" + HTMLReportGenerator.bgcolor + "\" CLASS=\"NavBarCell2\">&nbsp;</TD>");
                    diffFile.println("</TR>");
                    diffFile.println("</TABLE>");
                    diffFile.println("<HR>");
                    diffFile.println("<!-- End of nav bar -->");
                    diffFile.println("<h2>");
                    diffFile.println(currPkgName + " Documentation Differences");
                    diffFile.println("</h2>");
                    diffFile.println();
                    diffFile.println("<blockquote>");
                    diffFile.println("This file contains all the changes in documentation in the package <code>" + currPkgName + "</code> as colored differences.");
                    if (deleteEffect == 0)
                        diffFile.println("Deletions are shown <strike>like this</strike>, and");
                    else if (deleteEffect == 1)
                        diffFile.println("Deletions are shown <span style=\"background: #FFCCCC\">like this</span>, and");
                    if (insertEffect == 0)
                        diffFile.println("additions are shown in red <font color=\"red\">like this</font>.");
                    else if (insertEffect == 1)
                        diffFile.println("additions are shown <span style=\"background: #FFFF00\">like this</span>.");
                    diffFile.println("</blockquote>");
                    diffFile.println("<blockquote>");
                    diffFile.println("If no deletions or additions are shown in an entry, the HTML tags will be what has changed. The <i>new</i> HTML tags are shown in the differences. ");
                    diffFile.println("If no documentation existed, and then some was added in a later version, this change is noted in the appropriate class pages of differences, but the change is not shown on this page. Only changes in existing text are shown here. ");
                    diffFile.println("Similarly, documentation which was inherited from another class or interface is not shown here.");
                    diffFile.println("</blockquote>");
                    diffFile.println("<blockquote>");
                    diffFile.println(" Note that an HTML error in the new documentation may cause the display of other documentation changes to be presented incorrectly. For instance, failure to close a &lt;code&gt; tag will cause all subsequent paragraphs to be displayed differently.");
                    diffFile.println("</blockquote>");
                    diffFile.println("<hr>");
                    diffFile.println();
                } catch(IOException e) {
                    System.out.println("IO Error while attempting to create " + fullDiffFileName);
                    System.out.println("Error: " + e.getMessage());
                    System.exit(1);
                }
            } 
            diffFile.println(diffOutput.text_);
            if (i != docDiffsArr.length - 1 && 
                diffOutput.className_ != null && 
                docDiffsArr[i+1].className_ != null &&
                diffOutput.className_.compareTo(docDiffsArr[i+1].className_) != 0)
                diffFile.println("<hr align=\"left\" width=\"100%\">");
        } 
        if (currPkgName != null)
            closeDiffFile(); 
        emitDocDiffIndex(fullReportFileName, docDiffsArr);
    }
    public static void emitDocDiffIndex(String fullReportFileName, 
                                        DiffOutput[] docDiffsArr) { 
        String fullDiffFileName = fullReportFileName + 
            JDiff.DIR_SEP + diffFileName + "index" +
            HTMLReportGenerator.reportFileExt;
        try {
            FileOutputStream fos = new FileOutputStream(fullDiffFileName);
            diffFile = new PrintWriter(fos);
            diffFile.println("<!DOCTYPE HTML PUBLIC \"-
            diffFile.println("<HTML>");
            diffFile.println("<HEAD>");
            diffFile.println("<meta name=\"generator\" content=\"JDiff v" + JDiff.version + "\">");
            diffFile.println("<!-- Generated by the JDiff Javadoc doclet -->");
            diffFile.println("<!-- (" + JDiff.jDiffLocation + ") -->");
            diffFile.println("<meta name=\"description\" content=\"" + JDiff.jDiffDescription + "\">");
            diffFile.println("<meta name=\"keywords\" content=\"" + JDiff.jDiffKeywords + "\">");
            diffFile.println("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"" + "../" + "stylesheet-jdiff.css\" TITLE=\"Style\">");
            diffFile.println("<TITLE>");
            diffFile.println("All Documentation Differences");
            diffFile.println("</TITLE>");
            diffFile.println("</HEAD>");
            diffFile.println("<BODY>");
            diffFile.println("<!-- Start of nav bar -->");
            diffFile.println("<TABLE summary=\"Navigation bar\" BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\">");
            diffFile.println("<TR>");
            diffFile.println("<TD COLSPAN=2 BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">");
            diffFile.println("  <TABLE summary=\"Navigation bar\" BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\">");
            diffFile.println("    <TR ALIGN=\"center\" VALIGN=\"top\">");
            diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"" + HTMLReportGenerator.newDocPrefix + "index.html\" target=\"_top\"><FONT CLASS=\"NavBarFont1\"><B><tt>" + APIDiff.newAPIName_ + "</tt></B></FONT></A>&nbsp;</TD>");
            diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"" + HTMLReportGenerator.reportFileName + "-summary" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Overview</B></FONT></A>&nbsp;</TD>");
            diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> &nbsp;<FONT CLASS=\"NavBarFont1\">Package</FONT>&nbsp;</TD>");
            diffFile.println("      <TD BGCOLOR=\"#FFFFFF\" CLASS=\"NavBarCell1\"> &nbsp;<FONT CLASS=\"NavBarFont1\">Class</FONT>&nbsp;</TD>");
            if (!Diff.noDocDiffs) {
                diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1Rev\"> <FONT CLASS=\"NavBarFont1Rev\"><B>Text Changes</B></FONT>&nbsp;</TD>");
            }
            if (HTMLReportGenerator.doStats) {
                diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"jdiff_statistics" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Statistics</B></FONT></A>&nbsp;</TD>");
            }
            diffFile.println("      <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> <A HREF=\"jdiff_help" + HTMLReportGenerator.reportFileExt + "\"><FONT CLASS=\"NavBarFont1\"><B>Help</B></FONT></A>&nbsp;</TD>");
            diffFile.println("    </TR>");
            diffFile.println("  </TABLE>");
            diffFile.println("</TD>");
            diffFile.println("<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM><b>Generated by<br><a href=\"" + JDiff.jDiffLocation + "\" class=\"staysblack\" target=\"_top\">JDiff</a></b></EM></TD>");
            diffFile.println("</TR>");
            diffFile.println("<TR>");
            diffFile.println("  <TD BGCOLOR=\"" + HTMLReportGenerator.bgcolor + "\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">");
            diffFile.println("  <A HREF=\"" + "../" + HTMLReportGenerator.reportFileName + HTMLReportGenerator.reportFileExt + "\" TARGET=\"_top\"><B>FRAMES</B></A>  &nbsp;");
            diffFile.println("  &nbsp;<A HREF=\"" + diffFileName + "index" + HTMLReportGenerator.reportFileExt + "\" TARGET=\"_top\"><B>NO FRAMES</B></A></FONT></TD>");
            diffFile.println("  <TD BGCOLOR=\"" + HTMLReportGenerator.bgcolor + "\" CLASS=\"NavBarCell2\">&nbsp;</TD>");
            diffFile.println("</TR>");
            diffFile.println("</TABLE>");
            diffFile.println("<HR>");
            diffFile.println("<!-- End of nav bar -->");
            diffFile.println("<h2>");
            diffFile.println("All Documentation Differences");
            diffFile.println("</h2>");
            diffFile.println();
            boolean firstPackage = true; 
            boolean firstClass = true; 
            boolean firstCtor = true; 
            boolean firstMethod = true; 
            boolean firstField = true; 
            for (int i = 0; i < docDiffsArr.length; i++) {
                DiffOutput diffOutput = docDiffsArr[i];
                String link = "<a href=\"" + Diff.diffFileName + diffOutput.pkgName_ + HTMLReportGenerator.reportFileExt + "#" + diffOutput.id_ + "\">";
                if (firstPackage || diffOutput.pkgName_.compareTo(docDiffsArr[i-1].pkgName_) != 0) {
                    if (firstPackage) {
                        firstPackage = false;
                    } else {
                        diffFile.println("<br>");
                    }
                    firstClass = true;
                    firstCtor = true;
                    firstMethod = true;
                    firstField = true;
                    String id = diffOutput.pkgName_ + "!package";
                    firstDiffOutput.put(id, id);
                    if (diffOutput.className_ == null) {
                        diffFile.println("<A NAME=\"" + id + "\"></A>" + link + "Package <b>" + diffOutput.pkgName_ + "</b></a><br>");
                    } else {
                        diffFile.println("<A NAME=\"" + id + "\"></A>" + "Package <b>" + diffOutput.pkgName_ + "</b><br>");
                    }
                }
                if (diffOutput.className_ != null && 
                    (firstClass || 
                     diffOutput.className_.compareTo(docDiffsArr[i-1].className_) != 0)) {
                    if (firstClass) {
                        firstClass = false;
                    } else {
                        diffFile.println("<br>");
                    }
                    firstCtor = true;
                    firstMethod = true;
                    firstField = true;
                    String id = diffOutput.pkgName_ + "." + diffOutput.className_ + "!class";
                    firstDiffOutput.put(id, id);
                    if (diffOutput.id_.endsWith("!class")) {
                        diffFile.println("<A NAME=\"" + id + "\"></A>&nbsp;&nbsp;Class " + link + diffOutput.className_ + "</a><br>");
                    } else {
                        diffFile.println("<A NAME=\"" + id + "\"></A>&nbsp;&nbsp;Class " + diffOutput.className_ + "<br>");
                    }
                }
                if (diffOutput.className_ != null && 
                    !diffOutput.id_.endsWith("!class")) {
                    int ctorIdx = diffOutput.id_.indexOf(".ctor");
                    if (ctorIdx != -1) {
                        diffFile.println("&nbsp;&nbsp;&nbsp;&nbsp;" + link + diffOutput.className_ + diffOutput.id_.substring(ctorIdx + 5) + "</a><br>");
                    } else {
                        int methodIdx = diffOutput.id_.indexOf(".dmethod.");
                        if (methodIdx != -1) {
                            diffFile.println("&nbsp;&nbsp;&nbsp;&nbsp;"  + "Method " + link + diffOutput.id_.substring(methodIdx + 9) + "</a><br>");
                        } else {
                            int fieldIdx = diffOutput.id_.indexOf(".field.");
                            if (fieldIdx != -1) {
                                diffFile.println("&nbsp;&nbsp;&nbsp;&nbsp;" + "Field " + link + diffOutput.id_.substring(fieldIdx + 7) + "</a><br>");
                            }
                        } 
                    } 
                } 
            }
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + fullDiffFileName);
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
        closeDiffFile();
    }
    public static void closeDiffFile() { 
        if (diffFile != null) {
            diffFile.println();
            diffFile.println("</BODY>");
            diffFile.println("</HTML>");
            diffFile.close();
        }
    }
    public static PrintWriter diffFile = null;
    public static String diffFileName = "docdiffs_";
    private static String currPkgName = null;
    public static boolean noDocDiffs = true;
    public static int deleteEffect = 0;
    public static int insertEffect = 1;
    public static Hashtable firstDiffOutput = new Hashtable();
    public static boolean showAllChanges = false;
    private static List docDiffs = new ArrayList(); 
    private static boolean trace = false;
}  
