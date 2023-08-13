    public Content getEnumConstantsDetailsTreeHeader(ClassDoc classDoc,
            Content memberDetailsTree);
    public Content getEnumConstantsTreeHeader(FieldDoc enumConstant,
            Content enumConstantsDetailsTree);
    public Content getSignature(FieldDoc enumConstant);
    public void addDeprecated(FieldDoc enumConstant, Content enumConstantsTree);
    public void addComments(FieldDoc enumConstant, Content enumConstantsTree);
    public void addTags(FieldDoc enumConstant, Content enumConstantsTree);
    public Content getEnumConstantsDetails(Content memberDetailsTree);
    public Content getEnumConstants(Content enumConstantsTree, boolean isLastContent);
    public void close() throws IOException;
}
