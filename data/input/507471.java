package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.util.*;
public class MemberClassAccessFilter
implements   MemberVisitor
{
    private final Clazz         referencingClass;
    private final MemberVisitor memberVisitor;
    public MemberClassAccessFilter(Clazz         referencingClass,
                                   MemberVisitor memberVisitor)
    {
        this.referencingClass = referencingClass;
        this.memberVisitor    = memberVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (accepted(programClass, programField.getAccessFlags()))
        {
            memberVisitor.visitProgramField(programClass, programField);
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (accepted(programClass, programMethod.getAccessFlags()))
        {
            memberVisitor.visitProgramMethod(programClass, programMethod);
        }
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        if (accepted(libraryClass, libraryField.getAccessFlags()))
        {
            memberVisitor.visitLibraryField(libraryClass, libraryField);
        }
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (accepted(libraryClass, libraryMethod.getAccessFlags()))
        {
            memberVisitor.visitLibraryMethod(libraryClass, libraryMethod);
        }
    }
    private boolean accepted(Clazz clazz, int memberAccessFlags)
    {
        int accessLevel = AccessUtil.accessLevel(memberAccessFlags);
        return
            (accessLevel >= AccessUtil.PUBLIC                                                              ) ||
            (accessLevel >= AccessUtil.PRIVATE         && referencingClass.equals(clazz)                   ) ||
            (accessLevel >= AccessUtil.PACKAGE_VISIBLE && (ClassUtil.internalPackageName(referencingClass.getName()).equals(
                                                           ClassUtil.internalPackageName(clazz.getName())))) ||
            (accessLevel >= AccessUtil.PROTECTED       && (referencingClass.extends_(clazz)                  ||
                                                           referencingClass.extendsOrImplements(clazz))            );
    }
}
