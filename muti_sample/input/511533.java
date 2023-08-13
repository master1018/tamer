public class DataEntryDirectoryFilter
implements   DataEntryFilter
{
    public boolean accepts(DataEntry dataEntry)
    {
        return dataEntry != null && dataEntry.isDirectory();
    }
}