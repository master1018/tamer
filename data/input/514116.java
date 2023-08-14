public class DataEntryWriterFactory
{
    public static DataEntryWriter createDataEntryWriter(ClassPath classPath,
                                                        int       fromIndex,
                                                        int       toIndex)
    {
        DataEntryWriter writer = null;
        for (int index = toIndex - 1; index >= fromIndex; index--)
        {
            ClassPathEntry entry = classPath.get(index);
            writer = createClassPathEntryWriter(entry, writer);
        }
        return writer;
    }
    private static DataEntryWriter createClassPathEntryWriter(ClassPathEntry  classPathEntry,
                                                              DataEntryWriter alternativeWriter)
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
        System.out.println("Preparing output " +
                           (isJar ? "jar" :
                            isWar ? "war" :
                            isEar ? "ear" :
                            isZip ? "zip" :
                                    "directory") +
                           " [" + entryName + "]" +
                           (filter    != null ||
                            jarFilter != null ||
                            warFilter != null ||
                            earFilter != null ||
                            zipFilter != null ? " (filtered)" : ""));
        DataEntryWriter writer = new DirectoryWriter(classPathEntry.getFile(),
                                                     isJar ||
                                                     isWar ||
                                                     isEar ||
                                                     isZip);
        writer = wrapInJarWriter(writer, isZip, zipFilter, ".zip", isJar || isWar || isEar);
        writer = wrapInJarWriter(writer, isEar, earFilter, ".ear", isJar || isWar);
        writer = wrapInJarWriter(writer, isWar, warFilter, ".war", isJar);
        writer = wrapInJarWriter(writer, isJar, jarFilter, ".jar", false);
        writer = filter != null?
            new FilteredDataEntryWriter(
            new DataEntryNameFilter(
            new ListParser(new FileNameParser()).parse(filter)),
                writer) :
            writer;
        return alternativeWriter != null ?
            new CascadingDataEntryWriter(writer, alternativeWriter) :
            writer;
    }
    private static DataEntryWriter wrapInJarWriter(DataEntryWriter writer,
                                                   boolean         isJar,
                                                   List            jarFilter,
                                                   String          jarExtension,
                                                   boolean         dontWrap)
    {
        DataEntryWriter jarWriter = dontWrap ?
            (DataEntryWriter)new ParentDataEntryWriter(writer) :
            (DataEntryWriter)new JarWriter(writer);
        DataEntryWriter filteredJarWriter = jarFilter != null?
            new FilteredDataEntryWriter(
            new DataEntryParentFilter(
            new DataEntryNameFilter(
            new ListParser(new FileNameParser()).parse(jarFilter))),
                 jarWriter) :
            jarWriter;
        return new FilteredDataEntryWriter(
               new DataEntryParentFilter(
               new DataEntryNameFilter(
               new ExtensionMatcher(jarExtension))),
                   filteredJarWriter,
                   isJar ? jarWriter : writer);
    }
    private static boolean endsWithIgnoreCase(String string, String suffix)
    {
        int stringLength = string.length();
        int suffixLength = suffix.length();
        return string.regionMatches(true, stringLength - suffixLength, suffix, 0, suffixLength);
    }
}
