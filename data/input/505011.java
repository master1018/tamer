import proguard.classfile.ClassConstants;
import java.io.*;
import java.util.zip.*;
public class ZipDataEntry implements DataEntry
{
    private final DataEntry      parent;
    private final ZipEntry       zipEntry;
    private       ZipInputStream zipInputStream;
    public ZipDataEntry(DataEntry      parent,
                        ZipEntry       zipEntry,
                        ZipInputStream zipInputStream)
    {
        this.parent         = parent;
        this.zipEntry       = zipEntry;
        this.zipInputStream = zipInputStream;
    }
    public String getName()
    {
        String name = zipEntry.getName()
            .replace(File.separatorChar, ClassConstants.INTERNAL_PACKAGE_SEPARATOR);
        int length = name.length();
        return length > 0 &&
               name.charAt(length-1) == ClassConstants.INTERNAL_PACKAGE_SEPARATOR ?
                   name.substring(0, length -1) :
                   name;
    }
    public boolean isDirectory()
    {
        return zipEntry.isDirectory();
    }
    public InputStream getInputStream() throws IOException
    {
        return zipInputStream;
    }
    public void closeInputStream() throws IOException
    {
        zipInputStream.closeEntry();
        zipInputStream = null;
    }
    public DataEntry getParent()
    {
        return parent;
    }
    public String toString()
    {
        return parent.toString() + ':' + getName();
    }
}
