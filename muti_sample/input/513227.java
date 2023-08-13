public class internal_error extends Exception
  {
    public internal_error(String msg)
      {
    super(msg);
      }
    public void crash()
      {
    System.err.println("JavaCUP Fatal Internal Error Detected");
    System.err.println(getMessage());
    printStackTrace();
    System.exit(-1);
      }
  };
