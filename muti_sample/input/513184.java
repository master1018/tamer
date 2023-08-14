package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class VariableRemapper
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor
{
    private final CodeAttributeEditor codeAttributeEditor = new CodeAttributeEditor();
    private int[] variableMap;
    public void setVariableMap(int[] variableMap)
    {
        this.variableMap = variableMap;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttributeEditor.reset(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttributeEditor.visitCodeAttribute(clazz, method, codeAttribute);
        codeAttribute.attributesAccept(clazz, method, this);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
        localVariableTableAttribute.u2localVariableTableLength =
            removeEmptyLocalVariables(localVariableTableAttribute.localVariableTable,
                                      localVariableTableAttribute.u2localVariableTableLength);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
        localVariableTypeTableAttribute.u2localVariableTypeTableLength =
            removeEmptyLocalVariableTypes(localVariableTypeTableAttribute.localVariableTypeTable,
                                          localVariableTypeTableAttribute.u2localVariableTypeTableLength);
    }
    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        localVariableInfo.u2index =
            remapVariable(localVariableInfo.u2index);
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        localVariableTypeInfo.u2index =
            remapVariable(localVariableTypeInfo.u2index);
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        int oldVariableIndex = variableInstruction.variableIndex;
        int newVariableIndex = remapVariable(oldVariableIndex);
        if (newVariableIndex != oldVariableIndex)
        {
            Instruction replacementInstruction =
                new VariableInstruction(variableInstruction.opcode,
                                        newVariableIndex,
                                        variableInstruction.constant).shrink();
            codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
        }
    }
    private int remapVariable(int variableIndex)
    {
        return variableMap[variableIndex];
    }
    private int removeEmptyLocalVariables(LocalVariableInfo[] localVariableInfos,
                                          int                 localVariableInfoCount)
    {
        int newIndex = 0;
        for (int index = 0; index < localVariableInfoCount; index++)
        {
            LocalVariableInfo localVariableInfo = localVariableInfos[index];
            if (localVariableInfo.u2index >= 0)
            {
                localVariableInfos[newIndex++] = localVariableInfo;
            }
        }
        return newIndex;
    }
    private int removeEmptyLocalVariableTypes(LocalVariableTypeInfo[] localVariableTypeInfos,
                                              int                     localVariableTypeInfoCount)
    {
        int newIndex = 0;
        for (int index = 0; index < localVariableTypeInfoCount; index++)
        {
            LocalVariableTypeInfo localVariableTypeInfo = localVariableTypeInfos[index];
            if (localVariableTypeInfo.u2index >= 0)
            {
                localVariableTypeInfos[newIndex++] = localVariableTypeInfo;
            }
        }
        return newIndex;
    }
}
