public class WriterFactoryImpl implements WriterFactory {
    private ConfigurationImpl configuration;
    public WriterFactoryImpl(ConfigurationImpl configuration) {
        this.configuration = configuration;
    }
    public ConstantsSummaryWriter getConstantsSummaryWriter() throws Exception {
        return new ConstantsSummaryWriterImpl(configuration);
    }
    public PackageSummaryWriter getPackageSummaryWriter(PackageDoc packageDoc,
        PackageDoc prevPkg, PackageDoc nextPkg) throws Exception {
        return new PackageWriterImpl(ConfigurationImpl.getInstance(), packageDoc,
            prevPkg, nextPkg);
    }
    public ClassWriter getClassWriter(ClassDoc classDoc, ClassDoc prevClass,
            ClassDoc nextClass, ClassTree classTree)
            throws Exception {
        return new ClassWriterImpl(classDoc, prevClass, nextClass, classTree);
    }
    public AnnotationTypeWriter getAnnotationTypeWriter(
        AnnotationTypeDoc annotationType, Type prevType, Type nextType)
    throws Exception {
        return new AnnotationTypeWriterImpl(annotationType, prevType, nextType);
    }
    public AnnotationTypeOptionalMemberWriter
            getAnnotationTypeOptionalMemberWriter(
        AnnotationTypeWriter annotationTypeWriter) throws Exception {
        return new AnnotationTypeOptionalMemberWriterImpl(
            (SubWriterHolderWriter) annotationTypeWriter,
            annotationTypeWriter.getAnnotationTypeDoc());
    }
    public AnnotationTypeRequiredMemberWriter
            getAnnotationTypeRequiredMemberWriter(AnnotationTypeWriter annotationTypeWriter) throws Exception {
        return new AnnotationTypeRequiredMemberWriterImpl(
            (SubWriterHolderWriter) annotationTypeWriter,
            annotationTypeWriter.getAnnotationTypeDoc());
    }
    public EnumConstantWriter getEnumConstantWriter(ClassWriter classWriter)
            throws Exception {
        return new EnumConstantWriterImpl((SubWriterHolderWriter) classWriter,
            classWriter.getClassDoc());
    }
    public FieldWriter getFieldWriter(ClassWriter classWriter)
            throws Exception {
        return new FieldWriterImpl((SubWriterHolderWriter) classWriter,
            classWriter.getClassDoc());
    }
    public  MethodWriter getMethodWriter(ClassWriter classWriter)
            throws Exception {
        return new MethodWriterImpl((SubWriterHolderWriter) classWriter,
            classWriter.getClassDoc());
    }
    public ConstructorWriter getConstructorWriter(ClassWriter classWriter)
            throws Exception {
        return new ConstructorWriterImpl((SubWriterHolderWriter) classWriter,
            classWriter.getClassDoc());
    }
    public MemberSummaryWriter getMemberSummaryWriter(
        ClassWriter classWriter, int memberType)
    throws Exception {
        switch (memberType) {
            case VisibleMemberMap.CONSTRUCTORS:
                return (ConstructorWriterImpl) getConstructorWriter(classWriter);
            case VisibleMemberMap.ENUM_CONSTANTS:
                return (EnumConstantWriterImpl) getEnumConstantWriter(classWriter);
            case VisibleMemberMap.FIELDS:
                return (FieldWriterImpl) getFieldWriter(classWriter);
            case VisibleMemberMap.INNERCLASSES:
                return new NestedClassWriterImpl((SubWriterHolderWriter)
                    classWriter, classWriter.getClassDoc());
            case VisibleMemberMap.METHODS:
                return (MethodWriterImpl) getMethodWriter(classWriter);
            default:
                return null;
        }
    }
    public MemberSummaryWriter getMemberSummaryWriter(
        AnnotationTypeWriter annotationTypeWriter, int memberType)
    throws Exception {
        switch (memberType) {
            case VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL:
                return (AnnotationTypeOptionalMemberWriterImpl)
                    getAnnotationTypeOptionalMemberWriter(annotationTypeWriter);
            case VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED:
                return (AnnotationTypeRequiredMemberWriterImpl)
                    getAnnotationTypeRequiredMemberWriter(annotationTypeWriter);
            default:
                return null;
        }
    }
    public SerializedFormWriter getSerializedFormWriter() throws Exception {
        return new SerializedFormWriterImpl();
    }
}
