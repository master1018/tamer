public class GenFileStream extends PrintWriter
{
  public GenFileStream (String filename)
  {
    super (tmpCharArrayWriter = new CharArrayWriter());
    charArrayWriter = tmpCharArrayWriter;
    name = filename;
  } 
  public void close ()
  {
    File file = new File (name);
    try
    {
      if (checkError ())
        throw new IOException ();
      FileWriter fileWriter = new FileWriter (file);
      fileWriter.write (charArrayWriter.toCharArray ());
      fileWriter.close ();
    }
    catch (IOException e)
    {
      String[] parameters = {name, e.toString ()};
      System.err.println (Util.getMessage ("GenFileStream.1", parameters));
    }
    super.close ();
  } 
  public String name ()
  {
    return name;
  } 
  private        CharArrayWriter    charArrayWriter;
  private static CharArrayWriter tmpCharArrayWriter;
  private String                       name;
} 
