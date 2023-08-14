public class Test7005594 {
      static int test(byte a[]){
          int result=0;
          for( int i=0; i<a.length; i+=((0x7fffffff>>1)+1) ){
              result += a[i];
          }
          return result;
      }
      public static void main(String [] args){
          byte a[]=new byte[(0x7fffffff>>1)+2];
          int result = 0;
          try {
              result = test(a);
          } catch (ArrayIndexOutOfBoundsException e) {
              e.printStackTrace(System.out);
              System.out.println("Passed");
              System.exit(95);
          }
          System.out.println(result);
          System.out.println("FAILED");
          System.exit(97);
      }
}
