public class IllegalLoadFactor {
      public static void main(String argv[]) throws Exception {
          boolean testSucceeded = false;
          try{
              Hashtable bad1 = new Hashtable(100, -3);
          }
          catch (IllegalArgumentException e1) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("Hashtable, negative load factor");
          testSucceeded = false;
          try{
              Hashtable bad1 = new Hashtable(100, Float.NaN);
          }
          catch (IllegalArgumentException e1) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("Hashtable, NaN load factor");
          testSucceeded = false;
          try{
              HashMap bad1 = new HashMap(100, -3);
          }
          catch (IllegalArgumentException e1) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("HashMap, negative load factor");
          testSucceeded = false;
          try{
              HashMap bad1 = new HashMap(100, Float.NaN);
          }
          catch (IllegalArgumentException e1) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("HashMap, NaN load factor");
          testSucceeded = false;
          try{
              HashSet bad1 = new HashSet(100, -3);
          }
          catch (IllegalArgumentException e1) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("HashSet, negative load factor");
          testSucceeded = false;
          try{
              HashSet bad1 = new HashSet(100, Float.NaN);
          }
          catch (IllegalArgumentException e1) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("HashSet, NaN load factor");
          testSucceeded = false;
          try{
              WeakHashMap bad1 = new WeakHashMap(100, -3);
          }
          catch (IllegalArgumentException e1) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("WeakHashMap, negative load factor");
          testSucceeded = false;
          try{
              WeakHashMap bad1 = new WeakHashMap(100, Float.NaN);
          }
          catch (IllegalArgumentException e1) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("WeakHashMap, NaN load factor");
          Map goodMap = new Hashtable(100, .69f);
          goodMap = new HashMap(100, .69f);
          Set goodSet = new HashSet(100, .69f);
          goodMap = new WeakHashMap(100, .69f);
     }
}
