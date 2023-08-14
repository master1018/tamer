class AnnotationTypeMismatchExceptionProxy extends ExceptionProxy {
    private Method member;
    private String foundType;
    AnnotationTypeMismatchExceptionProxy(String foundType) {
        this.foundType = foundType;
    }
    AnnotationTypeMismatchExceptionProxy setMember(Method member) {
        this.member = member;
        return this;
    }
    protected RuntimeException generateException() {
        return new AnnotationTypeMismatchException(member, foundType);
    }
}
