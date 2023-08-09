final class CoverageOptions implements Serializable
{
    public boolean excludeSyntheticMethods ()
    {
        return m_excludeSyntheticMethods;
    }
    public boolean excludeBridgeMethods ()
    {
        return m_excludeBridgeMethods;
    }
    public boolean doSUIDCompensation ()
    {
        return m_doSUIDCompensation;
    }
    CoverageOptions (final boolean excludeSyntheticMethods,
                     final boolean excludeBridgeMethods,
                     final boolean doSUIDCompensation)
    {
        m_excludeSyntheticMethods = excludeSyntheticMethods;
        m_excludeBridgeMethods = excludeBridgeMethods;
        m_doSUIDCompensation = doSUIDCompensation;
    }
    static CoverageOptions readExternal (final DataInput in)
        throws IOException
    {
        return new CoverageOptions (in.readBoolean (),
                                    in.readBoolean (),
                                    in.readBoolean ());
    }
    static void writeExternal (final CoverageOptions options, final DataOutput out)
        throws IOException
    {
        out.writeBoolean (options.m_excludeSyntheticMethods);
        out.writeBoolean (options.m_excludeBridgeMethods);
        out.writeBoolean (options.m_doSUIDCompensation);
    }
    private final boolean m_excludeSyntheticMethods;
    private final boolean m_excludeBridgeMethods;
    private final boolean m_doSUIDCompensation;
} 
