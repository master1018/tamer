import proguard.classfile.ClassConstants;
import java.io.*;
public class DirectoryWriter implements DataEntryWriter
{
    private final File    baseFile;
    private final boolean isFile;
    private File         currentFile;
    private OutputStream currentOutputStream;
    private Finisher     currentFinisher;
    public DirectoryWriter(File    baseFile,
                           boolean isFile)
    {
        this.baseFile = baseFile;
        this.isFile   = isFile;
    }
    public boolean createDirectory(DataEntry dataEntry) throws IOException
    {
        if (!isFile &&
            currentFile != null)
        {
            closeEntry();
        }
        File directory = getFile(dataEntry);
        if (!directory.exists() &&
            !directory.mkdirs())
        {
            throw new IOException("Can't create directory [" + directory.getPath() + "]");
        }
        return true;
    }
    public OutputStream getOutputStream(DataEntry dataEntry) throws IOException
    {
        return getOutputStream(dataEntry,  null);
    }
    public OutputStream getOutputStream(DataEntry dataEntry,
                                        Finisher  finisher) throws IOException
    {
        File file = getFile(dataEntry);
        if (!isFile             &&
            currentFile != null &&
            !currentFile.equals(file))
        {
            closeEntry();
        }
        if (currentOutputStream == null)
        {
            File parentDirectory = file.getParentFile();
            if (parentDirectory != null   &&
                !parentDirectory.exists() &&
                !parentDirectory.mkdirs())
            {
                throw new IOException("Can't create directory [" + parentDirectory.getPath() + "]");
            }
            currentOutputStream =
                new BufferedOutputStream(
                new FileOutputStream(file));
            currentFinisher = finisher;
            currentFile     = file;
        }
        return currentOutputStream;
    }
    public void close() throws IOException
    {
        closeEntry();
    }
    private File getFile(DataEntry dataEntry)
    {
        return isFile ?
            baseFile :
            new File(baseFile,
                     dataEntry.getName().replace(ClassConstants.INTERNAL_PACKAGE_SEPARATOR,
                                                 File.separatorChar));
    }
    private void closeEntry() throws IOException
    {
        if (currentOutputStream != null)
        {
            if (currentFinisher != null)
            {
                currentFinisher.finish();
                currentFinisher = null;
            }
            currentOutputStream.close();
            currentOutputStream = null;
            currentFile         = null;
        }
    }
}
