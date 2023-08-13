 class AttributeTranslator {
    private AttributeTranslator() {
    }
    public static TypeList getExceptions(Method method) {
        AttributeList attribs = method.getAttributes();
        AttExceptions exceptions = (AttExceptions)
            attribs.findFirst(AttExceptions.ATTRIBUTE_NAME);
        if (exceptions == null) {
            return StdTypeList.EMPTY;
        }
        return exceptions.getExceptions();
    }
    public static Annotations getAnnotations(AttributeList attribs) {
        Annotations result = getAnnotations0(attribs);
        Annotation signature = getSignature(attribs);
        if (signature != null) {
            result = Annotations.combine(result, signature);
        }
        return result;
    }
    public static Annotations getClassAnnotations(DirectClassFile cf,
            CfOptions args) {
        CstType thisClass = cf.getThisClass();
        AttributeList attribs = cf.getAttributes();
        Annotations result = getAnnotations(attribs);
        Annotation enclosingMethod = translateEnclosingMethod(attribs);
        try {
            Annotations innerClassAnnotations =
                translateInnerClasses(thisClass, attribs,
                        enclosingMethod == null);
            if (innerClassAnnotations != null) {
                result = Annotations.combine(result, innerClassAnnotations);
            }
        } catch (Warning warn) {
            args.warn.println("warning: " + warn.getMessage());
        }
        if (enclosingMethod != null) {
            result = Annotations.combine(result, enclosingMethod);
        }
        if (AccessFlags.isAnnotation(cf.getAccessFlags())) {
            Annotation annotationDefault =
                translateAnnotationDefaults(cf);
            if (annotationDefault != null) {
                result = Annotations.combine(result, annotationDefault);
            }
        }
        return result;
    }
    public static Annotations getMethodAnnotations(Method method) {
        Annotations result = getAnnotations(method.getAttributes());
        TypeList exceptions = getExceptions(method);
        if (exceptions.size() != 0) {
            Annotation throwsAnnotation = 
                AnnotationUtils.makeThrows(exceptions);
            result = Annotations.combine(result, throwsAnnotation);
        }
        return result;
    }
    private static Annotations getAnnotations0(AttributeList attribs) {
        AttRuntimeVisibleAnnotations visible =
            (AttRuntimeVisibleAnnotations)
            attribs.findFirst(AttRuntimeVisibleAnnotations.ATTRIBUTE_NAME);
        AttRuntimeInvisibleAnnotations invisible =
            (AttRuntimeInvisibleAnnotations)
            attribs.findFirst(AttRuntimeInvisibleAnnotations.ATTRIBUTE_NAME);
        if (visible == null) {
            if (invisible == null) {
                return Annotations.EMPTY;
            }
            return invisible.getAnnotations();
        }
        if (invisible == null) {
            return visible.getAnnotations();
        }
        return Annotations.combine(visible.getAnnotations(),
                invisible.getAnnotations());
    }
    private static Annotation getSignature(AttributeList attribs) {
        AttSignature signature = (AttSignature)
            attribs.findFirst(AttSignature.ATTRIBUTE_NAME);
        if (signature == null) {
            return null;
        }
        return AnnotationUtils.makeSignature(signature.getSignature());
    }
    private static Annotation translateEnclosingMethod(AttributeList attribs) {
        AttEnclosingMethod enclosingMethod = (AttEnclosingMethod)
            attribs.findFirst(AttEnclosingMethod.ATTRIBUTE_NAME);
        if (enclosingMethod == null) {
            return null;
        }
        CstType enclosingClass = enclosingMethod.getEnclosingClass();
        CstNat nat = enclosingMethod.getMethod();
        if (nat == null) {
            return AnnotationUtils.makeEnclosingClass(enclosingClass);
        }
        return AnnotationUtils.makeEnclosingMethod(
                new CstMethodRef(enclosingClass, nat));
    }
    private static Annotations translateInnerClasses(CstType thisClass,
            AttributeList attribs, boolean needEnclosingClass) {
        AttInnerClasses innerClasses = (AttInnerClasses)
            attribs.findFirst(AttInnerClasses.ATTRIBUTE_NAME);
        if (innerClasses == null) {
            return null;
        }
        InnerClassList list = innerClasses.getInnerClasses();
        int size = list.size();
        InnerClassList.Item foundThisClass = null;
        ArrayList<Type> membersList = new ArrayList<Type>();
        for (int i = 0; i < size; i++) {
            InnerClassList.Item item = list.get(i);
            CstType innerClass = item.getInnerClass();
            if (innerClass.equals(thisClass)) {
                foundThisClass = item;
            } else if (thisClass.equals(item.getOuterClass())) {
                membersList.add(innerClass.getClassType());
            }
        }
        int membersSize = membersList.size();
        if ((foundThisClass == null) && (membersSize == 0)) {
            return null;
        }
        Annotations result = new Annotations();
        if (foundThisClass != null) {
            result.add(AnnotationUtils.makeInnerClass(
                               foundThisClass.getInnerName(),
                               foundThisClass.getAccessFlags()));
            if (needEnclosingClass) {
                CstType outer = foundThisClass.getOuterClass();
                if (outer == null) {
                    throw new Warning(
                            "Ignoring InnerClasses attribute for an " +
                            "anonymous inner class that doesn't come with " +
                            "an associated EnclosingMethod attribute. " +
                            "(This class was probably produced by a broken " +
                            "compiler.)");
                }
                result.add(AnnotationUtils.makeEnclosingClass(
                                   foundThisClass.getOuterClass()));
            }
        }
        if (membersSize != 0) {
            StdTypeList typeList = new StdTypeList(membersSize);
            for (int i = 0; i < membersSize; i++) {
                typeList.set(i, membersList.get(i));
            }
            typeList.setImmutable();
            result.add(AnnotationUtils.makeMemberClasses(typeList));
        }
        result.setImmutable();
        return result;
    }
    public static AnnotationsList getParameterAnnotations(Method method) {
        AttributeList attribs = method.getAttributes();
        AttRuntimeVisibleParameterAnnotations visible =
            (AttRuntimeVisibleParameterAnnotations)
            attribs.findFirst(
                    AttRuntimeVisibleParameterAnnotations.ATTRIBUTE_NAME);
        AttRuntimeInvisibleParameterAnnotations invisible =
            (AttRuntimeInvisibleParameterAnnotations)
            attribs.findFirst(
                    AttRuntimeInvisibleParameterAnnotations.ATTRIBUTE_NAME);
        if (visible == null) {
            if (invisible == null) {
                return AnnotationsList.EMPTY;
            }
            return invisible.getParameterAnnotations();
        }
        if (invisible == null) {
            return visible.getParameterAnnotations();
        }
        return AnnotationsList.combine(visible.getParameterAnnotations(),
                invisible.getParameterAnnotations());
    }
    private static Annotation translateAnnotationDefaults(DirectClassFile cf) {
        CstType thisClass = cf.getThisClass();
        MethodList methods = cf.getMethods();
        int sz = methods.size();
        Annotation result =
            new Annotation(thisClass, AnnotationVisibility.EMBEDDED);
        boolean any = false;
        for (int i = 0; i < sz; i++) {
            Method one = methods.get(i);
            AttributeList attribs = one.getAttributes();
            AttAnnotationDefault oneDefault = (AttAnnotationDefault)
                attribs.findFirst(AttAnnotationDefault.ATTRIBUTE_NAME);
            if (oneDefault != null) {
                NameValuePair pair = new NameValuePair(
                        one.getNat().getName(),
                        oneDefault.getValue());
                result.add(pair);
                any = true;
            }
        }
        if (! any) {
            return null;
        }
        result.setImmutable();
        return AnnotationUtils.makeAnnotationDefault(result);
    }
}
