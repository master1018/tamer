public class DataEntryReaderFactory
{
    public static DataEntryReader createDataEntryReader(String          messagePrefix,
                                                        ClassPathEntry  classPathEntry,
                                                        DataEntryReader reader)
    {
        String entryName = classPathEntry.getName();
        boolean isJar = endsWithIgnoreCase(entryName, ".jar");
        boolean isWar = endsWithIgnoreCase(entryName, ".war");
        boolean isEar = endsWithIgnoreCase(entryName, ".ear");
        boolean isZip = endsWithIgnoreCase(entryName, ".zip");
        List filter    = classPathEntry.getFilter();
        List jarFilter = classPathEntry.getJarFilter();
        List warFilter = classPathEntry.getWarFilter();
        List earFilter = classPathEntry.getEarFilter();
        List zipFilter = classPathEntry.getZipFilter();
        System.out.println(messagePrefix +
                           (isJar ? "jar" :
                            isWar ? "war" :
                            isEar ? "ear" :
                            isZip ? "zip" :
                                    "directory") +
                           " [" + classPathEntry.getName() + "]" +
                           (filter    != null ||
                            jarFilter != null ||
                            warFilter != null ||
                            earFilter != null ||
                            zipFilter != null ? " (filtered)" : ""));
        if (filter != null)
        {
            reader = new FilteredDataEntryReader(
                     new DataEntryNameFilter(
                     new ListParser(new FileNameParser()).parse(filter)),
                         reader);
        }
        reader = wrapInJarReader(reader, isJar, jarFilter, ".jar");
        if (!isJar)
        {
            reader = wrapInJarReader(reader, isWar, warFilter, ".war");
            if (!isWar)
            {
                reader = wrapInJarReader(reader, isEar, earFilter, ".ear");
                if (!isEar)
                {
                    reader = wrapInJarReader(reader, isZip, zipFilter, ".zip");
                }
            }
        }
        return reader;
    }
    private static DataEntryReader wrapInJarReader(DataEntryReader reader,
                                                   boolean         isJar,
                                                   List            jarFilter,
                                                   String          jarExtension)
    {
        DataEntryReader jarReader = new JarReader(reader);
        if (isJar)
        {
            return jarReader;
        }
        else
        {
            if (jarFilter != null)
            {
                jarReader = new FilteredDataEntryReader(
                            new DataEntryNameFilter(
                            new ListParser(new FileNameParser()).parse(jarFilter)),
                                jarReader);
            }
            return new FilteredDataEntryReader(
                   new DataEntryNameFilter(
                   new ExtensionMatcher(jarExtension)),
                       jarReader,
                       reader);
        }
    }
    private static boolean endsWithIgnoreCase(String string, String suffix)
    {
        int stringLength = string.length();
        int suffixLength = suffix.length();
        return string.regionMatches(true, stringLength - suffixLength, suffix, 0, suffixLength);
    }
}
