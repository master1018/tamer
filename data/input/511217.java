package proguard.classfile.editor;
import proguard.classfile.*;
public class ClassEditor
{
    private static final boolean DEBUG = false;
    private ProgramClass targetClass;
    public ClassEditor(ProgramClass targetClass)
    {
        this.targetClass = targetClass;
    }
    public void addInterface(int interfaceConstantIndex)
    {
        int   interfacesCount = targetClass.u2interfacesCount;
        int[] interfaces      = targetClass.u2interfaces;
        if (interfaces.length <= interfacesCount)
        {
            targetClass.u2interfaces = new int[interfacesCount+1];
            System.arraycopy(interfaces, 0,
                             targetClass.u2interfaces, 0,
                             interfacesCount);
            interfaces = targetClass.u2interfaces;
        }
        if (DEBUG)
        {
            System.out.println(targetClass.getName()+": adding interface ["+targetClass.getClassName(interfaceConstantIndex)+"]");
        }
        interfaces[targetClass.u2interfacesCount++] = interfaceConstantIndex;
    }
    public void removeInterface(int interfaceConstantIndex)
    {
        int   interfacesCount = targetClass.u2interfacesCount;
        int[] interfaces      = targetClass.u2interfaces;
        int interfaceIndex = findInterfaceIndex(interfaceConstantIndex);
        System.arraycopy(interfaces, interfaceIndex+1,
                         interfaces, interfaceIndex,
                         interfacesCount - interfaceIndex - 1);
        interfaces[--targetClass.u2interfacesCount] = 0;
    }
    private int findInterfaceIndex(int interfaceConstantIndex)
    {
        int   interfacesCount = targetClass.u2interfacesCount;
        int[] interfaces      = targetClass.u2interfaces;
        for (int index = 0; index < interfacesCount; index++)
        {
            if (interfaces[index] == interfaceConstantIndex)
            {
                return index;
            }
        }
        return interfacesCount;
    }
    public void addField(Field field)
    {
        int     fieldsCount = targetClass.u2fieldsCount;
        Field[] fields      = targetClass.fields;
        if (fields.length <= fieldsCount)
        {
            targetClass.fields = new ProgramField[fieldsCount+1];
            System.arraycopy(fields, 0,
                             targetClass.fields, 0,
                             fieldsCount);
            fields = targetClass.fields;
        }
        if (DEBUG)
        {
            System.out.println(targetClass.getName()+": adding field ["+field.getName(targetClass)+" "+field.getDescriptor(targetClass)+"]");
        }
        fields[targetClass.u2fieldsCount++] = field;
    }
    public void removeField(Field field)
    {
        int     fieldsCount = targetClass.u2fieldsCount;
        Field[] fields      = targetClass.fields;
        int fieldIndex = findFieldIndex(field);
        System.arraycopy(fields, fieldIndex+1,
                         fields, fieldIndex,
                         fieldsCount - fieldIndex - 1);
        fields[--targetClass.u2fieldsCount] = null;
    }
    private int findFieldIndex(Field field)
    {
        int     fieldsCount = targetClass.u2fieldsCount;
        Field[] fields      = targetClass.fields;
        for (int index = 0; index < fieldsCount; index++)
        {
            if (fields[index].equals(field))
            {
                return index;
            }
        }
        return fieldsCount;
    }
    public void addMethod(Method method)
    {
        int      methodsCount = targetClass.u2methodsCount;
        Method[] methods      = targetClass.methods;
        if (methods.length <= methodsCount)
        {
            targetClass.methods = new ProgramMethod[methodsCount+1];
            System.arraycopy(methods, 0,
                             targetClass.methods, 0,
                             methodsCount);
            methods = targetClass.methods;
        }
        if (DEBUG)
        {
            System.out.println(targetClass.getName()+": adding method ["+method.getName(targetClass)+method.getDescriptor(targetClass)+"]");
        }
        methods[targetClass.u2methodsCount++] = method;
    }
    public void removeMethod(Method method)
    {
        int      methodsCount = targetClass.u2methodsCount;
        Method[] methods      = targetClass.methods;
        int methodIndex = findMethodIndex(method);
        System.arraycopy(methods, methodIndex+1,
                         methods, methodIndex,
                         methodsCount - methodIndex - 1);
        methods[--targetClass.u2methodsCount] = null;
    }
    private int findMethodIndex(Method method)
    {
        int      methodsCount = targetClass.u2methodsCount;
        Method[] methods      = targetClass.methods;
        for (int index = 0; index < methodsCount; index++)
        {
            if (methods[index].equals(method))
            {
                return index;
            }
        }
        return methodsCount;
    }
}
