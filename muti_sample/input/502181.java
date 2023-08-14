class HasClassAnnotation implements Predicate<TestMethod> {
    private Class<? extends Annotation> annotationClass;
    public HasClassAnnotation(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }
    public boolean apply(TestMethod testMethod) {
        return testMethod.getEnclosingClass().getAnnotation(annotationClass) != null;
    }
}
