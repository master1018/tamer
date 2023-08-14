public class IllegalConstructorArgs {
      public static void main(String argv[]) {
          int testSucceeded=0;
        try{
           Vector bad1 = new Vector(-100, 10);
        }
        catch (IllegalArgumentException e1) {
            testSucceeded =1;
        }
        catch (NegativeArraySizeException e2) {
            testSucceeded =0;
        }
        if(testSucceeded == 0)
             throw new RuntimeException("Wrong exception thrown.");
     }
}
