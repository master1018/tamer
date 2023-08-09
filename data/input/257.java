    public Content getMethodDetailsTreeHeader(ClassDoc classDoc,
            Content memberDetailsTree);
    public Content getMethodDocTreeHeader(MethodDoc method,
            Content methodDetailsTree);
    public Content getSignature(MethodDoc method);
    public void addDeprecated(MethodDoc method, Content methodDocTree);
    public void addComments(Type holder, MethodDoc method, Content methodDocTree);
    public void addTags(MethodDoc method, Content methodDocTree);
    public Content getMethodDetails(Content methodDetailsTree);
    public Content getMethodDoc(Content methodDocTree, boolean isLastContent);
    public void close() throws IOException;
}
