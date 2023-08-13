public class TLRemoveTest {
    private static final int INITIAL_VALUE = 101;
    private static final int REMOVE_SET_VALUE = 102;
    static ThreadLocal<Integer> n = new ThreadLocal<Integer>() {
        protected synchronized Integer initialValue() {
            return INITIAL_VALUE;
        }
    };
    public static void main(String args[]) throws Throwable {
        int threadCount = 100;
        final int[] removeNode = {10,20,45,38};
        final int[] removeAndSet = {12,34,10};
        Thread th[] = new Thread[threadCount];
        final int x[] = new int[threadCount];
        final Throwable exceptions[] = new Throwable[threadCount];
        for(int i = 0; i<threadCount; i++) {
            final int threadId = i;
            th[i] = new Thread() {
                public void run() {
                    try{
                        n.set(threadId); 
                        for (int j = 0; j<threadId; j++)
                            Thread.currentThread().yield();
                        for(int removeId  : removeNode)
                            if(threadId == removeId){
                               n.remove(); 
                               break;
                            }
                        for(int removeId  : removeAndSet)
                            if(threadId == removeId){
                               n.remove(); 
                               n.set(REMOVE_SET_VALUE); 
                               break;
                            }
                        x[threadId] = n.get();
                    }
                    catch(Throwable ex){
                        exceptions[threadId] = ex;
                    }
                }
            };
            th[i].start();
        }
        for(int i = 0; i<threadCount; i++)
            th[i].join();
        for(int i = 0; i<threadCount; i++){
            int checkValue = i;
            for(int removeId : removeNode)
                if(removeId == i){
                    checkValue = INITIAL_VALUE;
                    break;
                }
            for(int removeId : removeAndSet)
                if(removeId == i){
                    checkValue = REMOVE_SET_VALUE;
                    break;
                }
            if(exceptions[i] != null)
                throw(exceptions[i]);
            if(x[i] != checkValue)
                throw(new Throwable("x[" + i + "] =" + x[i]));
        }
    }
}
