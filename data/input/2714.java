public class SlotTable {
    private Any[] theSlotData;
    private ORB orb;
    private boolean dirtyFlag;
    SlotTable( ORB orb, int slotSize ) {
        dirtyFlag = false;
        this.orb = orb;
        theSlotData = new Any[slotSize];
    }
    public void set_slot( int id, Any data ) throws InvalidSlot
    {
        if( id >= theSlotData.length ) {
            throw new InvalidSlot();
        }
        dirtyFlag = true;
        theSlotData[id] = data;
    }
    public Any get_slot( int id ) throws InvalidSlot
    {
        if( id >= theSlotData.length ) {
            throw new InvalidSlot();
        }
        if( theSlotData[id] == null ) {
            theSlotData [id] = new AnyImpl(orb);
        }
        return theSlotData[ id ];
    }
    void resetSlots( ) {
        if( dirtyFlag == true ) {
            for( int i = 0; i < theSlotData.length; i++ ) {
                theSlotData[i] = null;
            }
        }
    }
    int getSize( ) {
        return theSlotData.length;
    }
}
