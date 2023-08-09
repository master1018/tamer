import proguard.classfile.util.ClassUtil;
import proguard.obfuscate.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
public class ReTrace
implements   MappingProcessor
{
    private static final String REGEX_OPTION   = "-regex";
    private static final String VERBOSE_OPTION = "-verbose";
    public static final String STACK_TRACE_EXPRESSION = "(?:\\s*%c:.*)|(?:\\s*at\\s+%c.%m\\s*\\(.*?(?::%l)?\\)\\s*)";
    private static final String REGEX_CLASS       = "\\b(?:[A-Za-z0-9_$]+\\.)*[A-Za-z0-9_$]+\\b";
    private static final String REGEX_CLASS_SLASH = "\\b(?:[A-Za-z0-9_$]+/)*[A-Za-z0-9_$]+\\b";
    private static final String REGEX_LINE_NUMBER = "\\b[0-9]+\\b";
    private static final String REGEX_TYPE        = REGEX_CLASS + "(?:\\[\\])*";
    private static final String REGEX_MEMBER      = "\\b[A-Za-z0-9_$]+\\b";
    private static final String REGEX_ARGUMENTS   = "(?:" + REGEX_TYPE + "(?:\\s*,\\s*" + REGEX_TYPE + ")*)?";
    private final String  regularExpression;
    private final boolean verbose;
    private final File    mappingFile;
    private final File    stackTraceFile;
    private Map classMap       = new HashMap();
    private Map classFieldMap  = new HashMap();
    private Map classMethodMap = new HashMap();
    public ReTrace(String  regularExpression,
                   boolean verbose,
                   File    mappingFile)
    {
        this(regularExpression, verbose, mappingFile, null);
    }
    public ReTrace(String  regularExpression,
                   boolean verbose,
                   File    mappingFile,
                   File    stackTraceFile)
    {
        this.regularExpression = regularExpression;
        this.verbose           = verbose;
        this.mappingFile       = mappingFile;
        this.stackTraceFile    = stackTraceFile;
    }
    public void execute() throws IOException
    {
        MappingReader mappingReader = new MappingReader(mappingFile);
        mappingReader.pump(this);
        StringBuffer expressionBuffer    = new StringBuffer(regularExpression.length() + 32);
        char[]       expressionTypes     = new char[32];
        int          expressionTypeCount = 0;
        int index = 0;
        while (true)
        {
            int nextIndex = regularExpression.indexOf('%', index);
            if (nextIndex < 0                             ||
                nextIndex == regularExpression.length()-1 ||
                expressionTypeCount == expressionTypes.length)
            {
                break;
            }
            expressionBuffer.append(regularExpression.substring(index, nextIndex));
            expressionBuffer.append('(');
            char expressionType = regularExpression.charAt(nextIndex + 1);
            switch(expressionType)
            {
                case 'c':
                    expressionBuffer.append(REGEX_CLASS);
                    break;
                case 'C':
                    expressionBuffer.append(REGEX_CLASS_SLASH);
                    break;
                case 'l':
                    expressionBuffer.append(REGEX_LINE_NUMBER);
                    break;
                case 't':
                    expressionBuffer.append(REGEX_TYPE);
                    break;
                case 'f':
                    expressionBuffer.append(REGEX_MEMBER);
                    break;
                case 'm':
                    expressionBuffer.append(REGEX_MEMBER);
                    break;
                case 'a':
                    expressionBuffer.append(REGEX_ARGUMENTS);
                    break;
            }
            expressionBuffer.append(')');
            expressionTypes[expressionTypeCount++] = expressionType;
            index = nextIndex + 2;
        }
        expressionBuffer.append(regularExpression.substring(index));
        Pattern pattern = Pattern.compile(expressionBuffer.toString());
        LineNumberReader reader =
            new LineNumberReader(stackTraceFile == null ?
                (Reader)new InputStreamReader(System.in) :
                (Reader)new BufferedReader(new FileReader(stackTraceFile)));
        try
        {
            StringBuffer outLine = new StringBuffer(256);
            List         extraOutLines  = new ArrayList();
            String className = null;
            while (true)
            {
                String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches())
                {
                    int    lineNumber = 0;
                    String type       = null;
                    String arguments  = null;
                    for (int expressionTypeIndex = 0; expressionTypeIndex < expressionTypeCount; expressionTypeIndex++)
                    {
                        int startIndex = matcher.start(expressionTypeIndex + 1);
                        if (startIndex >= 0)
                        {
                            String match = matcher.group(expressionTypeIndex + 1);
                            char expressionType = expressionTypes[expressionTypeIndex];
                            switch (expressionType)
                            {
                                case 'c':
                                    className = originalClassName(match);
                                    break;
                                case 'C':
                                    className = originalClassName(ClassUtil.externalClassName(match));
                                    break;
                                case 'l':
                                    lineNumber = Integer.parseInt(match);
                                    break;
                                case 't':
                                    type = originalType(match);
                                    break;
                                case 'a':
                                    arguments = originalArguments(match);
                                    break;
                            }
                        }
                    }
                    int lineIndex = 0;
                    outLine.setLength(0);
                    extraOutLines.clear();
                    for (int expressionTypeIndex = 0; expressionTypeIndex < expressionTypeCount; expressionTypeIndex++)
                    {
                        int startIndex = matcher.start(expressionTypeIndex + 1);
                        if (startIndex >= 0)
                        {
                            int    endIndex = matcher.end(expressionTypeIndex + 1);
                            String match    = matcher.group(expressionTypeIndex + 1);
                            outLine.append(line.substring(lineIndex, startIndex));
                            char expressionType = expressionTypes[expressionTypeIndex];
                            switch (expressionType)
                            {
                                case 'c':
                                    className = originalClassName(match);
                                    outLine.append(className);
                                    break;
                                case 'C':
                                    className = originalClassName(ClassUtil.externalClassName(match));
                                    outLine.append(ClassUtil.internalClassName(className));
                                    break;
                                case 'l':
                                    lineNumber = Integer.parseInt(match);
                                    outLine.append(match);
                                    break;
                                case 't':
                                    type = originalType(match);
                                    outLine.append(type);
                                    break;
                                case 'f':
                                    originalFieldName(className,
                                                      match,
                                                      type,
                                                      outLine,
                                                      extraOutLines);
                                    break;
                                case 'm':
                                    originalMethodName(className,
                                                       match,
                                                       lineNumber,
                                                       type,
                                                       arguments,
                                                       outLine,
                                                       extraOutLines);
                                    break;
                                case 'a':
                                    arguments = originalArguments(match);
                                    outLine.append(arguments);
                                    break;
                            }
                            lineIndex = endIndex;
                        }
                    }
                    outLine.append(line.substring(lineIndex));
                    System.out.println(outLine);
                    for (int extraLineIndex = 0; extraLineIndex < extraOutLines.size(); extraLineIndex++)
                    {
                        System.out.println(extraOutLines.get(extraLineIndex));
                    }
                }
                else
                {
                    System.out.println(line);
                }
            }
        }
        catch (IOException ex)
        {
            throw new IOException("Can't read stack trace (" + ex.getMessage() + ")");
        }
        finally
        {
            if (stackTraceFile != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ex)
                {
                }
            }
        }
    }
    private void originalFieldName(String       className,
                                   String       obfuscatedFieldName,
                                   String       type,
                                   StringBuffer outLine,
                                   List         extraOutLines)
    {
        int extraIndent = -1;
        Map fieldMap = (Map)classFieldMap.get(className);
        if (fieldMap != null)
        {
            Set fieldSet = (Set)fieldMap.get(obfuscatedFieldName);
            if (fieldSet != null)
            {
                Iterator fieldInfoIterator = fieldSet.iterator();
                while (fieldInfoIterator.hasNext())
                {
                    FieldInfo fieldInfo = (FieldInfo)fieldInfoIterator.next();
                    if (fieldInfo.matches(type))
                    {
                        if (extraIndent < 0)
                        {
                            extraIndent = outLine.length();
                            if (verbose)
                            {
                                outLine.append(fieldInfo.type).append(' ');
                            }
                            outLine.append(fieldInfo.originalName);
                        }
                        else
                        {
                            StringBuffer extraBuffer = new StringBuffer();
                            for (int counter = 0; counter < extraIndent; counter++)
                            {
                                extraBuffer.append(' ');
                            }
                            if (verbose)
                            {
                                extraBuffer.append(fieldInfo.type).append(' ');
                            }
                            extraBuffer.append(fieldInfo.originalName);
                            extraOutLines.add(extraBuffer);
                        }
                    }
                }
            }
        }
        if (extraIndent < 0)
        {
            outLine.append(obfuscatedFieldName);
        }
    }
    private void originalMethodName(String       className,
                                    String       obfuscatedMethodName,
                                    int          lineNumber,
                                    String       type,
                                    String       arguments,
                                    StringBuffer outLine,
                                    List         extraOutLines)
    {
        int extraIndent = -1;
        Map methodMap = (Map)classMethodMap.get(className);
        if (methodMap != null)
        {
            Set methodSet = (Set)methodMap.get(obfuscatedMethodName);
            if (methodSet != null)
            {
                Iterator methodInfoIterator = methodSet.iterator();
                while (methodInfoIterator.hasNext())
                {
                    MethodInfo methodInfo = (MethodInfo)methodInfoIterator.next();
                    if (methodInfo.matches(lineNumber, type, arguments))
                    {
                        if (extraIndent < 0)
                        {
                            extraIndent = outLine.length();
                            if (verbose)
                            {
                                outLine.append(methodInfo.type).append(' ');
                            }
                            outLine.append(methodInfo.originalName);
                            if (verbose)
                            {
                                outLine.append('(').append(methodInfo.arguments).append(')');
                            }
                        }
                        else
                        {
                            StringBuffer extraBuffer = new StringBuffer();
                            for (int counter = 0; counter < extraIndent; counter++)
                            {
                                extraBuffer.append(' ');
                            }
                            if (verbose)
                            {
                                extraBuffer.append(methodInfo.type).append(' ');
                            }
                            extraBuffer.append(methodInfo.originalName);
                            if (verbose)
                            {
                                extraBuffer.append('(').append(methodInfo.arguments).append(')');
                            }
                            extraOutLines.add(extraBuffer);
                        }
                    }
                }
            }
        }
        if (extraIndent < 0)
        {
            outLine.append(obfuscatedMethodName);
        }
    }
    private String originalArguments(String obfuscatedArguments)
    {
        StringBuffer originalArguments = new StringBuffer();
        int startIndex = 0;
        while (true)
        {
            int endIndex = obfuscatedArguments.indexOf(',', startIndex);
            if (endIndex < 0)
            {
                break;
            }
            originalArguments.append(originalType(obfuscatedArguments.substring(startIndex, endIndex).trim())).append(',');
            startIndex = endIndex + 1;
        }
        originalArguments.append(originalType(obfuscatedArguments.substring(startIndex).trim()));
        return originalArguments.toString();
    }
    private String originalType(String obfuscatedType)
    {
        int index = obfuscatedType.indexOf('[');
        return index >= 0 ?
            originalClassName(obfuscatedType.substring(0, index)) + obfuscatedType.substring(index) :
            originalClassName(obfuscatedType);
    }
    private String originalClassName(String obfuscatedClassName)
    {
        String originalClassName = (String)classMap.get(obfuscatedClassName);
        return originalClassName != null ?
            originalClassName :
            obfuscatedClassName;
    }
    public boolean processClassMapping(String className, String newClassName)
    {
        classMap.put(newClassName, className);
        return true;
    }
    public void processFieldMapping(String className, String fieldType, String fieldName, String newFieldName)
    {
        Map fieldMap = (Map)classFieldMap.get(className);
        if (fieldMap == null)
        {
            fieldMap = new HashMap();
            classFieldMap.put(className, fieldMap);
        }
        Set fieldSet = (Set)fieldMap.get(newFieldName);
        if (fieldSet == null)
        {
            fieldSet = new LinkedHashSet();
            fieldMap.put(newFieldName, fieldSet);
        }
        fieldSet.add(new FieldInfo(fieldType,
                                   fieldName));
    }
    public void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String newMethodName)
    {
        Map methodMap = (Map)classMethodMap.get(className);
        if (methodMap == null)
        {
            methodMap = new HashMap();
            classMethodMap.put(className, methodMap);
        }
        Set methodSet = (Set)methodMap.get(newMethodName);
        if (methodSet == null)
        {
            methodSet = new LinkedHashSet();
            methodMap.put(newMethodName, methodSet);
        }
        methodSet.add(new MethodInfo(firstLineNumber,
                                     lastLineNumber,
                                     methodReturnType,
                                     methodArguments,
                                     methodName));
    }
    private static class FieldInfo
    {
        private String type;
        private String originalName;
        private FieldInfo(String type, String originalName)
        {
            this.type         = type;
            this.originalName = originalName;
        }
        private boolean matches(String type)
        {
            return
                type == null || type.equals(this.type);
        }
    }
    private static class MethodInfo
    {
        private int    firstLineNumber;
        private int    lastLineNumber;
        private String type;
        private String arguments;
        private String originalName;
        private MethodInfo(int firstLineNumber, int lastLineNumber, String type, String arguments, String originalName)
        {
            this.firstLineNumber = firstLineNumber;
            this.lastLineNumber  = lastLineNumber;
            this.type            = type;
            this.arguments       = arguments;
            this.originalName    = originalName;
        }
        private boolean matches(int lineNumber, String type, String arguments)
        {
            return
                (lineNumber == 0    || (firstLineNumber <= lineNumber && lineNumber <= lastLineNumber) || lastLineNumber == 0) &&
                (type       == null || type.equals(this.type))                                                                 &&
                (arguments  == null || arguments.equals(this.arguments));
        }
    }
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.err.println("Usage: java proguard.ReTrace [-verbose] <mapping_file> [<stacktrace_file>]");
            System.exit(-1);
        }
        String  regularExpresssion = STACK_TRACE_EXPRESSION;
        boolean verbose            = false;
        int argumentIndex = 0;
        while (argumentIndex < args.length)
        {
            String arg = args[argumentIndex];
            if (arg.equals(REGEX_OPTION))
            {
                regularExpresssion = args[++argumentIndex];
            }
            else if (arg.equals(VERBOSE_OPTION))
            {
                verbose = true;
            }
            else
            {
                break;
            }
            argumentIndex++;
        }
        if (argumentIndex >= args.length)
        {
            System.err.println("Usage: java proguard.ReTrace [-regex <regex>] [-verbose] <mapping_file> [<stacktrace_file>]");
            System.exit(-1);
        }
        File mappingFile    = new File(args[argumentIndex++]);
        File stackTraceFile = argumentIndex < args.length ?
            new File(args[argumentIndex]) :
            null;
        ReTrace reTrace = new ReTrace(regularExpresssion, verbose, mappingFile, stackTraceFile);
        try
        {
            reTrace.execute();
        }
        catch (IOException ex)
        {
            if (verbose)
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
