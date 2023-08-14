public class CascadingDataEntryWriter implements DataEntryWriter
{
    private DataEntryWriter dataEntryWriter1;
    private DataEntryWriter dataEntryWriter2;
    public CascadingDataEntryWriter(DataEntryWriter dataEntryWriter1,
                                    DataEntryWriter dataEntryWriter2)
    {
        this.dataEntryWriter1 = dataEntryWriter1;
        this.dataEntryWriter2 = dataEntryWriter2;
    }
    public boolean createDirectory(DataEntry dataEntry) throws IOException
    {
        return dataEntryWriter1.createDirectory(dataEntry) ||
               dataEntryWriter2.createDirectory(dataEntry);
    }
    public OutputStream getOutputStream(DataEntry dataEntry) throws IOException
    {
        return getOutputStream(dataEntry,  null);
    }
    public OutputStream getOutputStream(DataEntry dataEntry,
                                        Finisher  finisher) throws IOException
    {
        OutputStream outputStream =
            dataEntryWriter1.getOutputStream(dataEntry, finisher);
        return outputStream != null ?
            outputStream :
            dataEntryWriter2.getOutputStream(dataEntry, finisher);
    }
    public void close() throws IOException
    {
        dataEntryWriter1.close();
        dataEntryWriter2.close();
        dataEntryWriter1 = null;
        dataEntryWriter2 = null;
    }
}
