public class SerializedFormBuilder extends AbstractBuilder {
    public static final String NAME = "SerializedForm";
    private SerializedFormWriter writer;
    private SerializedFormWriter.SerialFieldWriter fieldWriter;
    private SerializedFormWriter.SerialMethodWriter methodWriter;
    private static final String SERIAL_VERSION_UID_HEADER = "serialVersionUID:";
    private PackageDoc currentPackage;
    private ClassDoc currentClass;
    protected MemberDoc currentMember;
    private Content contentTree;
    private SerializedFormBuilder(Configuration configuration) {
        super(configuration);
    }
    public static SerializedFormBuilder getInstance(Configuration configuration) {
        SerializedFormBuilder builder = new SerializedFormBuilder(configuration);
        return builder;
    }
    public void build() throws IOException {
        if (! serialClassFoundToDocument(configuration.root.classes())) {
            return;
        }
        try {
            writer = configuration.getWriterFactory().getSerializedFormWriter();
            if (writer == null) {
                return;
            }
        } catch (Exception e) {
            throw new DocletAbortException();
        }
        build(LayoutParser.getInstance(configuration).parseXML(NAME), contentTree);
        writer.close();
    }
    public String getName() {
        return NAME;
    }
    public void buildSerializedForm(XMLNode node, Content serializedTree) throws Exception {
        serializedTree = writer.getHeader(configuration.getText(
                "doclet.Serialized_Form"));
        buildChildren(node, serializedTree);
        writer.addFooter(serializedTree);
        writer.printDocument(serializedTree);
        writer.close();
    }
    public void buildSerializedFormSummaries(XMLNode node, Content serializedTree) {
        Content serializedSummariesTree = writer.getSerializedSummariesHeader();
        PackageDoc[] packages = configuration.packages;
        for (int i = 0; i < packages.length; i++) {
            currentPackage = packages[i];
            buildChildren(node, serializedSummariesTree);
        }
        serializedTree.addContent(writer.getSerializedContent(
                serializedSummariesTree));
    }
    public void buildPackageSerializedForm(XMLNode node, Content serializedSummariesTree) {
        Content packageSerializedTree = writer.getPackageSerializedHeader();
        String foo = currentPackage.name();
        ClassDoc[] classes = currentPackage.allClasses(false);
        if (classes == null || classes.length == 0) {
            return;
        }
        if (!serialInclude(currentPackage)) {
            return;
        }
        if (!serialClassFoundToDocument(classes)) {
            return;
        }
        buildChildren(node, packageSerializedTree);
        serializedSummariesTree.addContent(packageSerializedTree);
    }
    public void buildPackageHeader(XMLNode node, Content packageSerializedTree) {
        packageSerializedTree.addContent(writer.getPackageHeader(
                Util.getPackageName(currentPackage)));
    }
    public void buildClassSerializedForm(XMLNode node, Content packageSerializedTree) {
        Content classSerializedTree = writer.getClassSerializedHeader();
        ClassDoc[] classes = currentPackage.allClasses(false);
        Arrays.sort(classes);
        for (int j = 0; j < classes.length; j++) {
            currentClass = classes[j];
            fieldWriter = writer.getSerialFieldWriter(currentClass);
            methodWriter = writer.getSerialMethodWriter(currentClass);
            if(currentClass.isClass() && currentClass.isSerializable()) {
                if(!serialClassInclude(currentClass)) {
                    continue;
                }
                Content classTree = writer.getClassHeader(currentClass);
                buildChildren(node, classTree);
                classSerializedTree.addContent(classTree);
            }
        }
        packageSerializedTree.addContent(classSerializedTree);
    }
    public void buildSerialUIDInfo(XMLNode node, Content classTree) {
        Content serialUidTree = writer.getSerialUIDInfoHeader();
        FieldDoc[] fields = currentClass.fields(false);
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].name().equals("serialVersionUID") &&
                fields[i].constantValueExpression() != null) {
                writer.addSerialUIDInfo(SERIAL_VERSION_UID_HEADER,
                        fields[i].constantValueExpression(), serialUidTree);
                break;
            }
        }
        classTree.addContent(serialUidTree);
    }
    public void buildClassContent(XMLNode node, Content classTree) {
        Content classContentTree = writer.getClassContentHeader();
        buildChildren(node, classContentTree);
        classTree.addContent(classContentTree);
    }
    public void buildSerializableMethods(XMLNode node, Content classContentTree) {
        Content serializableMethodTree = methodWriter.getSerializableMethodsHeader();
        MemberDoc[] members = currentClass.serializationMethods();
        int membersLength = members.length;
        if (membersLength > 0) {
            for (int i = 0; i < membersLength; i++) {
                currentMember = members[i];
                Content methodsContentTree = methodWriter.getMethodsContentHeader(
                        (i == membersLength - 1));
                buildChildren(node, methodsContentTree);
                serializableMethodTree.addContent(methodsContentTree);
            }
        }
        if (currentClass.serializationMethods().length > 0) {
            classContentTree.addContent(methodWriter.getSerializableMethods(
                    configuration.getText("doclet.Serialized_Form_methods"),
                    serializableMethodTree));
            if (currentClass.isSerializable() && !currentClass.isExternalizable()) {
                if (currentClass.serializationMethods().length == 0) {
                    Content noCustomizationMsg = methodWriter.getNoCustomizationMsg(
                            configuration.getText(
                            "doclet.Serializable_no_customization"));
                    classContentTree.addContent(methodWriter.getSerializableMethods(
                    configuration.getText("doclet.Serialized_Form_methods"),
                    noCustomizationMsg));
                }
            }
        }
    }
    public void buildMethodSubHeader(XMLNode node, Content methodsContentTree)  {
        methodWriter.addMemberHeader((MethodDoc)currentMember, methodsContentTree);
    }
    public void buildDeprecatedMethodInfo(XMLNode node, Content methodsContentTree) {
        methodWriter.addDeprecatedMemberInfo((MethodDoc) currentMember, methodsContentTree);
    }
    public void buildMethodInfo(XMLNode node, Content methodsContentTree)  {
        if(configuration.nocomment){
            return;
        }
        buildChildren(node, methodsContentTree);
    }
    public void buildMethodDescription(XMLNode node, Content methodsContentTree) {
        methodWriter.addMemberDescription((MethodDoc) currentMember, methodsContentTree);
    }
    public void buildMethodTags(XMLNode node, Content methodsContentTree) {
        methodWriter.addMemberTags((MethodDoc) currentMember, methodsContentTree);
        MethodDoc method = (MethodDoc)currentMember;
        if (method.name().compareTo("writeExternal") == 0
                && method.tags("serialData").length == 0) {
            if (configuration.serialwarn) {
                configuration.getDocletSpecificMsg().warning(
                        currentMember.position(), "doclet.MissingSerialDataTag",
                        method.containingClass().qualifiedName(), method.name());
            }
        }
    }
    public void buildFieldHeader(XMLNode node, Content classContentTree) {
        if (currentClass.serializableFields().length > 0) {
            buildFieldSerializationOverview(currentClass, classContentTree);
        }
    }
    public void buildFieldSerializationOverview(ClassDoc classDoc, Content classContentTree) {
        if (classDoc.definesSerializableFields()) {
            FieldDoc serialPersistentField = classDoc.serializableFields()[0];
            if (fieldWriter.shouldPrintOverview(serialPersistentField)) {
                Content serializableFieldsTree = fieldWriter.getSerializableFieldsHeader();
                Content fieldsOverviewContentTree = fieldWriter.getFieldsContentHeader(true);
                fieldWriter.addMemberDeprecatedInfo(serialPersistentField,
                        fieldsOverviewContentTree);
                if (!configuration.nocomment) {
                    fieldWriter.addMemberDescription(serialPersistentField,
                            fieldsOverviewContentTree);
                    fieldWriter.addMemberTags(serialPersistentField,
                            fieldsOverviewContentTree);
                }
                serializableFieldsTree.addContent(fieldsOverviewContentTree);
                classContentTree.addContent(fieldWriter.getSerializableFields(
                        configuration.getText("doclet.Serialized_Form_class"),
                        serializableFieldsTree));
            }
        }
    }
    public void buildSerializableFields(XMLNode node, Content classContentTree) {
        MemberDoc[] members = currentClass.serializableFields();
        int membersLength = members.length;
        if (membersLength > 0) {
            Content serializableFieldsTree = fieldWriter.getSerializableFieldsHeader();
            for (int i = 0; i < membersLength; i++) {
                currentMember = members[i];
                if (!currentClass.definesSerializableFields()) {
                    Content fieldsContentTree = fieldWriter.getFieldsContentHeader(
                            (i == membersLength - 1));
                    buildChildren(node, fieldsContentTree);
                    serializableFieldsTree.addContent(fieldsContentTree);
                }
                else {
                    buildSerialFieldTagsInfo(serializableFieldsTree);
                }
            }
            classContentTree.addContent(fieldWriter.getSerializableFields(
                    configuration.getText("doclet.Serialized_Form_fields"),
                    serializableFieldsTree));
        }
    }
    public void buildFieldSubHeader(XMLNode node, Content fieldsContentTree) {
        if (!currentClass.definesSerializableFields()) {
            FieldDoc field = (FieldDoc) currentMember;
            fieldWriter.addMemberHeader(field.type().asClassDoc(),
                    field.type().typeName(), field.type().dimension(), field.name(),
                    fieldsContentTree);
        }
    }
    public void buildFieldDeprecationInfo(XMLNode node, Content fieldsContentTree) {
        if (!currentClass.definesSerializableFields()) {
            FieldDoc field = (FieldDoc)currentMember;
            fieldWriter.addMemberDeprecatedInfo(field, fieldsContentTree);
        }
    }
    public void buildSerialFieldTagsInfo(Content serializableFieldsTree) {
        if(configuration.nocomment){
            return;
        }
        FieldDoc field = (FieldDoc)currentMember;
        SerialFieldTag[] tags = field.serialFieldTags();
        Arrays.sort(tags);
        int tagsLength = tags.length;
        for (int i = 0; i < tagsLength; i++) {
            Content fieldsContentTree = fieldWriter.getFieldsContentHeader(
                    (i == tagsLength - 1));
            fieldWriter.addMemberHeader(tags[i].fieldTypeDoc(),
                    tags[i].fieldType(), "", tags[i].fieldName(), fieldsContentTree);
            fieldWriter.addMemberDescription(tags[i], fieldsContentTree);
            serializableFieldsTree.addContent(fieldsContentTree);
        }
    }
    public void buildFieldInfo(XMLNode node, Content fieldsContentTree) {
        if(configuration.nocomment){
            return;
        }
        FieldDoc field = (FieldDoc)currentMember;
        ClassDoc cd = field.containingClass();
        if ((field.tags("serial").length == 0) && ! field.isSynthetic()
                && configuration.serialwarn) {
            configuration.message.warning(field.position(),
                    "doclet.MissingSerialTag", cd.qualifiedName(),
                    field.name());
        }
        fieldWriter.addMemberDescription(field, fieldsContentTree);
        fieldWriter.addMemberTags(field, fieldsContentTree);
    }
    public static boolean serialInclude(Doc doc) {
        if (doc == null) {
            return false;
        }
        return doc.isClass() ?
            serialClassInclude((ClassDoc)doc) :
            serialDocInclude(doc);
    }
    private static boolean serialClassInclude(ClassDoc cd) {
        if (cd.isEnum()) {
            return false;
        }
        try {
            cd.superclassType();
        } catch (NullPointerException e) {
            return false;
        }
        if (cd.isSerializable()) {
            if (cd.tags("serial").length > 0) {
                return serialDocInclude(cd);
            } else if (cd.isPublic() || cd.isProtected()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    private static boolean serialDocInclude(Doc doc) {
        if (doc.isEnum()) {
            return false;
        }
        Tag[] serial = doc.tags("serial");
        if (serial.length > 0) {
            String serialtext = serial[0].text().toLowerCase();
            if (serialtext.indexOf("exclude") >= 0) {
                return false;
            } else if (serialtext.indexOf("include") >= 0) {
                return true;
            }
        }
        return true;
    }
    private boolean serialClassFoundToDocument(ClassDoc[] classes) {
        for (int i = 0; i < classes.length; i++) {
            if (serialClassInclude(classes[i])) {
                return true;
            }
        }
        return false;
    }
}
