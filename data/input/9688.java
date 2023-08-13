public class ParseURL {
  public static void main(String args[]) throws MalformedURLException {
    String url = new URL(new URL("http:
    if (url.equalsIgnoreCase("http:
      System.err.println("Success!!");
    } else {
      throw new RuntimeException("The result is "+url+", it should be http:
    }
  }
}
