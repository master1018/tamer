public class DataEntryParentFilter
implements   DataEntryFilter
{
    private final DataEntryFilter dataEntryFilter;
    public DataEntryParentFilter(DataEntryFilter dataEntryFilter)
    {
        this.dataEntryFilter = dataEntryFilter;
    }
    public boolean accepts(DataEntry dataEntry)
    {
        return dataEntry != null && dataEntryFilter.accepts(dataEntry.getParent());
    }
}
