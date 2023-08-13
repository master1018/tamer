public class FilteredDataEntryWriter implements DataEntryWriter
{
    private final DataEntryFilter dataEntryFilter;
    private DataEntryWriter acceptedDataEntryWriter;
    private DataEntryWriter rejectedDataEntryWriter;
    public FilteredDataEntryWriter(DataEntryFilter dataEntryFilter,
                                   DataEntryWriter acceptedDataEntryWriter)
    {
        this(dataEntryFilter, acceptedDataEntryWriter, null);
    }
    public FilteredDataEntryWriter(DataEntryFilter dataEntryFilter,
                                   DataEntryWriter acceptedDataEntryWriter,
                                   DataEntryWriter rejectedDataEntryWriter)
    {
        this.dataEntryFilter         = dataEntryFilter;
        this.acceptedDataEntryWriter = acceptedDataEntryWriter;
        this.rejectedDataEntryWriter = rejectedDataEntryWriter;
    }
    public boolean createDirectory(DataEntry dataEntry) throws IOException
    {
        DataEntryWriter dataEntryWriter = dataEntryFilter.accepts(dataEntry) ?
            acceptedDataEntryWriter :
            rejectedDataEntryWriter;
        return dataEntryWriter != null &&
               dataEntryWriter.createDirectory(dataEntry);
    }
    public OutputStream getOutputStream(DataEntry dataEntry) throws IOException
    {
        return getOutputStream(dataEntry,  null);
    }
    public OutputStream getOutputStream(DataEntry dataEntry,
                                        Finisher  finisher) throws IOException
    {
        DataEntryWriter dataEntryWriter = dataEntryFilter.accepts(dataEntry) ?
            acceptedDataEntryWriter :
            rejectedDataEntryWriter;
        return dataEntryWriter != null ?
            dataEntryWriter.getOutputStream(dataEntry, finisher) :
            null;
    }
    public void close() throws IOException
    {
        if (acceptedDataEntryWriter != null)
        {
            acceptedDataEntryWriter.close();
            acceptedDataEntryWriter = null;
        }
        if (rejectedDataEntryWriter != null)
        {
            rejectedDataEntryWriter.close();
            rejectedDataEntryWriter = null;
        }
    }
}
