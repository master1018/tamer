public final class TransientObjectManager {
    private ORB orb ;
    private int maxSize = 128;
    private Element[] elementArray;
    private Element freeList;
    void dprint( String msg ) {
        ORBUtility.dprint( this, msg ) ;
    }
    public TransientObjectManager( ORB orb )
    {
        this.orb = orb ;
        elementArray = new Element[maxSize];
        elementArray[maxSize-1] = new Element(maxSize-1,null);
        for ( int i=maxSize-2; i>=0; i-- )
            elementArray[i] = new Element(i,elementArray[i+1]);
        freeList = elementArray[0];
    }
    public synchronized byte[] storeServant(java.lang.Object servant, java.lang.Object servantData)
    {
        if ( freeList == null )
            doubleSize();
        Element elem = freeList;
        freeList = (Element)freeList.servant;
        byte[] result = elem.getKey(servant, servantData);
        if (orb.transientObjectManagerDebugFlag)
            dprint( "storeServant returns key for element " + elem ) ;
        return result ;
    }
    public synchronized java.lang.Object lookupServant(byte transientKey[])
    {
        int index = ORBUtility.bytesToInt(transientKey,0);
        int counter = ORBUtility.bytesToInt(transientKey,4);
        if (orb.transientObjectManagerDebugFlag)
            dprint( "lookupServant called with index=" + index + ", counter=" + counter ) ;
        if (elementArray[index].counter == counter &&
            elementArray[index].valid ) {
            if (orb.transientObjectManagerDebugFlag)
                dprint( "\tcounter is valid" ) ;
            return elementArray[index].servant;
        }
        if (orb.transientObjectManagerDebugFlag)
            dprint( "\tcounter is invalid" ) ;
        return null;
    }
    public synchronized java.lang.Object lookupServantData(byte transientKey[])
    {
        int index = ORBUtility.bytesToInt(transientKey,0);
        int counter = ORBUtility.bytesToInt(transientKey,4);
        if (orb.transientObjectManagerDebugFlag)
            dprint( "lookupServantData called with index=" + index + ", counter=" + counter ) ;
        if (elementArray[index].counter == counter &&
            elementArray[index].valid ) {
            if (orb.transientObjectManagerDebugFlag)
                dprint( "\tcounter is valid" ) ;
            return elementArray[index].servantData;
        }
        if (orb.transientObjectManagerDebugFlag)
            dprint( "\tcounter is invalid" ) ;
        return null;
    }
    public synchronized void deleteServant(byte transientKey[])
    {
        int index = ORBUtility.bytesToInt(transientKey,0);
        if (orb.transientObjectManagerDebugFlag)
            dprint( "deleting servant at index=" + index ) ;
        elementArray[index].delete(freeList);
        freeList = elementArray[index];
    }
    public synchronized byte[] getKey(java.lang.Object servant)
    {
        for ( int i=0; i<maxSize; i++ )
            if ( elementArray[i].valid &&
                 elementArray[i].servant == servant )
                return elementArray[i].toBytes();
        return null;
    }
    private void doubleSize()
    {
        Element old[] = elementArray;
        int oldSize = maxSize;
        maxSize *= 2;
        elementArray = new Element[maxSize];
        for ( int i=0; i<oldSize; i++ )
            elementArray[i] = old[i];
        elementArray[maxSize-1] = new Element(maxSize-1,null);
        for ( int i=maxSize-2; i>=oldSize; i-- )
            elementArray[i] = new Element(i,elementArray[i+1]);
        freeList = elementArray[oldSize];
    }
}
final class Element {
    java.lang.Object servant=null;     
    java.lang.Object servantData=null;
    int index=-1;
    int counter=0;
    boolean valid=false; 
    Element(int i, java.lang.Object next)
    {
        servant = next;
        index = i;
    }
    byte[] getKey(java.lang.Object servant, java.lang.Object servantData)
    {
        this.servant = servant;
        this.servantData = servantData;
        this.valid = true;
        return toBytes();
    }
    byte[] toBytes()
    {
        byte key[] = new byte[8];
        ORBUtility.intToBytes(index, key, 0);
        ORBUtility.intToBytes(counter, key, 4);
        return key;
    }
    void delete(Element freeList)
    {
        if ( !valid )    
            return;
        counter++;
        servantData = null;
        valid = false;
        servant = freeList;
    }
    public String toString()
    {
        return "Element[" + index + ", " + counter + "]" ;
    }
}
