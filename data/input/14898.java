    public Content getMemberSummaryHeader(ClassDoc classDoc,
            Content memberSummaryTree);
    public Content getSummaryTableTree(ClassDoc classDoc);
    public void addMemberSummary(ClassDoc classDoc, ProgramElementDoc member,
        Tag[] firstSentenceTags, Content tableTree, int counter);
    public Content getInheritedSummaryHeader(ClassDoc classDoc);
    public void addInheritedMemberSummary(ClassDoc classDoc,
        ProgramElementDoc member, boolean isFirst, boolean isLast,
        Content linksTree);
    public Content getInheritedSummaryLinksTree();
    public Content getMemberTree(Content memberTree);
    public void close() throws IOException;
}
