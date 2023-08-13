public class PICurrent extends org.omg.CORBA.LocalObject
    implements Current
{
    private int slotCounter;
    private ORB myORB;
    private OMGSystemException wrapper ;
    private boolean orbInitializing;
    private ThreadLocal threadLocalSlotTable
        = new ThreadLocal( ) {
            protected Object initialValue( ) {
                SlotTable table = new SlotTable( myORB, slotCounter );
                return new SlotTableStack( myORB, table );
            }
        };
    PICurrent( ORB myORB ) {
        this.myORB = myORB;
        wrapper = OMGSystemException.get( myORB,
            CORBALogDomains.RPC_PROTOCOL ) ;
        this.orbInitializing = true;
        slotCounter = 0;
    }
    int allocateSlotId( ) {
        int slotId = slotCounter;
        slotCounter = slotCounter + 1;
        return slotId;
    }
    SlotTable getSlotTable( ) {
        SlotTable table = (SlotTable)
                ((SlotTableStack)threadLocalSlotTable.get()).peekSlotTable();
        return table;
    }
    void pushSlotTable( ) {
        SlotTableStack st = (SlotTableStack)threadLocalSlotTable.get();
        st.pushSlotTable( );
    }
    void popSlotTable( ) {
        SlotTableStack st = (SlotTableStack)threadLocalSlotTable.get();
        st.popSlotTable( );
    }
    public void set_slot( int id, Any data ) throws InvalidSlot
    {
        if( orbInitializing ) {
            throw wrapper.invalidPiCall3() ;
        }
        getSlotTable().set_slot( id, data );
    }
    public Any get_slot( int id ) throws InvalidSlot
    {
        if( orbInitializing ) {
            throw wrapper.invalidPiCall4() ;
        }
        return getSlotTable().get_slot( id );
    }
    void resetSlotTable( ) {
        getSlotTable().resetSlots();
    }
    void setORBInitializing( boolean init ) {
        this.orbInitializing = init;
    }
}
