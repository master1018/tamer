import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.evaluation.value.*;
public class BasicInvocationUnit
extends      SimplifiedVisitor
implements   InvocationUnit,
             ConstantVisitor,
             MemberVisitor
{
    protected final ValueFactory valueFactory;
    private boolean isStatic;
    private boolean isLoad;
    private Stack   stack;
    private Clazz   returnTypeClass;
    public BasicInvocationUnit(ValueFactory valueFactory)
    {
        this.valueFactory = valueFactory;
    }
    public void enterMethod(Clazz clazz, Method method, Variables variables)
    {
        String descriptor = method.getDescriptor(clazz);
        boolean isStatic =
            (method.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0;
        int parameterSize = ClassUtil.internalMethodParameterSize(descriptor, isStatic);
        variables.reset(parameterSize);
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(descriptor);
        int parameterIndex = 0;
        int variableIndex  = 0;
        if (!isStatic)
        {
            Value value = getMethodParameterValue(clazz,
                                                  method,
                                                  parameterIndex++,
                                                  ClassUtil.internalTypeFromClassName(clazz.getName()),
                                                  clazz);
            variables.store(variableIndex++, value);
        }
        Clazz[] referencedClasses = ((ProgramMethod)method).referencedClasses;
        int referencedClassIndex = 0;
        while (internalTypeEnumeration.hasMoreTypes())
        {
            String type = internalTypeEnumeration.nextType();
            Clazz referencedClass = referencedClasses != null &&
                                    ClassUtil.isInternalClassType(type) ?
                referencedClasses[referencedClassIndex++] :
                null;
            Value value = getMethodParameterValue(clazz,
                                                  method,
                                                  parameterIndex++,
                                                  type,
                                                  referencedClass);
            variables.store(variableIndex++, value);
            if (value.isCategory2())
            {
                variableIndex++;
            }
        }
    }
    public void exitMethod(Clazz clazz, Method method, Value returnValue)
    {
        setMethodReturnValue(clazz, method, returnValue);
    }
    public void invokeMember(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction, Stack stack)
    {
        int constantIndex = constantInstruction.constantIndex;
        switch (constantInstruction.opcode)
        {
            case InstructionConstants.OP_GETSTATIC:
                isStatic = true;
                isLoad   = true;
                break;
            case InstructionConstants.OP_PUTSTATIC:
                isStatic = true;
                isLoad   = false;
                break;
            case InstructionConstants.OP_GETFIELD:
                isStatic = false;
                isLoad   = true;
                break;
            case InstructionConstants.OP_PUTFIELD:
                isStatic = false;
                isLoad   = false;
                break;
            case InstructionConstants.OP_INVOKESTATIC:
                isStatic = true;
                break;
            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKEINTERFACE:
                isStatic = false;
                break;
        }
        this.stack = stack;
        clazz.constantPoolEntryAccept(constantIndex, this);
        this.stack = null;
    }
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        if (!isLoad)
        {
            setFieldValue(clazz, fieldrefConstant, stack.pop());
        }
        if (!isStatic)
        {
            setFieldClassValue(clazz, fieldrefConstant, stack.apop());
        }
        if (isLoad)
        {
            String type = fieldrefConstant.getType(clazz);
            stack.push(getFieldValue(clazz, fieldrefConstant, type));
        }
    }
    public void visitAnyMethodrefConstant(Clazz clazz, RefConstant methodrefConstant)
    {
        String type = methodrefConstant.getType(clazz);
        int parameterCount = ClassUtil.internalMethodParameterCount(type);
        if (!isStatic)
        {
            parameterCount++;
        }
        for (int parameterIndex = parameterCount-1; parameterIndex >= 0; parameterIndex--)
        {
            setMethodParameterValue(clazz, methodrefConstant, parameterIndex, stack.pop());
        }
        String returnType = ClassUtil.internalMethodReturnType(type);
        if (returnType.charAt(0) != ClassConstants.INTERNAL_TYPE_VOID)
        {
            stack.push(getMethodReturnValue(clazz, methodrefConstant, returnType));
        }
    }
    protected void setFieldClassValue(Clazz          clazz,
                                      RefConstant    refConstant,
                                      ReferenceValue value)
    {
    }
    protected Value getFieldClassValue(Clazz       clazz,
                                       RefConstant refConstant,
                                       String      type)
    {
        returnTypeClass = null;
        refConstant.referencedMemberAccept(this);
        return valueFactory.createValue(type,
                                        returnTypeClass,
                                        true);
    }
    protected void setFieldValue(Clazz       clazz,
                                 RefConstant refConstant,
                                 Value       value)
    {
    }
    protected Value getFieldValue(Clazz       clazz,
                                  RefConstant refConstant,
                                  String      type)
    {
        returnTypeClass = null;
        refConstant.referencedMemberAccept(this);
        return valueFactory.createValue(type,
                                        returnTypeClass,
                                        true);
    }
    protected void setMethodParameterValue(Clazz       clazz,
                                           RefConstant refConstant,
                                           int         parameterIndex,
                                           Value       value)
    {
    }
    protected Value getMethodParameterValue(Clazz  clazz,
                                            Method method,
                                            int    parameterIndex,
                                            String type,
                                            Clazz  referencedClass)
    {
        return valueFactory.createValue(type, referencedClass, true);
    }
    protected void setMethodReturnValue(Clazz  clazz,
                                        Method method,
                                        Value  value)
    {
    }
    protected Value getMethodReturnValue(Clazz       clazz,
                                         RefConstant refConstant,
                                         String      type)
    {
        returnTypeClass = null;
        refConstant.referencedMemberAccept(this);
        return valueFactory.createValue(type,
                                        returnTypeClass,
                                        true);
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        returnTypeClass = programField.referencedClass;
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        Clazz[] referencedClasses = programMethod.referencedClasses;
        if (referencedClasses != null)
        {
            returnTypeClass = referencedClasses[referencedClasses.length - 1];
        }
    }
    public void visitLibraryField(LibraryClass programClass, LibraryField programField)
    {
        returnTypeClass = programField.referencedClass;
    }
    public void visitLibraryMethod(LibraryClass programClass, LibraryMethod programMethod)
    {
        Clazz[] referencedClasses = programMethod.referencedClasses;
        if (referencedClasses != null)
        {
            returnTypeClass = referencedClasses[referencedClasses.length - 1];
        }
    }
}
