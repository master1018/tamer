public class Test5091921 {
  private static int result = 0;
  public static int test_ge1(int limit) {
    int indx;
    int sum = 0;
    for (indx = 500; indx >= limit; indx -= 2) {
      sum += 2000 / indx;
      result = sum;
    }
    return sum;
  }
  public static int test_le1(int limit) {
    int indx;
    int sum = 0;
    for (indx = -500; indx <= limit; indx += 2)
    {
      sum += 3000 / indx;
      result = sum;
    }
    return sum;
  }
  public static volatile int c = 1;
  public static int test_wrap1(int limit)
  {
    int indx;
    int sum = 0;
    for (indx = 0xffffffff; indx < limit; indx += 0x20000000)
    {
      sum += c;
    }
    return sum;
  }
  static int[] box5 = {1,2,3,4,5,6,7,8,9};
  public static int test_rce5(int[] b, int limit)
  {
    int indx;
    int sum = b[1];
    result = sum;
    for (indx = 0x80000000; indx < limit; ++indx)
    {
      if (indx > 0x80000000)
      {
        if (indx - 9 < -9)
        {
          sum += indx;
          result = sum;
          sum ^= b[indx & 7];
          result = sum;
        }
        else
          break;
      }
      else
      {
        sum += b[indx & 3];
        result = sum;
      }
    }
    return sum;
  }
  static int[] box6 = {1,2,3,4,5,6,7,8,9};
  public static int test_rce6(int[] b, int limit)
  {
    int indx;
    int sum = b[1];
    result = sum;
    for (indx = 0x80000000; indx < limit; ++indx)
    {
      if (indx > 0x80000000)
      {
        if (indx < 0)
        {
          sum += result;
          result = sum;
        }
        else
          break;
        if (indx * 28 + 1 < 0)
        {
          sum += indx;
          result = sum;
          sum ^= b[indx & 7];
          result = sum;
        }
        else
          break;
      }
      else
      {
        sum += b[indx & 3];
        result = sum;
      }
    }
    return sum;
  }
  static int[] box7 = {1,2,3,4,5,6,7,8,9,0x7fffffff};
  public static int test_rce7(int[] b)
  {
    int indx;
    int max = b[9];
    int sum = b[7];
    result = sum;
    for (indx = 0; indx < b.length; ++indx)
    {
      if (indx <= max)
      {
        sum += (indx ^ 15) + ((result != 0) ? 0 : sum);
        result = sum;
      }
      else
        throw new RuntimeException();
    }
    for (indx = -7; indx < b.length; ++indx)
    {
      if (indx <= 9)
      {
        sum += (sum ^ 15) + ((result != 0) ? 0 : sum);
        result = sum;
      }
      else
        throw new RuntimeException();
    }
    return sum;
  }
  static int[] box8 = {-1,0,1,2,3,4,5,6,7,8,0x80000000};
  public static int test_rce8(int[] b)
  {
    int indx;
    int sum = b[5];
    int min = b[10];
    result = sum;
    for (indx = b.length-1; indx >= 0; --indx)
    {
      if (indx >= min)
      {
        sum += (sum ^ 9) + ((result != 0) ? 0 :sum);
        result = sum;
      }
      else
        throw new RuntimeException();
    }
    return sum;
  }
  public static void main(String[] args)
  {
    result=1;
    int r = 0;
    try {
      r = test_ge1(0x80000000);
      System.out.println(result);
      System.out.println("test_ge1 FAILED");
      System.exit(1);
    }
    catch (ArithmeticException e1) {
      System.out.println("test_ge1: Expected exception caught");
      if (result != 5986) {
        System.out.println(result);
        System.out.println("test_ge1 FAILED");
        System.exit(97);
      }
    }
    System.out.println("test_ge1 WORKED");
    result=0;
    try
    {
      r = test_le1(0x7fffffff);
      System.out.println(result);
      System.out.println("test_le1 FAILED");
      System.exit(1);
    }
    catch (ArithmeticException e1)
    {
      System.out.println("test_le1: Expected exception caught");
      if (result != -9039)
      {
        System.out.println(result);
        System.out.println("test_le1 FAILED");
        System.exit(97);
      }
    }
    System.out.println("test_le1 WORKED");
    result=0;
    r = test_wrap1(0x7fffffff);
    if (r != 4)
    {
      System.out.println(result);
      System.out.println("test_wrap1 FAILED");
      System.exit(97);
    }
    else
    {
      System.out.println("test_wrap1 WORKED");
    }
    result=0;
    r = test_rce5(box5,0x80000100);
    if (result != 3)
    {
      System.out.println(result);
      System.out.println("test_rce5 FAILED");
      System.exit(97);
    }
    else
    {
      System.out.println("test_rce5 WORKED");
    }
    result=0;
    r = test_rce6(box6,0x80000100);
    if (result != 6)
    {
      System.out.println(result);
      System.out.println("test_rce6 FAILED");
      System.exit(97);
    }
    else
    {
      System.out.println("test_rce6 WORKED");
    }
    result=0;
    r = test_rce7(box7);
    if (result != 14680079)
    {
      System.out.println(result);
      System.out.println("test_rce7 FAILED");
      System.exit(97);
    }
    else
    {
      System.out.println("test_rce7 WORKED");
    }
    result=0;
    r = test_rce8(box8);
    if (result != 16393)
    {
      System.out.println(result);
      System.out.println("test_rce8 FAILED");
      System.exit(97);
    }
    else
    {
      System.out.println("test_rce8 WORKED");
    }
  }
}
