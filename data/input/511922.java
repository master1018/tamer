import proguard.classfile.ClassConstants;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
public class JarWriter implements DataEntryWriter, Finisher
{
    private final DataEntryWriter dataEntryWriter;
    private final Manifest        manifest;
    private final String          comment;
    private OutputStream    currentParentOutputStream;
    private ZipOutputStream currentJarOutputStream;
    private Finisher        currentFinisher;
    private DataEntry       currentDataEntry;
    private final Set jarEntryNames = new HashSet();
    public JarWriter(DataEntryWriter dataEntryWriter)
    {
        this(dataEntryWriter, null, null);
    }
    public JarWriter(DataEntryWriter dataEntryWriter,
                     Manifest        manifest,
                     String          comment)
    {
        this.dataEntryWriter = dataEntryWriter;
        this.manifest        = manifest;
        this.comment         = comment;
    }
    public boolean createDirectory(DataEntry dataEntry) throws IOException
    {
        if (!prepareEntry(dataEntry))
        {
            return false;
        }
        closeEntry();
        String name = dataEntry.getName() + ClassConstants.INTERNAL_PACKAGE_SEPARATOR;
        if (jarEntryNames.add(name))
        {
            currentJarOutputStream.putNextEntry(new ZipEntry(name));
            currentJarOutputStream.closeEntry();
        }
        currentFinisher  = null;
        currentDataEntry = null;
        return true;
    }
    public OutputStream getOutputStream(DataEntry dataEntry) throws IOException
    {
        return getOutputStream(dataEntry,  null);
    }
    public OutputStream getOutputStream(DataEntry dataEntry,
                                        Finisher  finisher) throws IOException
    {
        if (!prepareEntry(dataEntry))
        {
            return null;
        }
        if (!dataEntry.equals(currentDataEntry))
        {
            closeEntry();
            String name = dataEntry.getName();
            if (!jarEntryNames.add(name))
            {
                throw new IOException("Duplicate zip entry ["+dataEntry+"]");
            }
            currentJarOutputStream.putNextEntry(new ZipEntry(name));
            currentFinisher  = finisher;
            currentDataEntry = dataEntry;
        }
        return currentJarOutputStream;
    }
    public void finish() throws IOException
    {
        if (currentJarOutputStream != null)
        {
            closeEntry();
            currentJarOutputStream.finish();
            currentJarOutputStream    = null;
            currentParentOutputStream = null;
            jarEntryNames.clear();
        }
    }
    public void close() throws IOException
    {
        dataEntryWriter.close();
    }
    private boolean prepareEntry(DataEntry dataEntry) throws IOException
    {
        OutputStream parentOutputStream =
            dataEntryWriter.getOutputStream(dataEntry.getParent(), this);
        if (parentOutputStream == null)
        {
            return false;
        }
        if (currentParentOutputStream == null)
        {
            currentParentOutputStream = parentOutputStream;
            currentJarOutputStream = manifest != null ?
                new JarOutputStream(parentOutputStream, manifest) :
                new ZipOutputStream(parentOutputStream);
            if (comment != null)
            {
                currentJarOutputStream.setComment(comment);
            }
        }
        return true;
    }
    private void closeEntry() throws IOException
    {
        if (currentDataEntry != null)
        {
            if (currentFinisher != null)
            {
                currentFinisher.finish();
                currentFinisher = null;
            }
            currentJarOutputStream.closeEntry();
            currentDataEntry = null;
        }
    }
}
