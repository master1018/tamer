public class RegionMatches {
  public static void main (String args[]) throws Exception {
      String s1="abc";
      String s2="def";
      if (!s1.regionMatches(0,s2,0,Integer.MIN_VALUE))
          throw new RuntimeException("Integer overflow in RegionMatches");
  }
}
