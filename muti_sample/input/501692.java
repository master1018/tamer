public class DirectoryPump implements DataEntryPump
{
    private final File directory;
    public DirectoryPump(File directory)
    {
        this.directory = directory;
    }
    public void pumpDataEntries(DataEntryReader dataEntryReader)
    throws IOException
    {
        if (!directory.exists())
        {
            throw new IOException("No such file or directory");
        }
        readFiles(directory, dataEntryReader);
    }
    private void readFiles(File file, DataEntryReader dataEntryReader)
    throws IOException
    {
        dataEntryReader.read(new FileDataEntry(directory, file));
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (int index = 0; index < files.length; index++)
            {
                readFiles(files[index], dataEntryReader);
            }
        }
    }
}
