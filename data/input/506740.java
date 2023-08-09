import proguard.classfile.ClassPool;
import proguard.classfile.util.ClassUtil;
import proguard.io.*;
import java.io.IOException;
import java.util.*;
public class OutputWriter
{
    private final Configuration configuration;
    public OutputWriter(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public void execute(ClassPool programClassPool) throws IOException
    {
        ClassPath programJars = configuration.programJars;
        ClassPathEntry firstEntry = programJars.get(0);
        if (firstEntry.isOutput())
        {
            throw new IOException("The output jar [" + firstEntry.getName() +
                                  "] must be specified after an input jar, or it will be empty.");
        }
        for (int index = 0; index < programJars.size() - 1; index++)
        {
            ClassPathEntry entry = programJars.get(index);
            if (entry.isOutput())
            {
                if (entry.getFilter()    == null &&
                    entry.getJarFilter() == null &&
                    entry.getWarFilter() == null &&
                    entry.getEarFilter() == null &&
                    entry.getZipFilter() == null &&
                    programJars.get(index + 1).isOutput())
                {
                    throw new IOException("The output jar [" + entry.getName() +
                                          "] must have a filter, or all subsequent jars will be empty.");
                }
                for (int inIndex = 0; inIndex < programJars.size(); inIndex++)
                {
                    ClassPathEntry otherEntry = programJars.get(inIndex);
                    if (!otherEntry.isOutput() &&
                        entry.getFile().equals(otherEntry.getFile()))
                    {
                        throw new IOException("The output jar [" + entry.getName() +
                                              "] must be different from all input jars.");
                    }
                }
            }
        }
        int firstInputIndex = 0;
        int lastInputIndex  = 0;
        for (int index = 0; index < programJars.size(); index++)
        {
            ClassPathEntry entry = programJars.get(index);
            if (!entry.isOutput())
            {
                lastInputIndex = index;
            }
            else
            {
                int nextIndex = index + 1;
                if (nextIndex == programJars.size() ||
                    !programJars.get(nextIndex).isOutput())
                {
                    writeOutput(programClassPool,
                                programJars,
                                firstInputIndex,
                                lastInputIndex + 1,
                                nextIndex);
                    firstInputIndex = nextIndex;
                }
            }
        }
    }
    private void writeOutput(ClassPool programClassPool,
                             ClassPath classPath,
                             int       fromInputIndex,
                             int       fromOutputIndex,
                             int       toOutputIndex)
    throws IOException
    {
        try
        {
            DataEntryWriter writer =
                DataEntryWriterFactory.createDataEntryWriter(classPath,
                                                             fromOutputIndex,
                                                             toOutputIndex);
            DataEntryReader classRewriter =
                new ClassRewriter(programClassPool, writer);
            DataEntryReader resourceCopier =
                new DataEntryCopier(writer);
            DataEntryReader resourceRewriter = resourceCopier;
            if (configuration.adaptResourceFileContents != null)
            {
                resourceRewriter =
                    new NameFilter(configuration.adaptResourceFileContents,
                    new NameFilter("META-INF
    private static Map createPackagePrefixMap(ClassPool classPool)
    {
        Map PackagePrefixMap = new HashMap();
        Iterator iterator = classPool.classNames();
        while (iterator.hasNext())
        {
            String className     = (String)iterator.next();
            String PackagePrefix = ClassUtil.internalPackagePrefix(className);
            String mappedNewPackagePrefix = (String)PackagePrefixMap.get(PackagePrefix);
            if (mappedNewPackagePrefix == null ||
                !mappedNewPackagePrefix.equals(PackagePrefix))
            {
                String newClassName     = classPool.getClass(className).getName();
                String newPackagePrefix = ClassUtil.internalPackagePrefix(newClassName);
                PackagePrefixMap.put(PackagePrefix, newPackagePrefix);
            }
        }
        return PackagePrefixMap;
    }
}
