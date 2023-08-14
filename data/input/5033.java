public class Bug4804273 {
  public static void main (String[] args) {
      int errors=0;
      Locale loc = new Locale ("sv", "se");   
      Locale.setDefault (loc);
      Collator col = Collator.getInstance ();
      String[] data = {"A",
                       "Aa",
                       "Ae",
                       "B",
                       "Y",
                       "U\u0308", 
                       "Z",
                       "A\u030a", 
                       "A\u0308", 
                       "\u00c6", 
                       "O\u0308", 
                       "a\u030b", 
                       "\u00d8", 
                       "a",
                       "aa",
                       "ae",
                       "b",
                       "y",
                       "u\u0308", 
                       "z",
                       "A\u030b", 
                       "a\u030a", 
                       "a\u0308", 
                       "\u00e6", 
                       "o\u0308", 
                       "\u00f8", 
      };
      String[] sortedData = {"a",
                             "A",
                             "aa",
                             "Aa",
                             "ae",
                             "Ae",
                             "b",
                             "B",
                             "y",
                             "Y",
                             "u\u0308", 
                             "U\u0308", 
                             "z",
                             "Z",
                             "a\u030a", 
                             "A\u030a", 
                             "a\u0308", 
                             "A\u0308", 
                             "a\u030b", 
                             "A\u030b", 
                             "\u00e6", 
                             "\u00c6", 
                             "o\u0308", 
                             "O\u0308", 
                             "\u00f8", 
                             "\u00d8", 
      };
      Arrays.sort (data, col);
      System.out.println ("Using " + loc.getDisplayName());
      for (int i = 0;  i < data.length;  i++) {
          System.out.println(data[i] + "  :  " + sortedData[i]);
          if (sortedData[i].compareTo(data[i]) != 0) {
              errors++;
          }
      }
      if (errors > 0)
          throw new RuntimeException("There are " + errors + " words sorted incorrectly!");
  }
}
