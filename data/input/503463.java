import proguard.classfile.*;
import proguard.classfile.util.ClassUtil;
import java.io.IOException;
import java.util.Map;
public class DataEntryObfuscator implements DataEntryReader
{
    private final ClassPool       classPool;
    private final Map             packagePrefixMap;
    private final DataEntryReader dataEntryReader;
    public DataEntryObfuscator(ClassPool       classPool,
                               Map             packagePrefixMap,
                               DataEntryReader dataEntryReader)
    {
        this.classPool        = classPool;
        this.packagePrefixMap = packagePrefixMap;
        this.dataEntryReader  = dataEntryReader;
    }
    public void read(DataEntry dataEntry) throws IOException
    {
        dataEntryReader.read(renamedDataEntry(dataEntry));
    }
    private DataEntry renamedDataEntry(DataEntry dataEntry)
    {
        String dataEntryName = dataEntry.getName();
        for (int suffixIndex = dataEntryName.length() - 1;
             suffixIndex > 0;
             suffixIndex--)
        {
            char c = dataEntryName.charAt(suffixIndex);
            if (!Character.isLetterOrDigit(c))
            {
                String className = dataEntryName.substring(0, suffixIndex);
                if (c == ClassConstants.INTERNAL_PACKAGE_SEPARATOR)
                {
                    break;
                }
                Clazz clazz = classPool.getClass(className);
                if (clazz != null)
                {
                    String newClassName = clazz.getName();
                    if (!className.equals(newClassName))
                    {
                        String newDataEntryName =
                            newClassName + dataEntryName.substring(suffixIndex);
                        return new RenamedDataEntry(dataEntry, newDataEntryName);
                    }
                    break;
                }
            }
        }
        String packagePrefix    = ClassUtil.internalPackagePrefix(dataEntryName);
        String newPackagePrefix = (String)packagePrefixMap.get(packagePrefix);
        if (newPackagePrefix != null &&
            !packagePrefix.equals(newPackagePrefix))
        {
            String newDataEntryName =
                newPackagePrefix + dataEntryName.substring(packagePrefix.length());
            return new RenamedDataEntry(dataEntry, newDataEntryName);
        }
        return dataEntry;
    }
}
