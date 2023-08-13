import proguard.classfile.ClassPool;
import proguard.classfile.util.WarningPrinter;
import proguard.classfile.visitor.*;
import proguard.io.*;
import java.io.IOException;
public class InputReader
{
    private final Configuration configuration;
    public InputReader(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public void execute(ClassPool programClassPool,
                        ClassPool libraryClassPool) throws IOException
    {
        if (configuration.programJars == null)
        {
            throw new IOException("The input is empty. You have to specify one or more '-injars' options");
        }
        checkInputOutput(configuration.libraryJars,
                         configuration.programJars);
        checkInputOutput(configuration.programJars,
                         configuration.programJars);
        WarningPrinter warningPrinter = new WarningPrinter(System.err, configuration.warn);
        WarningPrinter notePrinter    = new WarningPrinter(System.out, configuration.note);
        DuplicateClassPrinter duplicateClassPrinter = new DuplicateClassPrinter(notePrinter);
        readInput("Reading program ",
                  configuration.programJars,
                  new ClassFilter(
                  new ClassReader(false,
                                  configuration.skipNonPublicLibraryClasses,
                                  configuration.skipNonPublicLibraryClassMembers,
                                  warningPrinter,
                  new ClassPresenceFilter(programClassPool, duplicateClassPrinter,
                  new ClassPoolFiller(programClassPool)))));
        if (programClassPool.size() == 0)
        {
            throw new IOException("The input doesn't contain any classes. Did you specify the proper '-injars' options?");
        }
        if (configuration.libraryJars != null)
        {
            readInput("Reading library ",
                      configuration.libraryJars,
                      new ClassFilter(
                      new ClassReader(true,
                                      configuration.skipNonPublicLibraryClasses,
                                      configuration.skipNonPublicLibraryClassMembers,
                                      warningPrinter,
                      new ClassPresenceFilter(programClassPool, duplicateClassPrinter,
                      new ClassPresenceFilter(libraryClassPool, duplicateClassPrinter,
                      new ClassPoolFiller(libraryClassPool))))));
        }
        int noteCount = notePrinter.getWarningCount();
        if (noteCount > 0)
        {
            System.err.println("Note: there were " + noteCount +
                               " duplicate class definitions.");
        }
        int warningCount = warningPrinter.getWarningCount();
        if (warningCount > 0)
        {
            System.err.println("Warning: there were " + warningCount +
                               " classes in incorrectly named files.");
            System.err.println("         You should make sure all file names correspond to their class names.");
            System.err.println("         The directory hierarchies must correspond to the package hierarchies.");
            if (!configuration.ignoreWarnings)
            {
                System.err.println("         If you don't mind the mentioned classes not being written out,");
                System.err.println("         you could try your luck using the '-ignorewarnings' option.");
                throw new IOException("Please correct the above warnings first.");
            }
        }
    }
    private void checkInputOutput(ClassPath inputClassPath,
                                  ClassPath outputClassPath)
    throws IOException
    {
        if (inputClassPath == null ||
            outputClassPath == null)
        {
            return;
        }
        for (int index1 = 0; index1 < inputClassPath.size(); index1++)
        {
            ClassPathEntry entry1 = inputClassPath.get(index1);
            if (!entry1.isOutput())
            {
                for (int index2 = 0; index2 < outputClassPath.size(); index2++)
                {
                    ClassPathEntry entry2 = outputClassPath.get(index2);
                    if (entry2.isOutput() &&
                        entry2.getName().equals(entry1.getName()))
                    {
                        throw new IOException("Input jars and output jars must be different ["+entry1.getName()+"]");
                    }
                }
            }
        }
    }
    private void readInput(String          messagePrefix,
                           ClassPath       classPath,
                           DataEntryReader reader) throws IOException
    {
        readInput(messagePrefix,
                  classPath,
                  0,
                  classPath.size(),
                  reader);
    }
    public void readInput(String          messagePrefix,
                          ClassPath       classPath,
                          int             fromIndex,
                          int             toIndex,
                          DataEntryReader reader) throws IOException
    {
        for (int index = fromIndex; index < toIndex; index++)
        {
            ClassPathEntry entry = classPath.get(index);
            if (!entry.isOutput())
            {
                readInput(messagePrefix, entry, reader);
            }
        }
    }
    private void readInput(String          messagePrefix,
                           ClassPathEntry  classPathEntry,
                           DataEntryReader dataEntryReader) throws IOException
    {
        try
        {
            DataEntryReader reader =
                DataEntryReaderFactory.createDataEntryReader(messagePrefix,
                                                             classPathEntry,
                                                             dataEntryReader);
            DirectoryPump directoryPump =
                new DirectoryPump(classPathEntry.getFile());
            directoryPump.pumpDataEntries(reader);
        }
        catch (IOException ex)
        {
            throw new IOException("Can't read [" + classPathEntry + "] (" + ex.getMessage() + ")");
        }
    }
}
