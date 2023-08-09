abstract class ClassWriter
{
    public static void writeClassTable (final ClassDef classTable, final OutputStream out)
        throws IOException
    {
        classTable.writeInClassFormat (new UDataOutputStream (out));
    }
} 
