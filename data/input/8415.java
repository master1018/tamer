public class LinkInfoImpl extends LinkInfo {
    public static final int ALL_CLASSES_FRAME = 1;
    public static final int CONTEXT_CLASS = 2;
    public static final int CONTEXT_MEMBER = 3;
    public static final int CONTEXT_CLASS_USE = 4;
    public static final int CONTEXT_INDEX = 5;
    public static final int CONTEXT_CONSTANT_SUMMARY = 6;
    public static final int CONTEXT_SERIALIZED_FORM = 7;
    public static final int CONTEXT_SERIAL_MEMBER = 8;
    public static final int CONTEXT_PACKAGE = 9;
    public static final int CONTEXT_SEE_TAG = 10;
    public static final int CONTEXT_VALUE_TAG = 11;
    public static final int CONTEXT_TREE = 12;
    public static final int PACKAGE_FRAME = 13;
    public static final int CONTEXT_CLASS_HEADER = 14;
    public static final int CONTEXT_CLASS_SIGNATURE = 15;
    public static final int CONTEXT_RETURN_TYPE = 16;
    public static final int CONTEXT_SUMMARY_RETURN_TYPE = 17;
    public static final int CONTEXT_EXECUTABLE_MEMBER_PARAM = 18;
    public static final int CONTEXT_SUPER_INTERFACES = 19;
    public static final int CONTEXT_IMPLEMENTED_INTERFACES = 20;
    public static final int CONTEXT_IMPLEMENTED_CLASSES = 21;
    public static final int CONTEXT_SUBINTERFACES = 22;
    public static final int CONTEXT_SUBCLASSES = 23;
    public static final int CONTEXT_CLASS_SIGNATURE_PARENT_NAME = 24;
    public static final int CONTEXT_METHOD_DOC_COPY = 26;
    public static final int CONTEXT_METHOD_SPECIFIED_BY = 27;
    public static final int CONTEXT_METHOD_OVERRIDES = 28;
    public static final int CONTEXT_ANNOTATION = 29;
    public static final int CONTEXT_FIELD_DOC_COPY = 30;
    public static final int CONTEXT_CLASS_TREE_PARENT = 31;
    public static final int CONTEXT_MEMBER_TYPE_PARAMS = 32;
    public static final int CONTEXT_CLASS_USE_HEADER = 33;
    public int context;
    public String where = "";
    public String styleName ="";
    public String target = "";
    public LinkInfoImpl (int context, ClassDoc classDoc, String label,
            String target){
        this.classDoc = classDoc;
        this.label = label;
        this.target = target;
        setContext(context);
    }
    public LinkInfoImpl (int context, ClassDoc classDoc, String where, String label,
            boolean isStrong, String styleName){
        this.classDoc = classDoc;
        this.where = where;
        this.label = label;
        this.isStrong = isStrong;
        this.styleName = styleName;
        setContext(context);
    }
    public LinkInfoImpl (int context, ClassDoc classDoc, String where, String label,
            boolean isStrong){
        this.classDoc = classDoc;
        this.where = where;
        this.label = label;
        this.isStrong = isStrong;
        setContext(context);
    }
    public LinkInfoImpl (ClassDoc classDoc, String label){
        this.classDoc = classDoc;
        this.label = label;
        setContext(context);
    }
    public LinkInfoImpl (int context, ExecutableMemberDoc executableMemberDoc,
            boolean isStrong){
        this.executableMemberDoc = executableMemberDoc;
        this.isStrong = isStrong;
        setContext(context);
    }
    public LinkInfoImpl (int context, ClassDoc classDoc,  boolean isStrong){
        this.classDoc = classDoc;
        this.isStrong = isStrong;
        setContext(context);
    }
    public LinkInfoImpl (int context, Type type){
        this.type = type;
        setContext(context);
    }
    public LinkInfoImpl (int context, Type type, boolean isVarArg){
        this.type = type;
        this.isVarArg = isVarArg;
        setContext(context);
    }
    public LinkInfoImpl (int context, Type type, String label,
            boolean isStrong){
        this.type = type;
        this.label = label;
        this.isStrong = isStrong;
        setContext(context);
    }
    public LinkInfoImpl (int context, ClassDoc classDoc, String label,
            boolean isStrong){
        this.classDoc = classDoc;
        this.label = label;
        this.isStrong = isStrong;
        setContext(context);
    }
    public int getContext() {
        return context;
    }
    public void setContext(int c) {
        switch (c) {
            case ALL_CLASSES_FRAME:
            case PACKAGE_FRAME:
            case CONTEXT_IMPLEMENTED_CLASSES:
            case CONTEXT_SUBCLASSES:
            case CONTEXT_METHOD_DOC_COPY:
            case CONTEXT_FIELD_DOC_COPY:
            case CONTEXT_CLASS_USE_HEADER:
                includeTypeInClassLinkLabel = false;
                break;
            case CONTEXT_ANNOTATION:
                excludeTypeParameterLinks = true;
                excludeTypeBounds = true;
                break;
            case CONTEXT_IMPLEMENTED_INTERFACES:
            case CONTEXT_SUPER_INTERFACES:
            case CONTEXT_SUBINTERFACES:
            case CONTEXT_CLASS_TREE_PARENT:
            case CONTEXT_TREE:
            case CONTEXT_CLASS_SIGNATURE_PARENT_NAME:
                excludeTypeParameterLinks = true;
                excludeTypeBounds = true;
                includeTypeInClassLinkLabel = false;
                includeTypeAsSepLink = true;
                break;
            case CONTEXT_PACKAGE:
            case CONTEXT_CLASS_USE:
            case CONTEXT_CLASS_HEADER:
            case CONTEXT_CLASS_SIGNATURE:
                excludeTypeParameterLinks = true;
                includeTypeAsSepLink = true;
                includeTypeInClassLinkLabel = false;
                break;
            case CONTEXT_MEMBER_TYPE_PARAMS:
                includeTypeAsSepLink = true;
                includeTypeInClassLinkLabel = false;
                break;
            case CONTEXT_RETURN_TYPE:
            case CONTEXT_SUMMARY_RETURN_TYPE:
            case CONTEXT_EXECUTABLE_MEMBER_PARAM:
                excludeTypeBounds = true;
                break;
        }
        context = c;
        if (type != null &&
            type.asTypeVariable()!= null &&
            type.asTypeVariable().owner() instanceof ExecutableMemberDoc){
            excludeTypeParameterLinks = true;
        }
    }
    public boolean isLinkable() {
        return Util.isLinkable(classDoc, ConfigurationImpl.getInstance());
    }
}
