import proguard.classfile.*;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.*;
import java.io.PrintStream;
public class ShortestUsagePrinter
implements   ClassVisitor,
             MemberVisitor
{
    private final ShortestUsageMarker shortestUsageMarker;
    private final boolean             verbose;
    private final PrintStream         ps;
    public ShortestUsagePrinter(ShortestUsageMarker shortestUsageMarker)
    {
        this(shortestUsageMarker, true);
    }
    public ShortestUsagePrinter(ShortestUsageMarker shortestUsageMarker,
                                boolean             verbose)
    {
        this(shortestUsageMarker, verbose, System.out);
    }
    public ShortestUsagePrinter(ShortestUsageMarker shortestUsageMarker,
                                boolean             verbose,
                                PrintStream         printStream)
    {
        this.shortestUsageMarker = shortestUsageMarker;
        this.verbose             = verbose;
        this.ps                  = printStream;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        ps.println(ClassUtil.externalClassName(programClass.getName()));
        printReason(programClass);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        ps.println(ClassUtil.externalClassName(libraryClass.getName()));
        ps.println("  is a library class.\n");
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        String name = programField.getName(programClass);
        String type = programField.getDescriptor(programClass);
        ps.println(ClassUtil.externalClassName(programClass.getName()) +
                   (verbose ?
                        ": " + ClassUtil.externalFullFieldDescription(0, name, type):
                        "."  + name) +
                   lineNumberRange(programClass, programField));
        printReason(programField);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        String name = programMethod.getName(programClass);
        String type = programMethod.getDescriptor(programClass);
        ps.println(ClassUtil.externalClassName(programClass.getName()) +
                   (verbose ?
                        ": " + ClassUtil.externalFullMethodDescription(programClass.getName(), 0, name, type):
                        "."  + name) +
                   lineNumberRange(programClass, programMethod));
        printReason(programMethod);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        String name = libraryField.getName(libraryClass);
        String type = libraryField.getDescriptor(libraryClass);
        ps.println(ClassUtil.externalClassName(libraryClass.getName()) +
                   (verbose ?
                        ": " + ClassUtil.externalFullFieldDescription(0, name, type):
                        "."  + name));
        ps.println("  is a library field.\n");
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        String name = libraryMethod.getName(libraryClass);
        String type = libraryMethod.getDescriptor(libraryClass);
        ps.println(ClassUtil.externalClassName(libraryClass.getName()) +
                   (verbose ?
                        ": " + ClassUtil.externalFullMethodDescription(libraryClass.getName(), 0, name, type):
                        "."  + name));
        ps.println("  is a library method.\n");
    }
    private void printReason(VisitorAccepter visitorAccepter)
    {
        if (shortestUsageMarker.isUsed(visitorAccepter))
        {
            ShortestUsageMark shortestUsageMark = shortestUsageMarker.getShortestUsageMark(visitorAccepter);
            ps.print("  " + shortestUsageMark.getReason());
            shortestUsageMark.acceptClassVisitor(this);
            shortestUsageMark.acceptMemberVisitor(this);
        }
        else
        {
            ps.println("  is not being kept.\n");
        }
    }
    private static String lineNumberRange(ProgramClass programClass, ProgramMember programMember)
    {
        String range = programMember.getLineNumberRange(programClass);
        return range != null ?
            (" (" + range + ")") :
            "";
    }
}
