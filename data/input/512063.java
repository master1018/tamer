public class ParsedTagInfo extends TagInfo
{
    private ContainerInfo mContainer;
    private String mCommentText;
    private Comment mComment;
    ParsedTagInfo(String name, String kind, String text, ContainerInfo base, SourcePositionInfo sp)
    {
        super(name, kind, text, SourcePositionInfo.findBeginning(sp, text));
        mContainer = base;
        mCommentText = text;
    }
    public TagInfo[] commentTags()
    {
        if (mComment == null) {
            mComment = new Comment(mCommentText, mContainer, position());
        }
        return mComment.tags();
    }
    protected void setCommentText(String comment)
    {
        mCommentText = comment;
    }
    public static <T extends ParsedTagInfo> TagInfo[]
    joinTags(T[] tags)
    {
        ArrayList<TagInfo> list = new ArrayList<TagInfo>();
        final int N = tags.length;
        for (int i=0; i<N; i++) {
            TagInfo[] t = tags[i].commentTags();
            final int M = t.length;
            for (int j=0; j<M; j++) {
                list.add(t[j]);
            }
        }
        return list.toArray(new TagInfo[list.size()]);
    }
}
