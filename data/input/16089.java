public class Bug4248694 {
  public static void main (String[] args) {
      int errors=0;
      Locale loc = new Locale ("is", "is");   
      Locale.setDefault (loc);
      Collator col = Collator.getInstance ();
      String[] data = {"\u00e6ard",
                       "Zard",
                       "aard",
                       "\u00feard",
                       "vird",
                       "\u00c6ard",
                       "Zerd",
                       "\u00deard"};
      String[] sortedData = {"aard",
                             "vird",
                             "Zard",
                             "Zerd",
                             "\u00feard",
                             "\u00deard",
                             "\u00e6ard",
                             "\u00c6ard"};
      Arrays.sort (data, col);
      System.out.println ("Using " + loc.getDisplayName());
      for (int i = 0;  i < data.length;  i++) {
          System.out.println(data[i] + "  :  " + sortedData[i]);
          if (sortedData[i].compareTo(data[i]) != 0) {
              errors++;
          }
      }
      if (errors > 0)
          throw new RuntimeException();
  }
}
