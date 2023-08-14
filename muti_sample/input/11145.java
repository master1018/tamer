    public void addAnnotationDetailsTreeHeader(ClassDoc classDoc,
            Content memberDetailsTree);
    public Content getAnnotationDocTreeHeader(MemberDoc member,
            Content annotationDetailsTree);
    public Content getAnnotationDetails(Content annotationDetailsTree);
    public Content getAnnotationDoc(Content annotationDocTree, boolean isLastContent);
    public Content getSignature(MemberDoc member);
    public void addDeprecated(MemberDoc member, Content annotationDocTree);
    public void addComments(MemberDoc member, Content annotationDocTree);
    public void addTags(MemberDoc member, Content annotationDocTree);
    public void close() throws IOException;
}
