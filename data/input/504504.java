public class SeeTagInfo extends TagInfo
{
    private ContainerInfo mBase;
    LinkReference mLink;
    SeeTagInfo(String name, String kind, String text, ContainerInfo base,
            SourcePositionInfo position)
    {
        super(name, kind, text, position);
        mBase = base;
    }
    protected LinkReference linkReference() {
        if (mLink == null) {
            mLink = LinkReference.parse(text(), mBase, position(),
                           (!"@see".equals(name())) && (mBase != null ? mBase.checkLevel() : true));
        }
        return mLink;
    }
    public String label()
    {
        return linkReference().label;
    }
    @Override
    public void makeHDF(HDF data, String base)
    {
        LinkReference linkRef = linkReference();
        if (linkRef.kind != null) {
            setKind(linkRef.kind);
        }
        super.makeHDF(data, base);
        data.setValue(base + ".label", linkRef.label);
        if (linkRef.href != null) {
            data.setValue(base + ".href", linkRef.href);
        }
    }
    public boolean checkLevel() {
        return linkReference().checkLevel();
    }
    public static void makeHDF(HDF data, String base, SeeTagInfo[] tags)
    {
        int j=0;
        for (SeeTagInfo tag: tags) {
            if (tag.mBase.checkLevel() && tag.checkLevel()) {
                tag.makeHDF(data, base + "." + j);
                j++;
            }
        }
    }
}
