final class SessionData implements ISessionData, Serializable 
{
    public IMetaData getMetaData ()
    {
        return m_mdata;
    }
    public ICoverageData getCoverageData ()
    {
        return m_cdata;
    }
    public SessionData (final IMetaData mdata, final ICoverageData cdata)
    {
        if (mdata == null) throw new IllegalArgumentException ("null input: mdata");
        if (cdata == null) throw new IllegalArgumentException ("null input: cdata");
        m_mdata = mdata;
        m_cdata = cdata;
    }
    private final IMetaData m_mdata;
    private final ICoverageData m_cdata;
} 
