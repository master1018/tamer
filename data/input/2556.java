    public Content getConstructorDetailsTreeHeader(ClassDoc classDoc,
            Content memberDetailsTree);
    public Content getConstructorDocTreeHeader(ConstructorDoc constructor,
            Content constructorDetailsTree);
    public Content getSignature(ConstructorDoc constructor);
    public void addDeprecated(ConstructorDoc constructor, Content constructorDocTree);
    public void addComments(ConstructorDoc constructor, Content constructorDocTree);
    public void addTags(ConstructorDoc constructor, Content constructorDocTree);
    public Content getConstructorDetails(Content memberDetailsTree);
    public Content getConstructorDoc(Content constructorDocTree, boolean isLastContent);
    public void setFoundNonPubConstructor(boolean foundNonPubConstructor);
    public void close() throws IOException;
}
