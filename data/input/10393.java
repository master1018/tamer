public final class ObjectUtility {
    private ObjectUtility() {}
    public static Object concatenateArrays( Object arr1, Object arr2 )
    {
        Class comp1 = arr1.getClass().getComponentType() ;
        Class comp2 = arr2.getClass().getComponentType() ;
        int len1 = Array.getLength( arr1 ) ;
        int len2 = Array.getLength( arr2 ) ;
        if ((comp1 == null) || (comp2 == null))
            throw new IllegalStateException( "Arguments must be arrays" ) ;
        if (!comp1.equals( comp2 ))
            throw new IllegalStateException(
                "Arguments must be arrays with the same component type" ) ;
        Object result = Array.newInstance( comp1, len1 + len2 ) ;
        int index = 0 ;
        for (int ctr=0; ctr<len1; ctr++)
            Array.set( result, index++, Array.get( arr1, ctr ) ) ;
        for (int ctr=0; ctr<len2; ctr++)
            Array.set( result, index++, Array.get( arr2, ctr ) ) ;
        return result ;
    }
}
