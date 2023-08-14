package proguard.classfile.instruction;
import proguard.classfile.ClassConstants;
public class InstructionUtil
{
    public static char internalTypeFromArrayType(byte arrayType)
    {
        switch (arrayType)
        {
            case InstructionConstants.ARRAY_T_BOOLEAN: return ClassConstants.INTERNAL_TYPE_BOOLEAN;
            case InstructionConstants.ARRAY_T_CHAR:    return ClassConstants.INTERNAL_TYPE_CHAR;
            case InstructionConstants.ARRAY_T_FLOAT:   return ClassConstants.INTERNAL_TYPE_FLOAT;
            case InstructionConstants.ARRAY_T_DOUBLE:  return ClassConstants.INTERNAL_TYPE_DOUBLE;
            case InstructionConstants.ARRAY_T_BYTE:    return ClassConstants.INTERNAL_TYPE_BYTE;
            case InstructionConstants.ARRAY_T_SHORT:   return ClassConstants.INTERNAL_TYPE_SHORT;
            case InstructionConstants.ARRAY_T_INT:     return ClassConstants.INTERNAL_TYPE_INT;
            case InstructionConstants.ARRAY_T_LONG:    return ClassConstants.INTERNAL_TYPE_LONG;
            default: throw new IllegalArgumentException("Unknown array type ["+arrayType+"]");
        }
    }
}
