public class BuilderFactory {
    private Configuration configuration;
    private WriterFactory writerFactory;
    public BuilderFactory (Configuration configuration) {
        this.configuration = configuration;
        this.writerFactory = configuration.getWriterFactory();
    }
    public AbstractBuilder getConstantsSummaryBuider() throws Exception {
        return ConstantsSummaryBuilder.getInstance(configuration,
            writerFactory.getConstantsSummaryWriter());
    }
    public AbstractBuilder getPackageSummaryBuilder(PackageDoc pkg, PackageDoc prevPkg,
            PackageDoc nextPkg) throws Exception {
        return PackageSummaryBuilder.getInstance(configuration, pkg,
            writerFactory.getPackageSummaryWriter(pkg, prevPkg, nextPkg));
    }
    public AbstractBuilder getClassBuilder(ClassDoc classDoc,
        ClassDoc prevClass, ClassDoc nextClass, ClassTree classTree)
            throws Exception {
        return ClassBuilder.getInstance(configuration, classDoc,
            writerFactory.getClassWriter(classDoc, prevClass, nextClass,
                classTree));
    }
    public AbstractBuilder getAnnotationTypeBuilder(
        AnnotationTypeDoc annotationType,
        Type prevType, Type nextType)
            throws Exception {
        return AnnotationTypeBuilder.getInstance(configuration, annotationType,
            writerFactory.getAnnotationTypeWriter(annotationType, prevType,
            nextType));
    }
    public AbstractBuilder getMethodBuilder(ClassWriter classWriter)
           throws Exception {
        return MethodBuilder.getInstance(configuration,
            classWriter.getClassDoc(),
            writerFactory.getMethodWriter(classWriter));
    }
    public AbstractBuilder getAnnotationTypeOptionalMemberBuilder(
            AnnotationTypeWriter annotationTypeWriter)
    throws Exception {
        return AnnotationTypeOptionalMemberBuilder.getInstance(configuration,
            annotationTypeWriter.getAnnotationTypeDoc(),
            writerFactory.getAnnotationTypeOptionalMemberWriter(
                annotationTypeWriter));
    }
    public AbstractBuilder getAnnotationTypeRequiredMemberBuilder(
            AnnotationTypeWriter annotationTypeWriter)
    throws Exception {
        return AnnotationTypeRequiredMemberBuilder.getInstance(configuration,
            annotationTypeWriter.getAnnotationTypeDoc(),
            writerFactory.getAnnotationTypeRequiredMemberWriter(
                annotationTypeWriter));
    }
    public AbstractBuilder getEnumConstantsBuilder(ClassWriter classWriter)
            throws Exception {
        return EnumConstantBuilder.getInstance(configuration, classWriter.getClassDoc(),
            writerFactory.getEnumConstantWriter(classWriter));
    }
    public AbstractBuilder getFieldBuilder(ClassWriter classWriter)
            throws Exception {
        return FieldBuilder.getInstance(configuration, classWriter.getClassDoc(),
            writerFactory.getFieldWriter(classWriter));
    }
    public AbstractBuilder getConstructorBuilder(ClassWriter classWriter)
            throws Exception {
        return ConstructorBuilder.getInstance(configuration,
            classWriter.getClassDoc(), writerFactory.getConstructorWriter(
            classWriter));
    }
    public AbstractBuilder getMemberSummaryBuilder(ClassWriter classWriter)
            throws Exception {
        return MemberSummaryBuilder.getInstance(classWriter, configuration);
    }
    public AbstractBuilder getMemberSummaryBuilder(
            AnnotationTypeWriter annotationTypeWriter)
    throws Exception {
        return MemberSummaryBuilder.getInstance(annotationTypeWriter,
            configuration);
    }
    public AbstractBuilder getSerializedFormBuilder()
            throws Exception {
        return SerializedFormBuilder.getInstance(configuration);
    }
}
