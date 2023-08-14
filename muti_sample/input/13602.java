public class SlotTableStack
{
    private class SlotTablePool {
        private SlotTable[] pool;
        private final int  HIGH_WATER_MARK = 5;
        private int currentIndex;
        SlotTablePool( ) {
            pool = new SlotTable[HIGH_WATER_MARK];
            currentIndex = 0;
        }
        void putSlotTable( SlotTable table ) {
            if( currentIndex >= HIGH_WATER_MARK ) {
                return;
            }
            pool[currentIndex] = table;
            currentIndex++;
        }
        SlotTable getSlotTable( ) {
            if( currentIndex == 0 ) {
                return null;
            }
            currentIndex--;
            return pool[currentIndex];
        }
    }
    private java.util.List tableContainer;
    private int currentIndex;
    private SlotTablePool tablePool;
    private ORB orb;
    private InterceptorsSystemException wrapper ;
    SlotTableStack( ORB orb, SlotTable table ) {
       this.orb = orb;
       wrapper = InterceptorsSystemException.get( orb, CORBALogDomains.RPC_PROTOCOL ) ;
       currentIndex = 0;
       tableContainer = new java.util.ArrayList( );
       tablePool = new SlotTablePool( );
       tableContainer.add( currentIndex, table );
       currentIndex++;
    }
    void pushSlotTable( ) {
        SlotTable table = tablePool.getSlotTable( );
        if( table == null ) {
            SlotTable tableTemp = peekSlotTable();
            table = new SlotTable( orb, tableTemp.getSize( ));
        }
        if (currentIndex == tableContainer.size()) {
            tableContainer.add( currentIndex, table );
        } else if (currentIndex > tableContainer.size()) {
            throw wrapper.slotTableInvariant( new Integer( currentIndex ),
                new Integer( tableContainer.size() ) ) ;
        } else {
            tableContainer.set( currentIndex, table );
        }
        currentIndex++;
    }
    void  popSlotTable( ) {
        if( currentIndex <= 1 ) {
            throw wrapper.cantPopOnlyPicurrent() ;
        }
        currentIndex--;
        SlotTable table = (SlotTable)tableContainer.get( currentIndex );
        tableContainer.set( currentIndex, null ); 
        table.resetSlots( );
        tablePool.putSlotTable( table );
    }
    SlotTable peekSlotTable( ) {
       return (SlotTable) tableContainer.get( currentIndex - 1);
    }
}
