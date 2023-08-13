package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
public class AccessFixer
extends      SimplifiedVisitor
implements   ConstantVisitor,
             ClassVisitor,
             MemberVisitor
{
    private MyReferencedClassFinder referencedClassFinder = new MyReferencedClassFinder();
    private Clazz referencingClass;
    private Clazz referencedClass;
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        referencingClass = clazz;
        referencedClass  = stringConstant.referencedClass;
        stringConstant.referencedClassAccept(this);
        stringConstant.referencedMemberAccept(this);
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        referencingClass = clazz;
        clazz.constantPoolEntryAccept(refConstant.u2classIndex, referencedClassFinder);
        refConstant.referencedMemberAccept(this);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        referencingClass = clazz;
        classConstant.referencedClassAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass) {}
    public void visitProgramClass(ProgramClass programClass)
    {
        int currentAccessFlags  = programClass.getAccessFlags();
        int currentAccessLevel  = AccessUtil.accessLevel(currentAccessFlags);
        Clazz referencingClass = this.referencingClass;
        int requiredAccessLevel =
            inSamePackage(programClass, referencingClass) ? AccessUtil.PACKAGE_VISIBLE :
                                                            AccessUtil.PUBLIC;
        if (currentAccessLevel < requiredAccessLevel)
        {
            programClass.u2accessFlags =
                AccessUtil.replaceAccessFlags(currentAccessFlags,
                                              AccessUtil.accessFlags(requiredAccessLevel));
        }
    }
    public void visitLibraryMember(LibraryClass libraryClass, LibraryMember libraryMember) {}
    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        int currentAccessFlags  = programMember.getAccessFlags();
        int currentAccessLevel  = AccessUtil.accessLevel(currentAccessFlags);
        int requiredAccessLevel =
            programClass.equals(referencingClass)         ? AccessUtil.PRIVATE         :
            inSamePackage(programClass, referencingClass) ? AccessUtil.PACKAGE_VISIBLE :
            referencedClass.extends_(referencingClass) &&
            referencingClass.extends_(programClass)       ? AccessUtil.PROTECTED       :
                                                            AccessUtil.PUBLIC;
        if (currentAccessLevel < requiredAccessLevel)
        {
            programMember.u2accessFlags =
                AccessUtil.replaceAccessFlags(currentAccessFlags,
                                              AccessUtil.accessFlags(requiredAccessLevel));
        }
    }
    private class MyReferencedClassFinder
    extends       SimplifiedVisitor
    implements    ConstantVisitor
    {
        public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
        {
            referencedClass = classConstant.referencedClass;
        }
    }
    private boolean inSamePackage(ProgramClass class1, Clazz class2)
    {
        return ClassUtil.internalPackageName(class1.getName()).equals(
               ClassUtil.internalPackageName(class2.getName()));
    }
}
