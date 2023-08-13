package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
public class MethodImplementationTraveler
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private final boolean       visitThisMethod;
    private final boolean       visitSuperMethods;
    private final boolean       visitInterfaceMethods;
    private final boolean       visitOverridingMethods;
    private final MemberVisitor memberVisitor;
    public MethodImplementationTraveler(boolean       visitThisMethod,
                                        boolean       visitSuperMethods,
                                        boolean       visitInterfaceMethods,
                                        boolean       visitOverridingMethods,
                                        MemberVisitor memberVisitor)
    {
        this.visitThisMethod        = visitThisMethod;
        this.visitSuperMethods      = visitSuperMethods;
        this.visitInterfaceMethods  = visitInterfaceMethods;
        this.visitOverridingMethods = visitOverridingMethods;
        this.memberVisitor          = memberVisitor;
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (visitThisMethod)
        {
            programMethod.accept(programClass, memberVisitor);
        }
        if (!isSpecial(programClass, programMethod))
        {
            programClass.hierarchyAccept(false,
                                         visitSuperMethods,
                                         visitInterfaceMethods,
                                         visitOverridingMethods,
                                         new NamedMethodVisitor(programMethod.getName(programClass),
                                                                programMethod.getDescriptor(programClass),
                                         new MemberAccessFilter(0,
                                                                ClassConstants.INTERNAL_ACC_PRIVATE |
                                                                ClassConstants.INTERNAL_ACC_STATIC,
                                         memberVisitor)));
        }
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (visitThisMethod)
        {
            libraryMethod.accept(libraryClass, memberVisitor);
        }
        if (!isSpecial(libraryClass, libraryMethod))
        {
            libraryClass.hierarchyAccept(false,
                                         visitSuperMethods,
                                         visitInterfaceMethods,
                                         visitOverridingMethods,
                                         new NamedMethodVisitor(libraryMethod.getName(libraryClass),
                                                                libraryMethod.getDescriptor(libraryClass),
                                         new MemberAccessFilter(0,
                                                                ClassConstants.INTERNAL_ACC_PRIVATE |
                                                                ClassConstants.INTERNAL_ACC_STATIC,
                                         memberVisitor)));
        }
    }
    private boolean isSpecial(Clazz clazz, Method method)
    {
        return (method.getAccessFlags() &
                (ClassConstants.INTERNAL_ACC_PRIVATE |
                 ClassConstants.INTERNAL_ACC_STATIC)) != 0 ||
               method.getName(clazz).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT);
    }
}
