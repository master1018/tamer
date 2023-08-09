    public abstract ClassWriter getClassWriter(ClassDoc classDoc,
        ClassDoc prevClass, ClassDoc nextClass, ClassTree classTree)
            throws Exception;
    public abstract AnnotationTypeWriter getAnnotationTypeWriter(
        AnnotationTypeDoc annotationType, Type prevType, Type nextType)
            throws Exception;
    public abstract MethodWriter getMethodWriter(ClassWriter classWriter)
            throws Exception;
    public abstract AnnotationTypeOptionalMemberWriter
            getAnnotationTypeOptionalMemberWriter(
        AnnotationTypeWriter annotationTypeWriter) throws Exception;
    public abstract AnnotationTypeRequiredMemberWriter
            getAnnotationTypeRequiredMemberWriter(
        AnnotationTypeWriter annotationTypeWriter) throws Exception;
    public abstract EnumConstantWriter getEnumConstantWriter(
        ClassWriter classWriter) throws Exception;
    public abstract FieldWriter getFieldWriter(ClassWriter classWriter)
            throws Exception;
    public abstract ConstructorWriter getConstructorWriter(
        ClassWriter classWriter)
    throws Exception;
    public abstract MemberSummaryWriter getMemberSummaryWriter(
        ClassWriter classWriter, int memberType)
    throws Exception;
    public abstract MemberSummaryWriter getMemberSummaryWriter(
        AnnotationTypeWriter annotationTypeWriter, int memberType)
    throws Exception;
    public SerializedFormWriter getSerializedFormWriter() throws Exception;
}
