import proguard.classfile.*;
import proguard.classfile.constant.RefConstant;
import proguard.evaluation.BasicInvocationUnit;
import proguard.evaluation.value.*;
public class LoadingInvocationUnit
extends      BasicInvocationUnit
{
    private boolean loadFieldValues;
    private boolean loadMethodParameterValues;
    private boolean loadMethodReturnValues;
    public LoadingInvocationUnit(ValueFactory valueFactory)
    {
        this(valueFactory, false, false, false);
    }
    public LoadingInvocationUnit(ValueFactory valueFactory,
                                 boolean      loadFieldValues,
                                 boolean      loadMethodParameterValues,
                                 boolean      loadMethodReturnValues)
    {
        super(valueFactory);
        this.loadFieldValues           = loadFieldValues;
        this.loadMethodParameterValues = loadMethodParameterValues;
        this.loadMethodReturnValues    = loadMethodReturnValues;
    }
    protected Value getFieldClassValue(Clazz       clazz,
                                       RefConstant refConstant,
                                       String      type)
    {
        if (loadFieldValues)
        {
            Member referencedMember = refConstant.referencedMember;
            if (referencedMember != null)
            {
                ReferenceValue value = StoringInvocationUnit.getFieldClassValue((Field)referencedMember);
                if (value != null &&
                    value.isParticular())
                {
                    return value;
                }
            }
        }
        return super.getFieldClassValue(clazz, refConstant, type);
    }
    protected Value getFieldValue(Clazz       clazz,
                                  RefConstant refConstant,
                                  String      type)
    {
        if (loadFieldValues)
        {
            Member referencedMember = refConstant.referencedMember;
            if (referencedMember != null)
            {
                Value value = StoringInvocationUnit.getFieldValue((Field)referencedMember);
                if (value != null &&
                    value.isParticular())
                {
                    return value;
                }
            }
        }
        return super.getFieldValue(clazz, refConstant, type);
    }
    protected Value getMethodParameterValue(Clazz  clazz,
                                            Method method,
                                            int    parameterIndex,
                                            String type,
                                            Clazz  referencedClass)
    {
        if (loadMethodParameterValues)
        {
            Value value = StoringInvocationUnit.getMethodParameterValue(method, parameterIndex);
            if (value != null &&
                value.isParticular())
            {
                return value;
            }
        }
        return super.getMethodParameterValue(clazz,
                                             method,
                                             parameterIndex,
                                             type,
                                             referencedClass);
    }
    protected Value getMethodReturnValue(Clazz       clazz,
                                         RefConstant refConstant,
                                         String      type)
    {
        if (loadMethodReturnValues)
        {
            Member referencedMember = refConstant.referencedMember;
            if (referencedMember != null)
            {
                Value value = StoringInvocationUnit.getMethodReturnValue((Method)referencedMember);
                if (value != null &&
                    value.isParticular())
                {
                    return value;
                }
            }
        }
        return super.getMethodReturnValue(clazz,
                                          refConstant,
                                          type);
    }
}
