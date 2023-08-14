public class HTMLReportGenerator {
    public HTMLReportGenerator() {
    }   
    private Comments existingComments_ = null;
    private Comments newComments_ = null;
    public Comments getNewComments() {
        Collections.sort(newComments_.commentsList_);
        return newComments_;
    }
    public void generate(APIComparator comp, Comments existingComments) {
        String fullReportFileName = reportFileName;
        if (outputDir != null)
            fullReportFileName = outputDir + JDiff.DIR_SEP + reportFileName;
        System.out.println("JDiff: generating HTML report into the file '" + fullReportFileName + reportFileExt + "' and the subdirectory '" + fullReportFileName + "'");
        existingComments_ = existingComments;
        newComments_ = new Comments();
        File opdir = new File(fullReportFileName);
        if (!opdir.mkdir() && !opdir.exists()) {
            System.out.println("Error: could not create the subdirectory '" + fullReportFileName + "'");
            System.exit(3);
        }
        if (!Diff.noDocDiffs) {
            Diff.emitDocDiffs(fullReportFileName);
        }
        String changesSummaryName = fullReportFileName + JDiff.DIR_SEP +
            reportFileName + "-summary" + reportFileExt;
        apiDiff = comp.apiDiff;
        try {
            FileOutputStream fos = new FileOutputStream(changesSummaryName);
            reportFile = new PrintWriter(fos);
            writeStartHTMLHeader();
            String oldAPIName = "Old API";
            if (apiDiff.oldAPIName_ != null)
                oldAPIName = apiDiff.oldAPIName_;
            String newAPIName = "New API";
            if (apiDiff.newAPIName_ != null)
                newAPIName = apiDiff.newAPIName_;
            if (windowTitle == null) 
                writeHTMLTitle("Android API Differences Report");
            else
                writeHTMLTitle(windowTitle);
            writeStyleSheetRef();
            writeText("</HEAD>");
            writeText("<body class=\"gc-documentation\">");
            writeNavigationBar(reportFileName + "-summary", null, null, 
                               null, 0, true,
                               apiDiff.packagesRemoved.size() != 0, 
                               apiDiff.packagesAdded.size() != 0,
                               apiDiff.packagesChanged.size() != 0);
            if (docTitle == null) {
                writeText("  <div id=\"titleAligner\" style=\"vertical-align:top;padding:1em;margin-left:0;text-align:left;\">");
                writeText("    <H1 class=\"pagecontenth1\">API&nbsp;Differences&nbsp;Report</H1>");
                writeText("  </div>");
            } else {
                writeText("  <div id=\"titleAligner\" style=\"vertical-align:top;padding:1em;margin-left:0;text-align:left;\">");
                writeText("    <H1 class=\"pagecontenth1\">" + docTitle + "</H1>");
                writeText("  </div>");
            }
            writeText("<p>This document details the changes in the Android framework API. It shows ");
            writeText("additions, modifications, and removals for packages, classes, methods, and "); 
            writeText("fields. Each reference to an API change includes a brief description of the ");
            writeText("API and an explanation of the change and suggested workaround, where available.</p>");
            writeText("<p>The differences described in this report are based a comparison of the APIs ");
            writeText("whose versions are specified in the upper-right corner of this page. It compares a ");
            writeText("newer \"to\" API to an older \"from\" version, noting any changes relative to the ");
            writeText("older API. So, for example, indicated API removals are no longer present in the \"to\" ");
            writeText("API.</p>");
            writeText("<p>To navigate the report, use the \"Select a Diffs Index\" and \"Filter the Index\" ");
            writeText("controls on the left. The report uses text formatting to indicate <em>interface names</em>, ");
            writeText("<a href= ><tt>links to reference documentation</tt></a>, and <a href= >links to change ");
            writeText("description</a>. </p>");
            writeText("<p>For more information about the Android framework API and SDK, ");
            writeText("see the <a href=\"http:
            writeReport(apiDiff);
            writeHTMLFooter();
            reportFile.close();
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + changesSummaryName);
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
        String tln = fullReportFileName + reportFileExt;
        String tlf = fullReportFileName + JDiff.DIR_SEP + 
            "jdiff_topleftframe" + reportFileExt;
        String allDiffsIndexName = fullReportFileName + JDiff.DIR_SEP + 
            "alldiffs_index"; 
        String packagesIndexName = fullReportFileName + JDiff.DIR_SEP + 
            "packages_index"; 
        String classesIndexName = fullReportFileName + JDiff.DIR_SEP + 
            "classes_index"; 
        String constructorsIndexName = fullReportFileName + JDiff.DIR_SEP + 
            "constructors_index"; 
        String methodsIndexName = fullReportFileName + JDiff.DIR_SEP + 
            "methods_index"; 
        String fieldsIndexName = fullReportFileName + JDiff.DIR_SEP + 
            "fields_index"; 
        HTMLFiles hf = new HTMLFiles(this);
        hf.emitTopLevelFile(tln, apiDiff);
        hf.emitTopLeftFile(tlf);
        hf.emitHelp(fullReportFileName, apiDiff);
        hf.emitStylesheet();
        HTMLIndexes h = new HTMLIndexes(this);
        h.emitAllBottomLeftFiles(packagesIndexName, classesIndexName, 
                            constructorsIndexName, methodsIndexName,
                            fieldsIndexName, allDiffsIndexName, apiDiff);
        if (doStats) {
            String sf = fullReportFileName + JDiff.DIR_SEP + 
                "jdiff_statistics" + reportFileExt;
            HTMLStatistics stats = new HTMLStatistics(this);
            stats.emitStatistics(sf, apiDiff);
        }
    }   
    public void writeReport(APIDiff apiDiff) {
        if (apiDiff.packagesRemoved.size() != 0) {
            writeTableStart("Removed Packages", 2);
            Iterator iter = apiDiff.packagesRemoved.iterator();
            while (iter.hasNext()) {
                PackageAPI pkgAPI = (PackageAPI)(iter.next());
                String pkgName = pkgAPI.name_;
                if (trace) System.out.println("Package " + pkgName + " was removed.");
                writePackageTableEntry(pkgName, 0, pkgAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (apiDiff.packagesAdded.size() != 0) {
            writeTableStart("Added Packages", 2);
            Iterator iter = apiDiff.packagesAdded.iterator();
            while (iter.hasNext()) {
                PackageAPI pkgAPI = (PackageAPI)(iter.next());
                String pkgName = pkgAPI.name_;
                if (trace) System.out.println("Package " + pkgName + " was added.");
                writePackageTableEntry(pkgName, 1, pkgAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (apiDiff.packagesChanged.size() != 0) {
            writeTableStart("Changed Packages", 3);
            Iterator iter = apiDiff.packagesChanged.iterator();
            while (iter.hasNext()) {
                PackageDiff pkgDiff = (PackageDiff)(iter.next());
                String pkgName = pkgDiff.name_;
                if (trace) System.out.println("Package " + pkgName + " was changed.");
                writePackageTableEntry(pkgName, 2, null, false);
            }
            writeTableEnd();
            writeText("<!-- End of API section -->");
            writeText("<!-- Start of packages section -->");
            PackageDiff[] pkgDiffs = new PackageDiff[apiDiff.packagesChanged.size()];
            pkgDiffs = (PackageDiff[])apiDiff.packagesChanged.toArray(pkgDiffs);
            for (int i = 0; i < pkgDiffs.length; i++) {
                reportChangedPackage(pkgDiffs, i);
            }
        }
            writeText("</div><!-- end pagecontent -->");
            writeText("</div><!-- end codesitecontent -->");
            writeText("<div style=\"padding-left: 10px; padding-right: 10px; margin-top: 0; padding-bottom: 15px;\">");
            writeText("  <table style=\"width: 100%; border: none;\"><tr>");
            writeText("    <td style=\"text-align:center;font-size: 10pt; border: none; color: ccc;\"> ");
            writeText("      <span>&copy;2008 Google - ");
            writeText("            <a href=\"http:
            writeText("            <a href=\"http:
            writeText("            <a href=\"http:
            writeText("      </span>");
            writeText("      <div style=\"position:relative;margin-top:-2em;" );
            writeText("        font-size:8pt;color:aaa;text-align:right;\">");
            writeText("        <em>Generated by <a href=\"http:
            writeText("        align=\"right\" src=\"../../../assets/jdiff_logo.gif\">");
            writeText("      </span>");
            writeText("    </td>");
            writeText(" </tr></table>");
            writeText("</div>");
            writeText("</div><!-- end gc-containter -->");
}
    public void reportChangedPackage(PackageDiff[] pkgDiffs, int pkgIndex) {
        PackageDiff pkgDiff = pkgDiffs[pkgIndex];
        String pkgName = pkgDiff.name_;
        PrintWriter oldReportFile = null;
        oldReportFile = reportFile;
        String localReportFileName = null;
        try {
            localReportFileName = reportFileName + JDiff.DIR_SEP + "pkg_" + pkgName + reportFileExt;
            if (outputDir != null)
                localReportFileName = outputDir + JDiff.DIR_SEP + localReportFileName;
            FileOutputStream fos = new FileOutputStream(localReportFileName);
            reportFile = new PrintWriter(fos);
            writeStartHTMLHeader();
            writeHTMLTitle(pkgName);
            writeStyleSheetRef();
            writeText("</HEAD>");
            writeText("<BODY>");
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + localReportFileName);
            System.out.println("Error: "+ e.getMessage());
            System.exit(1);
        }
        String pkgRef = pkgName;
        pkgRef = pkgRef.replace('.', '/');
        pkgRef = newDocPrefix + pkgRef + "/package-summary";
        String linkedPkgName = "<A HREF=\"" + pkgRef + ".html\" target=\"_top\"><font size=\"+1\"><tt>" + pkgName + "</tt></font></A>";
        String prevPkgRef = null;
        if (pkgIndex != 0) {
            prevPkgRef = "pkg_" + pkgDiffs[pkgIndex-1].name_ + reportFileExt;
        }
        String nextPkgRef = null;
        if (pkgIndex < pkgDiffs.length - 1) {
            nextPkgRef = "pkg_" + pkgDiffs[pkgIndex+1].name_ + reportFileExt;
        }
        writeSectionHeader("Package " + linkedPkgName, pkgName, 
                           prevPkgRef, nextPkgRef,
                           null, 1,
                           pkgDiff.classesRemoved.size() != 0, 
                           pkgDiff.classesAdded.size() != 0,
                           pkgDiff.classesChanged.size() != 0);
        if (reportDocChanges && pkgDiff.documentationChange_ != null) {
            String pkgDocRef = pkgName + "/package-summary";
            pkgDocRef = pkgDocRef.replace('.', '/');
            String oldPkgRef = pkgDocRef;
            String newPkgRef = pkgDocRef;
            if (oldDocPrefix != null)
                oldPkgRef = oldDocPrefix + oldPkgRef;
            else 
                oldPkgRef = null;
            newPkgRef = newDocPrefix + newPkgRef;
            if (oldPkgRef != null) 
                pkgDiff.documentationChange_ += "<A HREF=\"" + oldPkgRef +
                    ".html#package_description\" target=\"_self\"><font size=\"+1\"><tt>old</tt></font></A> to ";
            else
                pkgDiff.documentationChange_ += "<font size=\"+1\"><tt>old</tt></font> to ";
            pkgDiff.documentationChange_ += "<A HREF=\"" + newPkgRef + 
                ".html#package_description\" target=\"_self\"><font size=\"+1\"><tt>new</tt></font></A>. ";
            writeText(pkgDiff.documentationChange_);
        }
        if (pkgDiff.classesRemoved.size() != 0) {
            boolean hasClasses = false;
            boolean hasInterfaces = false;
            Iterator iter = pkgDiff.classesRemoved.iterator();
            while (iter.hasNext()) {
                ClassAPI classAPI = (ClassAPI)(iter.next());
                if (classAPI.isInterface_)
                    hasInterfaces = true;
                else
                    hasClasses = true;
            }
            if (hasInterfaces && hasClasses)
                writeTableStart("Removed Classes and Interfaces", 2);
            else if (!hasInterfaces && hasClasses)
                     writeTableStart("Removed Classes", 2);
            else if (hasInterfaces && !hasClasses)
                     writeTableStart("Removed Interfaces", 2);
            iter = pkgDiff.classesRemoved.iterator();
            while (iter.hasNext()) {
                ClassAPI classAPI = (ClassAPI)(iter.next());
                String className = classAPI.name_;
                if (trace) System.out.println("Class/Interface " + className + " was removed.");
                writeClassTableEntry(pkgName, className, 0, classAPI.isInterface_, classAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (pkgDiff.classesAdded.size() != 0) {
            boolean hasClasses = false;
            boolean hasInterfaces = false;
            Iterator iter = pkgDiff.classesAdded.iterator();
            while (iter.hasNext()) {
                ClassAPI classAPI = (ClassAPI)(iter.next());
                if (classAPI.isInterface_)
                    hasInterfaces = true;
                else
                    hasClasses = true;
            }
            if (hasInterfaces && hasClasses)
                writeTableStart("Added Classes and Interfaces", 2);
            else if (!hasInterfaces && hasClasses)
                     writeTableStart("Added Classes", 2);
            else if (hasInterfaces && !hasClasses)
                     writeTableStart("Added Interfaces", 2);
            iter = pkgDiff.classesAdded.iterator();
            while (iter.hasNext()) {
                ClassAPI classAPI = (ClassAPI)(iter.next());
                String className = classAPI.name_;
                if (trace) System.out.println("Class/Interface " + className + " was added.");
                writeClassTableEntry(pkgName, className, 1, classAPI.isInterface_, classAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (pkgDiff.classesChanged.size() != 0) {
            boolean hasClasses = false;
            boolean hasInterfaces = false;
            Iterator iter = pkgDiff.classesChanged.iterator();
            while (iter.hasNext()) {
                ClassDiff classDiff = (ClassDiff)(iter.next());
                if (classDiff.isInterface_)
                    hasInterfaces = true;
                else
                    hasClasses = true;
            }
            if (hasInterfaces && hasClasses)
                writeTableStart("Changed Classes and Interfaces", 2);
            else if (!hasInterfaces && hasClasses)
                     writeTableStart("Changed Classes", 2);
            else if (hasInterfaces && !hasClasses)
                     writeTableStart("Changed Interfaces", 2);
            iter = pkgDiff.classesChanged.iterator();
            while (iter.hasNext()) {
                ClassDiff classDiff = (ClassDiff)(iter.next());
                String className = classDiff.name_;
                if (trace) System.out.println("Package " + pkgDiff.name_ + ", class/Interface " + className + " was changed.");
                writeClassTableEntry(pkgName, className, 2, classDiff.isInterface_, null, false);
            }
            writeTableEnd();
            ClassDiff[] classDiffs = new ClassDiff[pkgDiff.classesChanged.size()];
            classDiffs = (ClassDiff[])pkgDiff.classesChanged.toArray(classDiffs);
            for (int k = 0; k < classDiffs.length; k++) {
                reportChangedClass(pkgName, classDiffs, k);
            }
        }
        writeSectionFooter(pkgName, prevPkgRef, nextPkgRef, null, 1);
        writeHTMLFooter();
        reportFile.close();
        reportFile = oldReportFile;
    }
    public void reportChangedClass(String pkgName, ClassDiff[] classDiffs, int classIndex) {
        ClassDiff classDiff = classDiffs[classIndex];
        String className = classDiff.name_;
        PrintWriter oldReportFile = null;
        oldReportFile = reportFile;
        String localReportFileName = null;
        try {
            localReportFileName = reportFileName + JDiff.DIR_SEP + pkgName + "." + className + reportFileExt;
            if (outputDir != null)
                localReportFileName = outputDir + JDiff.DIR_SEP + localReportFileName;
            FileOutputStream fos = new FileOutputStream(localReportFileName);
            reportFile = new PrintWriter(fos);
            writeStartHTMLHeader();
            writeHTMLTitle(pkgName + "." + className);
            writeStyleSheetRef();
            writeText("</HEAD>");
            writeText("<BODY>");
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + localReportFileName);
            System.out.println("Error: "+ e.getMessage());
            System.exit(1);
        }
        String classRef = pkgName + "." + className;
        classRef = classRef.replace('.', '/');
        if (className.indexOf('.') != -1) {
            classRef = pkgName + ".";
            classRef = classRef.replace('.', '/');
            classRef = newDocPrefix + classRef + className;
        } else {
            classRef = newDocPrefix + classRef;
        }
        String linkedClassName = "<A HREF=\"" + classRef + ".html\" target=\"_top\"><font size=\"+1\"><tt>" + className + "</tt></font></A>";
        String lcn = pkgName + "." + linkedClassName;
        String prevClassRef = null;
        if (classIndex != 0) {
            prevClassRef = pkgName + "." + classDiffs[classIndex-1].name_ + reportFileExt;
        }
        String nextClassRef = null;
        if (classIndex < classDiffs.length - 1) {
            nextClassRef = pkgName + "." + classDiffs[classIndex+1].name_ + reportFileExt;
        }
        if (classDiff.isInterface_)
            lcn = "Interface " + lcn;
        else
            lcn = "Class " + lcn;
        boolean hasCtors = classDiff.ctorsRemoved.size() != 0 ||
            classDiff.ctorsAdded.size() != 0 ||
            classDiff.ctorsChanged.size() != 0;
        boolean hasMethods = classDiff.methodsRemoved.size() != 0 ||
            classDiff.methodsAdded.size() != 0 ||
            classDiff.methodsChanged.size() != 0;
        boolean hasFields = classDiff.fieldsRemoved.size() != 0 ||
            classDiff.fieldsAdded.size() != 0 ||
            classDiff.fieldsChanged.size() != 0;
        writeSectionHeader(lcn, pkgName, prevClassRef, nextClassRef, 
                           className, 2,
                           hasCtors, hasMethods, hasFields);
        if (classDiff.inheritanceChange_ != null)
            writeText("<p><font xsize=\"+1\">" + classDiff.inheritanceChange_ + "</font>");
        if (reportDocChanges && classDiff.documentationChange_ != null) {
            String oldClassRef = null;
            if (oldDocPrefix != null) {
                oldClassRef = pkgName + "." + className;
                oldClassRef = oldClassRef.replace('.', '/');
                if (className.indexOf('.') != -1) {
                    oldClassRef = pkgName + ".";
                    oldClassRef = oldClassRef.replace('.', '/');
                    oldClassRef = oldDocPrefix + oldClassRef + className;
                } else {
                    oldClassRef = oldDocPrefix + oldClassRef;
                }
            }
            if (oldDocPrefix != null) 
                classDiff.documentationChange_ += "<A HREF=\"" + oldClassRef +
                    ".html\" target=\"_self\"><font size=\"+1\"><tt>old</tt></font></A> to ";
            else
                classDiff.documentationChange_ += "<font size=\"+1\"><tt>old</tt></font> to ";
            classDiff.documentationChange_ += "<A HREF=\"" + classRef + 
                ".html\" target=\"_self\"><font size=\"+1\"><tt>new</tt></font></A>. ";
            writeText(classDiff.documentationChange_);
        }
        if (classDiff.modifiersChange_ != null)
            writeText("<p>" + classDiff.modifiersChange_);
        reportAllCtors(pkgName, classDiff);
        reportAllMethods(pkgName, classDiff);
        reportAllFields(pkgName, classDiff);
        writeSectionFooter(pkgName, prevClassRef, nextClassRef, className, 2);
        writeHTMLFooter();
        reportFile.close();
        reportFile = oldReportFile;
    }
    public void reportAllCtors(String pkgName, ClassDiff classDiff) {
        String className = classDiff.name_;
        writeText("<a NAME=\"constructors\"></a>"); 
        if (classDiff.ctorsRemoved.size() != 0) {
            writeTableStart("Removed Constructors", 2);
            Iterator iter = classDiff.ctorsRemoved.iterator();
            while (iter.hasNext()) {
                ConstructorAPI ctorAPI = (ConstructorAPI)(iter.next());
                String ctorType = ctorAPI.type_;
                if (ctorType.compareTo("void") == 0)
                    ctorType = "";
                String id = className + "(" + ctorType + ")";
                if (trace) System.out.println("Constructor " + id + " was removed.");
                writeCtorTableEntry(pkgName, className, ctorType, 0, ctorAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (classDiff.ctorsAdded.size() != 0) {
            writeTableStart("Added Constructors", 2);
            Iterator iter = classDiff.ctorsAdded.iterator();
            while (iter.hasNext()) {
                ConstructorAPI ctorAPI = (ConstructorAPI)(iter.next());
                String ctorType = ctorAPI.type_;
                if (ctorType.compareTo("void") == 0)
                    ctorType = "";
                String id = className + "(" + ctorType + ")";
                if (trace) System.out.println("Constructor " + id + " was added.");
                writeCtorTableEntry(pkgName, className, ctorType, 1, ctorAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (classDiff.ctorsChanged.size() != 0) {
            writeTableStart("Changed Constructors", 3);
            Iterator iter = classDiff.ctorsChanged.iterator();
            while (iter.hasNext()) {
                MemberDiff memberDiff = (MemberDiff)(iter.next());
                if (trace) System.out.println("Constructor for " + className +
                    " was changed from " + memberDiff.oldType_ + " to " + 
                    memberDiff.newType_);
                writeCtorChangedTableEntry(pkgName, className, memberDiff);
            }
            writeTableEnd();
        }
    }
    public void reportAllMethods(String pkgName, ClassDiff classDiff) {
        writeText("<a NAME=\"methods\"></a>"); 
        String className = classDiff.name_;
        if (classDiff.methodsRemoved.size() != 0) {
            writeTableStart("Removed Methods", 2);
            Iterator iter = classDiff.methodsRemoved.iterator();
            while (iter.hasNext()) {
                MethodAPI methodAPI = (MethodAPI)(iter.next());
                String methodName = methodAPI.name_ + "(" + methodAPI.getSignature() + ")";
                if (trace) System.out.println("Method " + methodName + " was removed.");
                writeMethodTableEntry(pkgName, className, methodAPI, 0, methodAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (classDiff.methodsAdded.size() != 0) {
            writeTableStart("Added Methods", 2);
            Iterator iter = classDiff.methodsAdded.iterator();
            while (iter.hasNext()) {
                MethodAPI methodAPI = (MethodAPI)(iter.next());
                String methodName = methodAPI.name_ + "(" + methodAPI.getSignature() + ")";
                if (trace) System.out.println("Method " + methodName + " was added.");
                writeMethodTableEntry(pkgName, className, methodAPI, 1, methodAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (classDiff.methodsChanged.size() != 0) {
            writeTableStart("Changed Methods", 3);
            Iterator iter = classDiff.methodsChanged.iterator();
            while (iter.hasNext()) {
                MemberDiff memberDiff = (MemberDiff)(iter.next());
                if (trace) System.out.println("Method " + memberDiff.name_ + 
                      " was changed.");
                writeMethodChangedTableEntry(pkgName, className, memberDiff);
            }
            writeTableEnd();
        }
    }
    public void reportAllFields(String pkgName, ClassDiff classDiff) {
        writeText("<a NAME=\"fields\"></a>"); 
        String className = classDiff.name_;
        if (classDiff.fieldsRemoved.size() != 0) {
            writeTableStart("Removed Fields", 2);
            Iterator iter = classDiff.fieldsRemoved.iterator();
            while (iter.hasNext()) {
                FieldAPI fieldAPI = (FieldAPI)(iter.next());
                String fieldName = fieldAPI.name_;
                if (trace) System.out.println("Field " + fieldName + " was removed.");
                writeFieldTableEntry(pkgName, className, fieldAPI, 0, fieldAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (classDiff.fieldsAdded.size() != 0) {
            writeTableStart("Added Fields", 2);
            Iterator iter = classDiff.fieldsAdded.iterator();
            while (iter.hasNext()) {
                FieldAPI fieldAPI = (FieldAPI)(iter.next());
                String fieldName = fieldAPI.name_;
                if (trace) System.out.println("Field " + fieldName + " was added.");
                writeFieldTableEntry(pkgName, className, fieldAPI, 1, fieldAPI.doc_, false);
            }
            writeTableEnd();
        }
        if (classDiff.fieldsChanged.size() != 0) {
            writeTableStart("Changed Fields", 3);
            Iterator iter = classDiff.fieldsChanged.iterator();
            while (iter.hasNext()) {
                MemberDiff memberDiff = (MemberDiff)(iter.next());
                if (trace) System.out.println("Field " + pkgName + "." + className + "." + memberDiff.name_ + " was changed from " + memberDiff.oldType_ + " to " + memberDiff.newType_);
                writeFieldChangedTableEntry(pkgName, className, memberDiff);
            }
            writeTableEnd();
        }
    }
    public void writeStartHTMLHeaderWithDate() {
        writeStartHTMLHeader(true);
    }
    public void writeStartHTMLHeader() {
        writeStartHTMLHeader(false);
    }
    public void writeStartHTMLHeader(boolean addDate) {
        writeText("<!DOCTYPE HTML PUBLIC \"-
        writeText("<HTML>");
        writeText("<HEAD>");
        writeText("<meta name=\"generator\" content=\"JDiff v" + JDiff.version + "\">");
        writeText("<!-- Generated by the JDiff Javadoc doclet -->");
        writeText("<!-- (" + JDiff.jDiffLocation + ") -->");
        if (addDate)
            writeText("<!-- on " + new Date() + " -->");
        writeText("<meta name=\"description\" content=\"" + JDiff.jDiffDescription + "\">");
        writeText("<meta name=\"keywords\" content=\"" + JDiff.jDiffKeywords + "\">");
    }
    public void writeHTMLTitle(String title) {
        writeText("<TITLE>");
        writeText(title);
        writeText("</TITLE>");
    }
    public void writeStyleSheetRef() {
        writeStyleSheetRef(false);
    }
    public void writeStyleSheetRef(boolean inSameDir) {
        if (inSameDir) {
            writeText("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../assets/codesite/codesite.css\" />");
            writeText("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../assets/codesite/codesearch.css\" />");
            writeText("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../assets/codesite/semantic_headers.css\" />");
            writeText("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../assets/style.css\" />");
            writeText("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"stylesheet-jdiff.css\" TITLE=\"Style\">");
	}
        else {
            writeText("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../assets/codesite/codesite.css\" />");
            writeText("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../assets/codesite/codesearch.css\" />");
            writeText("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../assets/codesite/semantic_headers.css\" />");
            writeText("<link rel=\"stylesheet\" type=\"text/css\" href=\"../../../assets/style.css\" />");
            writeText("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"../stylesheet-jdiff.css\" TITLE=\"Style\">");
	}
    }
    public void writeHTMLFooter() {
    writeText("<script src=\"http:
    writeText("</script>");
    writeText("<script type=\"text/javascript\">");
    writeText("  try {");
    writeText("    var pageTracker = _gat._getTracker(\"UA-18071-1\");");
    writeText("    pageTracker._setAllowAnchor(true);");
    writeText("    pageTracker._initData();");
    writeText("    pageTracker._trackPageview();");
    writeText("  } catch(e) {}");
    writeText("</script>");
    writeText("</BODY>");
    writeText("</HTML>");
    }
    public void writeSectionHeader(String title, String packageName, 
                                   String prevElemLink, String nextElemLink,
                                   String className, int level, 
                                   boolean hasRemovals, 
                                   boolean hasAdditions, 
                                   boolean hasChanges) {
        writeNavigationBar(packageName, prevElemLink, nextElemLink,
                           className, level, true,
                           hasRemovals, hasAdditions, hasChanges);
        if (level != 0) {
            reportFile.println("<H2>");
            reportFile.println(title);
            reportFile.println("</H2>");
        }
    }
    public void writeSectionFooter(String packageName, 
                                   String prevElemLink, String nextElemLink,
                                   String className, int level) {
            writeText("</div><!-- end codesitecontent -->");
            writeText("<div style=\"padding-left: 10px; padding-right: 10px; margin-top: 0; padding-bottom: 15px;\">");
            writeText("  <table style=\"width: 100%; border: none;\"><tr>");
            writeText("    <td style=\"text-align:center;font-size: 10pt; border: none; color: ccc;\"> ");
            writeText("      <span>&copy;2008 Google - ");
            writeText("            <a href=\"http:
            writeText("            <a href=\"http:
            writeText("            <a href=\"http:
            writeText("      </span>");
            writeText("      <div style=\"xborder:1px solid red;position:relative;margin-top:-2em;" );
            writeText("        font-size:8pt;color:aaa;text-align:right;\">");
            writeText("        <em>Generated by <a href=\"http:
            writeText("        align=\"right\" src=\"../../../assets/jdiff_logo.gif\">");
            writeText("      </span>");
            writeText("    </td>");
            writeText(" </tr></table>");
            writeText("</div>");
            writeText("</div><!-- end gc-containter -->");
    }
    public void writeNavigationBar(String pkgName,
                                   String prevElemLink, String nextElemLink,
                                   String className, int level,
                                   boolean upperNavigationBar,
                                   boolean hasRemovals, boolean hasAdditions, 
                                   boolean hasChanges) {
            String oldAPIName = "Old API";
            if (apiDiff.oldAPIName_ != null)
                oldAPIName = apiDiff.oldAPIName_;
            String newAPIName = "New API";
            if (apiDiff.newAPIName_ != null)
                newAPIName = apiDiff.newAPIName_;
            SimpleDateFormat formatter
              = new SimpleDateFormat ("yyyy.MM.dd HH:mm");
            Date day = new Date();
	    reportFile.println("<!-- Start of nav bar -->");
	    reportFile.println("<div id=\"gc-container\" style=\"padding-left:1em;padding-right:1em;\" id=\"pagecontent\">");
	    reportFile.println("<a name=\"top\"></a>");
	    reportFile.println("<div id=\"gc-header\">");
	    reportFile.println("  <div id=\"logo\"  style=\"padding-left:1em;\">");
	    reportFile.println("    <a href=\"../../../documentation.html\" target=\"_top\"><img style=\"border: 0;\" src=\"../../../assets-google/android-logo-sm.gif\" \"/></a>");
	    reportFile.println("  </div> <!-- End logo -->");
	    reportFile.println("  <div class=\"and-diff-id\">");
            reportFile.println("    <table class=\"diffspectable\">");
	    reportFile.println("      <tr>");
	    reportFile.println("        <td colspan=\"2\" class=\"diffspechead\">API Diff Specification</td>");
	    reportFile.println("      </tr>");
	    reportFile.println("      <tr>");
	    reportFile.println("        <td class=\"diffspec\" style=\"padding-top:.25em\">To Version:</td>");
	    reportFile.println("        <td class=\"diffvaluenew\" style=\"padding-top:.25em\">" + newAPIName + "</td>");
	    reportFile.println("      </tr>");
	    reportFile.println("      <tr>");
	    reportFile.println("        <td class=\"diffspec\">From Version:</td>");
	    reportFile.println("        <td class=\"diffvalueold\">" + oldAPIName + "</td>");
	    reportFile.println("      </tr>");
	    reportFile.println("      <tr>");
	    reportFile.println("        <td class=\"diffspec\">Generated</td>");
	    reportFile.println("        <td class=\"diffvalue\">" + formatter.format( day ) + "</td>");
	    reportFile.println("      </tr>");
 	    reportFile.println("    </table>");
	    reportFile.println("  </div> <!-- End and-diff-id -->");
            if (doStats) {
	    	reportFile.println("  <div class=\"and-diff-id\">");
	    	reportFile.println("    <table class=\"diffspectable\">");
	    	reportFile.println("      <tr>");
	    	reportFile.println("        <td class=\"diffspec\" colspan=\"2\"><a href=\"jdiff_statistics.html\">Statistics</a></div>");
	    	reportFile.println("      </tr>");
 	    	reportFile.println("    </table>");
	    	reportFile.println("  </div> <!-- End and-diff-id -->");
	    }
	    reportFile.println("</div> <!-- End gc-header -->");
	    reportFile.println("<div id=\"codesiteContent\" style=\"margin-top: 70px;margin-bottom:80px;\">");
    }
    public void writeTableStart(String title, int colSpan) {
        reportFile.println("<p>");
        int idx = title.indexOf(' ');
        String namedAnchor = title.substring(0, idx);
        reportFile.println("<a NAME=\"" + namedAnchor + "\"></a>"); 
        reportFile.println("<TABLE summary=\"" + title+ "\" WIDTH=\"100%\">");
        reportFile.println("<TR>");
        reportFile.print("  <TH VALIGN=\"TOP\" COLSPAN=" + colSpan + ">");
        reportFile.println(title + "</FONT></TD>");
        reportFile.println("</TH>");
    }
    public String makeTwoRows(String name) {
        if (name.length() < 30)
            return name;
        int idx = name.indexOf(".", 20);
        if (idx == -1)
            return name;
        int len = name.length();
        String res = name.substring(0, idx+1) + "<br>" + name.substring(idx+1, len);
        return res;
    }
    public void writePackageTableEntry(String pkgName, int linkType, 
                                       String possibleComment, boolean useOld) {
        if (!useOld) {
            reportFile.println("<TR BGCOLOR=\"" + bgcolor + "\" CLASS=\"TableRowColor\">");
            reportFile.println("  <TD VALIGN=\"TOP\" WIDTH=\"25%\">");
            reportFile.println("  <A NAME=\"" + pkgName + "\"></A>"); 
        }
        if (linkType == 0) {
            if (oldDocPrefix == null) {
                reportFile.print("  " + pkgName);
            } else {
                writePackageTableEntry(pkgName, 1, possibleComment, true);
            }
        } else if (linkType == 1) {
            String pkgRef = pkgName;
            pkgRef = pkgRef.replace('.', '/');
            if (useOld)
                pkgRef = oldDocPrefix + pkgRef + "/package-summary";
            else
                pkgRef = newDocPrefix + pkgRef + "/package-summary";
            reportFile.println("  <nobr><A HREF=\"" + pkgRef + ".html\" target=\"_top\"><font size=\"+1\"><tt>" + pkgName + "</tt></font></A></nobr>");
        } else if (linkType == 2) {
            reportFile.println("  <nobr><A HREF=\"pkg_" + pkgName + reportFileExt + "\">" + pkgName + "</A></nobr>");
        } 
        if (!useOld) {
            reportFile.println("  </TD>");
            emitComment(pkgName, possibleComment, linkType);
            reportFile.println("</TR>");
        }
    }
    public void writeClassTableEntry(String pkgName, String className, 
                                     int linkType, boolean isInterface, 
                                     String possibleComment, boolean useOld) {
        if (!useOld) {
            reportFile.println("<TR BGCOLOR=\"" + bgcolor + "\" CLASS=\"TableRowColor\">");
            reportFile.println("  <TD VALIGN=\"TOP\" WIDTH=\"25%\">");
            reportFile.println("  <A NAME=\"" + className + "\"></A>"); 
        }
        String fqName = pkgName + "." + className;
        String shownClassName = makeTwoRows(className);
        if (linkType == 0) {
            if (oldDocPrefix == null) {
                if (isInterface)
                    reportFile.println("  <I>" + shownClassName + "</I>");
                else
                    reportFile.println("  " + shownClassName);
            } else {
                writeClassTableEntry(pkgName, className, 
                                     1, isInterface, 
                                     possibleComment, true);
            }
        } else if (linkType == 1) {
            String classRef = fqName;
            if (className.indexOf('.') != -1) {
                classRef = pkgName + ".";
                classRef = classRef.replace('.', '/');
                if (useOld)
                    classRef = oldDocPrefix + classRef + className;
                else
                    classRef = newDocPrefix + classRef + className;
            } else {
                classRef = classRef.replace('.', '/');
                if (useOld)
                    classRef = oldDocPrefix + classRef;
                else
                    classRef = newDocPrefix + classRef;
            }
            reportFile.print("  <nobr><A HREF=\"" + classRef + ".html\" target=\"_top\"><font size=\"+1\"><tt>");
            if (isInterface)
                reportFile.print("<I>" + shownClassName + "</I>");
            else
                reportFile.print(shownClassName);
            reportFile.println("</tt></font></A></nobr>");
        } else if (linkType == 2) {
            reportFile.print("  <nobr><A HREF=\"" + fqName + reportFileExt + "\">");
            if (isInterface)
                reportFile.print("<I>" + shownClassName + "</I>");
            else
                reportFile.print(shownClassName);
            reportFile.println("</A></nobr>");
        } 
        if (!useOld) {
            reportFile.println("  </TD>");
            emitComment(fqName, possibleComment, linkType);
            reportFile.println("</TR>");
        }
    }
    public void writeCtorTableEntry(String pkgName, String className, 
                                    String type, int linkType, 
                                    String possibleComment, boolean useOld) {
        String fqName = pkgName + "." + className;
        String shownClassName = makeTwoRows(className);
        String lt = "removed";
        if (linkType ==1)
            lt = "added";
        String commentID = fqName + ".ctor_" + lt + "(" + type + ")";
        if (!useOld) {
            reportFile.println("<TR BGCOLOR=\"" + bgcolor + "\" CLASS=\"TableRowColor\">");
            reportFile.println("  <TD VALIGN=\"TOP\" WIDTH=\"25%\">");
            reportFile.println("  <A NAME=\"" + commentID + "\"></A>"); 
        }
        String shortType = simpleName(type);
        if (linkType == 0) {
            if (oldDocPrefix == null) {
                reportFile.print("  <nobr>" + pkgName);
                emitTypeWithParens(shortType);
                reportFile.println("</nobr>");
            } else {
                writeCtorTableEntry(pkgName, className, 
                                    type, 1, 
                                    possibleComment, true);
            }
        } else if (linkType == 1) {
            String memberRef = fqName.replace('.', '/');
            if (className.indexOf('.') != -1) {
                memberRef = pkgName + ".";
                memberRef = memberRef.replace('.', '/');
                if (useOld) {
                    memberRef = oldDocPrefix + memberRef + className;
                } else {
                    memberRef = newDocPrefix + memberRef + className;
                }
            } else {
                if (useOld) {
                    memberRef = oldDocPrefix + memberRef;
                } else {
                    memberRef = newDocPrefix + memberRef;
                }
            }
            reportFile.print("  <nobr><A HREF=\"" + memberRef + ".html#" + className +
                             "(" + type + ")\" target=\"_top\"><font size=\"+1\"><tt>" + shownClassName + "</tt></font></A>");
            emitTypeWithParens(shortType);
            reportFile.println("</nobr>");
        }
        if (!useOld) {
            reportFile.println("  </TD>");
            emitComment(commentID, possibleComment, linkType);
            reportFile.println("</TR>");
        }
    }
    public void writeCtorChangedTableEntry(String pkgName, String className, 
                                           MemberDiff memberDiff) {
        String fqName = pkgName + "." + className;
        String newSignature = memberDiff.newType_;
        if (newSignature.compareTo("void") == 0)
            newSignature = "";
        String commentID = fqName + ".ctor_changed(" + newSignature + ")";
        reportFile.println("<TR BGCOLOR=\"" + bgcolor + "\" CLASS=\"TableRowColor\">");
        reportFile.println("  <TD VALIGN=\"TOP\" WIDTH=\"25%\">");
        reportFile.println("  <A NAME=\"" + commentID + "\"></A>"); 
        String memberRef = fqName.replace('.', '/');            
        String shownClassName = makeTwoRows(className);
        if (className.indexOf('.') != -1) {
            memberRef = pkgName + ".";
            memberRef = memberRef.replace('.', '/');
            memberRef = newDocPrefix + memberRef + className;
        } else {
            memberRef = newDocPrefix + memberRef;
        }
        String newType = memberDiff.newType_;
        if (newType.compareTo("void") == 0)
            newType = "";
        String shortNewType = simpleName(memberDiff.newType_);
        reportFile.print("  <nobr><A HREF=\"" + memberRef + ".html#" + className + "(" + newType + ")\" target=\"_top\"><font size=\"+1\"><tt>");
        reportFile.print(shownClassName);
        reportFile.print("</tt></font></A>");
        emitTypeWithParens(shortNewType);
        reportFile.println("  </nobr>");
        reportFile.println("  </TD>");
        if (reportDocChanges && memberDiff.documentationChange_ != null) {
            String oldMemberRef = null;
            String oldType = null;
            if (oldDocPrefix != null) {
                oldMemberRef = pkgName + "." + className;
                oldMemberRef = oldMemberRef.replace('.', '/');
                if (className.indexOf('.') != -1) {
                    oldMemberRef = pkgName + ".";
                    oldMemberRef = oldMemberRef.replace('.', '/');
                    oldMemberRef = oldDocPrefix + oldMemberRef + className;
                } else {
                    oldMemberRef = oldDocPrefix + oldMemberRef;
                }
                oldType = memberDiff.oldType_;
                if (oldType.compareTo("void") == 0)
                    oldType = "";
            }
            if (oldDocPrefix != null) 
                memberDiff.documentationChange_ += "<A HREF=\"" + 
                    oldMemberRef + ".html#" + className + "(" + oldType + 
                    ")\" target=\"_self\"><font size=\"+1\"><tt>old</tt></font></A> to ";
            else 
                memberDiff.documentationChange_ += "<font size=\"+1\"><tt>old</tt></font> to ";
            memberDiff.documentationChange_ += "<A HREF=\"" + memberRef + 
                ".html#" + className + "(" + newType + 
                ")\" target=\"_self\"><font size=\"+1\"><tt>new</tt></font></A>.<br>";
        }
        emitChanges(memberDiff, 0);
        emitComment(commentID, null, 2);
        reportFile.println("</TR>");
    }
    public void writeMethodTableEntry(String pkgName, String className, 
                                      MethodAPI methodAPI, int linkType, 
                                      String possibleComment, boolean useOld) {
        String fqName = pkgName + "." + className;
        String signature = methodAPI.getSignature(); 
        String methodName = methodAPI.name_;
        String lt = "removed";
        if (linkType ==1)
            lt = "added";
        String commentID = fqName + "." + methodName + "_" + lt + "(" + signature + ")";
        if (!useOld) {
            reportFile.println("<TR BGCOLOR=\"" + bgcolor + "\" CLASS=\"TableRowColor\">");
            reportFile.println("  <TD VALIGN=\"TOP\" WIDTH=\"25%\">");
            reportFile.println("  <A NAME=\"" + commentID + "\"></A>"); 
        }
        if (signature.compareTo("void") == 0)
            signature = "";
        String shortSignature = simpleName(signature);
        String returnType = methodAPI.returnType_;
        String shortReturnType = simpleName(returnType);
        if (linkType == 0) {
            if (oldDocPrefix == null) {
                reportFile.print("  <nobr>");
                emitType(shortReturnType);
                reportFile.print("&nbsp;" + methodName);
                emitTypeWithParens(shortSignature);
                reportFile.println("</nobr>");
            } else {
                writeMethodTableEntry(pkgName, className, 
                                      methodAPI, 1, 
                                      possibleComment, true);
            }
        } else if (linkType == 1) {
            String memberRef = fqName.replace('.', '/');
            if (className.indexOf('.') != -1) {
                memberRef = pkgName + ".";
                memberRef = memberRef.replace('.', '/');
                if (useOld) {
                    memberRef = oldDocPrefix + memberRef + className;
                } else {
                    memberRef = newDocPrefix + memberRef + className;
                }
            } else {
                if (useOld) {
                    memberRef = oldDocPrefix + memberRef;
                } else {
                    memberRef = newDocPrefix + memberRef;
                }
            }
            reportFile.print("  <nobr>");
            emitType(shortReturnType);
            reportFile.print("&nbsp;<A HREF=\"" + memberRef + ".html#" + methodName +
               "(" + signature + ")\" target=\"_top\"><font size=\"+1\"><tt>" + methodName + "</tt></font></A>");
            emitTypeWithParens(shortSignature);
            reportFile.println("</nobr>");
        }
        if (!useOld) {
            reportFile.println("  </TD>");
            emitComment(commentID, possibleComment, linkType);
            reportFile.println("</TR>");
        }
    }
    public void writeMethodChangedTableEntry(String pkgName, String className, 
                                      MemberDiff memberDiff) {
        String memberName = memberDiff.name_;
        String fqName = pkgName + "." + className;
        String newSignature = memberDiff.newSignature_;
        String commentID = fqName + "." + memberName + "_changed(" + newSignature + ")";
        reportFile.println("<TR BGCOLOR=\"" + bgcolor + "\" CLASS=\"TableRowColor\">");
        reportFile.println("  <TD VALIGN=\"TOP\" WIDTH=\"25%\">");
        reportFile.println("  <A NAME=\"" + commentID + "\"></A>"); 
        String memberRef = fqName.replace('.', '/');            
        if (className.indexOf('.') != -1) {
            memberRef = pkgName + ".";
            memberRef = memberRef.replace('.', '/');
            memberRef = newDocPrefix + memberRef + className;
        } else {
            memberRef = newDocPrefix + memberRef;
        }
        if (className.indexOf('.') == -1 &&
            memberDiff.modifiersChange_ != null &&
            memberDiff.modifiersChange_.indexOf("but is now inherited from") != -1) {
            memberRef = memberDiff.inheritedFrom_;
            memberRef = memberRef.replace('.', '/'); 
            memberRef = newDocPrefix + memberRef;
        }
        String newReturnType = memberDiff.newType_;
        String shortReturnType = simpleName(newReturnType); 
        String shortSignature = simpleName(newSignature);        
        reportFile.print("  <nobr>");
        emitTypeWithNoParens(shortReturnType); 
        reportFile.print("&nbsp;<A HREF=\"" + memberRef + ".html#" + 
                         memberName + "(" + newSignature + ")\" target=\"_top\"><font size=\"+1\"><tt>");
        reportFile.print(memberName);
        reportFile.print("</tt></font></A>");
        emitTypeWithParens(shortSignature);
        reportFile.println("  </nobr>");
        reportFile.println("  </TD>");
        if (reportDocChanges && memberDiff.documentationChange_ != null) {
            String oldMemberRef = null;
            String oldSignature = null;
            if (oldDocPrefix != null) {
                oldMemberRef = pkgName + "." + className;
                oldMemberRef = oldMemberRef.replace('.', '/');
                if (className.indexOf('.') != -1) {
                    oldMemberRef = pkgName + ".";
                    oldMemberRef = oldMemberRef.replace('.', '/');
                    oldMemberRef = oldDocPrefix + oldMemberRef + className;
                } else {
                    oldMemberRef = oldDocPrefix + oldMemberRef;
                }
                oldSignature = memberDiff.oldSignature_;
            }
            if (oldDocPrefix != null) 
                memberDiff.documentationChange_ += "<A HREF=\"" + 
                    oldMemberRef + ".html#" + memberName + "(" + 
                    oldSignature + ")\" target=\"_self\"><font size=\"+1\"><tt>old</tt></font></A> to ";
            else
                memberDiff.documentationChange_ += "<font size=\"+1\"><tt>old</tt></font> to ";
            memberDiff.documentationChange_ += "<A HREF=\"" + memberRef + 
                ".html#" + memberName + "(" + newSignature + 
                ")\" target=\"_self\"><font size=\"+1\"><tt>new</tt></font></A>.<br>";
        }
        emitChanges(memberDiff, 1);
        if (memberDiff.modifiersChange_ != null) {
            int parentIdx = memberDiff.modifiersChange_.indexOf("now inherited from");
            if (parentIdx != -1) {
                commentID = memberDiff.inheritedFrom_ + "." + memberName + 
                    "_changed(" + newSignature + ")";
            }
        }
        emitComment(commentID, null, 2);
        reportFile.println("</TR>");
    }
    public void writeFieldTableEntry(String pkgName, String className, 
                                     FieldAPI fieldAPI, int linkType, 
                                     String possibleComment, boolean useOld) {
        String fqName = pkgName + "." + className;
        String fieldName = fieldAPI.name_;
        String commentID = fqName + "." + fieldName;
        if (!useOld) {
            reportFile.println("<TR BGCOLOR=\"" + bgcolor + "\" CLASS=\"TableRowColor\">");
            reportFile.println("  <TD VALIGN=\"TOP\" WIDTH=\"25%\">");
            reportFile.println("  <A NAME=\"" + commentID + "\"></A>"); 
        }
        String fieldType = fieldAPI.type_;
        if (fieldType.compareTo("void") == 0)
            fieldType = "";
        String shortFieldType = simpleName(fieldType);
        if (linkType == 0) {
            if (oldDocPrefix == null) {
                reportFile.print("  ");
                emitType(shortFieldType);
                reportFile.println("&nbsp;" + fieldName);
            } else {
                writeFieldTableEntry(pkgName, className, 
                                     fieldAPI, 1, 
                                     possibleComment, true);
            }
        } else if (linkType == 1) {
            String memberRef = fqName.replace('.', '/');
            if (className.indexOf('.') != -1) {
                memberRef = pkgName + ".";
                memberRef = memberRef.replace('.', '/');
                if (useOld)
                    memberRef = oldDocPrefix + memberRef + className;
                else
                    memberRef = newDocPrefix + memberRef + className;
            } else {
                if (useOld)
                    memberRef = oldDocPrefix + memberRef;
                else
                    memberRef = newDocPrefix + memberRef;
            }
            reportFile.print("  <nobr>");
            emitType(shortFieldType);
            reportFile.println("&nbsp;<A HREF=\"" + memberRef + ".html#" + fieldName +
               "\" target=\"_top\"><font size=\"+1\"><tt>" + fieldName + "</tt></font></A></nobr>");
        }
        if (!useOld) {
            reportFile.println("  </TD>");
            emitComment(commentID, possibleComment, linkType);
            reportFile.println("</TR>");
        }
        }
    public void writeFieldChangedTableEntry(String pkgName, String className, 
                                            MemberDiff memberDiff) {
        String memberName = memberDiff.name_;
        String fqName = pkgName + "." + className;
        String commentID = fqName + "." + memberName;
        reportFile.println("<TR BGCOLOR=\"" + bgcolor + "\" CLASS=\"TableRowColor\">");
        reportFile.println("  <TD VALIGN=\"TOP\" WIDTH=\"25%\">");
        reportFile.println("  <A NAME=\"" + commentID + "\"></A>"); 
        String memberRef = fqName.replace('.', '/');            
        if (className.indexOf('.') != -1) {
            memberRef = pkgName + ".";
            memberRef = memberRef.replace('.', '/');
            memberRef = newDocPrefix + memberRef + className;
        } else {
            memberRef = newDocPrefix + memberRef;
        }
        if (className.indexOf('.') == -1 &&
            memberDiff.modifiersChange_ != null &&
            memberDiff.modifiersChange_.indexOf("but is now inherited from") != -1) {
            memberRef = memberDiff.inheritedFrom_;
            memberRef = memberRef.replace('.', '/'); 
            memberRef = newDocPrefix + memberRef;
        }
        String newType = memberDiff.newType_;
        String shortNewType = simpleName(newType); 
        reportFile.print("  <nobr>");
        emitTypeWithNoParens(shortNewType);
        reportFile.print("&nbsp;<A HREF=\"" + memberRef + ".html#" + 
                         memberName + "\" target=\"_top\"><font size=\"+1\"><tt>");
        reportFile.print(memberName);
        reportFile.print("</tt></font></A></nobr>");
        reportFile.println("  </TD>");
        if (reportDocChanges && memberDiff.documentationChange_ != null) {
            String oldMemberRef = null;
            if (oldDocPrefix != null) {
                oldMemberRef = pkgName + "." + className;
                oldMemberRef = oldMemberRef.replace('.', '/');
                if (className.indexOf('.') != -1) {
                    oldMemberRef = pkgName + ".";
                    oldMemberRef = oldMemberRef.replace('.', '/');
                    oldMemberRef = oldDocPrefix + oldMemberRef + className;
                } else {
                    oldMemberRef = oldDocPrefix + oldMemberRef;
                }
            }
            if (oldDocPrefix != null) 
                memberDiff.documentationChange_ += "<A HREF=\"" + 
                    oldMemberRef + ".html#" + memberName + "\" target=\"_self\"><font size=\"+1\"><tt>old</tt></font></A> to ";
            else
                memberDiff.documentationChange_ += "<font size=\"+1\"><tt>old</tt></font> to ";
            memberDiff.documentationChange_ += "<A HREF=\"" + memberRef + 
                ".html#" + memberName + "\" target=\"_self\"><font size=\"+1\"><tt>new</tt></font></A>.<br>";
        }
        emitChanges(memberDiff, 2);
        if (memberDiff.modifiersChange_ != null) {
            int parentIdx = memberDiff.modifiersChange_.indexOf("now inherited from");
            if (parentIdx != -1) {
                commentID = memberDiff.inheritedFrom_ + "." + memberName;
            }
        }
        emitComment(commentID, null, 2);
        reportFile.println("</TR>");
    }
    public void emitChanges(MemberDiff memberDiff, int memberType){
        reportFile.println("  <TD VALIGN=\"TOP\" WIDTH=\"30%\">");
        boolean hasContent = false;
        if (memberDiff.oldType_.compareTo(memberDiff.newType_) != 0) {
            String shortOldType = simpleName(memberDiff.oldType_);
            String shortNewType = simpleName(memberDiff.newType_);
            if (memberType == 1) {
                reportFile.print("Change in return type from ");
            } else {
                reportFile.print("Change in type from ");
            }
            if (shortOldType.compareTo(shortNewType) == 0) {
                shortOldType = memberDiff.oldType_;
                shortNewType = memberDiff.newType_;
            }
            emitType(shortOldType);
            reportFile.print(" to ");
            emitType(shortNewType);
            reportFile.println(".<br>");
            hasContent = true;
        }
        if (memberType == 1 &&
            memberDiff.oldSignature_ != null && 
            memberDiff.newSignature_ != null && 
            memberDiff.oldSignature_.compareTo(memberDiff.newSignature_) != 0) {
            String shortOldSignature = simpleName(memberDiff.oldSignature_);
            String shortNewSignature = simpleName(memberDiff.newSignature_);
            if (shortOldSignature.compareTo(shortNewSignature) == 0) {
                shortOldSignature = memberDiff.oldSignature_;
                shortNewSignature = memberDiff.newSignature_;
            }
            if (hasContent)
                reportFile.print(" "); 
            reportFile.print("Change in signature from ");
            if (shortOldSignature.compareTo("") == 0)
                shortOldSignature = "void";
            emitType(shortOldSignature);
            reportFile.print(" to ");
            if (shortNewSignature.compareTo("") == 0)
                shortNewSignature = "void";
            emitType(shortNewSignature);
            reportFile.println(".<br>");
            hasContent = true;
        }
        if (memberType != 2 &&
            memberDiff.oldExceptions_ != null && 
            memberDiff.newExceptions_ != null && 
            memberDiff.oldExceptions_.compareTo(memberDiff.newExceptions_) != 0) {
            if (hasContent)
                reportFile.print(" "); 
            int spaceInOld = memberDiff.oldExceptions_.indexOf(" ");
            if (memberDiff.oldExceptions_.compareTo("no exceptions") == 0)
                spaceInOld = -1;
            int spaceInNew = memberDiff.newExceptions_.indexOf(" ");
            if (memberDiff.newExceptions_.compareTo("no exceptions") == 0)
                spaceInNew = -1;
            if (spaceInOld == -1 || spaceInNew == -1) {
                reportFile.print("Change in exceptions thrown from ");
                emitException(memberDiff.oldExceptions_); 
                reportFile.print(" to " );
                emitException(memberDiff.newExceptions_); 
                reportFile.println(".<br>");
            } else {
                boolean firstChange = true;
                int numRemoved = 0;
                StringTokenizer stOld = new StringTokenizer(memberDiff.oldExceptions_, ", ");
                while (stOld.hasMoreTokens()) {
                    String oldException = stOld.nextToken();
                    if (!memberDiff.newExceptions_.startsWith(oldException) &&
                        !(memberDiff.newExceptions_.indexOf(", " + oldException) != -1)) {
                        if (firstChange) {
                            reportFile.print("Change in exceptions: ");
                            firstChange = false;
                        }
                        if (numRemoved != 0)
                            reportFile.print(", ");
                        emitException(oldException);
                        numRemoved++;
                    }
                }
                if (numRemoved == 1)
                    reportFile.print(" was removed.");
                else if (numRemoved > 1)
                    reportFile.print(" were removed.");
                int numAdded = 0;
                StringTokenizer stNew = new StringTokenizer(memberDiff.newExceptions_, ", ");
                while (stNew.hasMoreTokens()) {
                    String newException = stNew.nextToken();
                    if (!memberDiff.oldExceptions_.startsWith(newException) &&
                        !(memberDiff.oldExceptions_.indexOf(", " + newException) != -1)) {
                        if (firstChange) {
                            reportFile.print("Change in exceptions: ");
                            firstChange = false;
                        }
                        if (numAdded != 0)
                            reportFile.println(", ");    
                        else
                            reportFile.println(" ");
                        emitException(newException);
                        numAdded++;
                    }
                }
                if (numAdded == 1)
                    reportFile.print(" was added");
                else if (numAdded > 1)
                    reportFile.print(" were added");
                else if (numAdded == 0 && numRemoved == 0 && firstChange)
                    reportFile.print("Exceptions were reordered");
                reportFile.println(".<br>");
            }
            hasContent = true;
        }
        if (memberDiff.documentationChange_ != null) {
            if (hasContent)
                reportFile.print(" "); 
            reportFile.print(memberDiff.documentationChange_);
            hasContent = true;
        }
        if (memberDiff.modifiersChange_ != null) {
            if (hasContent)
                reportFile.print(" "); 
            reportFile.println(memberDiff.modifiersChange_);
            hasContent = true;
        }
        reportFile.println("  </TD>");
    }
    public void emitException(String ex) {
        if (ex.compareTo("no exceptions") == 0) {
            reportFile.print(ex);
        } else {
            if (ex.indexOf(' ') != -1) {
                reportFile.print("(<code>" + ex + "</code>)");
            } else {
                reportFile.print("<code>" + ex + "</code>");
            }
        }
    }
    public void emitType(String type) {
        if (type.compareTo("") == 0)
            return;
        if (type.indexOf(' ') != -1) {
            reportFile.print("(<code>" + type + "</code>)");
        } else {
            reportFile.print("<code>" + type + "</code>");
        }
    }
    public static void emitTypeWithParens(String type) {
        emitTypeWithParens(type, true);
    }
    public static void emitTypeWithParens(String type, boolean addBreaks) {
        if (type.compareTo("") == 0)
            reportFile.print("()");
        else {
            int idx = type.indexOf(", ");
            if (!addBreaks || idx == -1) {
                reportFile.print("(<code>" + type + "</code>)");
            } else {
                String sepType = null;
                StringTokenizer st = new StringTokenizer(type, ", ");
                while (st.hasMoreTokens()) {
                    String p = st.nextToken();
                    if (sepType == null)
                        sepType = p;
                    else
                        sepType += ",</nobr> " + p + "<nobr>";                        
                }
                reportFile.print("(<code>" + sepType + "<nobr></code>)");
            }
        }
    }
    public static void emitTypeWithNoParens(String type) {
        if (type.compareTo("") != 0)
            reportFile.print("<code>" + type + "</code>");
    }
    public static String simpleName(String fqNames) {
        if (fqNames == null)
            return null;
        String res = "";
        boolean hasContent = false;
        ArrayList<String> fqNamesList = new ArrayList<String>();
        int genericParametersDepth = 0;
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<fqNames.length(); i++) {
          char c = fqNames.charAt(i);
          if ('<' == c) {
            genericParametersDepth++;
          }
          if ('>' == c) {
            genericParametersDepth--;
          }
          if (',' != c || genericParametersDepth > 0) {
            buffer.append(c);
          } else if (',' == c) {
            fqNamesList.add(buffer.toString().trim());
            buffer = new StringBuffer(buffer.length());
          }
        }
        fqNamesList.add(buffer.toString().trim());
        for (String fqName : fqNamesList) {
            if (hasContent)
                res += ", "; 
            hasContent = true;
            int firstBracket = fqName.indexOf('<');
            int lastBracket = fqName.lastIndexOf('>');
            String genericParameter = null;
            if (firstBracket != -1 && lastBracket != -1) {
              genericParameter = simpleName(fqName.substring(firstBracket + 1, lastBracket));
              fqName = fqName.substring(0, firstBracket);              
            }
            int lastDot = fqName.lastIndexOf('.');
            if (lastDot < 0) {
                res += fqName; 
            } else {
                res += fqName.substring(lastDot+1);
            }
            if (genericParameter != null)
              res += "&lt;" + genericParameter + "&gt;";            
        }
        return res;
    }
    public void emitComment(String commentID, String possibleComment, 
                            int linkType) {
        if (noCommentsOnRemovals && linkType == 0) {
            reportFile.println("  <TD>&nbsp;</TD>");
            return;
        }
        if (noCommentsOnAdditions && linkType == 1) {
            reportFile.println("  <TD>&nbsp;</TD>");
            return;
        }
        if (noCommentsOnChanges && linkType == 2) {
            reportFile.println("  <TD>&nbsp;</TD>");
            return;
        }
        if (!noCommentsOnChanges && possibleComment == null) {
            possibleComment = (String)Comments.allPossibleComments.get(commentID);
        }
        if (possibleComment != null) {
            int fsidx = RootDocToXML.endOfFirstSentence(possibleComment, false);
            if (fsidx != -1 && fsidx != 0)
                possibleComment = possibleComment.substring(0, fsidx+1);
        }
        String comment = Comments.getComment(existingComments_, commentID);
        if (comment.compareTo(Comments.placeHolderText) == 0) {
            if (possibleComment != null && 
                possibleComment.indexOf("InsertOtherCommentsHere") == -1)
                reportFile.println("  <TD VALIGN=\"TOP\">" + possibleComment + "</TD>");
            else
                reportFile.println("  <TD>&nbsp;</TD>");
        } else {
            int idx = comment.indexOf("@first");
            if (idx == -1) {
                reportFile.println("  <TD VALIGN=\"TOP\">" + Comments.convertAtLinks(comment, "", null, null) + "</TD>");
            } else {
                reportFile.print("  <TD VALIGN=\"TOP\">" + comment.substring(0, idx));
                if (possibleComment != null && 
                    possibleComment.indexOf("InsertOtherCommentsHere") == -1)
                    reportFile.print(possibleComment);
                reportFile.println(comment.substring(idx + 6) + "</TD>");
            }
        }
        SingleComment newComment = new SingleComment(commentID, comment);
        newComments_.addComment(newComment);
    }
    public void writeTableEnd() {
        reportFile.println("</TABLE>");
        reportFile.println("&nbsp;");
    } 
    public void writeText() {
        reportFile.println();
    } 
    public void writeText(String text) {
        reportFile.println(text);
    } 
    public void indent(int indent) {
        for (int i = 0; i < indent; i++)
            reportFile.print("&nbsp;");
    } 
    static String reportFileName = "changes";
    static String reportFileExt = ".html";
    static PrintWriter reportFile = null;
    static APIDiff apiDiff = null;
    public static boolean noCommentsOnRemovals = false;
    public static boolean noCommentsOnAdditions = false;
    public static boolean noCommentsOnChanges = false;
    public static boolean reportDocChanges = false;
    public static String newDocPrefix = "../";
    public static String oldDocPrefix = null;
    public static boolean doStats = false;
    public static String outputDir = null;
    public static String commentsDir = null;
    public static String docTitle = null;
    public static String windowTitle = null;
    static final String bgcolor = "#FFFFFF";
    private static final boolean trace = false;
}
