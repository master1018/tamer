public class ObjectAdapterIdArray extends ObjectAdapterIdBase {
    private final String[] objectAdapterId ;
    public ObjectAdapterIdArray( String[] objectAdapterId )
    {
        this.objectAdapterId = objectAdapterId ;
    }
    public ObjectAdapterIdArray( String name1, String name2 )
    {
        objectAdapterId = new String[2] ;
        objectAdapterId[0] = name1 ;
        objectAdapterId[1] = name2 ;
    }
    public int getNumLevels()
    {
        return objectAdapterId.length ;
    }
    public Iterator iterator()
    {
        return Arrays.asList( objectAdapterId ).iterator() ;
    }
    public String[] getAdapterName()
    {
        return (String[])(objectAdapterId.clone()) ;
    }
}
