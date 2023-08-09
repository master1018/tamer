public class ITLRemoveTest {
    private static final int INITIAL_VALUE = Integer.MIN_VALUE;
    private static final int REMOVE_SET_VALUE = Integer.MAX_VALUE;
    static InheritableThreadLocal<Integer> n = new InheritableThreadLocal<Integer>() {
        protected Integer initialValue() {
            return INITIAL_VALUE;
        }
        protected Integer childValue(Integer parentValue) {
            return(parentValue + 1);
        }
    };
    static int threadCount = 100;
    static int x[];
    static Throwable exceptions[];
    static final int[] removeNode = {10,20,45,38,88};
    static final int[] removeAndSet = {12,34,10};
    public static void main(String args[]) throws Throwable {
        x = new int[threadCount];
        exceptions = new Throwable[threadCount];
        Thread progenitor = new MyThread();
        progenitor.start();
        progenitor.join();
        for(int i = 0; i<threadCount; i++){
            int checkValue = i+INITIAL_VALUE;
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
    private static class MyThread extends Thread {
        public void run() {
            Thread child = null;
            int threadId=0;
            try{
                threadId = n.get();
                if (threadId < (threadCount-1+INITIAL_VALUE)) {
                    child = new MyThread();
                    child.start();
                }
                for (int j = 0; j<threadId; j++)
                    Thread.currentThread().yield();
                for(int removeId  : removeNode)
                   if((threadId-INITIAL_VALUE) == removeId){
                       n.remove();
                       break;
                   }
                 for(int removeId  : removeAndSet)
                    if((threadId-INITIAL_VALUE) == removeId){
                        n.remove();
                        n.set(REMOVE_SET_VALUE);
                        break;
                    }
                x[threadId-INITIAL_VALUE] =  n.get();
            }catch(Throwable ex){
                exceptions[threadId-INITIAL_VALUE] = ex;
            }
            if (child != null) {
                try {
                     child.join();
                } catch(InterruptedException e) {
                     throw(new RuntimeException("Interrupted"));
                }
            }
        }
    }
}
