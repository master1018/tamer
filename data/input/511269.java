public class ModelUtil {
    private ModelUtil() {
    }
    public static IClassDefinition getClass(IPackage aPackage,
            String qualifiedClassName) {
        for (IClassDefinition clazz : aPackage.getClasses()) {
            if (qualifiedClassName.equals(clazz.getName())) {
                return clazz;
            }
        }
        return null;
    }
    public static IAnnotation getAnnotation(IAnnotatableElement element,
            String qualifiedTypeName) {
        for (IAnnotation annotation : element.getAnnotations()) {
            if (qualifiedTypeName.equals(annotation.getType()
                    .getClassDefinition().getQualifiedName())) {
                return annotation;
            }
        }
        return null;
    }
    public static IAnnotationElement getAnnotationElement(
            IAnnotation annotation, String elementName) {
        for (IAnnotationElement element : annotation.getElements()) {
            if (elementName.equals(element.getDeclaringField().getName())) {
                return element;
            }
        }
        return null;
    }
    public static IField getField(IClassDefinition clazz, String fieldName) {
        for (IField field : clazz.getFields()) {
            if (fieldName.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }
    public static IAnnotationField getAnnotationField(
            IClassDefinition annotation, String fieldName) {
        for (IAnnotationField field : annotation.getAnnotationFields()) {
            if (fieldName.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }
    public static IPackage getPackage(IApi api, String packageName) {
        for (IPackage aPackage : api.getPackages()) {
            if (packageName.equals(aPackage.getName())) {
                return aPackage;
            }
        }
        return null;
    }
    public static String getPackageName(String classIdentifier) {
        int lastIndexOfSlash = classIdentifier.lastIndexOf('.');
        String packageName = null;
        if (lastIndexOfSlash == -1) {
            packageName = "";
        } else {
            packageName = classIdentifier.substring(0, lastIndexOfSlash);
        }
        return packageName;
    }
    public static String getClassName(String classIdentifier) {
        int lastIndexOfDot = classIdentifier.lastIndexOf('.');
        String className = null;
        if (lastIndexOfDot == -1) {
            className = classIdentifier;
        } else {
            className = classIdentifier.substring(lastIndexOfDot + 1);
        }
        return className;
    }
    public static String separate(Collection<? extends Object> elements,
            String separator) {
        StringBuilder s = new StringBuilder();
        boolean first = true;
        for (Object object : elements) {
            if (!first) {
                s.append(separator);
            }
            s.append(object.toString());
            first = false;
        }
        return s.toString();
    }
    public static boolean isJavaLangObject(ITypeReference type) {
        if (type instanceof IClassDefinition) {
            IClassDefinition clazz = (IClassDefinition) type;
            if ("java.lang".equals(clazz.getPackageName())) {
                return "Object".equals(clazz.getName());
            }
        }
        return false;
    }
}
