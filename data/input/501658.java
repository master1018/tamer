package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.optimize.info.VariableUsageMarker;
public class VariableCleaner
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private final VariableUsageMarker variableUsageMarker = new VariableUsageMarker();
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        variableUsageMarker.visitCodeAttribute(clazz, method, codeAttribute);
        codeAttribute.attributesAccept(clazz, method, this);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        localVariableTableAttribute.u2localVariableTableLength =
            removeEmptyLocalVariables(localVariableTableAttribute.localVariableTable,
                                      localVariableTableAttribute.u2localVariableTableLength,
                                      codeAttribute.u2maxLocals);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        localVariableTypeTableAttribute.u2localVariableTypeTableLength =
            removeEmptyLocalVariableTypes(localVariableTypeTableAttribute.localVariableTypeTable,
                                          localVariableTypeTableAttribute.u2localVariableTypeTableLength,
                                          codeAttribute.u2maxLocals);
    }
    private int removeEmptyLocalVariables(LocalVariableInfo[] localVariableInfos,
                                          int                 localVariableInfoCount,
                                          int                 maxLocals)
    {
        int newIndex = 0;
        for (int index = 0; index < localVariableInfoCount && index < maxLocals; index++)
        {
            if (variableUsageMarker.isVariableUsed(index))
            {
                localVariableInfos[newIndex++] = localVariableInfos[index];
            }
        }
        for (int index = newIndex; index < localVariableInfoCount; index++)
        {
            localVariableInfos[index] = null;
        }
        return newIndex;
    }
    private int removeEmptyLocalVariableTypes(LocalVariableTypeInfo[] localVariableTypeInfos,
                                              int                     localVariableTypeInfoCount,
                                              int                     maxLocals)
    {
        int newIndex = 0;
        for (int index = 0; index < localVariableTypeInfoCount && index < maxLocals; index++)
        {
            if (variableUsageMarker.isVariableUsed(index))
            {
                localVariableTypeInfos[newIndex++] = localVariableTypeInfos[index];
            }
        }
        for (int index = newIndex; index < localVariableTypeInfoCount; index++)
        {
            localVariableTypeInfos[index] = null;
        }
        return newIndex;
    }
}