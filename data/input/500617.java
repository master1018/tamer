import proguard.classfile.ClassPool;
import proguard.classfile.attribute.visitor.AllAttributeVisitor;
import proguard.classfile.constant.visitor.*;
import proguard.classfile.instruction.visitor.AllInstructionVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.util.*;
import java.io.IOException;
import java.util.*;
public class Initializer
{
    private final Configuration configuration;
    public Initializer(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public void execute(ClassPool programClassPool,
                        ClassPool libraryClassPool) throws IOException
    {
        int originalLibraryClassPoolSize = libraryClassPool.size();
        ClassPool reducedLibraryClassPool = configuration.useUniqueClassMemberNames ?
            null : new ClassPool();
        WarningPrinter classReferenceWarningPrinter = new WarningPrinter(System.err, configuration.warn);
        WarningPrinter dependencyWarningPrinter     = new WarningPrinter(System.err, configuration.warn);
        programClassPool.classesAccept(
            new ClassSuperHierarchyInitializer(programClassPool,
                                               libraryClassPool,
                                               classReferenceWarningPrinter,
                                               null));
        libraryClassPool.classesAccept(
            new ClassSuperHierarchyInitializer(programClassPool,
                                               libraryClassPool,
                                               null,
                                               dependencyWarningPrinter));
        WarningPrinter memberReferenceWarningPrinter = new WarningPrinter(System.err, configuration.warn);
        programClassPool.classesAccept(
            new ClassReferenceInitializer(programClassPool,
                                          libraryClassPool,
                                          classReferenceWarningPrinter,
                                          memberReferenceWarningPrinter,
                                          null));
        if (reducedLibraryClassPool != null)
        {
            programClassPool.classesAccept(
                new ReferencedClassVisitor(
                new LibraryClassFilter(
                new ClassPoolFiller(reducedLibraryClassPool))));
            reducedLibraryClassPool.classesAccept(
                new ClassSuperHierarchyInitializer(programClassPool,
                                                   libraryClassPool,
                                                   classReferenceWarningPrinter,
                                                   null));
        }
        WarningPrinter dynamicClassReferenceNotePrinter = new WarningPrinter(System.out, configuration.note);
        WarningPrinter classForNameNotePrinter          = new WarningPrinter(System.out, configuration.note);
        programClassPool.classesAccept(
            new AllMethodVisitor(
            new AllAttributeVisitor(
            new AllInstructionVisitor(
            new DynamicClassReferenceInitializer(programClassPool,
                                                 libraryClassPool,
                                                 dynamicClassReferenceNotePrinter,
                                                 null,
                                                 classForNameNotePrinter,
                                                 createClassNoteExceptionMatcher(configuration.keep))))));
        WarningPrinter getMemberNotePrinter = new WarningPrinter(System.out, configuration.note);
        programClassPool.classesAccept(
            new AllMethodVisitor(
            new AllAttributeVisitor(
            new AllInstructionVisitor(
            new DynamicMemberReferenceInitializer(programClassPool,
                                                  libraryClassPool,
                                                  getMemberNotePrinter,
                                                  createClassMemberNoteExceptionMatcher(configuration.keep, true),
                                                  createClassMemberNoteExceptionMatcher(configuration.keep, false))))));
        if (configuration.adaptClassStrings != null)
        {
            programClassPool.classesAccept(
                new ClassNameFilter(configuration.adaptClassStrings,
                new AllConstantVisitor(
                new StringReferenceInitializer(programClassPool,
                                               libraryClassPool))));
        }
        WarningPrinter fullyQualifiedClassNameNotePrinter = new WarningPrinter(System.out, configuration.note);
        WarningPrinter descriptorKeepNotePrinter          = new WarningPrinter(System.out, configuration.note);
        new FullyQualifiedClassNameChecker(programClassPool,
                                           libraryClassPool,
                                           fullyQualifiedClassNameNotePrinter).checkClassSpecifications(configuration.keep);
        new DescriptorKeepChecker(programClassPool,
                                  libraryClassPool,
                                  descriptorKeepNotePrinter).checkClassSpecifications(configuration.keep);
        if (reducedLibraryClassPool != null)
        {
            programClassPool.classesAccept(
                new ReferencedClassVisitor(
                new LibraryClassFilter(
                new ClassHierarchyTraveler(true, true, true, false,
                new LibraryClassFilter(
                new ClassPoolFiller(reducedLibraryClassPool))))));
            reducedLibraryClassPool.classesAccept(
                new ClassReferenceInitializer(programClassPool,
                                              libraryClassPool,
                                              null,
                                              null,
                                              dependencyWarningPrinter));
            libraryClassPool.clear();
            reducedLibraryClassPool.classesAccept(
                new MultiClassVisitor(new ClassVisitor[]
                {
                    new ClassHierarchyTraveler(true, true, true, false,
                    new LibraryClassFilter(
                    new ClassPoolFiller(libraryClassPool))),
                    new ReferencedClassVisitor(
                    new LibraryClassFilter(
                    new ClassHierarchyTraveler(true, true, true, false,
                    new LibraryClassFilter(
                    new ClassPoolFiller(libraryClassPool)))))
                }));
        }
        else
        {
            libraryClassPool.classesAccept(
                new ClassReferenceInitializer(programClassPool,
                                              libraryClassPool,
                                              null,
                                              null,
                                              dependencyWarningPrinter));
        }
        programClassPool.classesAccept(new ClassSubHierarchyInitializer());
        libraryClassPool.classesAccept(new ClassSubHierarchyInitializer());
        programClassPool.classesAccept(new StringSharer());
        libraryClassPool.classesAccept(new StringSharer());
        int fullyQualifiedNoteCount = fullyQualifiedClassNameNotePrinter.getWarningCount();
        if (fullyQualifiedNoteCount > 0)
        {
            System.out.println("Note: there were " + fullyQualifiedNoteCount +
                               " references to unknown classes.");
            System.out.println("      You should check your configuration for typos.");
        }
        int descriptorNoteCount = descriptorKeepNotePrinter.getWarningCount();
        if (descriptorNoteCount > 0)
        {
            System.out.println("Note: there were " + descriptorNoteCount +
                               " unkept descriptor classes in kept class members.");
            System.out.println("      You should consider explicitly keeping the mentioned classes");
            System.out.println("      (using '-keep').");
        }
        int dynamicClassReferenceNoteCount = dynamicClassReferenceNotePrinter.getWarningCount();
        if (dynamicClassReferenceNoteCount > 0)
        {
            System.out.println("Note: there were " + dynamicClassReferenceNoteCount +
                               " unresolved dynamic references to classes or interfaces.");
            System.err.println("      You should check if you need to specify additional program jars.");
        }
        int classForNameNoteCount = classForNameNotePrinter.getWarningCount();
        if (classForNameNoteCount > 0)
        {
            System.out.println("Note: there were " + classForNameNoteCount +
                               " class casts of dynamically created class instances.");
            System.out.println("      You might consider explicitly keeping the mentioned classes and/or");
            System.out.println("      their implementations (using '-keep').");
        }
        int getmemberNoteCount = getMemberNotePrinter.getWarningCount();
        if (getmemberNoteCount > 0)
        {
            System.out.println("Note: there were " + getmemberNoteCount +
                               " accesses to class members by means of introspection.");
            System.out.println("      You should consider explicitly keeping the mentioned class members");
            System.out.println("      (using '-keep' or '-keepclassmembers').");
        }
        int classReferenceWarningCount = classReferenceWarningPrinter.getWarningCount();
        if (classReferenceWarningCount > 0)
        {
            System.err.println("Warning: there were " + classReferenceWarningCount +
                               " unresolved references to classes or interfaces.");
            System.err.println("         You may need to specify additional library jars (using '-libraryjars'),");
            System.err.println("         or perhaps the '-dontskipnonpubliclibraryclasses' option.");
        }
        int dependencyWarningCount = dependencyWarningPrinter.getWarningCount();
        if (dependencyWarningCount > 0)
        {
            System.err.println("Warning: there were " + dependencyWarningCount +
                               " instances of library classes depending on program classes.");
            System.err.println("         You must avoid such dependencies, since the program classes will");
            System.err.println("         be processed, while the library classes will remain unchanged.");
        }
        int memberReferenceWarningCount = memberReferenceWarningPrinter.getWarningCount();
        if (memberReferenceWarningCount > 0)
        {
            System.err.println("Warning: there were " + memberReferenceWarningCount +
                               " unresolved references to program class members.");
            System.err.println("         Your input classes appear to be inconsistent.");
            System.err.println("         You may need to recompile them and try again.");
            System.err.println("         Alternatively, you may have to specify the options ");
            System.err.println("         '-dontskipnonpubliclibraryclasses' and/or");
            System.err.println("         '-dontskipnonpubliclibraryclassmembers'.");
        }
        if ((classReferenceWarningCount   > 0 ||
             dependencyWarningCount       > 0 ||
             memberReferenceWarningCount  > 0) &&
            !configuration.ignoreWarnings)
        {
            throw new IOException("Please correct the above warnings first.");
        }
        if ((configuration.note == null ||
             !configuration.note.isEmpty()) &&
            (configuration.warn != null &&
             configuration.warn.isEmpty() ||
             configuration.ignoreWarnings))
        {
            System.out.println("Note: You're ignoring all warnings!");
        }
        if (configuration.verbose)
        {
            System.out.println("Ignoring unused library classes...");
            System.out.println("  Original number of library classes: " + originalLibraryClassPoolSize);
            System.out.println("  Final number of library classes:    " + libraryClassPool.size());
        }
    }
    private StringMatcher createClassNoteExceptionMatcher(List noteExceptions)
    {
        if (noteExceptions != null)
        {
            List noteExceptionNames = new ArrayList(noteExceptions.size());
            for (int index = 0; index < noteExceptions.size(); index++)
            {
                KeepClassSpecification keepClassSpecification = (KeepClassSpecification)noteExceptions.get(index);
                if (keepClassSpecification.markClasses)
                {
                    String className = keepClassSpecification.className;
                    if (className != null)
                    {
                        noteExceptionNames.add(className);
                    }
                    String extendsClassName = keepClassSpecification.extendsClassName;
                    if (extendsClassName != null)
                    {
                        noteExceptionNames.add(extendsClassName);
                    }
                }
            }
            if (noteExceptionNames.size() > 0)
            {
                return new ListParser(new ClassNameParser()).parse(noteExceptionNames);
            }
        }
        return null;
    }
    private StringMatcher createClassMemberNoteExceptionMatcher(List    noteExceptions,
                                                                boolean isField)
    {
        if (noteExceptions != null)
        {
            List noteExceptionNames = new ArrayList();
            for (int index = 0; index < noteExceptions.size(); index++)
            {
                KeepClassSpecification keepClassSpecification = (KeepClassSpecification)noteExceptions.get(index);
                List memberSpecifications = isField ?
                    keepClassSpecification.fieldSpecifications :
                    keepClassSpecification.methodSpecifications;
                if (memberSpecifications != null)
                {
                    for (int index2 = 0; index2 < memberSpecifications.size(); index2++)
                    {
                        MemberSpecification memberSpecification =
                            (MemberSpecification)memberSpecifications.get(index2);
                        String memberName = memberSpecification.name;
                        if (memberName != null)
                        {
                            noteExceptionNames.add(memberName);
                        }
                    }
                }
            }
            if (noteExceptionNames.size() > 0)
            {
                return new ListParser(new ClassNameParser()).parse(noteExceptionNames);
            }
        }
        return null;
    }
}
