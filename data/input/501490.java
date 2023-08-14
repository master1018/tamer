import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.ConstantInstruction;
import proguard.evaluation.value.Value;
public interface InvocationUnit
{
    public void enterMethod(Clazz     clazz,
                            Method    method,
                            Variables variables);
    public void exitMethod(Clazz  clazz,
                           Method method,
                           Value  returnValue);
    public void invokeMember(Clazz               clazz,
                             Method              method,
                             CodeAttribute       codeAttribute,
                             int                 offset,
                             ConstantInstruction constantInstruction,
                             Stack               stack);
}
