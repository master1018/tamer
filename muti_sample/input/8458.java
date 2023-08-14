public abstract class
ASimpleInstrumentationTestCase
    extends AInstrumentationTestCase
{
    public ASimpleInstrumentationTestCase(String name)
    {
        super(name);
    }
    protected void
    assertClassArrayContainsClass(Class[] list, Class target)
        {
        boolean inList = false;
        for ( int x = 0; x < list.length; x++ )
            {
            if ( list[x] == target )
                {
                inList = true;
                }
            }
        assertTrue(inList);
        }
    protected void
    assertClassArrayDoesNotContainClassByName(Class[] list, String name)
        {
        boolean inList = false;
        for ( int x = 0; x < list.length; x++ )
            {
            if ( list[x].getName().equals(name) )
                {
                fail();
                }
            }
        }
}
