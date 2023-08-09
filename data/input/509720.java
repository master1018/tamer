public class RenamedDataEntry implements DataEntry
{
    private final DataEntry dataEntry;
    private final String    name;
    public RenamedDataEntry(DataEntry dataEntry,
                            String    name)
    {
        this.dataEntry = dataEntry;
        this.name      = name;
    }
    public String getName()
    {
        return name;
    }
    public boolean isDirectory()
    {
        return dataEntry.isDirectory();
    }
    public InputStream getInputStream() throws IOException
    {
        return dataEntry.getInputStream();
    }
    public void closeInputStream() throws IOException
    {
        dataEntry.closeInputStream();
    }
    public DataEntry getParent()
    {
        return dataEntry.getParent();
    }
    public String toString()
    {
        return name + " == " + dataEntry;
    }
}
