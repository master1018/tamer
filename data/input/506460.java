public final class AnnotationUtils {
    private static final CstType ANNOTATION_DEFAULT_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/AnnotationDefault;"));
    private static final CstType ENCLOSING_CLASS_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/EnclosingClass;"));
    private static final CstType ENCLOSING_METHOD_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/EnclosingMethod;"));
    private static final CstType INNER_CLASS_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/InnerClass;"));
    private static final CstType MEMBER_CLASSES_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/MemberClasses;"));
    private static final CstType SIGNATURE_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/Signature;"));
    private static final CstType THROWS_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/Throws;"));
    private static final CstUtf8 ACCESS_FLAGS_UTF = new CstUtf8("accessFlags");
    private static final CstUtf8 NAME_UTF = new CstUtf8("name");
    private static final CstUtf8 VALUE_UTF = new CstUtf8("value");
    private AnnotationUtils() {
    }
    public static Annotation makeAnnotationDefault(Annotation defaults) {
        Annotation result = new Annotation(ANNOTATION_DEFAULT_TYPE, SYSTEM);
        result.put(new NameValuePair(VALUE_UTF, new CstAnnotation(defaults)));
        result.setImmutable();
        return result;
    }
    public static Annotation makeEnclosingClass(CstType clazz) {
        Annotation result = new Annotation(ENCLOSING_CLASS_TYPE, SYSTEM);
        result.put(new NameValuePair(VALUE_UTF, clazz));
        result.setImmutable();
        return result;
    }
    public static Annotation makeEnclosingMethod(CstMethodRef method) {
        Annotation result = new Annotation(ENCLOSING_METHOD_TYPE, SYSTEM);
        result.put(new NameValuePair(VALUE_UTF, method));
        result.setImmutable();
        return result;
    }
    public static Annotation makeInnerClass(CstUtf8 name, int accessFlags) {
        Annotation result = new Annotation(INNER_CLASS_TYPE, SYSTEM);
        Constant nameCst =
            (name != null) ? new CstString(name) : CstKnownNull.THE_ONE;
        result.put(new NameValuePair(NAME_UTF, nameCst));
        result.put(new NameValuePair(ACCESS_FLAGS_UTF,
                        CstInteger.make(accessFlags)));
        result.setImmutable();
        return result;
    }
    public static Annotation makeMemberClasses(TypeList types) {
        CstArray array = makeCstArray(types);
        Annotation result = new Annotation(MEMBER_CLASSES_TYPE, SYSTEM);
        result.put(new NameValuePair(VALUE_UTF, array));
        result.setImmutable();
        return result;
    }
    public static Annotation makeSignature(CstUtf8 signature) {
        Annotation result = new Annotation(SIGNATURE_TYPE, SYSTEM);
        String raw = signature.getString();
        int rawLength = raw.length();
        ArrayList<String> pieces = new ArrayList<String>(20);
        for (int at = 0; at < rawLength; ) {
            char c = raw.charAt(at);
            int endAt = at + 1;
            if (c == 'L') {
                while (endAt < rawLength) {
                    c = raw.charAt(endAt);
                    if (c == ';') {
                        endAt++;
                        break;
                    } else if (c == '<') {
                        break;
                    }
                    endAt++;
                }
            } else {
                while (endAt < rawLength) {
                    c = raw.charAt(endAt);
                    if (c == 'L') {
                        break;
                    }
                    endAt++;
                }
            }
            pieces.add(raw.substring(at, endAt));
            at = endAt;
        }
        int size = pieces.size();
        CstArray.List list = new CstArray.List(size);
        for (int i = 0; i < size; i++) {
            list.set(i, new CstString(pieces.get(i)));
        }
        list.setImmutable();
        result.put(new NameValuePair(VALUE_UTF, new CstArray(list)));
        result.setImmutable();
        return result;
    }
    public static Annotation makeThrows(TypeList types) {
        CstArray array = makeCstArray(types);
        Annotation result = new Annotation(THROWS_TYPE, SYSTEM);
        result.put(new NameValuePair(VALUE_UTF, array));
        result.setImmutable();
        return result;
    }
    private static CstArray makeCstArray(TypeList types) {
        int size = types.size();
        CstArray.List list = new CstArray.List(size);
        for (int i = 0; i < size; i++) {
            list.set(i, CstType.intern(types.getType(i)));
        }
        list.setImmutable();
        return new CstArray(list);
    }
}
