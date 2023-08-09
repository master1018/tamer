package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class VariableEditor
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private boolean   modified;
    private boolean[] deleted     = new boolean[ClassConstants.TYPICAL_VARIABLES_SIZE];
    private int[]     variableMap = new int[ClassConstants.TYPICAL_VARIABLES_SIZE];
    private final VariableRemapper variableRemapper = new VariableRemapper();
    public void reset(int maxLocals)
    {
        if (deleted.length < maxLocals)
        {
            deleted = new boolean[maxLocals];
        }
        else
        {
            for (int index = 0; index < maxLocals; index++)
            {
                deleted[index] = false;
            }
        }
        modified = false;
    }
    public void deleteVariable(int variableIndex)
    {
        deleted[variableIndex] = true;
        modified = true;
    }
    public boolean isDeleted(int instructionOffset)
    {
        return deleted[instructionOffset];
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (!modified)
        {
            return;
        }
        int oldMaxLocals = codeAttribute.u2maxLocals;
        if (variableMap.length < oldMaxLocals)
        {
            variableMap = new int[oldMaxLocals];
        }
        int newVariableIndex = 0;
        for (int oldVariableIndex = 0; oldVariableIndex < oldMaxLocals; oldVariableIndex++)
        {
            variableMap[oldVariableIndex] = deleted[oldVariableIndex] ?
                -1 : newVariableIndex++;
        }
        variableRemapper.setVariableMap(variableMap);
        variableRemapper.visitCodeAttribute(clazz, method, codeAttribute);
        codeAttribute.u2maxLocals = newVariableIndex;
    }
}
