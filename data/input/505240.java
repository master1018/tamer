class KeywordEntry implements Comparable
{
    KeywordEntry(String label, String href, String comment)
    {
        this.label = label;
        this.href = href;
        this.comment = comment;
    }
    public void makeHDF(HDF data, String base)
    {
        data.setValue(base + ".label", this.label);
        data.setValue(base + ".href", this.href);
        data.setValue(base + ".comment", this.comment);
    }
    public char firstChar()
    {
        return Character.toUpperCase(this.label.charAt(0));
    }
    public int compareTo(Object that)
    {
        return this.label.compareToIgnoreCase(((KeywordEntry)that).label);
    }
    private String label;
    private String href;
    private String comment;
}
