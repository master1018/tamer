public class WrappedNull {
      public static void main(String argv[]) throws Exception {
          boolean testSucceeded = false;
          try{
              List l = Arrays.asList(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("Arrays.asList");
          testSucceeded = false;
          try{
              Collection c = Collections.unmodifiableCollection(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("unmodifiableCollection");
          testSucceeded = false;
          try{
              Set c = Collections.unmodifiableSet(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("unmodifiableSet");
          testSucceeded = false;
          try{
              List c = Collections.unmodifiableList(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("unmodifiableList");
          testSucceeded = false;
          try{
              Map c = Collections.unmodifiableMap(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("unmodifiableMap");
          testSucceeded = false;
          try{
              SortedSet c = Collections.unmodifiableSortedSet(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("unmodifiableSortedSet");
          testSucceeded = false;
          try{
              SortedMap c = Collections.unmodifiableSortedMap(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("unmodifiableSortedMap");
          testSucceeded = false;
          try{
              Collection c = Collections.synchronizedCollection(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("synchronizedCollection");
          testSucceeded = false;
          try{
              Set c = Collections.synchronizedSet(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("synchronizedSet");
          testSucceeded = false;
          try{
              List c = Collections.synchronizedList(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("synchronizedList");
          testSucceeded = false;
          try{
              Map c = Collections.synchronizedMap(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("synchronizedMap");
          testSucceeded = false;
          try{
              SortedSet c = Collections.synchronizedSortedSet(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("synchronizedSortedSet");
          testSucceeded = false;
          try{
              SortedMap c = Collections.synchronizedSortedMap(null);
          }
          catch (NullPointerException e) {
              testSucceeded = true;
          }
          if(!testSucceeded)
              throw new Exception("synchronizedSortedMap");
          List l = Arrays.asList(new Object[0]);
          Collection c = Collections.unmodifiableCollection(
                             Collections.EMPTY_SET);
          Set s = Collections.unmodifiableSet(Collections.EMPTY_SET);
          l = Collections.unmodifiableList(Collections.EMPTY_LIST);
          Map m = Collections.unmodifiableMap(Collections.EMPTY_MAP);
          SortedSet ss = Collections.unmodifiableSortedSet(new TreeSet());
          SortedMap sm = Collections.unmodifiableSortedMap(new TreeMap());
          c = Collections.synchronizedCollection(Collections.EMPTY_SET);
          s = Collections.synchronizedSet(Collections.EMPTY_SET);
          l = Collections.synchronizedList(Collections.EMPTY_LIST);
          m = Collections.synchronizedMap(Collections.EMPTY_MAP);
          ss = Collections.synchronizedSortedSet(new TreeSet());
          sm = Collections.synchronizedSortedMap(new TreeMap());
      }
}
