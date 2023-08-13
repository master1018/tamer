import proguard.classfile.*;
import proguard.classfile.constant.RefConstant;
import proguard.evaluation.BasicInvocationUnit;
import proguard.evaluation.value.*;
import proguard.optimize.info.*;
public class StoringInvocationUnit
extends      BasicInvocationUnit
{
    private boolean storeFieldValues;
    private boolean storeMethodParameterValues;
    private boolean storeMethodReturnValues;
    public StoringInvocationUnit(ValueFactory valueFactory)
    {
        this(valueFactory, true, true, true);
    }
    public StoringInvocationUnit(ValueFactory valueFactory,
                                 boolean      storeFieldValues,
                                 boolean      storeMethodParameterValues,
                                 boolean      storeMethodReturnValues)
    {
        super(valueFactory);
        this.storeFieldValues           = storeFieldValues;
        this.storeMethodParameterValues = storeMethodParameterValues;
        this.storeMethodReturnValues    = storeMethodReturnValues;
    }
    protected void setFieldClassValue(Clazz          clazz,
                                      RefConstant    refConstant,
                                      ReferenceValue value)
    {
        if (storeFieldValues)
        {
            Member referencedMember = refConstant.referencedMember;
            if (referencedMember != null)
            {
                generalizeFieldClassValue((Field)referencedMember, value);
            }
        }
    }
    protected void setFieldValue(Clazz       clazz,
                                 RefConstant refConstant,
                                 Value       value)
    {
        if (storeFieldValues)
        {
            Member referencedMember = refConstant.referencedMember;
            if (referencedMember != null)
            {
                generalizeFieldValue((Field)referencedMember, value);
            }
        }
    }
    protected void setMethodParameterValue(Clazz       clazz,
                                           RefConstant refConstant,
                                           int         parameterIndex,
                                           Value       value)
    {
        if (storeMethodParameterValues)
        {
            Member referencedMember = refConstant.referencedMember;
            if (referencedMember != null)
            {
                generalizeMethodParameterValue((Method)referencedMember,
                                               parameterIndex,
                                               value);
            }
        }
    }
    protected void setMethodReturnValue(Clazz  clazz,
                                        Method method,
                                        Value  value)
    {
        if (storeMethodReturnValues)
        {
            generalizeMethodReturnValue(method, value);
        }
    }
    private static void generalizeFieldClassValue(Field field, ReferenceValue value)
    {
        FieldOptimizationInfo info = FieldOptimizationInfo.getFieldOptimizationInfo(field);
        if (info != null)
        {
            info.generalizeReferencedClass(value);
        }
    }
    public static ReferenceValue getFieldClassValue(Field field)
    {
        FieldOptimizationInfo info = FieldOptimizationInfo.getFieldOptimizationInfo(field);
        return info != null ?
            info.getReferencedClass() :
            null;
    }
    private static void generalizeFieldValue(Field field, Value value)
    {
        FieldOptimizationInfo info = FieldOptimizationInfo.getFieldOptimizationInfo(field);
        if (info != null)
        {
            info.generalizeValue(value);
        }
    }
    public static Value getFieldValue(Field field)
    {
        FieldOptimizationInfo info = FieldOptimizationInfo.getFieldOptimizationInfo(field);
        return info != null ?
            info.getValue() :
            null;
    }
    private static void generalizeMethodParameterValue(Method method, int parameterIndex, Value value)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.generalizeParameter(parameterIndex, value);
        }
    }
    public static Value getMethodParameterValue(Method method, int parameterIndex)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info != null ?
            info.getParameter(parameterIndex) :
            null;
    }
    private static void generalizeMethodReturnValue(Method method, Value value)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.generalizeReturnValue(value);
        }
    }
    public static Value getMethodReturnValue(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info != null ?
            info.getReturnValue() :
            null;
    }
}
