public class ProcessMonitorThread extends java.lang.Thread {
    private HashMap serverTable;
    private int sleepTime;
    private static ProcessMonitorThread instance = null;
    private ProcessMonitorThread( HashMap ServerTable, int SleepTime ) {
        serverTable = ServerTable;
        sleepTime = SleepTime;
    }
    public void run( ) {
        while( true ) {
            try {
                Thread.sleep( sleepTime );
            } catch( java.lang.InterruptedException e ) {
                break;
            }
            Iterator serverList;
            synchronized ( serverTable ) {
                serverList = serverTable.values().iterator();
            }
            try {
                checkServerHealth( serverList );
            } catch( ConcurrentModificationException e ) {
                break;
            }
        }
    }
    private void checkServerHealth( Iterator serverList ) {
        if( serverList == null ) return;
        while (serverList.hasNext( ) ) {
            ServerTableEntry entry = (ServerTableEntry) serverList.next();
            entry.checkProcessHealth( );
        }
    }
    static void start( HashMap serverTable ) {
        int sleepTime = ORBConstants.DEFAULT_SERVER_POLLING_TIME;
        String pollingTime = System.getProperties().getProperty(
            ORBConstants.SERVER_POLLING_TIME );
        if ( pollingTime != null ) {
            try {
                sleepTime = Integer.parseInt( pollingTime );
            } catch (Exception e ) {
            }
        }
        instance = new ProcessMonitorThread( serverTable,
            sleepTime );
        instance.setDaemon( true );
        instance.start();
    }
    static void interruptThread( ) {
        instance.interrupt();
    }
}
