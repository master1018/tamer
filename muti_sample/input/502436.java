public class ThrowsTagInfo extends ParsedTagInfo
{
    static final Pattern PATTERN = Pattern.compile(
                                "(\\S+)\\s+(.*)",
                                Pattern.DOTALL);
    private ClassInfo mException;
    public ThrowsTagInfo(String name, String kind, String text,
            ContainerInfo base, SourcePositionInfo sp)
    {
        super(name, kind, text, base, sp);
        Matcher m = PATTERN.matcher(text);
        if (m.matches()) {
            setCommentText(m.group(2));
            String className = m.group(1);
            if (base instanceof ClassInfo) {
                mException = ((ClassInfo)base).findClass(className);
            }
            if (mException == null) {
                mException = Converter.obtainClass(className);
            }
        }
    }
    public ThrowsTagInfo(String name, String kind, String text,
                            ClassInfo exception, String exceptionComment,
                            ContainerInfo base, SourcePositionInfo sp)
    {
        super(name, kind, text, base, sp);
        mException = exception;
        setCommentText(exceptionComment);
    }
    public ClassInfo exception()
    {
        return mException;
    }
    public TypeInfo exceptionType()
    {
        if (mException != null) {
            return mException.asTypeInfo();
        } else {
            return null;
        }
    }
    public static void makeHDF(HDF data, String base, ThrowsTagInfo[] tags)
    {
        for (int i=0; i<tags.length; i++) {
            TagInfo.makeHDF(data, base + '.' + i + ".comment",
                    tags[i].commentTags());
            if (tags[i].exceptionType() != null) {
                tags[i].exceptionType().makeHDF(data, base + "." + i + ".type");
            }
        }
    }
}
