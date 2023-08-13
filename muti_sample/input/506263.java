public class ErrorBundle extends MessageBundle 
{
    public static final String SUMMARY_ENTRY = "summary";
    public static final String DETAIL_ENTRY = "details";
    public ErrorBundle(String resource, String id) throws NullPointerException
    {
        super(resource, id);
    }
    public ErrorBundle(String resource, String id, Object[] arguments) throws NullPointerException
    {
        super(resource, id, arguments);
    }
    public String getSummary(Locale loc, TimeZone timezone) throws MissingEntryException
    {
        return getEntry(SUMMARY_ENTRY,loc,timezone);
    }
    public String getSummary(Locale loc) throws MissingEntryException
    {
        return getEntry(SUMMARY_ENTRY,loc,TimeZone.getDefault());
    }
    public String getDetail(Locale loc, TimeZone timezone) throws MissingEntryException
    {
        return getEntry(DETAIL_ENTRY,loc,timezone);
    }
    public String getDetail(Locale loc) throws MissingEntryException
    {
        return getEntry(DETAIL_ENTRY,loc,TimeZone.getDefault());
    }
}
