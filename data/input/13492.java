public class DeprecatedAPIListBuilder {
    public static final int NUM_TYPES = 12;
    public static final int PACKAGE = 0;
    public static final int INTERFACE = 1;
    public static final int CLASS = 2;
    public static final int ENUM = 3;
    public static final int EXCEPTION = 4;
    public static final int ERROR = 5;
    public static final int ANNOTATION_TYPE = 6;
    public static final int FIELD = 7;
    public static final int METHOD = 8;
    public static final int CONSTRUCTOR = 9;
    public static final int ENUM_CONSTANT = 10;
    public static final int ANNOTATION_TYPE_MEMBER = 11;
    private List<List<Doc>> deprecatedLists;
    public DeprecatedAPIListBuilder(Configuration configuration) {
        deprecatedLists = new ArrayList<List<Doc>>();
        for (int i = 0; i < NUM_TYPES; i++) {
            deprecatedLists.add(i, new ArrayList<Doc>());
        }
        buildDeprecatedAPIInfo(configuration);
    }
    private void buildDeprecatedAPIInfo(Configuration configuration) {
        PackageDoc[] packages = configuration.packages;
        PackageDoc pkg;
        for (int c = 0; c < packages.length; c++) {
            pkg = packages[c];
            if (Util.isDeprecated(pkg)) {
                getList(PACKAGE).add(pkg);
            }
        }
        ClassDoc[] classes = configuration.root.classes();
        for (int i = 0; i < classes.length; i++) {
            ClassDoc cd = classes[i];
            if (Util.isDeprecated(cd)) {
                if (cd.isOrdinaryClass()) {
                    getList(CLASS).add(cd);
                } else if (cd.isInterface()) {
                    getList(INTERFACE).add(cd);
                } else if (cd.isException()) {
                    getList(EXCEPTION).add(cd);
                } else if (cd.isEnum()) {
                    getList(ENUM).add(cd);
                } else if (cd.isError()) {
                    getList(ERROR).add(cd);
                } else if (cd.isAnnotationType()) {
                    getList(ANNOTATION_TYPE).add(cd);
                }
            }
            composeDeprecatedList(getList(FIELD), cd.fields());
            composeDeprecatedList(getList(METHOD), cd.methods());
            composeDeprecatedList(getList(CONSTRUCTOR), cd.constructors());
            if (cd.isEnum()) {
                composeDeprecatedList(getList(ENUM_CONSTANT), cd.enumConstants());
            }
            if (cd.isAnnotationType()) {
                composeDeprecatedList(getList(ANNOTATION_TYPE_MEMBER),
                        ((AnnotationTypeDoc) cd).elements());
            }
        }
        sortDeprecatedLists();
    }
    private void composeDeprecatedList(List<Doc> list, MemberDoc[] members) {
        for (int i = 0; i < members.length; i++) {
            if (Util.isDeprecated(members[i])) {
                list.add(members[i]);
            }
        }
    }
    private void sortDeprecatedLists() {
        for (int i = 0; i < NUM_TYPES; i++) {
            Collections.sort(getList(i));
        }
    }
    public List<Doc> getList(int type) {
        return deprecatedLists.get(type);
    }
    public boolean hasDocumentation(int type) {
        return (deprecatedLists.get(type)).size() > 0;
    }
}
