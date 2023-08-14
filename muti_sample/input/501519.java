package proguard.classfile.visitor;
import proguard.classfile.*;
public class ClassHierarchyTraveler implements ClassVisitor
{
    private final boolean visitThisClass;
    private final boolean visitSuperClass;
    private final boolean visitInterfaces;
    private final boolean visitSubclasses;
    private final ClassVisitor classVisitor;
    public ClassHierarchyTraveler(boolean      visitThisClass,
                                  boolean      visitSuperClass,
                                  boolean      visitInterfaces,
                                  boolean      visitSubclasses,
                                  ClassVisitor classVisitor)
    {
        this.visitThisClass  = visitThisClass;
        this.visitSuperClass = visitSuperClass;
        this.visitInterfaces = visitInterfaces;
        this.visitSubclasses = visitSubclasses;
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.hierarchyAccept(visitThisClass,
                                     visitSuperClass,
                                     visitInterfaces,
                                     visitSubclasses,
                                     classVisitor);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.hierarchyAccept(visitThisClass,
                                     visitSuperClass,
                                     visitInterfaces,
                                     visitSubclasses,
                                     classVisitor);
    }
}
