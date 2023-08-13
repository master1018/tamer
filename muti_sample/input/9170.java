public class NullServantImpl implements NullServant
{
    private SystemException sysex ;
    public NullServantImpl( SystemException ex )
    {
        this.sysex = ex ;
    }
    public SystemException getException()
    {
        return sysex ;
    }
}
