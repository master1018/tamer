    public Content getClassHeader(ClassDoc classDoc);
    public Content getSerialUIDInfoHeader();
    public void addSerialUIDInfo(String header, String serialUID,
            Content serialUidTree);
    public Content getClassContentHeader();
    public SerialFieldWriter getSerialFieldWriter(ClassDoc classDoc);
    public SerialMethodWriter getSerialMethodWriter(ClassDoc classDoc);
    public abstract void close() throws IOException;
    public Content getSerializedContent(Content serializedTreeContent);
    public void addFooter(Content serializedTree);
    public abstract void printDocument(Content serializedTree);
    public interface SerialFieldWriter {
        public Content getSerializableFieldsHeader();
        public Content getFieldsContentHeader(boolean isLastContent);
        public Content getSerializableFields(String heading, Content contentTree);
        public void addMemberDeprecatedInfo(FieldDoc field, Content contentTree);
        public void addMemberDescription(FieldDoc field, Content contentTree);
        public void addMemberDescription(SerialFieldTag serialFieldTag, Content contentTree);
        public void addMemberTags(FieldDoc field, Content contentTree);
        public void addMemberHeader(ClassDoc fieldType, String fieldTypeStr,
            String fieldDimensions, String fieldName, Content contentTree);
        public boolean shouldPrintOverview(FieldDoc field);
    }
    public interface SerialMethodWriter {
        public Content getSerializableMethodsHeader();
        public Content getMethodsContentHeader(boolean isLastContent);
        public Content getSerializableMethods(String heading, Content serializableMethodTree);
        public Content getNoCustomizationMsg(String msg);
        public void addMemberHeader(MethodDoc member, Content methodsContentTree);
        public void addDeprecatedMemberInfo(MethodDoc member, Content methodsContentTree);
        public void addMemberDescription(MethodDoc member, Content methodsContentTree);
        public void addMemberTags(MethodDoc member, Content methodsContentTree);
    }
}
