public abstract class DocInfo
{
    public DocInfo(String rawCommentText, SourcePositionInfo sp)
    {
        mRawCommentText = rawCommentText;
        mPosition = sp;
    }
    public boolean isHidden()
    {
        return comment().isHidden();
    }
    public boolean isDocOnly() {
        return comment().isDocOnly();
    }
    public String getRawCommentText()
    {
        return mRawCommentText;
    }
    public Comment comment()
    {
        if (mComment == null) {
            mComment = new Comment(mRawCommentText, parent(), mPosition);
        }
        return mComment;
    }
    public SourcePositionInfo position()
    {
        return mPosition;
    }
    public abstract ContainerInfo parent();
    public void setSince(String since) {
        mSince = since;
    }
    public String getSince() {
        return mSince;
    }
    private String mRawCommentText;
    Comment mComment;
    SourcePositionInfo mPosition;
    private String mSince;
}
