package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.constant.Constant;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
import java.util.Arrays;
public class ConstantPoolSorter
extends      SimplifiedVisitor
implements   ClassVisitor
{
    private int[]                constantIndexMap       = new int[ClassConstants.TYPICAL_CONSTANT_POOL_SIZE];
    private ComparableConstant[] comparableConstantPool = new ComparableConstant[ClassConstants.TYPICAL_CONSTANT_POOL_SIZE];
    private Constant[]           newConstantPool        = new Constant[ClassConstants.TYPICAL_CONSTANT_POOL_SIZE];
    private final ConstantPoolRemapper constantPoolRemapper = new ConstantPoolRemapper();
    public void visitProgramClass(ProgramClass programClass)
    {
        int constantPoolCount = programClass.u2constantPoolCount;
        if (constantIndexMap.length < constantPoolCount)
        {
            constantIndexMap       = new int[constantPoolCount];
            comparableConstantPool = new ComparableConstant[constantPoolCount];
            newConstantPool        = new Constant[constantPoolCount];
        }
        int sortLength = 0;
        for (int oldIndex = 1; oldIndex < constantPoolCount; oldIndex++)
        {
            Constant constant = programClass.constantPool[oldIndex];
            if (constant != null)
            {
                comparableConstantPool[sortLength++] =
                    new ComparableConstant(programClass, oldIndex, constant);
            }
        }
        Arrays.sort(comparableConstantPool, 0, sortLength);
        int newLength = 1;
        int newIndex  = 1;
        ComparableConstant previousComparableConstant = null;
        for (int sortIndex = 0; sortIndex < sortLength; sortIndex++)
        {
            ComparableConstant comparableConstant = comparableConstantPool[sortIndex];
            if (!comparableConstant.equals(previousComparableConstant))
            {
                newIndex = newLength;
                Constant constant = comparableConstant.getConstant();
                newConstantPool[newLength++] = constant;
                int tag = constant.getTag();
                if (tag == ClassConstants.CONSTANT_Long ||
                    tag == ClassConstants.CONSTANT_Double)
                {
                    newConstantPool[newLength++] = null;
                }
                previousComparableConstant = comparableConstant;
            }
            constantIndexMap[comparableConstant.getIndex()] = newIndex;
        }
        System.arraycopy(newConstantPool, 0, programClass.constantPool, 0, newLength);
        for (int index = newLength; index < constantPoolCount; index++)
        {
            programClass.constantPool[index] = null;
        }
        programClass.u2constantPoolCount = newLength;
        constantPoolRemapper.setConstantIndexMap(constantIndexMap);
        constantPoolRemapper.visitProgramClass(programClass);
    }
}
