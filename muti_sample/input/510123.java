public class LocalizedMessage 
{
    protected final String id;
    protected final String resource;
    protected Object[] arguments;
    protected Object[] filteredArguments;
    protected Filter filter = null;
    public LocalizedMessage(String resource,String id) throws NullPointerException
    {
        if (resource == null || id == null)
        {
            throw new NullPointerException();
        }
        this.id = id;
        this.resource = resource;
        this.arguments = new Object[0];
        this.filteredArguments = arguments;
    }
    public LocalizedMessage(String resource, String id, Object[] arguments) throws NullPointerException
    {
        if (resource == null || id == null || arguments == null)
        {
            throw new NullPointerException();
        }
        this.id = id;
        this.resource = resource;
        this.arguments = arguments;
        this.filteredArguments = arguments;
    }
    public String getEntry(String key,Locale loc, TimeZone timezone) throws MissingEntryException
    {
        String entry = id + "." + key;
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle(resource,loc);
            String template = bundle.getString(entry);
            if (arguments == null || arguments.length == 0)
            {
                return template;
            }
            else
            {
                return formatWithTimeZone(template,filteredArguments,loc,timezone);
            }
        }
        catch (MissingResourceException mre)
        {
            throw new MissingEntryException("Can't find entry " + entry + " in resource file " + resource + ".",
                    resource,
                    entry); 
        }
    }
    protected String formatWithTimeZone(
            String template,
            Object[] arguments, 
            Locale locale,
            TimeZone timezone) 
    {
        MessageFormat mf = new MessageFormat(" ");
        mf.setLocale(locale);
        mf.applyPattern(template);
        if (!timezone.equals(TimeZone.getDefault())) 
        {
            Format[] formats = mf.getFormats();
            for (int i = 0; i < formats.length; i++) 
            {
                if (formats[i] instanceof DateFormat) 
                {
                    DateFormat temp = (DateFormat) formats[i];
                    temp.setTimeZone(timezone);
                    mf.setFormat(i,temp);
                }
            }
        }
        return mf.format(arguments);
    }
    public void setFilter(Filter filter)
    {
        if (filter == null)
        {
            filteredArguments = arguments;
        }
        else if (!filter.equals(this.filter))
        {
            filteredArguments = new Object[arguments.length];
            for (int i = 0; i < arguments.length; i++)
            {
                if (arguments[i] instanceof UntrustedInput) 
                {
                    filteredArguments[i] = filter.doFilter(((UntrustedInput) arguments[i]).getString());
                }
                else
                {
                    filteredArguments[i] = arguments[i];
                }
            }
        }
        this.filter = filter;
    }
    public Filter getFilter()
    {
        return filter;
    }
    public String getId()
    {
        return id;
    }
    public String getResource()
    {
        return resource;
    }
    public Object[] getArguments()
    {
        return arguments;
    }
}
