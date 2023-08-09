public class HTMLIndexes {
    public HTMLIndexes(HTMLReportGenerator h) {
        h_ = h;
    }
    private HTMLReportGenerator h_ = null;
    public void emitAllBottomLeftFiles(String packagesIndexName, 
                                       String classesIndexName, 
                                       String constructorsIndexName, 
                                       String methodsIndexName,
                                       String fieldsIndexName, 
                                       String allDiffsIndexName, 
                                       APIDiff apiDiff) {
        emitBottomLeftFile(packagesIndexName, apiDiff, 3, "Package");
        emitBottomLeftFile(classesIndexName, apiDiff, 3, "Class");
        emitBottomLeftFile(constructorsIndexName, apiDiff, 3, "Constructor");
        emitBottomLeftFile(methodsIndexName, apiDiff, 3, "Method");
        emitBottomLeftFile(fieldsIndexName, apiDiff, 3, "Field");
        emitBottomLeftFile(allDiffsIndexName, apiDiff, 3, "All");
        for (int indexType = 0; indexType < 3; indexType++) {
            emitBottomLeftFile(packagesIndexName, apiDiff, indexType, "Package");
            emitBottomLeftFile(classesIndexName, apiDiff, indexType, "Class");
            emitBottomLeftFile(constructorsIndexName, apiDiff, indexType, "Constructor");
            emitBottomLeftFile(methodsIndexName, apiDiff, indexType, "Method");
            emitBottomLeftFile(fieldsIndexName, apiDiff, indexType, "Field");
            emitBottomLeftFile(allDiffsIndexName, apiDiff, indexType, "All");
        }
        if (missingSincesFile != null)
            missingSincesFile.close();
    }
    public void emitBottomLeftFile(String indexBaseName, 
                                   APIDiff apiDiff, int indexType, 
                                   String programElementType) {
        String filename = indexBaseName;
        try {
            String title = "Indexes"; 
            if (indexType == 0) {
                filename += "_removals" + h_.reportFileExt;
                title = programElementType + " Removals Index";
            } else if (indexType == 1) {
                filename += "_additions" + h_.reportFileExt;
                title = programElementType + " Additions Index";
            } else if (indexType == 2) {
                filename += "_changes" + h_.reportFileExt;
                title = programElementType + " Changes Index";
            } else if (indexType == 3) {
                filename += "_all" + h_.reportFileExt;
                title = programElementType + " Differences Index";
            }
            FileOutputStream fos = new FileOutputStream(filename);
            h_.reportFile = new PrintWriter(fos);
            h_.writeStartHTMLHeader();
            h_.writeHTMLTitle(title);
            h_.writeStyleSheetRef();
            h_.writeText("</HEAD>");
            h_.writeText("<BODY>");
            if (programElementType.compareTo("Package") == 0) {
                emitPackagesIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("Class") == 0) {
                emitClassesIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("Constructor") == 0) {
                emitConstructorsIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("Method") == 0) {
                emitMethodsIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("Field") == 0) {
                emitFieldsIndex(apiDiff, indexType);
            } else if (programElementType.compareTo("All") == 0) {
                emitAllDiffsIndex(apiDiff, indexType);
            } else{
                System.out.println("Error: unknown program element type.");
                System.exit(3);
            }
            h_.writeHTMLFooter();
            h_.reportFile.close();
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + filename);
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    private void generateLetterIndex(List list, char currChar, boolean larger) {
        if (larger)
            return; 
        int size = -2;
	if (larger)
            size = -1;
        Iterator iter = null;
        if (isAllNames)
            iter = allNames.iterator();
        else
            iter = list.iterator();
        char oldsw = '\0';
        while (iter.hasNext()) {
            Index entry = (Index)(iter.next());
            char sw = entry.name_.charAt(0);
            char swu = Character.toUpperCase(sw);
            if (swu != Character.toUpperCase(oldsw)) {
                if (Character.toUpperCase(sw) != Character.toUpperCase(currChar)) {
                    if (swu == '_') {
                        h_.writeText("<a href=\"#" + swu + "\"><font size=\"" + size + "\">" + "underscore" + "</font></a> ");
                    } else {
                        h_.writeText("<a href=\"#" + swu + "\"><font size=\"" + size + "\">" + swu + "</font></a> ");
                    }
                }
                oldsw = sw;
            }
        }
        h_.writeText(" <a href=\"#topheader\"><font size=\"" + size + "\">TOP</font></a>");
        h_.writeText("<p><div style=\"line-height:1.5em;color:black\">");
    }
    private void emitIndexHeader(String indexName, int indexType,
                                 boolean hasRemovals, 
                                 boolean hasAdditions, boolean hasChanges) {
        String linkIndexName = indexName.toLowerCase();
        boolean isAllDiffs = false;
        if (indexName.compareTo("All Differences") == 0) {
            linkIndexName = "alldiffs";
            isAllDiffs = true;
        }
        h_.writeText("<a NAME=\"topheader\"></a>"); 
        h_.writeText("<table summary=\"Index for " +  indexName + "\" width=\"100%\" class=\"index\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        h_.writeText("  <tr>");
        h_.writeText("  <th class=\"indexHeader\">");
        h_.writeText("    Filter the Index:");
        h_.writeText("  </th>");
        h_.writeText("  </tr>");
        h_.writeText("  <tr>");
        h_.writeText("  <td class=\"indexText\" style=\"line-height:1.5em;padding-left:2em;\">");
        if (indexType == 3) {
             h_.writeText("<b>" + indexName + "</b>"); }
        else if (isAllDiffs) {
            h_.writeText("<a href=\"" + linkIndexName + "_index_all" + h_.reportFileExt + "\" class=\"hiddenlink\">" + indexName + "</a>");
        }
        else {
            h_.writeText("<a href=\"" + linkIndexName + "_index_all" + h_.reportFileExt + "\" class=\"staysblack\">All " + indexName + "</a>");
        }
        h_.writeText("  <br>");
        if (hasRemovals) {
          if (indexType == 0) {
            h_.writeText("<b>Removals</b>");
          } else {
            h_.writeText("<A HREF=\"" + linkIndexName + "_index_removals" + h_.reportFileExt + "\" class=\"hiddenlink\">Removals</A>");
          }
        } else {
            h_.writeText("<font color=\"#999999\">Removals</font>");
        }
        h_.writeText("  <br>");
        if (hasAdditions) {
          if (indexType == 1) {
            h_.writeText("<b>Additions</b>");
          } else {
            h_.writeText("<A HREF=\"" + linkIndexName + "_index_additions" + h_.reportFileExt + "\"class=\"hiddenlink\">Additions</A>");
          }
        } else {
            h_.writeText("<font color=\"#999999\">Additions</font>");
        }
        h_.writeText("  <br>");
        if (hasChanges) {
          if (indexType == 2) {
            h_.writeText("<b>Changes</b>");
          } else {
            h_.writeText("<A HREF=\"" + linkIndexName + "_index_changes" + h_.reportFileExt + "\"class=\"hiddenlink\">Changes</A>");
          }
        } else {
            h_.writeText("<font color=\"#999999\">Changes</font>");
        }
        h_.writeText("  </td>");
        h_.writeText("  </tr>");
        h_.writeText("</table>");
        h_.writeText("<font size=\"-2\"><strong>Bold</strong>&nbsp;indicates&nbsp;New;&nbsp;<strike>Strike</strike>&nbsp;indicates&nbsp;deleted</font>");
        h_.writeText("  </br>");
    }
    public void emitPackagesIndex(APIDiff apiDiff, int indexType) {
        packageNames = new ArrayList(); 
        boolean hasRemovals = false;
        if (apiDiff.packagesRemoved.size() != 0)
            hasRemovals = true;
        boolean hasAdditions = false;
        if (apiDiff.packagesAdded.size() != 0)
            hasAdditions = true;
        boolean hasChanges = false;
        if (apiDiff.packagesChanged.size() != 0)
            hasChanges = true;
        recordDiffs(hasRemovals, hasAdditions, hasChanges);
        Iterator iter = apiDiff.packagesRemoved.iterator();
        while ((indexType == 3 || indexType == 0) && iter.hasNext()) {
            PackageAPI pkg = (PackageAPI)(iter.next());
            packageNames.add(new Index(pkg.name_, 0));
        }
        iter = apiDiff.packagesAdded.iterator();
        while ((indexType == 3 || indexType == 1) && iter.hasNext()) {
            PackageAPI pkg = (PackageAPI)(iter.next());
            packageNames.add(new Index(pkg.name_, 1));
        }
        iter = apiDiff.packagesChanged.iterator();
        while ((indexType == 3 || indexType == 2) && iter.hasNext()) {
            PackageDiff pkg = (PackageDiff)(iter.next());
            packageNames.add(new Index(pkg.name_, 2));
        }
        Collections.sort(packageNames);
        emitIndexHeader("Packages", indexType, hasRemovals, hasAdditions, hasChanges);
        h_.writeText("<br>");
        iter = packageNames.iterator();
        char oldsw = '\0';
        while (iter.hasNext()) {
            Index pkg = (Index)(iter.next());
            oldsw = emitPackageIndexEntry(pkg, oldsw);
        }
    }
    public char emitPackageIndexEntry(Index pkg, char oldsw) {
        char res = oldsw;
        char sw = pkg.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            res = sw;
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
        }
        if (pkg.changeType_ == 0) {
            h_.writeText("<A HREF=\"" + h_.reportFileName + "-summary" + h_.reportFileExt + "#" + pkg.name_  + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + pkg.name_ + "</strike></A><br>");
        } else if (pkg.changeType_ == 1) {
            h_.writeText("<A HREF=\"" + h_.reportFileName + "-summary" + h_.reportFileExt + "#" + pkg.name_  + "\" class=\"hiddenlink\" target=\"rightframe\"><b>" + pkg.name_ + "</b></A><br>");
        } else if (pkg.changeType_ == 2) {
            h_.writeText("<A HREF=\"pkg_" + pkg.name_ + h_.reportFileExt + "\" class=\"hiddenlink\" target=\"rightframe\">" + pkg.name_ + "</A><br>");
        }
        return res;
    }
    public void emitIndexEntries(Iterator iter) {
        char oldsw = '\0';
        int multipleMarker = 0;
        Index currIndex = null; 
        while (iter.hasNext()) {
            Index nextIndex = (Index)(iter.next()); 
            if (currIndex == null) {
                currIndex = nextIndex; 
            } else {
                if (nextIndex.name_.compareTo(currIndex.name_) == 0) {
                    if (multipleMarker == 0)
                        multipleMarker = 1; 
                    else if (multipleMarker == 1)
                        multipleMarker = 2; 
                    oldsw = emitIndexEntry(currIndex, oldsw, multipleMarker);
                } else {
                    if (multipleMarker == 1)
                        multipleMarker = 2; 
                    oldsw = emitIndexEntry(currIndex, oldsw, multipleMarker);
                    multipleMarker = 0; 
                }
                currIndex = nextIndex;
            }
        }
        if (multipleMarker == 1)
            multipleMarker = 2; 
        if (currIndex != null)
            oldsw = emitIndexEntry(currIndex, oldsw, multipleMarker);
    }
    public static boolean logMissingSinces = true;
    public static PrintWriter missingSincesFile = null;
    public void emitMissingSinces(Iterator iter) {
        if (missingSincesFile == null) {
            String sinceFileName = h_.outputDir + JDiff.DIR_SEP + "missingSinces.txt";
            try {
                FileOutputStream fos = new FileOutputStream(sinceFileName);
                missingSincesFile = new PrintWriter(fos);
            } catch (IOException e) {
                System.out.println("IO Error while attempting to create " + sinceFileName);
                System.out.println("Error: " + e.getMessage());
                System.exit(1);
            }
        }
        while (iter.hasNext()) {
            Index currIndex = (Index)(iter.next()); 
            if (currIndex.changeType_ != 1) 
                continue;
            String programElementType = currIndex.ename_;
            String details = null;
            if (programElementType.compareTo("class") == 0) {
                details = currIndex.pkgName_ + "." + currIndex.name_;
                if (currIndex.isInterface_)
                    details = details + " Interface";
                else
                    details = details + " Class";
            } else if (programElementType.compareTo("constructor") == 0) {
                details = currIndex.pkgName_ + "." + currIndex.name_ + " Constructor (" + currIndex.type_ + ")";
            } else if (programElementType.compareTo("method") == 0) {
                details = currIndex.pkgName_ + "." + currIndex.className_ + " " + "Method " + currIndex.name_ + "(" + currIndex.type_ + ")";
            } else if (programElementType.compareTo("field") == 0) {
                details = currIndex.pkgName_ + "." + currIndex.className_ + " " + "Field " + currIndex.name_;
            } else {
                System.out.println("Error: unknown program element type");
                System.exit(3);
            }
            if (currIndex.doc_ == null) {
                if (logMissingSinces)
                    missingSincesFile.println("NO DOC BLOCK: " + details);
                else
                    System.out.println("Warning: the doc block for the new element: " + details + " is missing, so there is no @since tag");
            } else if (currIndex.doc_.indexOf("@since") != -1) {
                if (logMissingSinces)
                    missingSincesFile.println("OK: " + details);
            } else {
                if (logMissingSinces)
                    missingSincesFile.println("MISSING @SINCE TAG: " + details);
                else
                    System.out.println("Warning: the doc block for the new element: " + details + " is missing an @since tag");
            }
        }
    }
    public char emitIndexEntry(Index currIndex, char oldsw, int multipleMarker) {
        String programElementType = currIndex.ename_;
        if (programElementType.compareTo("class") == 0) {
            return emitClassIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (programElementType.compareTo("constructor") == 0) {
            return emitCtorIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (programElementType.compareTo("method") == 0) {
            return emitMethodIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (programElementType.compareTo("field") == 0) {
            return emitFieldIndexEntry(currIndex, oldsw, multipleMarker);
        } else {
            System.out.println("Error: unknown program element type");
            System.exit(3);
        }
        return '\0';
    }
    public void emitClassesIndex(APIDiff apiDiff, int indexType) {
        classNames = new ArrayList(); 
        boolean hasRemovals = false;
        boolean hasAdditions = false;
        boolean hasChanges = false;
        Iterator iter = apiDiff.packagesChanged.iterator();
        while (iter.hasNext()) {
            PackageDiff pkgDiff = (PackageDiff)(iter.next());
            if (pkgDiff.classesRemoved.size() != 0)
                hasRemovals = true;
            if (pkgDiff.classesAdded.size() != 0)
                hasAdditions = true;
            if (pkgDiff.classesChanged.size() != 0)
                hasChanges = true;
            recordDiffs(hasRemovals, hasAdditions, hasChanges);
            String pkgName = pkgDiff.name_;
            Iterator iterClass = pkgDiff.classesRemoved.iterator();
            while ((indexType == 3 || indexType == 0) && iterClass.hasNext()) {
                ClassAPI cls = (ClassAPI)(iterClass.next());
                classNames.add(new Index(cls.name_, 0, pkgName, cls.isInterface_));
            }
            iterClass = pkgDiff.classesAdded.iterator();
            while ((indexType == 3 || indexType == 1) && iterClass.hasNext()) {
                ClassAPI cls = (ClassAPI)(iterClass.next());
                Index idx = new Index(cls.name_, 1, pkgName, cls.isInterface_);
                idx.doc_ = cls.doc_; 
                classNames.add(idx);
            }
            iterClass = pkgDiff.classesChanged.iterator();
            while ((indexType == 3 || indexType == 2) && iterClass.hasNext()) {
                ClassDiff cls = (ClassDiff)(iterClass.next());
                classNames.add(new Index(cls.name_, 2, pkgName, cls.isInterface_));
            }
        }
        Collections.sort(classNames);
        emitIndexHeader("Classes", indexType, hasRemovals, hasAdditions, hasChanges);
        emitIndexEntries(classNames.iterator());
        if (indexType == 1)
            emitMissingSinces(classNames.iterator());
    }
    public char emitClassIndexEntry(Index cls, char oldsw, 
                                    int multipleMarker) {
        char res = oldsw;
        String className = cls.pkgName_ + "." + cls.name_;
        String classRef = cls.pkgName_ + "." + cls.name_;
        boolean isInterface = cls.isInterface_;
        char sw = cls.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            res = sw;
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
            if (sw == '_')
                h_.writeText("<br><b>underscore</b>&nbsp;");
            else
                h_.writeText("<br><font size=\"+2\">" + Character.toUpperCase(sw) + "</font>&nbsp;");
            generateLetterIndex(classNames, sw, false);
        }
        if (multipleMarker == 1) {
            h_.writeText("<i>" + cls.name_ + "</i><br>");
        }
        if (multipleMarker != 0)
            h_.indent(INDENT_SIZE);
        if (cls.changeType_ == 0) {
            h_.writeText("<A HREF=\"pkg_" + cls.pkgName_ + h_.reportFileExt + 
                         "#" + cls.name_ + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + cls.name_ + "</strike></A><br>");
        } else if (cls.changeType_ == 1) {
            String cn = cls.name_;
            if (multipleMarker != 0)
                cn = cls.pkgName_;
            if (isInterface)
                h_.writeText("<A HREF=\"pkg_" + cls.pkgName_ + h_.reportFileExt + "#" + cls.name_ + "\" class=\"hiddenlink\" target=\"rightframe\"><b><i>" + cn + "</i></b></A><br>");
            else
                h_.writeText("<A HREF=\"pkg_" + cls.pkgName_ + h_.reportFileExt + "#" + cls.name_ + "\" class=\"hiddenlink\" target=\"rightframe\"><b>" + cn + "</b></A><br>");
        } else if (cls.changeType_ == 2) {
            String cn = cls.name_;
            if (multipleMarker != 0)
                cn = cls.pkgName_;
            if (isInterface)
                h_.writeText("<A HREF=\"" + classRef + h_.reportFileExt + "\" class=\"hiddenlink\" target=\"rightframe\"><i>" + cn + "</i></A><br>");
            else
                h_.writeText("<A HREF=\"" + classRef + h_.reportFileExt + "\" class=\"hiddenlink\" target=\"rightframe\">" + cn + "</A><br>");
        }
        return res;
    }
    public void emitConstructorsIndex(APIDiff apiDiff, int indexType) {
        ctorNames = new ArrayList(); 
        boolean hasRemovals = false;
        boolean hasAdditions = false;
        boolean hasChanges = false;
        Iterator iter = apiDiff.packagesChanged.iterator();
        while (iter.hasNext()) {
            PackageDiff pkgDiff = (PackageDiff)(iter.next());
            String pkgName = pkgDiff.name_;
            Iterator iterClass = pkgDiff.classesChanged.iterator();
            while (iterClass.hasNext()) {
                ClassDiff classDiff = (ClassDiff)(iterClass.next());
                if (classDiff.ctorsRemoved.size() != 0)
                    hasRemovals = true;
                if (classDiff.ctorsAdded.size() != 0)
                    hasAdditions = true;
                if (classDiff.ctorsChanged.size() != 0)
                    hasChanges = true;
                recordDiffs(hasRemovals, hasAdditions, hasChanges);
                String className = classDiff.name_;
                Iterator iterCtor = classDiff.ctorsRemoved.iterator();
                while ((indexType == 3 || indexType == 0) && iterCtor.hasNext()) {
                    ConstructorAPI ctor = (ConstructorAPI)(iterCtor.next());
                    ctorNames.add(new Index(className, 0, pkgName, ctor.type_));
                }
                iterCtor = classDiff.ctorsAdded.iterator();
                while ((indexType == 3 || indexType == 1) && iterCtor.hasNext()) {
                    ConstructorAPI ctor = (ConstructorAPI)(iterCtor.next());
                    Index idx = new Index(className, 1, pkgName, ctor.type_);
                    idx.doc_ = ctor.doc_; 
                    ctorNames.add(idx);
                }
                iterCtor = classDiff.ctorsChanged.iterator();
                while ((indexType == 3 || indexType == 2) && iterCtor.hasNext()) {
                    MemberDiff ctor = (MemberDiff)(iterCtor.next());
                    ctorNames.add(new Index(className, 2, pkgName, ctor.newType_));
                }
            }
        }
        Collections.sort(ctorNames);
        emitIndexHeader("Constructors", indexType, hasRemovals, hasAdditions, hasChanges);
        emitIndexEntries(ctorNames.iterator());
        if (indexType == 1)
            emitMissingSinces(ctorNames.iterator());
    }
    public char emitCtorIndexEntry(Index ctor, char oldsw, int multipleMarker) {
        char res = oldsw;
        String className = ctor.pkgName_ + "." + ctor.name_;
        String memberRef = ctor.pkgName_ + "." + ctor.name_;
        String type = ctor.type_;
        if (type.compareTo("void") == 0)
            type = "";
        String shownType = HTMLReportGenerator.simpleName(type);
        char sw = ctor.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            res = sw;
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
            if (sw == '_')
                h_.writeText("<br><b>underscore</b>&nbsp;");
            else
                h_.writeText("<br><font size=\"+2\">" + Character.toUpperCase(sw) + "</font>&nbsp;");
            generateLetterIndex(ctorNames, sw, false);
        }
        if (multipleMarker == 1) {
            h_.writeText("<i>" + ctor.name_ + "</i><br>");
        }
        if (multipleMarker != 0)
            h_.indent(INDENT_SIZE);
        if (ctor.changeType_ == 0) {
            String commentID = className + ".ctor_removed(" + type + ")";
            h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + ctor.name_ + "</strike>");
            h_.emitTypeWithParens(shownType, false);
            h_.writeText("</A></nobr>&nbsp;constructor<br>");
        } else if (ctor.changeType_ == 1) {
            String commentID = className + ".ctor_added(" + type + ")";
            h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><b>" + ctor.name_ + "</b>");
            h_.emitTypeWithParens(shownType, false);
            h_.writeText("</A></nobr>&nbsp;constructor<br>");
        } else if (ctor.changeType_ == 2) {
            String commentID = className + ".ctor_changed(" + type + ")";
            h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + ctor.name_);
            h_.emitTypeWithParens(shownType, false);
            h_.writeText("</A></nobr>&nbsp;constructor<br>");
        }
        return res;
    }
    public void emitMethodsIndex(APIDiff apiDiff, int indexType) {
        methNames = new ArrayList(); 
        boolean hasRemovals = false;
        boolean hasAdditions = false;
        boolean hasChanges = false;
        Iterator iter = apiDiff.packagesChanged.iterator();
        while (iter.hasNext()) {
            PackageDiff pkgDiff = (PackageDiff)(iter.next());
            String pkgName = pkgDiff.name_;
            Iterator iterClass = pkgDiff.classesChanged.iterator();
            while (iterClass.hasNext()) {
                ClassDiff classDiff = (ClassDiff)(iterClass.next());
                if (classDiff.methodsRemoved.size() != 0)
                    hasRemovals = true;
                if (classDiff.methodsAdded.size() != 0)
                    hasAdditions = true;
                if (classDiff.methodsChanged.size() != 0)
                    hasChanges = true;
                recordDiffs(hasRemovals, hasAdditions, hasChanges);
                String className = classDiff.name_;
                Iterator iterMeth = classDiff.methodsRemoved.iterator();
                while ((indexType == 3 || indexType == 0) && iterMeth.hasNext()) {
                    MethodAPI meth = (MethodAPI)(iterMeth.next());
                    methNames.add(new Index(meth.name_, 0, pkgName, className, meth.getSignature()));
                }
                iterMeth = classDiff.methodsAdded.iterator();
                while ((indexType == 3 || indexType == 1) && iterMeth.hasNext()) {
                    MethodAPI meth = (MethodAPI)(iterMeth.next());
                    Index idx = new Index(meth.name_, 1, pkgName, className, meth.getSignature());
                    idx.doc_ = meth.doc_; 
                    methNames.add(idx);
                }
                iterMeth = classDiff.methodsChanged.iterator();
                while ((indexType == 3 || indexType == 2) && iterMeth.hasNext()) {
                    MemberDiff meth = (MemberDiff)(iterMeth.next());
                    methNames.add(new Index(meth.name_, 2, pkgName, className, meth.newSignature_));
                }
            }
        }
        Collections.sort(methNames);
        emitIndexHeader("Methods", indexType, hasRemovals, hasAdditions, hasChanges);
        emitIndexEntries(methNames.iterator());
        if (indexType == 1)
            emitMissingSinces(methNames.iterator());
    }
    public char emitMethodIndexEntry(Index meth, char oldsw, 
                                     int multipleMarker) {
        char res = oldsw;
        String className = meth.pkgName_ + "." + meth.className_;
        String memberRef = meth.pkgName_ + "." + meth.className_;
        String type = meth.type_;
        if (type.compareTo("void") == 0)
            type = "";
        String shownType = HTMLReportGenerator.simpleName(type);
        char sw = meth.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            res = sw;
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
            if (sw == '_')
                h_.writeText("<br><b>underscore</b>&nbsp;");
            else
                h_.writeText("<br><font size=\"+2\">" + Character.toUpperCase(sw) + "</font>&nbsp;");
            generateLetterIndex(methNames, sw, false);
        }
        if (multipleMarker == 1) {
            h_.writeText("<i>" + meth.name_ + "</i><br>");
        }
        if (multipleMarker != 0)
            h_.indent(INDENT_SIZE);
        if (meth.changeType_ == 0) {
            String commentID = className + "." + meth.name_ + "_removed(" + type + ")";                    
            if (multipleMarker == 0) {
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + meth.name_ + "</strike>");
                h_.emitTypeWithParens(shownType, false);
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">type&nbsp;<strike>");
                h_.emitTypeWithParens(shownType, false);
                h_.writeText("</strike>&nbsp;in&nbsp;" + className);
            }
            h_.writeText("</A></nobr><br>");
        } else if (meth.changeType_ == 1) {
            String commentID = className + "." + meth.name_ + "_added(" + type + ")";                    
            if (multipleMarker == 0) {
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><b>" + meth.name_ + "</b>");
                h_.emitTypeWithParens(shownType, false);
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">type&nbsp;<b>");
                h_.emitTypeWithParens(shownType, false);
                h_.writeText("</b>&nbsp;in&nbsp;" + className);
            }
            h_.writeText("</A></nobr><br>");
        } else if (meth.changeType_ == 2) {
            String commentID = className + "." + meth.name_ + "_changed(" + type + ")";                    
            if (multipleMarker == 0) {
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + meth.name_);
                h_.emitTypeWithParens(shownType, false);
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">type&nbsp;");
                h_.emitTypeWithParens(shownType, false);
                h_.writeText("&nbsp;in&nbsp;" + className);
            }
            h_.writeText("</A></nobr><br>");
        }
        return res;
    }
    public void emitFieldsIndex(APIDiff apiDiff, int indexType) {
        fieldNames = new ArrayList(); 
        boolean hasRemovals = false;
        boolean hasAdditions = false;
        boolean hasChanges = false;
        Iterator iter = apiDiff.packagesChanged.iterator();
        while (iter.hasNext()) {
            PackageDiff pkgDiff = (PackageDiff)(iter.next());
            String pkgName = pkgDiff.name_;
            Iterator iterClass = pkgDiff.classesChanged.iterator();
            while (iterClass.hasNext()) {
                ClassDiff classDiff = (ClassDiff)(iterClass.next());
                if (classDiff.fieldsRemoved.size() != 0)
                    hasRemovals = true;
                if (classDiff.fieldsAdded.size() != 0)
                    hasAdditions = true;
                if (classDiff.fieldsChanged.size() != 0)
                    hasChanges = true;
                recordDiffs(hasRemovals, hasAdditions, hasChanges);
                String className = classDiff.name_;
                Iterator iterField = classDiff.fieldsRemoved.iterator();
                while ((indexType == 3 || indexType == 0) && iterField.hasNext()) {
                    FieldAPI fld = (FieldAPI)(iterField.next());
                    fieldNames.add(new Index(fld.name_, 0, pkgName, className, fld.type_, true));
                }
                iterField = classDiff.fieldsAdded.iterator();
                while ((indexType == 3 || indexType == 1) && iterField.hasNext()) {
                    FieldAPI fld = (FieldAPI)(iterField.next());
                    Index idx = new Index(fld.name_, 1, pkgName, className, fld.type_, true);
                    idx.doc_ = fld.doc_; 
                    fieldNames.add(idx);
                }
                iterField = classDiff.fieldsChanged.iterator();
                while ((indexType == 3 || indexType == 2) && iterField.hasNext()) {
                    MemberDiff fld = (MemberDiff)(iterField.next());
                    fieldNames.add(new Index(fld.name_, 2, pkgName, className, fld.newType_, true));
                }
            }
        }
        Collections.sort(fieldNames);
        emitIndexHeader("Fields", indexType, hasRemovals, hasAdditions, hasChanges);
        emitIndexEntries(fieldNames.iterator());
        if (indexType == 1)
            emitMissingSinces(fieldNames.iterator());
    }
    public char emitFieldIndexEntry(Index fld, char oldsw, 
                                    int multipleMarker) {
        char res = oldsw;
        String className = fld.pkgName_ + "." + fld.className_;
        String memberRef = fld.pkgName_ + "." + fld.className_;
        String type = fld.type_;
        if (type.compareTo("void") == 0)
            type = "";
        String shownType = HTMLReportGenerator.simpleName(type);
        char sw = fld.name_.charAt(0);
        if (Character.toUpperCase(sw) != Character.toUpperCase(oldsw)) {
            res = sw;
            h_.writeText("<A NAME=\"" + Character.toUpperCase(res) + "\"></A>");
            if (sw == '_')
                h_.writeText("<br><b>underscore</b>&nbsp;");
            else
                h_.writeText("<br><font size=\"+2\">" + Character.toUpperCase(sw) + "</font>&nbsp;");
            generateLetterIndex(fieldNames, sw, false);
        }
        if (multipleMarker == 1) {
            h_.writeText("<i>" + fld.name_ + "</i><br>");
        }
        if (multipleMarker != 0) {
            h_.writeText("&nbsp;in&nbsp;");
        }
        if (fld.changeType_ == 0) {
            String commentID = className + "." + fld.name_;                    
            if (multipleMarker == 0) {            
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + fld.name_ + "</strike></A>");
                h_.writeText("</nobr><br>");
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\"><strike>" + className + "</strike></A>");
                h_.writeText("</nobr><br>");
            }
        } else if (fld.changeType_ == 1) {
            String commentID = className + "." + fld.name_;                    
            if (multipleMarker == 0) {            
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + fld.name_ + "</A>");
                h_.writeText("</nobr><br>");
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + className + "</A>");
                h_.writeText("</nobr><br>");
            }
        } else if (fld.changeType_ == 2) {
            String commentID = className + "." + fld.name_;                    
            if (multipleMarker == 0) {            
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + fld.name_ + "</A>");
                h_.writeText("</nobr><br>");
            } else {
                h_.writeText("<nobr><A HREF=\"" + memberRef + h_.reportFileExt + "#" + commentID + "\" class=\"hiddenlink\" target=\"rightframe\">" + className + "</A>");
                h_.writeText("</nobr><br>");
            }
        }
        return res;
    }
    public void emitAllDiffsIndex(APIDiff apiDiff, int indexType) {
        allNames = new ArrayList(); 
        allNames.addAll(packageNames);
        allNames.addAll(classNames);
        allNames.addAll(ctorNames);
        allNames.addAll(methNames);
        allNames.addAll(fieldNames);
        Collections.sort(allNames);
        emitIndexHeader("All Differences", indexType, atLeastOneRemoval, 
                        atLeastOneAddition, atLeastOneChange);
        isAllNames = true; 
        Iterator iter = allNames.iterator();
        char oldsw = '\0';
        int multipleMarker = 0;
        Index currIndex = null; 
        while (iter.hasNext()) {
            Index nextIndex = (Index)(iter.next()); 
            if (currIndex == null) {
                currIndex = nextIndex; 
            } else {
                if (nextIndex.name_.compareTo(currIndex.name_) == 0) {
                    if (multipleMarker == 0)
                        multipleMarker = 1; 
                    else if (multipleMarker == 1)
                        multipleMarker = 2; 
                    oldsw = emitIndexEntryForAny(currIndex, oldsw, multipleMarker);
                } else {
                    if (multipleMarker == 1)
                        multipleMarker = 2; 
                    oldsw = emitIndexEntryForAny(currIndex, oldsw, multipleMarker);
                    multipleMarker = 0; 
                }
                currIndex = nextIndex;
            }
        }
        if (multipleMarker == 1)
            multipleMarker = 2; 
        if (currIndex != null)
            oldsw = emitIndexEntryForAny(currIndex, oldsw, multipleMarker);
        isAllNames = false; 
    }
    public char emitIndexEntryForAny(Index currIndex, char oldsw, 
                                     int multipleMarker) {
        if (currIndex.ename_.compareTo("package") == 0) {
            h_.writeText("<!-- Package " + currIndex.name_ + " -->");
            return emitPackageIndexEntry(currIndex, oldsw);
        } else if (currIndex.ename_.compareTo("class") == 0) {
            h_.writeText("<!-- Class " + currIndex.name_ + " -->");
            return emitClassIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (currIndex.ename_.compareTo("constructor") == 0) {
            h_.writeText("<!-- Constructor " + currIndex.name_ + " -->");
            return emitCtorIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (currIndex.ename_.compareTo("method") == 0) {
            h_.writeText("<!-- Method " + currIndex.name_ + " -->");
            return emitMethodIndexEntry(currIndex, oldsw, multipleMarker);
        } else if (currIndex.ename_.compareTo("field") == 0) {
            h_.writeText("<!-- Field " + currIndex.name_ + " -->");
            return emitFieldIndexEntry(currIndex, oldsw, multipleMarker);
        }
        return '\0';
    }
    private List allNames = null; 
    private List packageNames = null; 
    private List classNames = null; 
    private List ctorNames = null; 
    private List methNames = null; 
    private List fieldNames = null; 
    private boolean isAllNames = false;
    private void recordDiffs(boolean hasRemovals, boolean hasAdditions, 
                        boolean hasChanges) {
        if (hasRemovals)
            atLeastOneRemoval = true;
        if (hasAdditions)
            atLeastOneAddition = true;
        if (hasChanges)
            atLeastOneChange = true;
    }
    private boolean atLeastOneRemoval = false;
    private boolean atLeastOneAddition = false;
    private boolean atLeastOneChange = false;
    private final int INDENT_SIZE = 2;
}
class Index implements Comparable {
    public String ename_ = null;
    public String name_ = null;
    public int changeType_;
    public String pkgName_ = null;
    public boolean isInterface_= false;
    public String doc_ = null;
    public String type_ = null;
    public String className_ = null;
    public Index(String name, int changeType) {
        ename_ = "package";
        name_ = name;
        changeType_ = changeType;
    }
    public Index(String name, int changeType, String pkgName, boolean isInterface) {
        ename_ = "class";
        name_ = name;
        changeType_ = changeType;
        pkgName_ = pkgName;
        isInterface_ = isInterface;
    }
    public Index(String name, int changeType, String pkgName, String type) {
        ename_ = "constructor";
        name_ = name;
        changeType_ = changeType;
        pkgName_ = pkgName;
        type_  = type;
    }
    public Index(String name, int changeType, String pkgName, 
                 String className, String type) {
        ename_ = "method";
        name_ = name;
        changeType_ = changeType;
        pkgName_ = pkgName;
        className_ = className;
        type_  = type;
    }
    public Index(String name, int changeType, String pkgName, 
                 String className, String type, boolean fld) {
        ename_ = "field";
        name_ = name;
        changeType_ = changeType;
        pkgName_ = pkgName;
        className_ = className;
        type_  = type;
    }
    public int compareTo(Object o) {
        return name_.compareToIgnoreCase(((Index)o).name_);
    }  
}
