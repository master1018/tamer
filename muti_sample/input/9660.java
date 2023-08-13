public class CStringUtilities {
  public static int getStringLength(Address addr) {
    int i = 0;
    while (addr.getCIntegerAt(i, 1, false) != 0) {
      i++;
    }
    return i;
  }
  private static String encoding = System.getProperty("file.encoding", "US-ASCII");
  public static String getString(Address addr) {
    if (addr == null) {
      return null;
    }
    List data = new ArrayList();
    byte val = 0;
    long i = 0;
    do {
      val = (byte) addr.getCIntegerAt(i, 1, false);
      if (val != 0) {
        data.add(new Byte(val));
      }
      ++i;
    } while (val != 0);
    byte[] bytes = new byte[data.size()];
    for (i = 0; i < data.size(); ++i) {
      bytes[(int) i] = ((Byte) data.get((int) i)).byteValue();
    }
    try {
      return new String(bytes, encoding);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Error converting bytes to String using " + encoding + " encoding", e);
    }
  }
}
