public class IndexBuilder {
    private Map<Character,List<Doc>> indexmap = new HashMap<Character,List<Doc>>();
    private boolean noDeprecated;
    private boolean classesOnly;
    protected final Object[] elements;
    private class DocComparator implements Comparator<Doc> {
        public int compare(Doc d1, Doc d2) {
            String doc1 = d1.name();
            String doc2 = d2.name();
            int compareResult;
            if ((compareResult = doc1.compareToIgnoreCase(doc2)) != 0) {
                return compareResult;
            } else if (d1 instanceof ProgramElementDoc && d2 instanceof ProgramElementDoc) {
                 doc1 = (((ProgramElementDoc) d1).qualifiedName());
                 doc2 = (((ProgramElementDoc) d2).qualifiedName());
                 return doc1.compareToIgnoreCase(doc2);
            } else {
                return 0;
            }
        }
    }
    public IndexBuilder(Configuration configuration, boolean noDeprecated) {
        this(configuration, noDeprecated, false);
    }
    public IndexBuilder(Configuration configuration, boolean noDeprecated,
                        boolean classesOnly) {
        if (classesOnly) {
            configuration.message.notice("doclet.Building_Index_For_All_Classes");
        } else {
            configuration.message.notice("doclet.Building_Index");
        }
        this.noDeprecated = noDeprecated;
        this.classesOnly = classesOnly;
        buildIndexMap(configuration.root);
        Set<Character> set = indexmap.keySet();
        elements =  set.toArray();
        Arrays.sort(elements);
    }
    protected void sortIndexMap() {
        for (Iterator<List<Doc>> it = indexmap.values().iterator(); it.hasNext(); ) {
            Collections.sort(it.next(), new DocComparator());
        }
    }
    protected void buildIndexMap(RootDoc root)  {
        PackageDoc[] packages = root.specifiedPackages();
        ClassDoc[] classes = root.classes();
        if (!classesOnly) {
            if (packages.length == 0) {
                Set<PackageDoc> set = new HashSet<PackageDoc>();
                PackageDoc pd;
                for (int i = 0; i < classes.length; i++) {
                    pd = classes[i].containingPackage();
                    if (pd != null && pd.name().length() > 0) {
                        set.add(pd);
                    }
                }
                adjustIndexMap(set.toArray(packages));
            } else {
                adjustIndexMap(packages);
            }
        }
        adjustIndexMap(classes);
        if (!classesOnly) {
            for (int i = 0; i < classes.length; i++) {
                if (shouldAddToIndexMap(classes[i])) {
                    putMembersInIndexMap(classes[i]);
                }
            }
        }
        sortIndexMap();
    }
    protected void putMembersInIndexMap(ClassDoc classdoc) {
        adjustIndexMap(classdoc.fields());
        adjustIndexMap(classdoc.methods());
        adjustIndexMap(classdoc.constructors());
    }
    protected void adjustIndexMap(Doc[] elements) {
        for (int i = 0; i < elements.length; i++) {
            if (shouldAddToIndexMap(elements[i])) {
                String name = elements[i].name();
                char ch = (name.length()==0)?
                    '*' :
                    Character.toUpperCase(name.charAt(0));
                Character unicode = new Character(ch);
                List<Doc> list = indexmap.get(unicode);
                if (list == null) {
                    list = new ArrayList<Doc>();
                    indexmap.put(unicode, list);
                }
                list.add(elements[i]);
            }
        }
    }
    protected boolean shouldAddToIndexMap(Doc element) {
        if (element instanceof PackageDoc)
            return !(noDeprecated && Util.isDeprecated(element));
        else
            return !(noDeprecated &&
                    (Util.isDeprecated(element) ||
                    Util.isDeprecated(((ProgramElementDoc)element).containingPackage())));
    }
    public Map<Character,List<Doc>> getIndexMap() {
        return indexmap;
    }
    public List<Doc> getMemberList(Character index) {
        return indexmap.get(index);
    }
    public Object[] elements() {
        return elements;
    }
}
