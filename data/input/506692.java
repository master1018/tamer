import proguard.classfile.ClassPool;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.visitor.*;
import java.io.*;
public class Shrinker
{
    private final Configuration configuration;
    public Shrinker(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public ClassPool execute(ClassPool programClassPool,
                             ClassPool libraryClassPool) throws IOException
    {
        if (configuration.keep == null)
        {
            throw new IOException("You have to specify '-keep' options for the shrinking step.");
        }
        programClassPool.classesAccept(new ClassCleaner());
        libraryClassPool.classesAccept(new ClassCleaner());
        UsageMarker usageMarker = configuration.whyAreYouKeeping == null ?
            new UsageMarker() :
            new ShortestUsageMarker();
        ClassPoolVisitor classPoolvisitor =
            ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keep,
                                                                    usageMarker,
                                                                    usageMarker,
                                                                    true,
                                                                    false,
                                                                    false);
        programClassPool.accept(classPoolvisitor);
        libraryClassPool.accept(classPoolvisitor);
        programClassPool.classesAccept(new InterfaceUsageMarker(usageMarker));
        programClassPool.classesAccept(
            new UsedClassFilter(usageMarker,
            new AllAttributeVisitor(true,
            new MultiAttributeVisitor(new AttributeVisitor[]
            {
                new InnerUsageMarker(usageMarker),
                new AnnotationUsageMarker(usageMarker),
            }))));
        if (configuration.whyAreYouKeeping != null)
        {
            System.out.println();
            ShortestUsagePrinter shortestUsagePrinter =
                new ShortestUsagePrinter((ShortestUsageMarker)usageMarker,
                                         configuration.verbose);
            ClassPoolVisitor whyClassPoolvisitor =
                ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.whyAreYouKeeping,
                                                                        shortestUsagePrinter,
                                                                        shortestUsagePrinter);
            programClassPool.accept(whyClassPoolvisitor);
            libraryClassPool.accept(whyClassPoolvisitor);
        }
        if (configuration.printUsage != null)
        {
            PrintStream ps = isFile(configuration.printUsage) ?
                new PrintStream(new BufferedOutputStream(new FileOutputStream(configuration.printUsage))) :
                System.out;
            programClassPool.classesAcceptAlphabetically(
                new UsagePrinter(usageMarker, true, ps));
            if (ps != System.out)
            {
                ps.close();
            }
        }
        int originalProgramClassPoolSize = programClassPool.size();
        ClassPool newProgramClassPool = new ClassPool();
        programClassPool.classesAccept(
            new UsedClassFilter(usageMarker,
            new MultiClassVisitor(
            new ClassVisitor[] {
                new ClassShrinker(usageMarker),
                new ClassPoolFiller(newProgramClassPool)
            })));
        programClassPool.clear();
        int newProgramClassPoolSize = newProgramClassPool.size();
        if (newProgramClassPoolSize == 0)
        {
            throw new IOException("The output jar is empty. Did you specify the proper '-keep' options?");
        }
        if (configuration.verbose)
        {
            System.out.println("Removing unused program classes and class elements...");
            System.out.println("  Original number of program classes: " + originalProgramClassPoolSize);
            System.out.println("  Final number of program classes:    " + newProgramClassPoolSize);
        }
        return newProgramClassPool;
    }
    private boolean isFile(File file)
    {
        return file.getPath().length() > 0;
    }
}
