    public Content getFieldDetailsTreeHeader(ClassDoc classDoc,
            Content memberDetailsTree);
    public Content getFieldDocTreeHeader(FieldDoc field,
            Content fieldDetailsTree);
    public Content getSignature(FieldDoc field);
    public void addDeprecated(FieldDoc field, Content fieldDocTree);
    public void addComments(FieldDoc field, Content fieldDocTree);
    public void addTags(FieldDoc field, Content fieldDocTree);
    public Content getFieldDetails(Content memberDetailsTree);
    public Content getFieldDoc(Content fieldDocTree, boolean isLastContent);
    public void close() throws IOException;
}
