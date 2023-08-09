public class ElementFilter {
    private ElementFilter() {} 
    private static Set<ElementKind> CONSTRUCTOR_KIND =
        Collections.unmodifiableSet(EnumSet.of(ElementKind.CONSTRUCTOR));
    private static Set<ElementKind> FIELD_KINDS =
        Collections.unmodifiableSet(EnumSet.of(ElementKind.FIELD,
                                               ElementKind.ENUM_CONSTANT));
    private static Set<ElementKind> METHOD_KIND =
        Collections.unmodifiableSet(EnumSet.of(ElementKind.METHOD));
    private static Set<ElementKind> PACKAGE_KIND =
        Collections.unmodifiableSet(EnumSet.of(ElementKind.PACKAGE));
    private static Set<ElementKind> TYPE_KINDS =
        Collections.unmodifiableSet(EnumSet.of(ElementKind.CLASS,
                                               ElementKind.ENUM,
                                               ElementKind.INTERFACE,
                                               ElementKind.ANNOTATION_TYPE));
    public static List<VariableElement>
            fieldsIn(Iterable<? extends Element> elements) {
        return listFilter(elements, FIELD_KINDS, VariableElement.class);
    }
    public static Set<VariableElement>
            fieldsIn(Set<? extends Element> elements) {
        return setFilter(elements, FIELD_KINDS, VariableElement.class);
    }
    public static List<ExecutableElement>
            constructorsIn(Iterable<? extends Element> elements) {
        return listFilter(elements, CONSTRUCTOR_KIND, ExecutableElement.class);
    }
    public static Set<ExecutableElement>
            constructorsIn(Set<? extends Element> elements) {
        return setFilter(elements, CONSTRUCTOR_KIND, ExecutableElement.class);
    }
    public static List<ExecutableElement>
            methodsIn(Iterable<? extends Element> elements) {
        return listFilter(elements, METHOD_KIND, ExecutableElement.class);
    }
    public static Set<ExecutableElement>
            methodsIn(Set<? extends Element> elements) {
        return setFilter(elements, METHOD_KIND, ExecutableElement.class);
    }
    public static List<TypeElement>
            typesIn(Iterable<? extends Element> elements) {
        return listFilter(elements, TYPE_KINDS, TypeElement.class);
    }
    public static Set<TypeElement>
            typesIn(Set<? extends Element> elements) {
        return setFilter(elements, TYPE_KINDS, TypeElement.class);
    }
    public static List<PackageElement>
            packagesIn(Iterable<? extends Element> elements) {
        return listFilter(elements, PACKAGE_KIND, PackageElement.class);
    }
    public static Set<PackageElement>
            packagesIn(Set<? extends Element> elements) {
        return setFilter(elements, PACKAGE_KIND, PackageElement.class);
    }
    private static <E extends Element> List<E> listFilter(Iterable<? extends Element> elements,
                                                          Set<ElementKind> targetKinds,
                                                          Class<E> clazz) {
        List<E> list = new ArrayList<E>();
        for (Element e : elements) {
            if (targetKinds.contains(e.getKind()))
                list.add(clazz.cast(e));
        }
        return list;
    }
    private static <E extends Element> Set<E> setFilter(Set<? extends Element> elements,
                                                        Set<ElementKind> targetKinds,
                                                        Class<E> clazz) {
        Set<E> set = new LinkedHashSet<E>();
        for (Element e : elements) {
            if (targetKinds.contains(e.getKind()))
                set.add(clazz.cast(e));
        }
        return set;
    }
}
