public class ArgumentWordReader extends WordReader
{
    private final String[] arguments;
    private int      index = 0;
    public ArgumentWordReader(String[] arguments, File baseDir)
    {
        super(baseDir);
        this.arguments = arguments;
    }
    protected String nextLine() throws IOException
    {
        return index < arguments.length ?
            arguments[index++] :
            null;
    }
    protected String lineLocationDescription()
    {
        return "argument number " + index;
    }
    public static void main(String[] args) {
        try
        {
            WordReader reader = new ArgumentWordReader(args, null);
            try
            {
                while (true)
                {
                    String word = reader.nextWord();
                    if (word == null)
                        System.exit(-1);
                    System.err.println("["+word+"]");
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            finally
            {
                reader.close();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
