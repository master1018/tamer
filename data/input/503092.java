import proguard.classfile.ClassConstants;
import proguard.classfile.util.ClassUtil;
import proguard.util.ListUtil;
import java.io.*;
import java.net.URL;
import java.util.*;
public class ConfigurationParser
{
    private WordReader reader;
    private String     nextWord;
    private String     lastComments;
    public ConfigurationParser(String[] args) throws IOException
    {
        this(args, null);
    }
    public ConfigurationParser(String[] args,
                               File     baseDir) throws IOException
    {
        reader = new ArgumentWordReader(args, baseDir);
        readNextWord();
    }
    public ConfigurationParser(File file) throws IOException
    {
        reader = new FileWordReader(file);
        readNextWord();
    }
    public ConfigurationParser(URL url) throws IOException
    {
        reader = new FileWordReader(url);
        readNextWord();
    }
    public void parse(Configuration configuration)
    throws ParseException, IOException
    {
        while (nextWord != null)
        {
            lastComments = reader.lastComments();
            if      (ConfigurationConstants.AT_DIRECTIVE                                     .startsWith(nextWord) ||
                     ConfigurationConstants.INCLUDE_DIRECTIVE                                .startsWith(nextWord)) configuration.lastModified                     = parseIncludeArgument(configuration.lastModified);
            else if (ConfigurationConstants.BASE_DIRECTORY_DIRECTIVE                         .startsWith(nextWord)) parseBaseDirectoryArgument();
            else if (ConfigurationConstants.INJARS_OPTION                                    .startsWith(nextWord)) configuration.programJars                      = parseClassPathArgument(configuration.programJars, false);
            else if (ConfigurationConstants.OUTJARS_OPTION                                   .startsWith(nextWord)) configuration.programJars                      = parseClassPathArgument(configuration.programJars, true);
            else if (ConfigurationConstants.LIBRARYJARS_OPTION                               .startsWith(nextWord)) configuration.libraryJars                      = parseClassPathArgument(configuration.libraryJars, false);
            else if (ConfigurationConstants.RESOURCEJARS_OPTION                              .startsWith(nextWord)) throw new ParseException("The '-resourcejars' option is no longer supported. Please use the '-injars' option for all input");
            else if (ConfigurationConstants.DONT_SKIP_NON_PUBLIC_LIBRARY_CLASSES_OPTION      .startsWith(nextWord)) configuration.skipNonPublicLibraryClasses      = parseNoArgument(false);
            else if (ConfigurationConstants.DONT_SKIP_NON_PUBLIC_LIBRARY_CLASS_MEMBERS_OPTION.startsWith(nextWord)) configuration.skipNonPublicLibraryClassMembers = parseNoArgument(false);
            else if (ConfigurationConstants.TARGET_OPTION                                    .startsWith(nextWord)) configuration.targetClassVersion               = parseClassVersion();
            else if (ConfigurationConstants.FORCE_PROCESSING_OPTION                          .startsWith(nextWord)) configuration.lastModified                     = parseNoArgument(Long.MAX_VALUE);
            else if (ConfigurationConstants.KEEP_OPTION                                      .startsWith(nextWord)) configuration.keep                             = parseKeepClassSpecificationArguments(configuration.keep, true,  false, false);
            else if (ConfigurationConstants.KEEP_CLASS_MEMBERS_OPTION                        .startsWith(nextWord)) configuration.keep                             = parseKeepClassSpecificationArguments(configuration.keep, false, false, false);
            else if (ConfigurationConstants.KEEP_CLASSES_WITH_MEMBERS_OPTION                 .startsWith(nextWord)) configuration.keep                             = parseKeepClassSpecificationArguments(configuration.keep, false, true,  false);
            else if (ConfigurationConstants.KEEP_NAMES_OPTION                                .startsWith(nextWord)) configuration.keep                             = parseKeepClassSpecificationArguments(configuration.keep, true,  false, true);
            else if (ConfigurationConstants.KEEP_CLASS_MEMBER_NAMES_OPTION                   .startsWith(nextWord)) configuration.keep                             = parseKeepClassSpecificationArguments(configuration.keep, false, false, true);
            else if (ConfigurationConstants.KEEP_CLASSES_WITH_MEMBER_NAMES_OPTION            .startsWith(nextWord)) configuration.keep                             = parseKeepClassSpecificationArguments(configuration.keep, false, true,  true);
            else if (ConfigurationConstants.PRINT_SEEDS_OPTION                               .startsWith(nextWord)) configuration.printSeeds                       = parseOptionalFile();
            else if (ConfigurationConstants.KEEP_DIRECTORIES_OPTION                          .startsWith(nextWord)) configuration.keepDirectories                  = parseCommaSeparatedList("directory name", true, true, false, false, true, false, false, configuration.keepDirectories);
            else if (ConfigurationConstants.DONT_SHRINK_OPTION                               .startsWith(nextWord)) configuration.shrink                           = parseNoArgument(false);
            else if (ConfigurationConstants.PRINT_USAGE_OPTION                               .startsWith(nextWord)) configuration.printUsage                       = parseOptionalFile();
            else if (ConfigurationConstants.WHY_ARE_YOU_KEEPING_OPTION                       .startsWith(nextWord)) configuration.whyAreYouKeeping                 = parseClassSpecificationArguments(configuration.whyAreYouKeeping);
            else if (ConfigurationConstants.DONT_OPTIMIZE_OPTION                             .startsWith(nextWord)) configuration.optimize                         = parseNoArgument(false);
            else if (ConfigurationConstants.OPTIMIZATION_PASSES                              .startsWith(nextWord)) configuration.optimizationPasses               = parseIntegerArgument();
            else if (ConfigurationConstants.OPTIMIZATIONS                                    .startsWith(nextWord)) configuration.optimizations                    = parseCommaSeparatedList("optimization name", true, false, false, false, false, false, false, configuration.optimizations);
            else if (ConfigurationConstants.ASSUME_NO_SIDE_EFFECTS_OPTION                    .startsWith(nextWord)) configuration.assumeNoSideEffects              = parseClassSpecificationArguments(configuration.assumeNoSideEffects);
            else if (ConfigurationConstants.ALLOW_ACCESS_MODIFICATION_OPTION                 .startsWith(nextWord)) configuration.allowAccessModification          = parseNoArgument(true);
            else if (ConfigurationConstants.MERGE_INTERFACES_AGGRESSIVELY_OPTION             .startsWith(nextWord)) configuration.mergeInterfacesAggressively      = parseNoArgument(true);
            else if (ConfigurationConstants.DONT_OBFUSCATE_OPTION                            .startsWith(nextWord)) configuration.obfuscate                        = parseNoArgument(false);
            else if (ConfigurationConstants.PRINT_MAPPING_OPTION                             .startsWith(nextWord)) configuration.printMapping                     = parseOptionalFile();
            else if (ConfigurationConstants.APPLY_MAPPING_OPTION                             .startsWith(nextWord)) configuration.applyMapping                     = parseFile();
            else if (ConfigurationConstants.OBFUSCATION_DICTIONARY_OPTION                    .startsWith(nextWord)) configuration.obfuscationDictionary            = parseFile();
            else if (ConfigurationConstants.CLASS_OBFUSCATION_DICTIONARY_OPTION              .startsWith(nextWord)) configuration.classObfuscationDictionary       = parseFile();
            else if (ConfigurationConstants.PACKAGE_OBFUSCATION_DICTIONARY_OPTION            .startsWith(nextWord)) configuration.packageObfuscationDictionary     = parseFile();
            else if (ConfigurationConstants.OVERLOAD_AGGRESSIVELY_OPTION                     .startsWith(nextWord)) configuration.overloadAggressively             = parseNoArgument(true);
            else if (ConfigurationConstants.USE_UNIQUE_CLASS_MEMBER_NAMES_OPTION             .startsWith(nextWord)) configuration.useUniqueClassMemberNames        = parseNoArgument(true);
            else if (ConfigurationConstants.DONT_USE_MIXED_CASE_CLASS_NAMES_OPTION           .startsWith(nextWord)) configuration.useMixedCaseClassNames           = parseNoArgument(false);
            else if (ConfigurationConstants.KEEP_PACKAGE_NAMES_OPTION                        .startsWith(nextWord)) configuration.keepPackageNames                 = parseCommaSeparatedList("package name", true, true, false, true, false, true, false, configuration.keepPackageNames);
            else if (ConfigurationConstants.FLATTEN_PACKAGE_HIERARCHY_OPTION                 .startsWith(nextWord)) configuration.flattenPackageHierarchy          = ClassUtil.internalClassName(parseOptionalArgument());
            else if (ConfigurationConstants.REPACKAGE_CLASSES_OPTION                         .startsWith(nextWord)) configuration.repackageClasses                 = ClassUtil.internalClassName(parseOptionalArgument());
            else if (ConfigurationConstants.DEFAULT_PACKAGE_OPTION                           .startsWith(nextWord)) configuration.repackageClasses                 = ClassUtil.internalClassName(parseOptionalArgument());
            else if (ConfigurationConstants.KEEP_ATTRIBUTES_OPTION                           .startsWith(nextWord)) configuration.keepAttributes                   = parseCommaSeparatedList("attribute name", true, true, false, true, false, false, false, configuration.keepAttributes);
            else if (ConfigurationConstants.RENAME_SOURCE_FILE_ATTRIBUTE_OPTION              .startsWith(nextWord)) configuration.newSourceFileAttribute           = parseOptionalArgument();
            else if (ConfigurationConstants.ADAPT_CLASS_STRINGS_OPTION                       .startsWith(nextWord)) configuration.adaptClassStrings                = parseCommaSeparatedList("class name", true, true, false, true, false, true, false, configuration.adaptClassStrings);
            else if (ConfigurationConstants.ADAPT_RESOURCE_FILE_NAMES_OPTION                 .startsWith(nextWord)) configuration.adaptResourceFileNames           = parseCommaSeparatedList("resource file name", true, true, false, false, false, false, false, configuration.adaptResourceFileNames);
            else if (ConfigurationConstants.ADAPT_RESOURCE_FILE_CONTENTS_OPTION              .startsWith(nextWord)) configuration.adaptResourceFileContents        = parseCommaSeparatedList("resource file name", true, true, false, false, false, false, false, configuration.adaptResourceFileContents);
            else if (ConfigurationConstants.DONT_PREVERIFY_OPTION                            .startsWith(nextWord)) configuration.preverify                        = parseNoArgument(false);
            else if (ConfigurationConstants.MICRO_EDITION_OPTION                             .startsWith(nextWord)) configuration.microEdition                     = parseNoArgument(true);
            else if (ConfigurationConstants.VERBOSE_OPTION                                   .startsWith(nextWord)) configuration.verbose                          = parseNoArgument(true);
            else if (ConfigurationConstants.DONT_NOTE_OPTION                                 .startsWith(nextWord)) configuration.note                             = parseCommaSeparatedList("class name", true, true, false, true, false, true, false, configuration.note);
            else if (ConfigurationConstants.DONT_WARN_OPTION                                 .startsWith(nextWord)) configuration.warn                             = parseCommaSeparatedList("class name", true, true, false, true, false, true, false, configuration.warn);
            else if (ConfigurationConstants.IGNORE_WARNINGS_OPTION                           .startsWith(nextWord)) configuration.ignoreWarnings                   = parseNoArgument(true);
            else if (ConfigurationConstants.PRINT_CONFIGURATION_OPTION                       .startsWith(nextWord)) configuration.printConfiguration               = parseOptionalFile();
            else if (ConfigurationConstants.DUMP_OPTION                                      .startsWith(nextWord)) configuration.dump                             = parseOptionalFile();
            else
            {
                throw new ParseException("Unknown option " + reader.locationDescription());
            }
        }
    }
    public void close() throws IOException
    {
        if (reader != null)
        {
            reader.close();
        }
    }
    private long parseIncludeArgument(long lastModified) throws ParseException, IOException
    {
        readNextWord("configuration file name");
        File file = file(nextWord);
        reader.includeWordReader(new FileWordReader(file));
        readNextWord();
        return Math.max(lastModified, file.lastModified());
    }
    private void parseBaseDirectoryArgument() throws ParseException, IOException
    {
        readNextWord("base directory name");
        reader.setBaseDir(file(nextWord));
        readNextWord();
    }
    private ClassPath parseClassPathArgument(ClassPath classPath,
                                             boolean   isOutput)
    throws ParseException, IOException
    {
        if (classPath == null)
        {
            classPath = new ClassPath();
        }
        while (true)
        {
            readNextWord("jar or directory name");
            ClassPathEntry entry = new ClassPathEntry(file(nextWord), isOutput);
            readNextWord();
            if (!configurationEnd() &&
                ConfigurationConstants.OPEN_ARGUMENTS_KEYWORD.equals(nextWord))
            {
                List[] filters = new List[5];
                int counter = 0;
                do
                {
                    filters[counter++] =
                        parseCommaSeparatedList("filter", true, false, true, false, true, false, false, null);
                }
                while (counter < filters.length &&
                       ConfigurationConstants.SEPARATOR_KEYWORD.equals(nextWord));
                if (!ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD.equals(nextWord))
                {
                    throw new ParseException("Expecting separating '" + ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD +
                                             "' or '" + ConfigurationConstants.SEPARATOR_KEYWORD +
                                             "', or closing '" + ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD +
                                             "' before " + reader.locationDescription());
                }
                entry.setFilter(filters[--counter]);
                if (counter > 0)
                {
                    entry.setJarFilter(filters[--counter]);
                    if (counter > 0)
                    {
                        entry.setWarFilter(filters[--counter]);
                        if (counter > 0)
                        {
                            entry.setEarFilter(filters[--counter]);
                            if (counter > 0)
                            {
                                entry.setZipFilter(filters[--counter]);
                            }
                        }
                    }
                }
                readNextWord();
            }
            classPath.add(entry);
            if (configurationEnd())
            {
                return classPath;
            }
            if (!nextWord.equals(ConfigurationConstants.JAR_SEPARATOR_KEYWORD))
            {
                throw new ParseException("Expecting class path separator '" + ConfigurationConstants.JAR_SEPARATOR_KEYWORD +
                                         "' before " + reader.locationDescription());
            }
        }
    }
    private int parseClassVersion()
    throws ParseException, IOException
    {
        readNextWord("java version");
        int classVersion = ClassUtil.internalClassVersion(nextWord);
        if (classVersion == 0)
        {
            throw new ParseException("Unsupported java version " + reader.locationDescription());
        }
        readNextWord();
        return classVersion;
    }
    private int parseIntegerArgument()
    throws ParseException, IOException
    {
        try
        {
            readNextWord("integer");
            int integer = Integer.parseInt(nextWord);
            readNextWord();
            return integer;
        }
        catch (NumberFormatException e)
        {
            throw new ParseException("Expecting integer argument instead of '" + nextWord +
                                     "' before " + reader.locationDescription());
        }
    }
    private File parseFile()
    throws ParseException, IOException
    {
        readNextWord("file name");
        File file = file(nextWord);
        readNextWord();
        return file;
    }
    private File parseOptionalFile()
    throws ParseException, IOException
    {
        readNextWord();
        if (configurationEnd())
        {
            return new File("");
        }
        File file = file(nextWord);
        readNextWord();
        return file;
    }
    private String parseOptionalArgument() throws IOException
    {
        readNextWord();
        if (configurationEnd())
        {
            return "";
        }
        String fileName = nextWord;
        readNextWord();
        return fileName;
    }
    private boolean parseNoArgument(boolean value) throws IOException
    {
        readNextWord();
        return value;
    }
    private long parseNoArgument(long value) throws IOException
    {
        readNextWord();
        return value;
    }
    private List parseKeepClassSpecificationArguments(List    keepClassSpecifications,
                                                      boolean markClasses,
                                                      boolean markConditionally,
                                                      boolean allowShrinking)
    throws ParseException, IOException
    {
        if (keepClassSpecifications == null)
        {
            keepClassSpecifications = new ArrayList();
        }
        boolean allowOptimization = false;
        boolean allowObfuscation  = false;
        while (true)
        {
            readNextWord("keyword '" + ConfigurationConstants.CLASS_KEYWORD +
                         "', '"      + ClassConstants.EXTERNAL_ACC_INTERFACE +
                         "', or '"   + ClassConstants.EXTERNAL_ACC_ENUM + "'", true);
            if (!ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD.equals(nextWord))
            {
                break;
            }
            readNextWord("keyword '" + ConfigurationConstants.ALLOW_SHRINKING_SUBOPTION +
                         "', '"      + ConfigurationConstants.ALLOW_OPTIMIZATION_SUBOPTION +
                         "', or '"   + ConfigurationConstants.ALLOW_OBFUSCATION_SUBOPTION + "'");
            if      (ConfigurationConstants.ALLOW_SHRINKING_SUBOPTION   .startsWith(nextWord))
            {
                allowShrinking    = true;
            }
            else if (ConfigurationConstants.ALLOW_OPTIMIZATION_SUBOPTION.startsWith(nextWord))
            {
                allowOptimization = true;
            }
            else if (ConfigurationConstants.ALLOW_OBFUSCATION_SUBOPTION .startsWith(nextWord))
            {
                allowObfuscation  = true;
            }
            else
            {
                throw new ParseException("Expecting keyword '" + ConfigurationConstants.ALLOW_SHRINKING_SUBOPTION +
                                         "', '"                + ConfigurationConstants.ALLOW_OPTIMIZATION_SUBOPTION +
                                         "', or '"             + ConfigurationConstants.ALLOW_OBFUSCATION_SUBOPTION +
                                         "' before " + reader.locationDescription());
            }
        }
        ClassSpecification classSpecification =
            parseClassSpecificationArguments();
        keepClassSpecifications.add(new KeepClassSpecification(markClasses,
                                                               markConditionally,
                                                               allowShrinking,
                                                               allowOptimization,
                                                               allowObfuscation,
                                                               classSpecification));
        return keepClassSpecifications;
    }
    private List parseClassSpecificationArguments(List classSpecifications)
    throws ParseException, IOException
    {
        if (classSpecifications == null)
        {
            classSpecifications = new ArrayList();
        }
        readNextWord("keyword '" + ConfigurationConstants.CLASS_KEYWORD +
                     "', '"      + ClassConstants.EXTERNAL_ACC_INTERFACE +
                     "', or '"   + ClassConstants.EXTERNAL_ACC_ENUM + "'", true);
        classSpecifications.add(parseClassSpecificationArguments());
        return classSpecifications;
    }
    private ClassSpecification parseClassSpecificationArguments()
    throws ParseException, IOException
    {
        String annotationType = null;
        int requiredSetClassAccessFlags   = 0;
        int requiredUnsetClassAccessFlags = 0;
        while (!ConfigurationConstants.CLASS_KEYWORD.equals(nextWord))
        {
            String strippedWord = nextWord.startsWith(ConfigurationConstants.NEGATOR_KEYWORD) ?
                nextWord.substring(1) :
                nextWord;
            int accessFlag =
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_PUBLIC)     ? ClassConstants.INTERNAL_ACC_PUBLIC      :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_FINAL)      ? ClassConstants.INTERNAL_ACC_FINAL       :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_INTERFACE)  ? ClassConstants.INTERNAL_ACC_INTERFACE   :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_ABSTRACT)   ? ClassConstants.INTERNAL_ACC_ABSTRACT    :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_ANNOTATION) ? ClassConstants.INTERNAL_ACC_ANNOTATTION :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_ENUM)       ? ClassConstants.INTERNAL_ACC_ENUM        :
                                                                              unknownAccessFlag();
            if (accessFlag == ClassConstants.INTERNAL_ACC_ANNOTATTION)
            {
                readNextWord("annotation type or keyword '" + ClassConstants.EXTERNAL_ACC_INTERFACE + "'", false);
                if (!nextWord.equals(ClassConstants.EXTERNAL_ACC_INTERFACE) &&
                    !nextWord.equals(ClassConstants.EXTERNAL_ACC_ENUM)      &&
                    !nextWord.equals(ConfigurationConstants.CLASS_KEYWORD))
                {
                    annotationType =
                        ListUtil.commaSeparatedString(
                        parseCommaSeparatedList("annotation type",
                                                false, false, false, true, false, false, true, null));
                    continue;
                }
            }
            if (strippedWord.equals(nextWord))
            {
                requiredSetClassAccessFlags   |= accessFlag;
            }
            else
            {
                requiredUnsetClassAccessFlags |= accessFlag;
            }
            if ((requiredSetClassAccessFlags &
                 requiredUnsetClassAccessFlags) != 0)
            {
                throw new ParseException("Conflicting class access modifiers for '" + strippedWord +
                                         "' before " + reader.locationDescription());
            }
            if (strippedWord.equals(ClassConstants.EXTERNAL_ACC_INTERFACE) ||
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_ENUM)      ||
                strippedWord.equals(ConfigurationConstants.CLASS_KEYWORD))
            {
                break;
            }
            readNextWord("keyword '" + ConfigurationConstants.CLASS_KEYWORD +
                         "', '"      + ClassConstants.EXTERNAL_ACC_INTERFACE +
                         "', or '"   + ClassConstants.EXTERNAL_ACC_ENUM + "'", true);
        }
        String externalClassName =
            ListUtil.commaSeparatedString(
            parseCommaSeparatedList("class name or interface name",
                                    true, false, false, true, false, false, false, null));
        String className = ConfigurationConstants.ANY_CLASS_KEYWORD.equals(externalClassName) ?
            null :
            ClassUtil.internalClassName(externalClassName);
        String extendsAnnotationType = null;
        String extendsClassName      = null;
        if (!configurationEnd())
        {
            if (ConfigurationConstants.IMPLEMENTS_KEYWORD.equals(nextWord) ||
                ConfigurationConstants.EXTENDS_KEYWORD.equals(nextWord))
            {
                readNextWord("class name or interface name", true);
                if (ConfigurationConstants.ANNOTATION_KEYWORD.equals(nextWord))
                {
                    extendsAnnotationType =
                        ListUtil.commaSeparatedString(
                        parseCommaSeparatedList("annotation type",
                                                true, false, false, true, false, false, true, null));
                }
                String externalExtendsClassName =
                    ListUtil.commaSeparatedString(
                    parseCommaSeparatedList("class name or interface name",
                                            false, false, false, true, false, false, false, null));
                extendsClassName = ConfigurationConstants.ANY_CLASS_KEYWORD.equals(externalExtendsClassName) ?
                    null :
                    ClassUtil.internalClassName(externalExtendsClassName);
            }
        }
        ClassSpecification classSpecification =
            new ClassSpecification(lastComments,
                                   requiredSetClassAccessFlags,
                                   requiredUnsetClassAccessFlags,
                                   annotationType,
                                   className,
                                   extendsAnnotationType,
                                   extendsClassName);
        if (!configurationEnd())
        {
            if (!ConfigurationConstants.OPEN_KEYWORD.equals(nextWord))
            {
                throw new ParseException("Expecting opening '" + ConfigurationConstants.OPEN_KEYWORD +
                                         "' at " + reader.locationDescription());
            }
            while (true)
            {
                readNextWord("class member description" +
                             " or closing '" + ConfigurationConstants.CLOSE_KEYWORD + "'", true);
                if (nextWord.equals(ConfigurationConstants.CLOSE_KEYWORD))
                {
                    readNextWord();
                    break;
                }
                parseMemberSpecificationArguments(externalClassName,
                                                  classSpecification);
            }
        }
        return classSpecification;
    }
    private void parseMemberSpecificationArguments(String             externalClassName,
                                                   ClassSpecification classSpecification)
    throws ParseException, IOException
    {
        String annotationType = null;
        int requiredSetMemberAccessFlags   = 0;
        int requiredUnsetMemberAccessFlags = 0;
        while (!configurationEnd(true))
        {
            if (ConfigurationConstants.ANNOTATION_KEYWORD.equals(nextWord))
            {
                annotationType =
                    ListUtil.commaSeparatedString(
                    parseCommaSeparatedList("annotation type",
                                            true, false, false, true, false, false, true, null));
                continue;
            }
            String strippedWord = nextWord.startsWith("!") ?
                nextWord.substring(1) :
                nextWord;
            int accessFlag =
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_PUBLIC)       ? ClassConstants.INTERNAL_ACC_PUBLIC       :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_PRIVATE)      ? ClassConstants.INTERNAL_ACC_PRIVATE      :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_PROTECTED)    ? ClassConstants.INTERNAL_ACC_PROTECTED    :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_STATIC)       ? ClassConstants.INTERNAL_ACC_STATIC       :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_FINAL)        ? ClassConstants.INTERNAL_ACC_FINAL        :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_SYNCHRONIZED) ? ClassConstants.INTERNAL_ACC_SYNCHRONIZED :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_VOLATILE)     ? ClassConstants.INTERNAL_ACC_VOLATILE     :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_TRANSIENT)    ? ClassConstants.INTERNAL_ACC_TRANSIENT    :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_NATIVE)       ? ClassConstants.INTERNAL_ACC_NATIVE       :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_ABSTRACT)     ? ClassConstants.INTERNAL_ACC_ABSTRACT     :
                strippedWord.equals(ClassConstants.EXTERNAL_ACC_STRICT)       ? ClassConstants.INTERNAL_ACC_STRICT       :
                                                                                0;
            if (accessFlag == 0)
            {
                break;
            }
            if (strippedWord.equals(nextWord))
            {
                requiredSetMemberAccessFlags   |= accessFlag;
            }
            else
            {
                requiredUnsetMemberAccessFlags |= accessFlag;
            }
            if ((requiredSetMemberAccessFlags &
                 requiredUnsetMemberAccessFlags) != 0)
            {
                throw new ParseException("Conflicting class member access modifiers for " +
                                         reader.locationDescription());
            }
            readNextWord("class member description");
        }
        if (ConfigurationConstants.ANY_CLASS_MEMBER_KEYWORD.equals(nextWord) ||
            ConfigurationConstants.ANY_FIELD_KEYWORD       .equals(nextWord) ||
            ConfigurationConstants.ANY_METHOD_KEYWORD      .equals(nextWord))
        {
            if (ConfigurationConstants.ANY_CLASS_MEMBER_KEYWORD.equals(nextWord))
            {
                checkFieldAccessFlags(requiredSetMemberAccessFlags,
                                      requiredUnsetMemberAccessFlags);
                checkMethodAccessFlags(requiredSetMemberAccessFlags,
                                       requiredUnsetMemberAccessFlags);
                classSpecification.addField(
                    new MemberSpecification(requiredSetMemberAccessFlags,
                                            requiredUnsetMemberAccessFlags,
                                            annotationType,
                                            null,
                                            null));
                classSpecification.addMethod(
                    new MemberSpecification(requiredSetMemberAccessFlags,
                                            requiredUnsetMemberAccessFlags,
                                            annotationType,
                                            null,
                                            null));
            }
            else if (ConfigurationConstants.ANY_FIELD_KEYWORD.equals(nextWord))
            {
                checkFieldAccessFlags(requiredSetMemberAccessFlags,
                                      requiredUnsetMemberAccessFlags);
                classSpecification.addField(
                    new MemberSpecification(requiredSetMemberAccessFlags,
                                            requiredUnsetMemberAccessFlags,
                                            annotationType,
                                            null,
                                            null));
            }
            else if (ConfigurationConstants.ANY_METHOD_KEYWORD.equals(nextWord))
            {
                checkMethodAccessFlags(requiredSetMemberAccessFlags,
                                       requiredUnsetMemberAccessFlags);
                classSpecification.addMethod(
                    new MemberSpecification(requiredSetMemberAccessFlags,
                                            requiredUnsetMemberAccessFlags,
                                            annotationType,
                                            null,
                                            null));
            }
            readNextWord("separator '" + ConfigurationConstants.SEPARATOR_KEYWORD + "'");
            if (!ConfigurationConstants.SEPARATOR_KEYWORD.equals(nextWord))
            {
                throw new ParseException("Expecting separator '" + ConfigurationConstants.SEPARATOR_KEYWORD +
                                         "' before " + reader.locationDescription());
            }
        }
        else
        {
            checkJavaIdentifier("java type");
            String type = nextWord;
            readNextWord("class member name");
            String name = nextWord;
            if (ConfigurationConstants.OPEN_ARGUMENTS_KEYWORD.equals(name))
            {
                if (!(type.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT) ||
                      type.equals(externalClassName) ||
                      type.equals(ClassUtil.externalShortClassName(externalClassName))))
                {
                    throw new ParseException("Expecting type and name " +
                                             "instead of just '" + type +
                                             "' before " + reader.locationDescription());
                }
                type = ClassConstants.EXTERNAL_TYPE_VOID;
                name = ClassConstants.INTERNAL_METHOD_NAME_INIT;
            }
            else
            {
                checkJavaIdentifier("class member name");
                readNextWord("opening '" + ConfigurationConstants.OPEN_ARGUMENTS_KEYWORD +
                             "' or separator '" + ConfigurationConstants.SEPARATOR_KEYWORD + "'");
            }
            if (ConfigurationConstants.SEPARATOR_KEYWORD.equals(nextWord))
            {
                checkFieldAccessFlags(requiredSetMemberAccessFlags,
                                      requiredUnsetMemberAccessFlags);
                String descriptor = ClassUtil.internalType(type);
                classSpecification.addField(
                    new MemberSpecification(requiredSetMemberAccessFlags,
                                            requiredUnsetMemberAccessFlags,
                                            annotationType,
                                            name,
                                            descriptor));
            }
            else if (ConfigurationConstants.OPEN_ARGUMENTS_KEYWORD.equals(nextWord))
            {
                checkMethodAccessFlags(requiredSetMemberAccessFlags,
                                       requiredUnsetMemberAccessFlags);
                String descriptor =
                    ClassUtil.internalMethodDescriptor(type,
                                                       parseCommaSeparatedList("argument", true, true, true, true, false, false, false, null));
                if (!ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD.equals(nextWord))
                {
                    throw new ParseException("Expecting separating '" + ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD +
                                             "' or closing '" + ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD +
                                             "' before " + reader.locationDescription());
                }
                readNextWord("separator '" + ConfigurationConstants.SEPARATOR_KEYWORD + "'");
                if (!ConfigurationConstants.SEPARATOR_KEYWORD.equals(nextWord))
                {
                    throw new ParseException("Expecting separator '" + ConfigurationConstants.SEPARATOR_KEYWORD +
                                             "' before " + reader.locationDescription());
                }
                classSpecification.addMethod(
                    new MemberSpecification(requiredSetMemberAccessFlags,
                                            requiredUnsetMemberAccessFlags,
                                            annotationType,
                                            name,
                                            descriptor));
            }
            else
            {
                throw new ParseException("Expecting opening '" + ConfigurationConstants.OPEN_ARGUMENTS_KEYWORD +
                                         "' or separator '" + ConfigurationConstants.SEPARATOR_KEYWORD +
                                         "' before " + reader.locationDescription());
            }
        }
    }
    private List parseCommaSeparatedList(String  expectedDescription,
                                         boolean readFirstWord,
                                         boolean allowEmptyList,
                                         boolean expectClosingParenthesis,
                                         boolean checkJavaIdentifiers,
                                         boolean replaceSystemProperties,
                                         boolean replaceExternalClassNames,
                                         boolean replaceExternalTypes,
                                         List    list)
    throws ParseException, IOException
    {
        if (list == null)
        {
            list = new ArrayList();
        }
        if (readFirstWord)
        {
            if (expectClosingParenthesis || !allowEmptyList)
            {
                readNextWord(expectedDescription);
            }
            else
            {
                readNextWord();
                if (configurationEnd() ||
                    nextWord.equals(ConfigurationConstants.ANY_ATTRIBUTE_KEYWORD))
                {
                    return list;
                }
            }
        }
        while (true)
        {
            if (expectClosingParenthesis &&
                list.size() == 0         &&
                (ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD.equals(nextWord) ||
                 ConfigurationConstants.SEPARATOR_KEYWORD.equals(nextWord)))
            {
                break;
            }
            if (checkJavaIdentifiers)
            {
                checkJavaIdentifier("java type");
            }
            if (replaceSystemProperties)
            {
                nextWord = replaceSystemProperties(nextWord);
            }
            if (replaceExternalClassNames)
            {
                nextWord = ClassUtil.internalClassName(nextWord);
            }
            if (replaceExternalTypes)
            {
                nextWord = ClassUtil.internalType(nextWord);
            }
            list.add(nextWord);
            if (expectClosingParenthesis)
            {
                readNextWord("separating '" + ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD +
                             "' or closing '" + ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD +
                             "'");
            }
            else
            {
                readNextWord();
            }
            if (!ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD.equals(nextWord))
            {
                break;
            }
            readNextWord(expectedDescription);
        }
        return list;
    }
    private int unknownAccessFlag() throws ParseException
    {
        throw new ParseException("Unexpected keyword " + reader.locationDescription());
    }
    private File file(String word) throws ParseException
    {
        String fileName = replaceSystemProperties(word);
        File   file     = new File(fileName);
        if (!file.isAbsolute())
        {
            file = new File(reader.getBaseDir(), fileName);
        }
        try
        {
            file = file.getCanonicalFile();
        }
        catch (IOException ex)
        {
        }
        return file;
    }
    private String replaceSystemProperties(String word) throws ParseException
    {
        int fromIndex = 0;
        while (true)
        {
            fromIndex = word.indexOf(ConfigurationConstants.OPEN_SYSTEM_PROPERTY, fromIndex);
            if (fromIndex < 0)
            {
                break;
            }
            int toIndex = word.indexOf(ConfigurationConstants.CLOSE_SYSTEM_PROPERTY, fromIndex+1);
            if (toIndex < 0)
            {
                throw new ParseException("Expecting closing '" + ConfigurationConstants.CLOSE_SYSTEM_PROPERTY +
                                         "' after opening '" + ConfigurationConstants.OPEN_SYSTEM_PROPERTY +
                                         "' in " + reader.locationDescription());
            }
            String propertyName  = word.substring(fromIndex+1, toIndex);
            String propertyValue = System.getProperty(propertyName);
            if (propertyValue == null)
            {
                throw new ParseException("Value of system property '" + propertyName +
                                         "' is undefined in " + reader.locationDescription());
            }
            word = word.substring(0, fromIndex) +
                       propertyValue +
                       word.substring(toIndex+1);
        }
        return word;
    }
    private void readNextWord(String expectedDescription)
    throws ParseException, IOException
    {
        readNextWord(expectedDescription, false);
    }
    private void readNextWord(String  expectedDescription,
                              boolean expectingAtCharacter)
    throws ParseException, IOException
    {
        readNextWord();
        if (configurationEnd(expectingAtCharacter))
        {
            throw new ParseException("Expecting " + expectedDescription +
                                     " before " + reader.locationDescription());
        }
    }
    private void readNextWord() throws IOException
    {
        nextWord = reader.nextWord();
    }
    private boolean configurationEnd()
    {
        return configurationEnd(false);
    }
    private boolean configurationEnd(boolean expectingAtCharacter)
    {
        return nextWord == null ||
               nextWord.startsWith(ConfigurationConstants.OPTION_PREFIX) ||
               (!expectingAtCharacter &&
                nextWord.equals(ConfigurationConstants.AT_DIRECTIVE));
    }
    private void checkJavaIdentifier(String expectedDescription)
    throws ParseException
    {
        if (!isJavaIdentifier(nextWord))
        {
            throw new ParseException("Expecting " + expectedDescription +
                                     " before " + reader.locationDescription());
        }
    }
    private boolean isJavaIdentifier(String aWord)
    {
        for (int index = 0; index < aWord.length(); index++)
        {
            char c = aWord.charAt(index);
            if (!(Character.isJavaIdentifierPart(c) ||
                  c == '.' ||
                  c == '[' ||
                  c == ']' ||
                  c == '<' ||
                  c == '>' ||
                  c == '-' ||
                  c == '!' ||
                  c == '*' ||
                  c == '?' ||
                  c == '%'))
            {
                return false;
            }
        }
        return true;
    }
    private void checkFieldAccessFlags(int requiredSetMemberAccessFlags,
                                       int requiredUnsetMemberAccessFlags)
    throws ParseException
    {
        if (((requiredSetMemberAccessFlags |
              requiredUnsetMemberAccessFlags) &
            ~ClassConstants.VALID_INTERNAL_ACC_FIELD) != 0)
        {
            throw new ParseException("Invalid method access modifier for field before " +
                                     reader.locationDescription());
        }
    }
    private void checkMethodAccessFlags(int requiredSetMemberAccessFlags,
                                        int requiredUnsetMemberAccessFlags)
    throws ParseException
    {
        if (((requiredSetMemberAccessFlags |
              requiredUnsetMemberAccessFlags) &
            ~ClassConstants.VALID_INTERNAL_ACC_METHOD) != 0)
        {
            throw new ParseException("Invalid field access modifier for method before " +
                                     reader.locationDescription());
        }
    }
    public static void main(String[] args)
    {
        try
        {
            ConfigurationParser parser = new ConfigurationParser(args);
            try
            {
                parser.parse(new Configuration());
            }
            catch (ParseException ex)
            {
                ex.printStackTrace();
            }
            finally
            {
                parser.close();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
