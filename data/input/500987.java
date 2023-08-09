public class AttrTagInfo extends TagInfo
{
    private static final String REF_COMMAND = "ref";
    private static final String NAME_COMMAND = "name";
    private static final String DESCRIPTION_COMMAND = "description";
    private static final Pattern TEXT = Pattern.compile("(\\S+)\\s*(.*)", Pattern.DOTALL);
    private static final Pattern NAME_TEXT = Pattern.compile("(\\S+)(.*)",
                                                Pattern.DOTALL);
    private ContainerInfo mBase;
    private String mCommand;
    private FieldInfo mRefField;
    private AttributeInfo mAttrInfo;
    private String mAttrName;
    private Comment mDescrComment;
    AttrTagInfo(String name, String kind, String text, ContainerInfo base,
            SourcePositionInfo position)
    {
        super(name, kind, text, position);
        mBase = base;
        parse(text, base, position);
    }
    void parse(String text, ContainerInfo base, SourcePositionInfo position) {
        Matcher m;
        m = TEXT.matcher(text);
        if (!m.matches()) {
            Errors.error(Errors.BAD_ATTR_TAG, position, "Bad @attr tag: " + text);
            return;
        }
        String command = m.group(1);
        String more = m.group(2);
        if (REF_COMMAND.equals(command)) {
            String ref = more.trim();
            LinkReference linkRef = LinkReference.parse(ref, mBase, position, false);
            if (!linkRef.good) {
                Errors.error(Errors.BAD_ATTR_TAG, position, "Unresolved @attr ref: " + ref);
                return;
            }
            if (!(linkRef.memberInfo instanceof FieldInfo)) {
                Errors.error(Errors.BAD_ATTR_TAG, position, "@attr must be a field: " + ref);
                return;
            }
            mCommand = command;
            mRefField = (FieldInfo)linkRef.memberInfo;
        }
        else if (NAME_COMMAND.equals(command)) {
            m = NAME_TEXT.matcher(more);
            if (!m.matches() || m.group(2).trim().length() != 0) {
                Errors.error(Errors.BAD_ATTR_TAG, position, "Bad @attr name tag: " + more);
                return;
            }
            mCommand = command;
            mAttrName = m.group(1);
        }
        else if (DESCRIPTION_COMMAND.equals(command)) {
            mCommand = command;
            mDescrComment = new Comment(more, base, position);
        }
        else {
            Errors.error(Errors.BAD_ATTR_TAG, position, "Bad @attr command: " + command);
        }
    }
    public FieldInfo reference() {
        return REF_COMMAND.equals(mCommand) ? mRefField : null;
    }
    @Override
    public String name() {
        return NAME_COMMAND.equals(mCommand) ? mAttrName : null;
    }
    public Comment description() {
        return DESCRIPTION_COMMAND.equals(mCommand) ? mDescrComment : null;
    }
    @Override
    public void makeHDF(HDF data, String base)
    {
        super.makeHDF(data, base);
    }
    public void setAttribute(AttributeInfo info) {
        mAttrInfo = info;
    }
    public static void makeReferenceHDF(HDF data, String base, AttrTagInfo[] tags)
    {
        int i=0;
        for (AttrTagInfo t: tags) {
            if (REF_COMMAND.equals(t.mCommand)) {
                if (t.mAttrInfo == null) {
                    String msg = "ERROR: unlinked attr: " + t.mRefField.name();
                    if (false) {
                        System.out.println(msg);
                    } else {
                        throw new RuntimeException(msg);
                    }
                } else {
                    data.setValue(base + "." + i + ".name", t.mAttrInfo.name());
                    data.setValue(base + "." + i + ".href", t.mAttrInfo.htmlPage());
                    i++;
                }
            }
        }
    }
}
