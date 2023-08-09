import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.util.ListUtil;
public class MappingKeeper implements MappingProcessor
{
    private final ClassPool      classPool;
    private final WarningPrinter warningPrinter;
    private Clazz clazz;
    public MappingKeeper(ClassPool      classPool,
                         WarningPrinter warningPrinter)
    {
        this.classPool      = classPool;
        this.warningPrinter = warningPrinter;
    }
    public boolean processClassMapping(String className,
                                       String newClassName)
    {
        String name = ClassUtil.internalClassName(className);
        clazz = classPool.getClass(name);
        if (clazz != null)
        {
            String newName = ClassUtil.internalClassName(newClassName);
            if (warningPrinter != null)
            {
                String currentNewName = ClassObfuscator.newClassName(clazz);
                if (currentNewName != null &&
                    !currentNewName.equals(newName))
                {
                    warningPrinter.print(name,
                                         currentNewName,
                                         "Warning: " +
                                         className +
                                         " is not being kept as '" +
                                         ClassUtil.externalClassName(currentNewName) +
                                         "', but remapped to '" +
                                         newClassName + "'");
                }
            }
            ClassObfuscator.setNewClassName(clazz, newName);
            return true;
        }
        return false;
    }
    public void processFieldMapping(String className,
                                    String fieldType,
                                    String fieldName,
                                    String newFieldName)
    {
        if (clazz != null)
        {
            String name       = fieldName;
            String descriptor = ClassUtil.internalType(fieldType);
            Field field = clazz.findField(name, descriptor);
            if (field != null)
            {
                if (warningPrinter != null)
                {
                    String currentNewName = MemberObfuscator.newMemberName(field);
                    if (currentNewName != null &&
                        !currentNewName.equals(newFieldName))
                    {
                        warningPrinter.print(ClassUtil.internalClassName(className),
                                             "Warning: " +
                                             className +
                                             ": field '" + fieldType + " " + fieldName +
                                             "' is not being kept as '" + currentNewName +
                                             "', but remapped to '" + newFieldName + "'");
                    }
                }
                MemberObfuscator.setFixedNewMemberName(field, newFieldName);
            }
        }
    }
    public void processMethodMapping(String className,
                                     int    firstLineNumber,
                                     int    lastLineNumber,
                                     String methodReturnType,
                                     String methodName,
                                     String methodArguments,
                                     String newMethodName)
    {
        if (clazz != null)
        {
            String descriptor = ClassUtil.internalMethodDescriptor(methodReturnType,
                                                                   ListUtil.commaSeparatedList(methodArguments));
            Method method = clazz.findMethod(methodName, descriptor);
            if (method != null)
            {
                if (warningPrinter != null)
                {
                    String currentNewName = MemberObfuscator.newMemberName(method);
                    if (currentNewName != null &&
                        !currentNewName.equals(newMethodName))
                    {
                        warningPrinter.print(ClassUtil.internalClassName(className),
                                             "Warning: " +
                                             className +
                                             ": method '" + methodReturnType + " " + methodName + ClassConstants.EXTERNAL_METHOD_ARGUMENTS_OPEN + methodArguments + ClassConstants.EXTERNAL_METHOD_ARGUMENTS_CLOSE +
                                             "' is not being kept as '" + currentNewName +
                                             "', but remapped to '" + newMethodName + "'");
                    }
                }
                MemberObfuscator.setFixedNewMemberName(method, newMethodName);
            }
        }
    }
}
