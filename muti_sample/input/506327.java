public class DictionaryNameFactory implements NameFactory
{
    private static final char COMMENT_CHARACTER = '#';
    private final List        names;
    private final NameFactory nameFactory;
    private int index = 0;
    public DictionaryNameFactory(File        file,
                                 NameFactory nameFactory) throws IOException
    {
        this.names       = new ArrayList();
        this.nameFactory = nameFactory;
        Reader reader = new FileReader(file);
        try
        {
            StringBuffer buffer = new StringBuffer();
            while (true)
            {
                int c = reader.read();
                if (c != -1 &&
                    (buffer.length() == 0 ?
                         Character.isJavaIdentifierStart((char)c) :
                         Character.isJavaIdentifierPart((char)c)))
                {
                    buffer.append((char)c);
                }
                else
                {
                    if (buffer.length() > 0)
                    {
                        String name = buffer.toString();
                        if (!names.contains(name))
                        {
                            names.add(name);
                        }
                        buffer.setLength(0);
                    }
                    if (c == COMMENT_CHARACTER)
                    {
                        do
                        {
                            c = reader.read();
                        }
                        while (c != -1 &&
                               c != '\n' &&
                               c != '\r');
                    }
                    if (c == -1)
                    {
                        return;
                    }
                }
            }
        }
        finally
        {
            reader.close();
        }
    }
    public DictionaryNameFactory(DictionaryNameFactory dictionaryNameFactory,
                                 NameFactory           nameFactory)
    {
        this.names       = dictionaryNameFactory.names;
        this.nameFactory = nameFactory;
    }
    public void reset()
    {
        index = 0;
        nameFactory.reset();
    }
    public String nextName()
    {
        String name;
        if (index < names.size())
        {
            name = (String)names.get(index++);
        }
        else
        {
            do
            {
                name = nameFactory.nextName();
            }
            while (names.contains(name));
        }
        return name;
    }
    public static void main(String[] args)
    {
        try
        {
            DictionaryNameFactory factory =
                new DictionaryNameFactory(new File(args[0]), new SimpleNameFactory());
            for (int counter = 0; counter < 50; counter++)
            {
                System.out.println("["+factory.nextName()+"]");
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
