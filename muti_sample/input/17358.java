public class ClassUseWriter extends SubWriterHolderWriter {
    final ClassDoc classdoc;
    Set<PackageDoc> pkgToPackageAnnotations = null;
    final Map<String,List<ProgramElementDoc>> pkgToClassTypeParameter;
    final Map<String,List<ProgramElementDoc>> pkgToClassAnnotations;
    final Map<String,List<ProgramElementDoc>> pkgToMethodTypeParameter;
    final Map<String,List<ProgramElementDoc>> pkgToMethodArgTypeParameter;
    final Map<String,List<ProgramElementDoc>> pkgToMethodReturnTypeParameter;
    final Map<String,List<ProgramElementDoc>> pkgToMethodAnnotations;
    final Map<String,List<ProgramElementDoc>> pkgToMethodParameterAnnotations;
    final Map<String,List<ProgramElementDoc>> pkgToFieldTypeParameter;
    final Map<String,List<ProgramElementDoc>> pkgToFieldAnnotations;
    final Map<String,List<ProgramElementDoc>> pkgToSubclass;
    final Map<String,List<ProgramElementDoc>> pkgToSubinterface;
    final Map<String,List<ProgramElementDoc>> pkgToImplementingClass;
    final Map<String,List<ProgramElementDoc>> pkgToField;
    final Map<String,List<ProgramElementDoc>> pkgToMethodReturn;
    final Map<String,List<ProgramElementDoc>> pkgToMethodArgs;
    final Map<String,List<ProgramElementDoc>> pkgToMethodThrows;
    final Map<String,List<ProgramElementDoc>> pkgToConstructorAnnotations;
    final Map<String,List<ProgramElementDoc>> pkgToConstructorParameterAnnotations;
    final Map<String,List<ProgramElementDoc>> pkgToConstructorArgs;
    final Map<String,List<ProgramElementDoc>> pkgToConstructorArgTypeParameter;
    final Map<String,List<ProgramElementDoc>> pkgToConstructorThrows;
    final SortedSet<PackageDoc> pkgSet;
    final MethodWriterImpl methodSubWriter;
    final ConstructorWriterImpl constrSubWriter;
    final FieldWriterImpl fieldSubWriter;
    final NestedClassWriterImpl classSubWriter;
    final String classUseTableSummary;
    final String subclassUseTableSummary;
    final String subinterfaceUseTableSummary;
    final String fieldUseTableSummary;
    final String methodUseTableSummary;
    final String constructorUseTableSummary;
    public ClassUseWriter(ConfigurationImpl configuration,
                          ClassUseMapper mapper, String path,
                          String filename, String relpath,
                          ClassDoc classdoc) throws IOException {
        super(configuration, path, filename, relpath);
        this.classdoc = classdoc;
        if (mapper.classToPackageAnnotations.containsKey(classdoc.qualifiedName()))
                pkgToPackageAnnotations = new HashSet<PackageDoc>(mapper.classToPackageAnnotations.get(classdoc.qualifiedName()));
        configuration.currentcd = classdoc;
        this.pkgSet = new TreeSet<PackageDoc>();
        this.pkgToClassTypeParameter = pkgDivide(mapper.classToClassTypeParam);
        this.pkgToClassAnnotations = pkgDivide(mapper.classToClassAnnotations);
        this.pkgToMethodTypeParameter = pkgDivide(mapper.classToExecMemberDocTypeParam);
        this.pkgToMethodArgTypeParameter = pkgDivide(mapper.classToExecMemberDocArgTypeParam);
        this.pkgToFieldTypeParameter = pkgDivide(mapper.classToFieldDocTypeParam);
        this.pkgToFieldAnnotations = pkgDivide(mapper.annotationToFieldDoc);
        this.pkgToMethodReturnTypeParameter = pkgDivide(mapper.classToExecMemberDocReturnTypeParam);
        this.pkgToMethodAnnotations = pkgDivide(mapper.classToExecMemberDocAnnotations);
        this.pkgToMethodParameterAnnotations = pkgDivide(mapper.classToExecMemberDocParamAnnotation);
        this.pkgToSubclass = pkgDivide(mapper.classToSubclass);
        this.pkgToSubinterface = pkgDivide(mapper.classToSubinterface);
        this.pkgToImplementingClass = pkgDivide(mapper.classToImplementingClass);
        this.pkgToField = pkgDivide(mapper.classToField);
        this.pkgToMethodReturn = pkgDivide(mapper.classToMethodReturn);
        this.pkgToMethodArgs = pkgDivide(mapper.classToMethodArgs);
        this.pkgToMethodThrows = pkgDivide(mapper.classToMethodThrows);
        this.pkgToConstructorAnnotations = pkgDivide(mapper.classToConstructorAnnotations);
        this.pkgToConstructorParameterAnnotations = pkgDivide(mapper.classToConstructorParamAnnotation);
        this.pkgToConstructorArgs = pkgDivide(mapper.classToConstructorArgs);
        this.pkgToConstructorArgTypeParameter = pkgDivide(mapper.classToConstructorDocArgTypeParam);
        this.pkgToConstructorThrows = pkgDivide(mapper.classToConstructorThrows);
        if (pkgSet.size() > 0 &&
            mapper.classToPackage.containsKey(classdoc.qualifiedName()) &&
            !pkgSet.equals(mapper.classToPackage.get(classdoc.qualifiedName()))) {
            configuration.root.printWarning("Internal error: package sets don't match: " + pkgSet + " with: " +
                                   mapper.classToPackage.get(classdoc.qualifiedName()));
        }
        methodSubWriter = new MethodWriterImpl(this);
        constrSubWriter = new ConstructorWriterImpl(this);
        fieldSubWriter = new FieldWriterImpl(this);
        classSubWriter = new NestedClassWriterImpl(this);
        classUseTableSummary = configuration.getText("doclet.Use_Table_Summary",
                configuration.getText("doclet.classes"));
        subclassUseTableSummary = configuration.getText("doclet.Use_Table_Summary",
                configuration.getText("doclet.subclasses"));
        subinterfaceUseTableSummary = configuration.getText("doclet.Use_Table_Summary",
                configuration.getText("doclet.subinterfaces"));
        fieldUseTableSummary = configuration.getText("doclet.Use_Table_Summary",
                configuration.getText("doclet.fields"));
        methodUseTableSummary = configuration.getText("doclet.Use_Table_Summary",
                configuration.getText("doclet.methods"));
        constructorUseTableSummary = configuration.getText("doclet.Use_Table_Summary",
                configuration.getText("doclet.constructors"));
    }
    public static void generate(ConfigurationImpl configuration,
                                ClassTree classtree)  {
        ClassUseMapper mapper = new ClassUseMapper(configuration.root, classtree);
        ClassDoc[] classes = configuration.root.classes();
        for (int i = 0; i < classes.length; i++) {
            if (!(configuration.nodeprecated &&
                    Util.isDeprecated(classes[i].containingPackage())))
                ClassUseWriter.generate(configuration, mapper, classes[i]);
        }
        PackageDoc[] pkgs = configuration.packages;
        for (int i = 0; i < pkgs.length; i++) {
            if (!(configuration.nodeprecated && Util.isDeprecated(pkgs[i])))
                PackageUseWriter.generate(configuration, mapper, pkgs[i]);
        }
    }
    private Map<String,List<ProgramElementDoc>> pkgDivide(Map<String,? extends List<? extends ProgramElementDoc>> classMap) {
        Map<String,List<ProgramElementDoc>> map = new HashMap<String,List<ProgramElementDoc>>();
        List<? extends ProgramElementDoc> list= classMap.get(classdoc.qualifiedName());
        if (list != null) {
            Collections.sort(list);
            Iterator<? extends ProgramElementDoc> it = list.iterator();
            while (it.hasNext()) {
                ProgramElementDoc doc = it.next();
                PackageDoc pkg = doc.containingPackage();
                pkgSet.add(pkg);
                List<ProgramElementDoc> inPkg = map.get(pkg.name());
                if (inPkg == null) {
                    inPkg = new ArrayList<ProgramElementDoc>();
                    map.put(pkg.name(), inPkg);
                }
                inPkg.add(doc);
            }
        }
        return map;
    }
    public static void generate(ConfigurationImpl configuration,
                                ClassUseMapper mapper, ClassDoc classdoc) {
        ClassUseWriter clsgen;
        String path = DirectoryManager.getDirectoryPath(classdoc.
                                                            containingPackage());
        if (path.length() > 0) {
            path += File.separator;
        }
        path += "class-use";
        String filename = classdoc.name() + ".html";
        String pkgname = classdoc.containingPackage().name();
        pkgname += (pkgname.length() > 0)? ".class-use": "class-use";
        String relpath = DirectoryManager.getRelativePath(pkgname);
        try {
            clsgen = new ClassUseWriter(configuration,
                                        mapper, path, filename,
                                        relpath, classdoc);
            clsgen.generateClassUseFile();
            clsgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.
                error("doclet.exception_encountered",
                      exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void generateClassUseFile() throws IOException {
        Content body = getClassUseHeader();
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.addStyle(HtmlStyle.classUseContainer);
        if (pkgSet.size() > 0) {
            addClassUse(div);
        } else {
            div.addContent(getResource("doclet.ClassUse_No.usage.of.0",
                    classdoc.qualifiedName()));
        }
        body.addContent(div);
        addNavLinks(false, body);
        addBottom(body);
        printHtmlDocument(null, true, body);
    }
    protected void addClassUse(Content contentTree) throws IOException {
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        if (configuration.packages.length > 1) {
            addPackageList(ul);
            addPackageAnnotationList(ul);
        }
        addClassList(ul);
        contentTree.addContent(ul);
    }
    protected void addPackageList(Content contentTree) throws IOException {
        Content table = HtmlTree.TABLE(0, 3, 0, useTableSummary,
                getTableCaption(configuration().getText(
                "doclet.ClassUse_Packages.that.use.0",
                getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS_USE_HEADER, classdoc,
                false)))));
        table.addContent(getSummaryTableHeader(packageTableHeader, "col"));
        Content tbody = new HtmlTree(HtmlTag.TBODY);
        Iterator<PackageDoc> it = pkgSet.iterator();
        for (int i = 0; it.hasNext(); i++) {
            PackageDoc pkg = it.next();
            HtmlTree tr = new HtmlTree(HtmlTag.TR);
            if (i % 2 == 0) {
                tr.addStyle(HtmlStyle.altColor);
            } else {
                tr.addStyle(HtmlStyle.rowColor);
            }
            addPackageUse(pkg, tr);
            tbody.addContent(tr);
        }
        table.addContent(tbody);
        Content li = HtmlTree.LI(HtmlStyle.blockList, table);
        contentTree.addContent(li);
    }
    protected void addPackageAnnotationList(Content contentTree) throws IOException {
        if ((!classdoc.isAnnotationType()) ||
                pkgToPackageAnnotations == null ||
                pkgToPackageAnnotations.size() == 0) {
            return;
        }
        Content table = HtmlTree.TABLE(0, 3, 0, useTableSummary,
                getTableCaption(configuration().getText(
                "doclet.ClassUse_PackageAnnotation",
                getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS_USE_HEADER, classdoc,
                false)))));
        table.addContent(getSummaryTableHeader(packageTableHeader, "col"));
        Content tbody = new HtmlTree(HtmlTag.TBODY);
        Iterator<PackageDoc> it = pkgToPackageAnnotations.iterator();
        for (int i = 0; it.hasNext(); i++) {
            PackageDoc pkg = it.next();
            HtmlTree tr = new HtmlTree(HtmlTag.TR);
            if (i % 2 == 0) {
                tr.addStyle(HtmlStyle.altColor);
            } else {
                tr.addStyle(HtmlStyle.rowColor);
            }
            Content tdFirst = HtmlTree.TD(HtmlStyle.colFirst,
                    getPackageLink(pkg, new StringContent(pkg.name())));
            tr.addContent(tdFirst);
            HtmlTree tdLast = new HtmlTree(HtmlTag.TD);
            tdLast.addStyle(HtmlStyle.colLast);
            addSummaryComment(pkg, tdLast);
            tr.addContent(tdLast);
            tbody.addContent(tr);
        }
        table.addContent(tbody);
        Content li = HtmlTree.LI(HtmlStyle.blockList, table);
        contentTree.addContent(li);
    }
    protected void addClassList(Content contentTree) throws IOException {
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        for (Iterator<PackageDoc> it = pkgSet.iterator(); it.hasNext();) {
            PackageDoc pkg = it.next();
            Content li = HtmlTree.LI(HtmlStyle.blockList, getMarkerAnchor(pkg.name()));
            Content link = new RawHtml(
                    configuration.getText("doclet.ClassUse_Uses.of.0.in.1",
                    getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS_USE_HEADER,
                    classdoc, false)),
                    getPackageLinkString(pkg, Util.getPackageName(pkg), false)));
            Content heading = HtmlTree.HEADING(HtmlConstants.SUMMARY_HEADING, link);
            li.addContent(heading);
            addClassUse(pkg, li);
            ul.addContent(li);
        }
        Content li = HtmlTree.LI(HtmlStyle.blockList, ul);
        contentTree.addContent(li);
    }
    protected void addPackageUse(PackageDoc pkg, Content contentTree) throws IOException {
        Content tdFirst = HtmlTree.TD(HtmlStyle.colFirst,
                getHyperLink("", pkg.name(), new StringContent(Util.getPackageName(pkg))));
        contentTree.addContent(tdFirst);
        HtmlTree tdLast = new HtmlTree(HtmlTag.TD);
        tdLast.addStyle(HtmlStyle.colLast);
        addSummaryComment(pkg, tdLast);
        contentTree.addContent(tdLast);
    }
    protected void addClassUse(PackageDoc pkg, Content contentTree) throws IOException {
        String classLink = getLink(new LinkInfoImpl(
            LinkInfoImpl.CONTEXT_CLASS_USE_HEADER, classdoc, false));
        String pkgLink = getPackageLinkString(pkg, Util.getPackageName(pkg), false);
        classSubWriter.addUseInfo(pkgToClassAnnotations.get(pkg.name()),
                configuration.getText("doclet.ClassUse_Annotation", classLink,
                pkgLink), classUseTableSummary, contentTree);
        classSubWriter.addUseInfo(pkgToClassTypeParameter.get(pkg.name()),
                configuration.getText("doclet.ClassUse_TypeParameter", classLink,
                pkgLink), classUseTableSummary, contentTree);
        classSubWriter.addUseInfo(pkgToSubclass.get(pkg.name()),
                configuration.getText("doclet.ClassUse_Subclass", classLink,
                pkgLink), subclassUseTableSummary, contentTree);
        classSubWriter.addUseInfo(pkgToSubinterface.get(pkg.name()),
                configuration.getText("doclet.ClassUse_Subinterface", classLink,
                pkgLink), subinterfaceUseTableSummary, contentTree);
        classSubWriter.addUseInfo(pkgToImplementingClass.get(pkg.name()),
                configuration.getText("doclet.ClassUse_ImplementingClass", classLink,
                pkgLink), classUseTableSummary, contentTree);
        fieldSubWriter.addUseInfo(pkgToField.get(pkg.name()),
                configuration.getText("doclet.ClassUse_Field", classLink,
                pkgLink), fieldUseTableSummary, contentTree);
        fieldSubWriter.addUseInfo(pkgToFieldAnnotations.get(pkg.name()),
                configuration.getText("doclet.ClassUse_FieldAnnotations", classLink,
                pkgLink), fieldUseTableSummary, contentTree);
        fieldSubWriter.addUseInfo(pkgToFieldTypeParameter.get(pkg.name()),
                configuration.getText("doclet.ClassUse_FieldTypeParameter", classLink,
                pkgLink), fieldUseTableSummary, contentTree);
        methodSubWriter.addUseInfo(pkgToMethodAnnotations.get(pkg.name()),
                configuration.getText("doclet.ClassUse_MethodAnnotations", classLink,
                pkgLink), methodUseTableSummary, contentTree);
        methodSubWriter.addUseInfo(pkgToMethodParameterAnnotations.get(pkg.name()),
                configuration.getText("doclet.ClassUse_MethodParameterAnnotations", classLink,
                pkgLink), methodUseTableSummary, contentTree);
        methodSubWriter.addUseInfo(pkgToMethodTypeParameter.get(pkg.name()),
                configuration.getText("doclet.ClassUse_MethodTypeParameter", classLink,
                pkgLink), methodUseTableSummary, contentTree);
        methodSubWriter.addUseInfo(pkgToMethodReturn.get(pkg.name()),
                configuration.getText("doclet.ClassUse_MethodReturn", classLink,
                pkgLink), methodUseTableSummary, contentTree);
        methodSubWriter.addUseInfo(pkgToMethodReturnTypeParameter.get(pkg.name()),
                configuration.getText("doclet.ClassUse_MethodReturnTypeParameter", classLink,
                pkgLink), methodUseTableSummary, contentTree);
        methodSubWriter.addUseInfo(pkgToMethodArgs.get(pkg.name()),
                configuration.getText("doclet.ClassUse_MethodArgs", classLink,
                pkgLink), methodUseTableSummary, contentTree);
        methodSubWriter.addUseInfo(pkgToMethodArgTypeParameter.get(pkg.name()),
                configuration.getText("doclet.ClassUse_MethodArgsTypeParameters", classLink,
                pkgLink), methodUseTableSummary, contentTree);
        methodSubWriter.addUseInfo(pkgToMethodThrows.get(pkg.name()),
                configuration.getText("doclet.ClassUse_MethodThrows", classLink,
                pkgLink), methodUseTableSummary, contentTree);
        constrSubWriter.addUseInfo(pkgToConstructorAnnotations.get(pkg.name()),
                configuration.getText("doclet.ClassUse_ConstructorAnnotations", classLink,
                pkgLink), constructorUseTableSummary, contentTree);
        constrSubWriter.addUseInfo(pkgToConstructorParameterAnnotations.get(pkg.name()),
                configuration.getText("doclet.ClassUse_ConstructorParameterAnnotations", classLink,
                pkgLink), constructorUseTableSummary, contentTree);
        constrSubWriter.addUseInfo(pkgToConstructorArgs.get(pkg.name()),
                configuration.getText("doclet.ClassUse_ConstructorArgs", classLink,
                pkgLink), constructorUseTableSummary, contentTree);
        constrSubWriter.addUseInfo(pkgToConstructorArgTypeParameter.get(pkg.name()),
                configuration.getText("doclet.ClassUse_ConstructorArgsTypeParameters", classLink,
                pkgLink), constructorUseTableSummary, contentTree);
        constrSubWriter.addUseInfo(pkgToConstructorThrows.get(pkg.name()),
                configuration.getText("doclet.ClassUse_ConstructorThrows", classLink,
                pkgLink), constructorUseTableSummary, contentTree);
    }
    protected Content getClassUseHeader() {
        String cltype = configuration.getText(classdoc.isInterface()?
            "doclet.Interface":"doclet.Class");
        String clname = classdoc.qualifiedName();
        String title = configuration.getText("doclet.Window_ClassUse_Header",
                cltype, clname);
        Content bodyTree = getBody(true, getWindowTitle(title));
        addTop(bodyTree);
        addNavLinks(true, bodyTree);
        Content headContent = getResource("doclet.ClassUse_Title", cltype, clname);
        Content heading = HtmlTree.HEADING(HtmlConstants.CLASS_PAGE_HEADING,
                true, HtmlStyle.title, headContent);
        Content div = HtmlTree.DIV(HtmlStyle.header, heading);
        bodyTree.addContent(div);
        return bodyTree;
    }
    protected Content getNavLinkPackage() {
        Content linkContent = getHyperLink("../package-summary.html", "",
                packageLabel);
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
    protected Content getNavLinkClass() {
        Content linkContent = new RawHtml(getLink(new LinkInfoImpl(
                LinkInfoImpl.CONTEXT_CLASS_USE_HEADER, classdoc, "",
                configuration.getText("doclet.Class"), false)));
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
    protected Content getNavLinkClassUse() {
        Content li = HtmlTree.LI(HtmlStyle.navBarCell1Rev, useLabel);
        return li;
    }
    protected Content getNavLinkTree() {
        Content linkContent = classdoc.containingPackage().isIncluded() ?
            getHyperLink("../package-tree.html", "", treeLabel) :
            getHyperLink(relativePath + "overview-tree.html", "", treeLabel);
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
}
