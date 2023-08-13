import proguard.classfile.*;
import proguard.classfile.constant.Constant;
import proguard.classfile.editor.ConstantPoolRemapper;
import proguard.classfile.visitor.ClassVisitor;
public class Utf8Shrinker implements ClassVisitor
{
    private int[]                constantIndexMap     = new int[ClassConstants.TYPICAL_CONSTANT_POOL_SIZE];
    private final ConstantPoolRemapper constantPoolRemapper = new ConstantPoolRemapper();
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.u2constantPoolCount =
            shrinkConstantPool(programClass.constantPool,
                               programClass.u2constantPoolCount);
        constantPoolRemapper.setConstantIndexMap(constantIndexMap);
        constantPoolRemapper.visitProgramClass(programClass);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }
    private int shrinkConstantPool(Constant[] constantPool, int length)
    {
        if (constantIndexMap.length < length)
        {
            constantIndexMap = new int[length];
        }
        int     counter = 1;
        boolean isUsed  = false;
        for (int index = 1; index < length; index++)
        {
            constantIndexMap[index] = counter;
            Constant constant = constantPool[index];
            if (constant != null)
            {
                isUsed = constant.getTag() != ClassConstants.CONSTANT_Utf8 ||
                         Utf8UsageMarker.isUsed(constant);
            }
            if (isUsed)
            {
                constantPool[counter++] = constant;
            }
        }
        for (int index = counter; index < length; index++)
        {
            constantPool[index] = null;
        }
        return counter;
    }
}
