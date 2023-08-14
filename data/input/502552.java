public class FileWordReader extends WordReader
{
    private final String           name;
    private LineNumberReader reader;
    public FileWordReader(File file) throws IOException
    {
        super(file.getParentFile());
        this.name   = file.getPath();
        this.reader = new LineNumberReader(
                      new BufferedReader(
                      new FileReader(file)));
    }
    public FileWordReader(URL url) throws IOException
    {
        super(null);
        this.name   = url.toString();
        this.reader = new LineNumberReader(
                       new BufferedReader(
                       new InputStreamReader(url.openStream())));
    }
    protected String nextLine() throws IOException
    {
        return reader.readLine();
    }
    protected String lineLocationDescription()
    {
        return "line " + reader.getLineNumber() + " of file '" + name + "'";
    }
    public void close() throws IOException
    {
        super.close();
        if (reader != null)
        {
            reader.close();
        }
    }
}
