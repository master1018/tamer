import proguard.classfile.ClassPool;
import proguard.classfile.editor.ClassElementSorter;
import proguard.classfile.visitor.*;
import proguard.obfuscate.Obfuscator;
import proguard.optimize.Optimizer;
import proguard.preverify.*;
import proguard.shrink.Shrinker;
import java.io.*;
public class ProGuard
{
    public static final String VERSION = "ProGuard, version 4.4";
    private final Configuration configuration;
    private       ClassPool     programClassPool = new ClassPool();
    private final ClassPool     libraryClassPool = new ClassPool();
    public ProGuard(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public void execute() throws IOException
    {
        System.out.println(VERSION);
        GPL.check();
        if (configuration.printConfiguration != null)
        {
            printConfiguration();
        }
        if (configuration.programJars != null     &&
            configuration.programJars.hasOutput() &&
            new UpToDateChecker(configuration).check())
        {
            return;
        }
        readInput();
        if (configuration.shrink    ||
            configuration.optimize  ||
            configuration.obfuscate ||
            configuration.preverify)
        {
            initialize();
        }
        if (configuration.targetClassVersion != 0)
        {
            target();
        }
        if (configuration.printSeeds != null)
        {
            printSeeds();
        }
        if (configuration.shrink)
        {
            shrink();
        }
        if (configuration.preverify)
        {
            inlineSubroutines();
        }
        if (configuration.optimize)
        {
            for (int optimizationPass = 0;
                 optimizationPass < configuration.optimizationPasses;
                 optimizationPass++)
            {
                if (!optimize())
                {
                    break;
                }
                if (configuration.shrink)
                {
                    configuration.printUsage       = null;
                    configuration.whyAreYouKeeping = null;
                    shrink();
                }
            }
        }
        if (configuration.obfuscate)
        {
            obfuscate();
        }
        if (configuration.preverify)
        {
            preverify();
        }
        if (configuration.shrink    ||
            configuration.optimize  ||
            configuration.obfuscate ||
            configuration.preverify)
        {
            sortClassElements();
        }
        if (configuration.programJars.hasOutput())
        {
            writeOutput();
        }
        if (configuration.dump != null)
        {
            dump();
        }
    }
    private void printConfiguration() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Printing configuration to [" + fileName(configuration.printConfiguration) + "]...");
        }
        PrintStream ps = createPrintStream(configuration.printConfiguration);
        try
        {
            new ConfigurationWriter(ps).write(configuration);
        }
        finally
        {
            closePrintStream(ps);
        }
    }
    private void readInput() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Reading input...");
        }
        new InputReader(configuration).execute(programClassPool, libraryClassPool);
    }
    private void initialize() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Initializing...");
        }
        new Initializer(configuration).execute(programClassPool, libraryClassPool);
    }
    private void target() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Setting target versions...");
        }
        new Targeter(configuration).execute(programClassPool);
    }
    private void printSeeds() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Printing kept classes, fields, and methods...");
        }
        if (configuration.keep == null)
        {
            throw new IOException("You have to specify '-keep' options for the shrinking step.");
        }
        PrintStream ps = createPrintStream(configuration.printSeeds);
        try
        {
            SimpleClassPrinter printer = new SimpleClassPrinter(false, ps);
            ClassPoolVisitor classPoolvisitor =
                ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keep,
                                                                        new ProgramClassFilter(printer),
                                                                        new ProgramMemberFilter(printer),
                                                                        true,
                                                                        true,
                                                                        true);
            programClassPool.accept(classPoolvisitor);
            libraryClassPool.accept(classPoolvisitor);
        }
        finally
        {
            closePrintStream(ps);
        }
    }
    private void shrink() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Shrinking...");
            if (configuration.whyAreYouKeeping != null)
            {
                System.out.println("Explaining why classes and class members are being kept...");
            }
            if (configuration.printUsage != null)
            {
                System.out.println("Printing usage to [" + fileName(configuration.printUsage) + "]...");
            }
        }
        programClassPool =
            new Shrinker(configuration).execute(programClassPool, libraryClassPool);
    }
    private void inlineSubroutines()
    {
        if (configuration.verbose)
        {
            System.out.println("Inlining subroutines...");
        }
        new SubroutineInliner(configuration).execute(programClassPool);
    }
    private boolean optimize() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Optimizing...");
        }
        return new Optimizer(configuration).execute(programClassPool, libraryClassPool);
    }
    private void obfuscate() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Obfuscating...");
            if (configuration.applyMapping != null)
            {
                System.out.println("Applying mapping [" + fileName(configuration.applyMapping) + "]");
            }
            if (configuration.printMapping != null)
            {
                System.out.println("Printing mapping to [" + fileName(configuration.printMapping) + "]...");
            }
        }
        new Obfuscator(configuration).execute(programClassPool, libraryClassPool);
    }
    private void preverify()
    {
        if (configuration.verbose)
        {
            System.out.println("Preverifying...");
        }
        new Preverifier(configuration).execute(programClassPool);
    }
    private void sortClassElements()
    {
        programClassPool.classesAccept(new ClassElementSorter());
    }
    private void writeOutput() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Writing output...");
        }
        new OutputWriter(configuration).execute(programClassPool);
    }
    private void dump() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Printing classes to [" + fileName(configuration.dump) + "]...");
        }
        PrintStream ps = createPrintStream(configuration.dump);
        try
        {
            programClassPool.classesAccept(new ClassPrinter(ps));
        }
        finally
        {
            closePrintStream(ps);
        }
    }
    private PrintStream createPrintStream(File file)
    throws FileNotFoundException
    {
        return isFile(file) ?
            new PrintStream(new BufferedOutputStream(new FileOutputStream(file))) :
            System.out;
    }
    private void closePrintStream(PrintStream printStream)
    {
        if (printStream == System.out)
        {
            printStream.flush();
        }
        else
        {
            printStream.close();
        }
    }
    private String fileName(File file)
    {
        return isFile(file) ?
            file.getAbsolutePath() :
            "standard output";
    }
    private boolean isFile(File file)
    {
        return file.getPath().length() > 0;
    }
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println(VERSION);
            System.out.println("Usage: java proguard.ProGuard [options ...]");
            System.exit(1);
        }
        Configuration configuration = new Configuration();
        try
        {
            ConfigurationParser parser = new ConfigurationParser(args);
            try
            {
                parser.parse(configuration);
            }
            finally
            {
                parser.close();
            }
            new ProGuard(configuration).execute();
        }
        catch (Exception ex)
        {
            if (configuration.verbose)
            {
                ex.printStackTrace();
            }
            else
            {
                System.err.println("Error: "+ex.getMessage());
            }
            System.exit(1);
        }
        System.exit(0);
    }
}
