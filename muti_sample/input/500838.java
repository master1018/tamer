public class MappingReader
{
    private final File mappingFile;
    public MappingReader(File mappingFile)
    {
        this.mappingFile = mappingFile;
    }
    public void pump(MappingProcessor mappingProcessor) throws IOException
    {
        LineNumberReader reader = new LineNumberReader(
                                  new BufferedReader(
                                  new FileReader(mappingFile)));
        try
        {
            String className = null;
            while (true)
            {
                String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                line = line.trim();
                if (line.endsWith(":"))
                {
                    className = processClassMapping(line, mappingProcessor);
                }
                else if (className != null)
                {
                    processClassMemberMapping(className, line, mappingProcessor);
                }
            }
        }
        catch (IOException ex)
        {
            throw new IOException("Can't process mapping file (" + ex.getMessage() + ")");
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException ex)
            {
            }
        }
    }
    private String processClassMapping(String           line,
                                       MappingProcessor mappingProcessor)
    {
        int arrowIndex = line.indexOf("->");
        if (arrowIndex < 0)
        {
            return null;
        }
        int colonIndex = line.indexOf(':', arrowIndex + 2);
        if (colonIndex < 0)
        {
            return null;
        }
        String className    = line.substring(0, arrowIndex).trim();
        String newClassName = line.substring(arrowIndex + 2, colonIndex).trim();
        boolean interested = mappingProcessor.processClassMapping(className, newClassName);
        return interested ? className : null;
    }
    private void processClassMemberMapping(String           className,
                                           String           line,
                                           MappingProcessor mappingProcessor)
    {
        int colonIndex1    =                           line.indexOf(':');
        int colonIndex2    = colonIndex1    < 0 ? -1 : line.indexOf(':', colonIndex1    + 1);
        int spaceIndex     =                           line.indexOf(' ', colonIndex2    + 2);
        int argumentIndex1 =                           line.indexOf('(', spaceIndex     + 1);
        int argumentIndex2 = argumentIndex1 < 0 ? -1 : line.indexOf(')', argumentIndex1 + 1);
        int arrowIndex     =                           line.indexOf("->", Math.max(spaceIndex, argumentIndex2) + 1);
        if (spaceIndex < 0 ||
            arrowIndex < 0)
        {
            return;
        }
        String type    = line.substring(colonIndex2 + 1, spaceIndex).trim();
        String name    = line.substring(spaceIndex + 1, argumentIndex1 >= 0 ? argumentIndex1 : arrowIndex).trim();
        String newName = line.substring(arrowIndex + 2).trim();
        if (type.length()    > 0 &&
            name.length()    > 0 &&
            newName.length() > 0)
        {
            if (argumentIndex2 < 0)
            {
                mappingProcessor.processFieldMapping(className, type, name, newName);
            }
            else
            {
                int firstLineNumber = 0;
                int lastLineNumber  = 0;
                if (colonIndex2 > 0)
                {
                    firstLineNumber = Integer.parseInt(line.substring(0, colonIndex1).trim());
                    lastLineNumber  = Integer.parseInt(line.substring(colonIndex1 + 1, colonIndex2).trim());
                }
                String arguments = line.substring(argumentIndex1 + 1, argumentIndex2).trim();
                mappingProcessor.processMethodMapping(className,
                                                      firstLineNumber,
                                                      lastLineNumber,
                                                      type,
                                                      name,
                                                      arguments,
                                                      newName);
            }
        }
    }
}
