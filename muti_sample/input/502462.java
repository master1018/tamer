public class ParentDataEntryWriter implements DataEntryWriter
{
    private DataEntryWriter dataEntryWriter;
    public ParentDataEntryWriter(DataEntryWriter dataEntryWriter)
    {
        this.dataEntryWriter = dataEntryWriter;
    }
    public boolean createDirectory(DataEntry dataEntry) throws IOException
    {
        return getOutputStream(dataEntry) != null;
    }
    public OutputStream getOutputStream(DataEntry dataEntry) throws IOException
    {
        return getOutputStream(dataEntry, null);
    }
    public OutputStream getOutputStream(DataEntry dataEntry,
                                        Finisher  finisher) throws IOException
    {
        return dataEntryWriter.getOutputStream(dataEntry.getParent(),
                                               finisher);
    }
    public void close() throws IOException
    {
        dataEntryWriter.close();
        dataEntryWriter = null;
    }
}
