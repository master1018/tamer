public class SyncUtil {
    private SyncUtil() {}
    public static void acquire( Sync sync )
    {
        boolean held = false ;
        while (!held) {
            try {
                sync.acquire() ;
                held = true ;
            } catch (InterruptedException exc) {
                held = false ;
            }
        }
    }
}
