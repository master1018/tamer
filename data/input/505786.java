public class T_monitorenter_2 {
     private int flg = 0;
     public boolean result = true;
     public void run(int v) throws InterruptedException  {
         synchronized(this) {
             synchronized(this) {
                 flg = v;
             }
             Thread.sleep(500);
             if(flg != v) {
                 result = false;
             }
         }
     }
}
