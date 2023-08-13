import proguard.classfile.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.visitor.AllConstantVisitor;
import proguard.classfile.editor.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.util.*;
import java.io.*;
import java.util.*;
public class Obfuscator
{
    private final Configuration configuration;
    public Obfuscator(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public void execute(ClassPool programClassPool,
                        ClassPool libraryClassPool) throws IOException
    {
        if (configuration.keep         == null &&
            configuration.applyMapping == null &&
            configuration.printMapping == null)
        {
            throw new IOException("You have to specify '-keep' options for the obfuscation step.");
        }
        programClassPool.classesAccept(new ClassCleaner());
        libraryClassPool.classesAccept(new ClassCleaner());
        ClassVisitor memberInfoLinker =
            configuration.useUniqueClassMemberNames ?
                (ClassVisitor)new AllMemberVisitor(new MethodLinker()) :
                (ClassVisitor)new BottomClassFilter(new MethodLinker());
        programClassPool.classesAccept(memberInfoLinker);
        libraryClassPool.classesAccept(memberInfoLinker);
        NameMarker nameMarker = new NameMarker();
        ClassPoolVisitor classPoolvisitor =
            ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keep,
                                                                    nameMarker,
                                                                    nameMarker,
                                                                    false,
                                                                    false,
                                                                    true);
        programClassPool.accept(classPoolvisitor);
        libraryClassPool.accept(classPoolvisitor);
        libraryClassPool.classesAccept(nameMarker);
        libraryClassPool.classesAccept(new AllMemberVisitor(nameMarker));
        AttributeUsageMarker requiredAttributeUsageMarker =
            new AttributeUsageMarker();
        AttributeVisitor optionalAttributeUsageMarker =
            configuration.keepAttributes == null ? null :
                new AttributeNameFilter(new ListParser(new NameParser()).parse(configuration.keepAttributes),
                                        requiredAttributeUsageMarker);
        programClassPool.classesAccept(
            new AllAttributeVisitor(true,
            new RequiredAttributeFilter(requiredAttributeUsageMarker,
                                        optionalAttributeUsageMarker)));
        programClassPool.classesAccept(new AttributeShrinker());
        if (configuration.applyMapping != null)
        {
            WarningPrinter warningPrinter = new WarningPrinter(System.err, configuration.warn);
            MappingReader reader = new MappingReader(configuration.applyMapping);
            MappingProcessor keeper =
                new MultiMappingProcessor(new MappingProcessor[]
                {
                    new MappingKeeper(programClassPool, warningPrinter),
                    new MappingKeeper(libraryClassPool, null),
                });
            reader.pump(keeper);
            int mappingWarningCount = warningPrinter.getWarningCount();
            if (mappingWarningCount > 0)
            {
                System.err.println("Warning: there were " + mappingWarningCount +
                                                            " kept classes and class members that were remapped anyway.");
                System.err.println("         You should adapt your configuration or edit the mapping file.");
                if (!configuration.ignoreWarnings)
                {
                    System.err.println("         If you are sure this remapping won't hurt,");
                    System.err.println("         you could try your luck using the '-ignorewarnings' option.");
                    throw new IOException("Please correct the above warnings first.");
                }
            }
        }
        DictionaryNameFactory classNameFactory = configuration.classObfuscationDictionary != null ?
            new DictionaryNameFactory(configuration.classObfuscationDictionary, null) :
            null;
        DictionaryNameFactory packageNameFactory = configuration.packageObfuscationDictionary != null ?
            new DictionaryNameFactory(configuration.packageObfuscationDictionary, null) :
            null;
        programClassPool.classesAccept(
            new ClassObfuscator(programClassPool,
                                classNameFactory,
                                packageNameFactory,
                                configuration.useMixedCaseClassNames,
                                configuration.keepPackageNames,
                                configuration.flattenPackageHierarchy,
                                configuration.repackageClasses,
                                configuration.allowAccessModification));
        NameFactory nameFactory = new SimpleNameFactory();
        if (configuration.obfuscationDictionary != null)
        {
            nameFactory = new DictionaryNameFactory(configuration.obfuscationDictionary,
                                                    nameFactory);
        }
        WarningPrinter warningPrinter = new WarningPrinter(System.err, configuration.warn);
        Map descriptorMap = new HashMap();
        if (configuration.useUniqueClassMemberNames)
        {
            programClassPool.classesAccept(
                new AllMemberVisitor(
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)));
            programClassPool.classesAccept(
                new AllMemberVisitor(
                new MemberObfuscator(configuration.overloadAggressively,
                                     nameFactory,
                                     descriptorMap)));
        }
        else
        {
            programClassPool.classesAccept(
                new MultiClassVisitor(new ClassVisitor[]
                {
                    new ClassHierarchyTraveler(true, false, false, true,
                    new AllMemberVisitor(
                    new MemberAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                    new MemberNameCollector(configuration.overloadAggressively,
                                            descriptorMap)))),
                    new ClassHierarchyTraveler(true, true, true, true,
                    new AllMemberVisitor(
                    new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                    new MemberNameCollector(configuration.overloadAggressively,
                                            descriptorMap)))),
                    new AllMemberVisitor(
                    new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                    new MemberObfuscator(configuration.overloadAggressively,
                                         nameFactory,
                                         descriptorMap))),
                    new MapCleaner(descriptorMap)
                }));
            programClassPool.classesAccept(
                new MultiClassVisitor(new ClassVisitor[]
                {
                    new AllMemberVisitor(
                    new MemberNameCollector(configuration.overloadAggressively,
                                            descriptorMap)),
                    new ClassHierarchyTraveler(false, true, true, false,
                    new AllMemberVisitor(
                    new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                    new MemberNameCollector(configuration.overloadAggressively,
                                            descriptorMap)))),
                    new AllMemberVisitor(
                    new MemberAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                    new MemberObfuscator(configuration.overloadAggressively,
                                         nameFactory,
                                         descriptorMap))),
                    new MapCleaner(descriptorMap)
                }));
        }
        NameFactory specialNameFactory =
            new SpecialNameFactory(new SimpleNameFactory());
        Map specialDescriptorMap = new HashMap();
        programClassPool.classesAccept(
            new AllMemberVisitor(
            new MemberSpecialNameFilter(
            new MemberNameCollector(configuration.overloadAggressively,
                                    specialDescriptorMap))));
        libraryClassPool.classesAccept(
            new AllMemberVisitor(
            new MemberSpecialNameFilter(
            new MemberNameCollector(configuration.overloadAggressively,
                                    specialDescriptorMap))));
        programClassPool.classesAccept(
            new MultiClassVisitor(new ClassVisitor[]
            {
                new ClassHierarchyTraveler(true, false, false, true,
                new AllMemberVisitor(
                new MemberAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)))),
                new ClassHierarchyTraveler(true, true, true, false,
                new AllMemberVisitor(
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)))),
                new ClassHierarchyTraveler(true, true, true, false,
                new AllMemberVisitor(
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberNameConflictFixer(configuration.overloadAggressively,
                                            descriptorMap,
                                            warningPrinter,
                new MemberObfuscator(configuration.overloadAggressively,
                                     specialNameFactory,
                                     specialDescriptorMap))))),
                new MapCleaner(descriptorMap)
            }));
        programClassPool.classesAccept(
            new MultiClassVisitor(new ClassVisitor[]
            {
                new AllMemberVisitor(
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)),
                new ClassHierarchyTraveler(false, true, true, false,
                new AllMemberVisitor(
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberNameCollector(configuration.overloadAggressively,
                                        descriptorMap)))),
                new AllMemberVisitor(
                new MemberAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                new MemberNameConflictFixer(configuration.overloadAggressively,
                                            descriptorMap,
                                            warningPrinter,
                new MemberObfuscator(configuration.overloadAggressively,
                                     specialNameFactory,
                                     specialDescriptorMap)))),
                new MapCleaner(descriptorMap)
            }));
        int warningCount = warningPrinter.getWarningCount();
        if (warningCount > 0)
        {
            System.err.println("Warning: there were " + warningCount +
                               " conflicting class member name mappings.");
            System.err.println("         Your configuration may be inconsistent.");
            if (!configuration.ignoreWarnings)
            {
                System.err.println("         If you are sure the conflicts are harmless,");
                System.err.println("         you could try your luck using the '-ignorewarnings' option.");
                throw new IOException("Please correct the above warnings first.");
            }
        }
        if (configuration.printMapping != null)
        {
            PrintStream ps = isFile(configuration.printMapping) ?
                new PrintStream(new BufferedOutputStream(new FileOutputStream(configuration.printMapping))) :
                System.out;
            programClassPool.classesAcceptAlphabetically(new MappingPrinter(ps));
            if (ps != System.out)
            {
                ps.close();
            }
        }
        programClassPool.classesAccept(new ClassRenamer());
        libraryClassPool.classesAccept(new ClassRenamer());
        programClassPool.classesAccept(new ClassReferenceFixer(false));
        libraryClassPool.classesAccept(new ClassReferenceFixer(false));
        programClassPool.classesAccept(new MemberReferenceFixer());
        if (configuration.repackageClasses != null &&
            configuration.allowAccessModification)
        {
            programClassPool.classesAccept(
                new AllConstantVisitor(
                new AccessFixer()));
        }
        if (configuration.newSourceFileAttribute != null)
        {
            programClassPool.classesAccept(new SourceFileRenamer(configuration.newSourceFileAttribute));
        }
        programClassPool.classesAccept(new NameAndTypeUsageMarker());
        programClassPool.classesAccept(new NameAndTypeShrinker());
        programClassPool.classesAccept(new Utf8UsageMarker());
        programClassPool.classesAccept(new Utf8Shrinker());
    }
    private boolean isFile(File file)
    {
        return file.getPath().length() > 0;
    }
}
