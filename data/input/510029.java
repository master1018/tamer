import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.ClassVisitor;
public class DuplicateClassPrinter implements ClassVisitor
{
    private final WarningPrinter notePrinter;
    public DuplicateClassPrinter(WarningPrinter notePrinter)
    {
        this.notePrinter = notePrinter;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        notePrinter.print(programClass.getName(),
                          "Note: duplicate definition of program class [" +
                          ClassUtil.externalClassName(programClass.getName()) + "]");
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        notePrinter.print(libraryClass.getName(),
                          "Note: duplicate definition of library class [" +
                          ClassUtil.externalClassName(libraryClass.getName()) + "]");
    }
}
