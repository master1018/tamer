import proguard.classfile.*;
import proguard.classfile.util.MethodLinker;
import proguard.classfile.visitor.*;
public class KeepMarker
implements   ClassVisitor,
             MemberVisitor
{
    private static final Object KEPT = new Object();
    public void visitProgramClass(ProgramClass programClass)
    {
        markAsKept(programClass);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        markAsKept(libraryClass);
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        markAsKept(programField);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        markAsKept(MethodLinker.lastMember(programMethod));
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        markAsKept(libraryField);
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        markAsKept(MethodLinker.lastMember(libraryMethod));
    }
    private static void markAsKept(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(KEPT);
    }
    public static boolean isKept(VisitorAccepter visitorAccepter)
    {
        return MethodLinker.lastVisitorAccepter(visitorAccepter).getVisitorInfo() == KEPT;
    }
}
