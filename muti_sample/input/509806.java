public class FilteredDataEntryReader implements DataEntryReader
{
    private final DataEntryFilter dataEntryFilter;
    private final DataEntryReader acceptedDataEntryReader;
    private final DataEntryReader rejectedDataEntryReader;
    public FilteredDataEntryReader(DataEntryFilter dataEntryFilter,
                                   DataEntryReader acceptedDataEntryReader)
    {
        this(dataEntryFilter, acceptedDataEntryReader, null);
    }
    public FilteredDataEntryReader(DataEntryFilter dataEntryFilter,
                                   DataEntryReader acceptedDataEntryReader,
                                   DataEntryReader rejectedDataEntryReader)
    {
        this.dataEntryFilter         = dataEntryFilter;
        this.acceptedDataEntryReader = acceptedDataEntryReader;
        this.rejectedDataEntryReader = rejectedDataEntryReader;
    }
    public void read(DataEntry dataEntry)
    throws IOException
    {
        DataEntryReader dataEntryReader = dataEntryFilter.accepts(dataEntry) ?
            acceptedDataEntryReader :
            rejectedDataEntryReader;
        if (dataEntryReader != null)
        {
            dataEntryReader.read(dataEntry);
        }
    }
}
