public class DataEntryNameFilter
implements   DataEntryFilter
{
    private final StringMatcher stringMatcher;
    public DataEntryNameFilter(StringMatcher stringMatcher)
    {
        this.stringMatcher = stringMatcher;
    }
    public boolean accepts(DataEntry dataEntry)
    {
        return dataEntry != null && stringMatcher.matches(dataEntry.getName());
    }
}
