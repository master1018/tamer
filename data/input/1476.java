public abstract class Configuration {
    protected BuilderFactory builderFactory;
    public TagletManager tagletManager;
    public String builderXMLPath;
    private static final String DEFAULT_BUILDER_XML = "resources/doclet.xml";
    public String tagletpath = "";
    public boolean serialwarn = false;
    public int sourcetab = DocletConstants.DEFAULT_TAB_STOP_LENGTH;
    public boolean linksource = false;
    public boolean nosince = false;
    public boolean copydocfilesubdirs = false;
    public String charset = "";
    public boolean keywords = false;
    public final MetaKeywords metakeywords = new MetaKeywords(this);
    protected Set<String> excludedDocFileDirs;
    protected Set<String> excludedQualifiers;
    public RootDoc root;
    public String destDirName = "";
    public String docFileDestDirName = "";
    public String docencoding = null;
    public boolean nocomment = false;
    public String encoding = null;
    public boolean showauthor = false;
    public boolean showversion = false;
    public String sourcepath = "";
    public boolean nodeprecated = false;
    public ClassDocCatalog classDocCatalog;
    public MessageRetriever message = null;
    public boolean notimestamp= false;
    public final Group group = new Group(this);
    public final Extern extern = new Extern(this);
    public abstract String getDocletSpecificBuildDate();
    public abstract void setSpecificDocletOptions(String[][] options);
    public abstract MessageRetriever getDocletSpecificMsg();
    public PackageDoc[] packages;
    public Configuration() {
        message =
            new MessageRetriever(this,
            "com.sun.tools.doclets.internal.toolkit.resources.doclets");
        excludedDocFileDirs = new HashSet<String>();
        excludedQualifiers = new HashSet<String>();
    }
    public BuilderFactory getBuilderFactory() {
        if (builderFactory == null) {
            builderFactory = new BuilderFactory(this);
        }
        return builderFactory;
    }
    public int optionLength(String option) {
        option = option.toLowerCase();
        if (option.equals("-author") ||
            option.equals("-docfilessubdirs") ||
            option.equals("-keywords") ||
            option.equals("-linksource") ||
            option.equals("-nocomment") ||
            option.equals("-nodeprecated") ||
            option.equals("-nosince") ||
            option.equals("-notimestamp") ||
            option.equals("-quiet") ||
            option.equals("-xnodate") ||
            option.equals("-version")) {
            return 1;
        } else if (option.equals("-d") ||
                   option.equals("-docencoding") ||
                   option.equals("-encoding") ||
                   option.equals("-excludedocfilessubdir") ||
                   option.equals("-link") ||
                   option.equals("-sourcetab") ||
                   option.equals("-noqualifier") ||
                   option.equals("-output") ||
                   option.equals("-sourcepath") ||
                   option.equals("-tag") ||
                   option.equals("-taglet") ||
                   option.equals("-tagletpath")) {
            return 2;
        } else if (option.equals("-group") ||
                   option.equals("-linkoffline")) {
            return 3;
        } else {
            return -1;  
        }
    }
    public abstract boolean validOptions(String options[][],
        DocErrorReporter reporter);
    private void initPackageArray() {
        Set<PackageDoc> set = new HashSet<PackageDoc>(Arrays.asList(root.specifiedPackages()));
        ClassDoc[] classes = root.specifiedClasses();
        for (int i = 0; i < classes.length; i++) {
            set.add(classes[i].containingPackage());
        }
        ArrayList<PackageDoc> results = new ArrayList<PackageDoc>(set);
        Collections.sort(results);
        packages = results.toArray(new PackageDoc[] {});
    }
    public void setOptions(String[][] options) {
        LinkedHashSet<String[]> customTagStrs = new LinkedHashSet<String[]>();
        for (int oi = 0; oi < options.length; ++oi) {
            String[] os = options[oi];
            String opt = os[0].toLowerCase();
            if (opt.equals("-d")) {
                destDirName = addTrailingFileSep(os[1]);
                docFileDestDirName = destDirName;
            } else  if (opt.equals("-docfilessubdirs")) {
                copydocfilesubdirs = true;
            } else  if (opt.equals("-docencoding")) {
                docencoding = os[1];
            } else  if (opt.equals("-encoding")) {
                encoding = os[1];
            } else  if (opt.equals("-author")) {
                showauthor = true;
            } else  if (opt.equals("-version")) {
                showversion = true;
            } else  if (opt.equals("-nodeprecated")) {
                nodeprecated = true;
            } else  if (opt.equals("-sourcepath")) {
                sourcepath = os[1];
            } else if (opt.equals("-classpath") &&
                       sourcepath.length() == 0) {
                sourcepath = os[1];
            } else if (opt.equals("-excludedocfilessubdir")) {
                addToSet(excludedDocFileDirs, os[1]);
            } else if (opt.equals("-noqualifier")) {
                addToSet(excludedQualifiers, os[1]);
            } else if (opt.equals("-linksource")) {
                linksource = true;
            } else if (opt.equals("-sourcetab")) {
                linksource = true;
                try {
                    sourcetab = Integer.parseInt(os[1]);
                } catch (NumberFormatException e) {
                    sourcetab = -1;
                }
                if (sourcetab <= 0) {
                    message.warning("doclet.sourcetab_warning");
                    sourcetab = DocletConstants.DEFAULT_TAB_STOP_LENGTH;
                }
            } else  if (opt.equals("-notimestamp")) {
                notimestamp = true;
            } else  if (opt.equals("-nocomment")) {
                nocomment = true;
            } else if (opt.equals("-tag") || opt.equals("-taglet")) {
                customTagStrs.add(os);
            } else if (opt.equals("-tagletpath")) {
                tagletpath = os[1];
            } else  if (opt.equals("-keywords")) {
                keywords = true;
            } else  if (opt.equals("-serialwarn")) {
                serialwarn = true;
            } else if (opt.equals("-group")) {
                group.checkPackageGroups(os[1], os[2]);
            } else if (opt.equals("-link")) {
                String url = os[1];
                extern.url(url, url, root, false);
            } else if (opt.equals("-linkoffline")) {
                String url = os[1];
                String pkglisturl = os[2];
                extern.url(url, pkglisturl, root, true);
            }
        }
        if (sourcepath.length() == 0) {
            sourcepath = System.getProperty("env.class.path") == null ? "" :
                System.getProperty("env.class.path");
        }
        if (docencoding == null) {
            docencoding = encoding;
        }
        classDocCatalog = new ClassDocCatalog(root.specifiedClasses(), this);
        initTagletManager(customTagStrs);
    }
    public void setOptions() {
        initPackageArray();
        setOptions(root.options());
        setSpecificDocletOptions(root.options());
    }
    private void initTagletManager(Set<String[]> customTagStrs) {
        tagletManager = tagletManager == null ?
            new TagletManager(nosince, showversion, showauthor, message) :
            tagletManager;
        String[] args;
        for (Iterator<String[]> it = customTagStrs.iterator(); it.hasNext(); ) {
            args = it.next();
            if (args[0].equals("-taglet")) {
                tagletManager.addCustomTag(args[1], tagletpath);
                continue;
            }
            String[] tokens = Util.tokenize(args[1],
                TagletManager.SIMPLE_TAGLET_OPT_SEPERATOR, 3);
            if (tokens.length == 1) {
                String tagName = args[1];
                if (tagletManager.isKnownCustomTag(tagName)) {
                    tagletManager.addNewSimpleCustomTag(tagName, null, "");
                } else {
                    StringBuffer heading = new StringBuffer(tagName + ":");
                    heading.setCharAt(0, Character.toUpperCase(tagName.charAt(0)));
                    tagletManager.addNewSimpleCustomTag(tagName, heading.toString(), "a");
                }
            } else if (tokens.length == 2) {
                tagletManager.addNewSimpleCustomTag(tokens[0], tokens[1], "");
            } else if (tokens.length >= 3) {
                tagletManager.addNewSimpleCustomTag(tokens[0], tokens[2], tokens[1]);
            } else {
                message.error("doclet.Error_invalid_custom_tag_argument", args[1]);
            }
        }
    }
    private void addToSet(Set<String> s, String str){
        StringTokenizer st = new StringTokenizer(str, ":");
        String current;
        while(st.hasMoreTokens()){
            current = st.nextToken();
            s.add(current);
        }
    }
    String addTrailingFileSep(String path) {
        String fs = System.getProperty("file.separator");
        String dblfs = fs + fs;
        int indexDblfs;
        while ((indexDblfs = path.indexOf(dblfs)) >= 0) {
            path = path.substring(0, indexDblfs) +
                path.substring(indexDblfs + fs.length());
        }
        if (!path.endsWith(fs))
            path += fs;
        return path;
    }
    public boolean generalValidOptions(String options[][],
            DocErrorReporter reporter) {
        boolean docencodingfound = false;
        String encoding = "";
        for (int oi = 0; oi < options.length; oi++) {
            String[] os = options[oi];
            String opt = os[0].toLowerCase();
            if (opt.equals("-d")) {
                String destdirname = addTrailingFileSep(os[1]);
                File destDir = new File(destdirname);
                if (!destDir.exists()) {
                    reporter.printNotice(getText("doclet.dest_dir_create",
                        destdirname));
                    (new File(destdirname)).mkdirs();
                } else if (!destDir.isDirectory()) {
                    reporter.printError(getText(
                        "doclet.destination_directory_not_directory_0",
                        destDir.getPath()));
                    return false;
                } else if (!destDir.canWrite()) {
                    reporter.printError(getText(
                        "doclet.destination_directory_not_writable_0",
                        destDir.getPath()));
                    return false;
                }
            } else if (opt.equals("-docencoding")) {
                docencodingfound = true;
                if (!checkOutputFileEncoding(os[1], reporter)) {
                    return false;
                }
            } else if (opt.equals("-encoding")) {
                encoding = os[1];
            }
        }
        if (!docencodingfound && encoding.length() > 0) {
            if (!checkOutputFileEncoding(encoding, reporter)) {
                return false;
            }
        }
        return true;
    }
    private boolean checkOutputFileEncoding(String docencoding,
            DocErrorReporter reporter) {
        OutputStream ost= new ByteArrayOutputStream();
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(ost, docencoding);
        } catch (UnsupportedEncodingException exc) {
            reporter.printError(getText("doclet.Encoding_not_supported",
                docencoding));
            return false;
        } finally {
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException exc) {
            }
        }
        return true;
    }
    public boolean shouldExcludeDocFileDir(String docfilesubdir){
        if (excludedDocFileDirs.contains(docfilesubdir)) {
            return true;
        } else {
            return false;
        }
    }
    public boolean shouldExcludeQualifier(String qualifier){
        if (excludedQualifiers.contains("all") ||
            excludedQualifiers.contains(qualifier) ||
            excludedQualifiers.contains(qualifier + ".*")) {
            return true;
        } else {
            int index = -1;
            while ((index = qualifier.indexOf(".", index + 1)) != -1) {
                if (excludedQualifiers.contains(qualifier.substring(0, index + 1) + "*")) {
                    return true;
                }
            }
            return false;
        }
    }
    public String getClassName(ClassDoc cd) {
        PackageDoc pd = cd.containingPackage();
        if (pd != null && shouldExcludeQualifier(cd.containingPackage().name())) {
            return cd.name();
        } else {
            return cd.qualifiedName();
        }
    }
    public String getText(String key) {
        try {
            return getDocletSpecificMsg().getText(key);
        } catch (Exception e) {
            return message.getText(key);
        }
    }
    public String getText(String key, String a1) {
        try {
            return getDocletSpecificMsg().getText(key, a1);
        } catch (Exception e) {
            return message.getText(key, a1);
        }
    }
    public String getText(String key, String a1, String a2) {
        try {
            return getDocletSpecificMsg().getText(key, a1, a2);
        } catch (Exception e) {
            return message.getText(key, a1, a2);
        }
    }
    public String getText(String key, String a1, String a2, String a3) {
        try {
            return getDocletSpecificMsg().getText(key, a1, a2, a3);
        } catch (Exception e) {
            return message.getText(key, a1, a2, a3);
        }
    }
    public boolean isGeneratedDoc(ClassDoc cd) {
        if (!nodeprecated) {
            return true;
        }
        return !(Util.isDeprecated(cd) || Util.isDeprecated(cd.containingPackage()));
    }
    public abstract WriterFactory getWriterFactory();
    public InputStream getBuilderXML() throws FileNotFoundException {
        return builderXMLPath == null ?
            Configuration.class.getResourceAsStream(DEFAULT_BUILDER_XML) :
            new FileInputStream(new File(builderXMLPath));
    }
    public abstract Locale getLocale();
    public abstract Comparator<ProgramElementDoc> getMemberComparator();
}
