public class T_t482_10_1 {
    public void run(){
        int arr[];
        arr = new int[2];
        int f = 1;
        try{
            arr = new int[3];
            if(f == 1)
                throw new Exception();
        }catch(Exception e){
            f = arr[0];
        }
        finally {
            f = 1;
        }
    }
}
