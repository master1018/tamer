public class TextBundle extends LocalizedMessage 
{
    public static final String TEXT_ENTRY = "text";
    public TextBundle(String resource, String id) throws NullPointerException 
    {
        super(resource, id);
    }
    public TextBundle(String resource, String id, Object[] arguments) throws NullPointerException 
    {
        super(resource, id, arguments);
    }
    public String getText(Locale loc, TimeZone timezone) throws MissingEntryException
    {
        return getEntry(TEXT_ENTRY,loc,timezone);
    }
    public String getText(Locale loc) throws MissingEntryException
    {
        return getEntry(TEXT_ENTRY,loc,TimeZone.getDefault());
    }
}
