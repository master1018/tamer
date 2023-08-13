public class TagletManager {
    public static final char SIMPLE_TAGLET_OPT_SEPERATOR = ':';
    public static final String ALT_SIMPLE_TAGLET_OPT_SEPERATOR = "-";
    private LinkedHashMap<String,Taglet> customTags;
    private Taglet[] packageTags;
    private Taglet[] typeTags;
    private Taglet[] fieldTags;
    private Taglet[] constructorTags;
    private Taglet[] methodTags;
    private Taglet[] overviewTags;
    private Taglet[] inlineTags;
    private Taglet[] serializedFormTags;
    private MessageRetriever message;
    private Set<String> standardTags;
    private Set<String> standardTagsLowercase;
    private Set<String> overridenStandardTags;
    private Set<String> potentiallyConflictingTags;
    private Set<String> unseenCustomTags;
    private boolean nosince;
    private boolean showversion;
    private boolean showauthor;
    public TagletManager(boolean nosince, boolean showversion,
                         boolean showauthor, MessageRetriever message){
        overridenStandardTags = new HashSet<String>();
        potentiallyConflictingTags = new HashSet<String>();
        standardTags = new HashSet<String>();
        standardTagsLowercase = new HashSet<String>();
        unseenCustomTags = new HashSet<String>();
        customTags = new LinkedHashMap<String,Taglet>();
        this.nosince = nosince;
        this.showversion = showversion;
        this.showauthor = showauthor;
        this.message = message;
        initStandardTags();
        initStandardTagsLowercase();
    }
    public void addCustomTag(Taglet customTag) {
        if (customTag != null) {
            String name = customTag.getName();
            if (customTags.containsKey(name)) {
                customTags.remove(name);
            }
            customTags.put(name, customTag);
            checkTagName(name);
        }
    }
    public void addCustomTag(String classname, String tagletPath) {
        try {
            Class<?> customTagClass = null;
            String cpString = null;   
            cpString = appendPath(System.getProperty("env.class.path"), cpString);
            cpString = appendPath(System.getProperty("java.class.path"), cpString);
            cpString = appendPath(tagletPath, cpString);
            URLClassLoader appClassLoader = new URLClassLoader(pathToURLs(cpString));
            customTagClass = appClassLoader.loadClass(classname);
            Method meth = customTagClass.getMethod("register",
                                                   new Class<?>[] {java.util.Map.class});
            Object[] list = customTags.values().toArray();
            Taglet lastTag = (list != null && list.length > 0)
                ? (Taglet) list[list.length-1] : null;
            meth.invoke(null, new Object[] {customTags});
            list = customTags.values().toArray();
            Object newLastTag = (list != null&& list.length > 0)
                ? list[list.length-1] : null;
            if (lastTag != newLastTag) {
                message.notice("doclet.Notice_taglet_registered", classname);
                if (newLastTag != null) {
                    checkTaglet(newLastTag);
                }
            }
        } catch (Exception exc) {
            message.error("doclet.Error_taglet_not_registered", exc.getClass().getName(), classname);
        }
    }
    private String appendPath(String path1, String path2) {
        if (path1 == null || path1.length() == 0) {
            return path2 == null ? "." : path2;
        } else if (path2 == null || path2.length() == 0) {
            return path1;
        } else {
            return path1  + File.pathSeparator + path2;
        }
    }
    private static URL[] pathToURLs(String path) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        URL[] urls = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            URL url = fileToURL(new File(st.nextToken()));
            if (url != null) {
                urls[count++] = url;
            }
        }
        if (urls.length != count) {
            URL[] tmp = new URL[count];
            System.arraycopy(urls, 0, tmp, 0, count);
            urls = tmp;
        }
        return urls;
    }
    private static URL fileToURL(File file) {
        String name;
        try {
            name = file.getCanonicalPath();
        } catch (IOException e) {
            name = file.getAbsolutePath();
        }
        name = name.replace(File.separatorChar, '/');
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        if (!file.isFile()) {
            name = name + "/";
        }
        try {
            return new URL("file", "", name);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("file");
        }
    }
    public void addNewSimpleCustomTag(String tagName, String header, String locations) {
        if (tagName == null || locations == null) {
            return;
        }
        Taglet tag = customTags.get(tagName);
        locations = locations.toLowerCase();
        if (tag == null || header != null) {
            customTags.remove(tagName);
            customTags.put(tagName, new SimpleTaglet(tagName, header, locations));
            if (locations != null && locations.indexOf('x') == -1) {
                checkTagName(tagName);
            }
        } else {
            customTags.remove(tagName);
            customTags.put(tagName, tag);
        }
    }
    private void checkTagName(String name) {
        if (standardTags.contains(name)) {
            overridenStandardTags.add(name);
        } else {
            if (name.indexOf('.') == -1) {
                potentiallyConflictingTags.add(name);
            }
            unseenCustomTags.add(name);
        }
    }
    private void checkTaglet(Object taglet) {
        if (taglet instanceof Taglet) {
            checkTagName(((Taglet) taglet).getName());
        } else if (taglet instanceof com.sun.tools.doclets.Taglet) {
            com.sun.tools.doclets.Taglet legacyTaglet = (com.sun.tools.doclets.Taglet) taglet;
            customTags.remove(legacyTaglet.getName());
            customTags.put(legacyTaglet.getName(), new LegacyTaglet(legacyTaglet));
            checkTagName(legacyTaglet.getName());
        } else {
            throw new IllegalArgumentException("Given object is not a taglet.");
        }
    }
    public void seenCustomTag(String name) {
        unseenCustomTags.remove(name);
    }
    public void checkTags(Doc doc, Tag[] tags, boolean areInlineTags) {
        if (tags == null) {
            return;
        }
        Taglet taglet;
        for (int i = 0; i < tags.length; i++) {
            String name = tags[i].name();
            if (name.length() > 0 && name.charAt(0) == '@') {
                name = name.substring(1, name.length());
            }
            if (! (standardTags.contains(name) || customTags.containsKey(name))) {
                if (standardTagsLowercase.contains(name.toLowerCase())) {
                    message.warning(tags[i].position(), "doclet.UnknownTagLowercase", tags[i].name());
                    continue;
                } else {
                    message.warning(tags[i].position(), "doclet.UnknownTag", tags[i].name());
                    continue;
                }
            }
            if ((taglet = customTags.get(name)) != null) {
                if (areInlineTags && ! taglet.isInlineTag()) {
                    printTagMisuseWarn(taglet, tags[i], "inline");
                }
                if ((doc instanceof RootDoc) && ! taglet.inOverview()) {
                    printTagMisuseWarn(taglet, tags[i], "overview");
                } else if ((doc instanceof PackageDoc) && ! taglet.inPackage()) {
                    printTagMisuseWarn(taglet, tags[i], "package");
                } else if ((doc instanceof ClassDoc) && ! taglet.inType()) {
                    printTagMisuseWarn(taglet, tags[i], "class");
                } else if ((doc instanceof ConstructorDoc) && ! taglet.inConstructor()) {
                    printTagMisuseWarn(taglet, tags[i], "constructor");
                } else if ((doc instanceof FieldDoc) && ! taglet.inField()) {
                    printTagMisuseWarn(taglet, tags[i], "field");
                } else if ((doc instanceof MethodDoc) && ! taglet.inMethod()) {
                    printTagMisuseWarn(taglet, tags[i], "method");
                }
            }
        }
    }
    private void printTagMisuseWarn(Taglet taglet, Tag tag, String holderType) {
        Set<String> locationsSet = new LinkedHashSet<String>();
        if (taglet.inOverview()) {
            locationsSet.add("overview");
        }
        if (taglet.inPackage()) {
            locationsSet.add("package");
        }
        if (taglet.inType()) {
            locationsSet.add("class/interface");
        }
        if (taglet.inConstructor())  {
            locationsSet.add("constructor");
        }
        if (taglet.inField()) {
            locationsSet.add("field");
        }
        if (taglet.inMethod()) {
            locationsSet.add("method");
        }
        if (taglet.isInlineTag()) {
            locationsSet.add("inline text");
        }
        String[] locations = locationsSet.toArray(new String[]{});
        if (locations == null || locations.length == 0) {
            return;
        }
        StringBuffer combined_locations = new StringBuffer();
        for (int i = 0; i < locations.length; i++) {
            if (i > 0) {
                combined_locations.append(", ");
            }
            combined_locations.append(locations[i]);
        }
        message.warning(tag.position(), "doclet.tag_misuse",
            "@" + taglet.getName(), holderType, combined_locations.toString());
    }
    public Taglet[] getPackageCustomTags() {
        if (packageTags == null) {
            initCustomTagArrays();
        }
        return packageTags;
    }
    public Taglet[] getTypeCustomTags() {
        if (typeTags == null) {
            initCustomTagArrays();
        }
        return typeTags;
    }
    public Taglet[] getInlineCustomTags() {
        if (inlineTags == null) {
            initCustomTagArrays();
        }
        return inlineTags;
    }
    public Taglet[] getFieldCustomTags() {
        if (fieldTags == null) {
            initCustomTagArrays();
        }
        return fieldTags;
    }
    public Taglet[] getSerializedFormTags() {
        if (serializedFormTags == null) {
            initCustomTagArrays();
        }
        return serializedFormTags;
    }
    public Taglet[] getCustomTags(Doc doc) {
        if (doc instanceof ConstructorDoc) {
            return getConstructorCustomTags();
        } else if (doc instanceof MethodDoc) {
            return getMethodCustomTags();
        } else if (doc instanceof FieldDoc) {
            return getFieldCustomTags();
        } else if (doc instanceof ClassDoc) {
            return getTypeCustomTags();
        } else if (doc instanceof PackageDoc) {
            return getPackageCustomTags();
        } else if (doc instanceof RootDoc) {
            return getOverviewCustomTags();
        }
        return null;
    }
    public Taglet[] getConstructorCustomTags() {
        if (constructorTags == null) {
            initCustomTagArrays();
        }
        return constructorTags;
    }
    public Taglet[] getMethodCustomTags() {
        if (methodTags == null) {
            initCustomTagArrays();
        }
        return methodTags;
    }
    public Taglet[] getOverviewCustomTags() {
        if (overviewTags == null) {
            initCustomTagArrays();
        }
        return overviewTags;
    }
    private void initCustomTagArrays() {
        Iterator<Taglet> it = customTags.values().iterator();
        ArrayList<Taglet> pTags = new ArrayList<Taglet>(customTags.size());
        ArrayList<Taglet> tTags = new ArrayList<Taglet>(customTags.size());
        ArrayList<Taglet> fTags = new ArrayList<Taglet>(customTags.size());
        ArrayList<Taglet> cTags = new ArrayList<Taglet>(customTags.size());
        ArrayList<Taglet> mTags = new ArrayList<Taglet>(customTags.size());
        ArrayList<Taglet> iTags = new ArrayList<Taglet>(customTags.size());
        ArrayList<Taglet> oTags = new ArrayList<Taglet>(customTags.size());
        Taglet current;
        while (it.hasNext()) {
            current = it.next();
            if (current.inPackage() && !current.isInlineTag()) {
                pTags.add(current);
            }
            if (current.inType() && !current.isInlineTag()) {
                tTags.add(current);
            }
            if (current.inField() && !current.isInlineTag()) {
                fTags.add(current);
            }
            if (current.inConstructor() && !current.isInlineTag()) {
                cTags.add(current);
            }
            if (current.inMethod() && !current.isInlineTag()) {
                mTags.add(current);
            }
            if (current.isInlineTag()) {
                iTags.add(current);
            }
            if (current.inOverview() && !current.isInlineTag()) {
                oTags.add(current);
            }
        }
        packageTags = pTags.toArray(new Taglet[] {});
        typeTags = tTags.toArray(new Taglet[] {});
        fieldTags = fTags.toArray(new Taglet[] {});
        constructorTags = cTags.toArray(new Taglet[] {});
        methodTags = mTags.toArray(new Taglet[] {});
        overviewTags = oTags.toArray(new Taglet[] {});
        inlineTags = iTags.toArray(new Taglet[] {});
        serializedFormTags = new Taglet[4];
        serializedFormTags[0] = customTags.get("serialData");
        serializedFormTags[1] = customTags.get("throws");
        serializedFormTags[2] = customTags.get("since");
        serializedFormTags[3] = customTags.get("see");
    }
    private void initStandardTags() {
        Taglet temp;
        customTags.put((temp = new ParamTaglet()).getName(), temp);
        customTags.put((temp = new ReturnTaglet()).getName(), temp);
        customTags.put((temp = new ThrowsTaglet()).getName(), temp);
        customTags.put((temp = new SimpleTaglet("exception",
            null, SimpleTaglet.METHOD + SimpleTaglet.CONSTRUCTOR)).getName(), temp);
        if (!nosince) {
            customTags.put((temp = new SimpleTaglet("since", message.getText("doclet.Since"),
               SimpleTaglet.ALL)).getName(), temp);
        }
        if (showversion) {
            customTags.put((temp = new SimpleTaglet("version", message.getText("doclet.Version"),
                SimpleTaglet.PACKAGE + SimpleTaglet.TYPE + SimpleTaglet.OVERVIEW)).getName(), temp);
        }
        if (showauthor) {
            customTags.put((temp = new SimpleTaglet("author", message.getText("doclet.Author"),
                SimpleTaglet.PACKAGE + SimpleTaglet.TYPE + SimpleTaglet.OVERVIEW)).getName(), temp);
        }
        customTags.put((temp = new SimpleTaglet("serialData", message.getText("doclet.SerialData"),
            SimpleTaglet.EXCLUDED)).getName(), temp);
        customTags.put((temp = new SimpleTaglet("factory", message.getText("doclet.Factory"),
            SimpleTaglet.METHOD)).getName(), temp);
        customTags.put((temp = new SeeTaglet()).getName(), temp);
        customTags.put((temp = new DocRootTaglet()).getName(), temp);
        customTags.put((temp = new InheritDocTaglet()).getName(), temp);
        customTags.put((temp = new ValueTaglet()).getName(), temp);
        customTags.put((temp = new LegacyTaglet(new LiteralTaglet())).getName(),
            temp);
        customTags.put((temp = new LegacyTaglet(new CodeTaglet())).getName(),
            temp);
        standardTags.add("param");
        standardTags.add("return");
        standardTags.add("throws");
        standardTags.add("exception");
        standardTags.add("since");
        standardTags.add("version");
        standardTags.add("author");
        standardTags.add("see");
        standardTags.add("deprecated");
        standardTags.add("link");
        standardTags.add("linkplain");
        standardTags.add("inheritDoc");
        standardTags.add("docRoot");
        standardTags.add("value");
        standardTags.add("serial");
        standardTags.add("serialData");
        standardTags.add("serialField");
        standardTags.add("Text");
        standardTags.add("literal");
        standardTags.add("code");
    }
    private void initStandardTagsLowercase() {
        Iterator<String> it = standardTags.iterator();
        while (it.hasNext()) {
            standardTagsLowercase.add(it.next().toLowerCase());
        }
    }
    public boolean isKnownCustomTag(String tagName) {
        return customTags.containsKey(tagName);
    }
    public void printReport() {
        printReportHelper("doclet.Notice_taglet_conflict_warn", potentiallyConflictingTags);
        printReportHelper("doclet.Notice_taglet_overriden", overridenStandardTags);
        printReportHelper("doclet.Notice_taglet_unseen", unseenCustomTags);
    }
    private void printReportHelper(String noticeKey, Set<String> names) {
        if (names.size() > 0) {
            String[] namesArray = names.toArray(new String[] {});
            String result = " ";
            for (int i = 0; i < namesArray.length; i++) {
                result += "@" + namesArray[i];
                if (i + 1 < namesArray.length) {
                    result += ", ";
                }
            }
            message.notice(noticeKey, result);
        }
    }
    public Taglet getTaglet(String name) {
        if (name.indexOf("@") == 0) {
            return customTags.get(name.substring(1));
        } else {
            return customTags.get(name);
        }
    }
}
