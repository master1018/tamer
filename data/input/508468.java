public class JarReader implements DataEntryReader
{
    private final DataEntryReader dataEntryReader;
    public JarReader(DataEntryReader dataEntryReader)
    {
        this.dataEntryReader = dataEntryReader;
    }
    public void read(DataEntry dataEntry) throws IOException
    {
        ZipInputStream zipInputStream = new ZipInputStream(dataEntry.getInputStream());
        try
        {
            while (true)
            {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (zipEntry == null)
                {
                    break;
                }
                dataEntryReader.read(new ZipDataEntry(dataEntry,
                                                      zipEntry,
                                                      zipInputStream));
            }
        }
        finally
        {
            dataEntry.closeInputStream();
        }
    }
}
