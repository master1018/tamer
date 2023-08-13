import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import java.io.PrintStream;
public class UsagePrinter
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor
{
    private final UsageMarker usageMarker;
    private final boolean     printUnusedItems;
    private final PrintStream ps;
    private String      className;
    public UsagePrinter(UsageMarker usageMarker,
                        boolean     printUnusedItems)
    {
        this(usageMarker, printUnusedItems, System.out);
    }
    public UsagePrinter(UsageMarker usageMarker,
                        boolean     printUnusedItems,
                        PrintStream printStream)
    {
        this.usageMarker      = usageMarker;
        this.printUnusedItems = printUnusedItems;
        this.ps               = printStream;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (usageMarker.isUsed(programClass))
        {
            if (printUnusedItems)
            {
                className = programClass.getName();
                programClass.fieldsAccept(this);
                programClass.methodsAccept(this);
                className = null;
            }
            else
            {
                ps.println(ClassUtil.externalClassName(programClass.getName()));
            }
        }
        else
        {
            if (printUnusedItems)
            {
                ps.println(ClassUtil.externalClassName(programClass.getName()));
            }
        }
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (usageMarker.isUsed(programField) ^ printUnusedItems)
        {
            printClassNameHeader();
            ps.println("    " +
                       lineNumberRange(programClass, programField) +
                       ClassUtil.externalFullFieldDescription(
                           programField.getAccessFlags(),
                           programField.getName(programClass),
                           programField.getDescriptor(programClass)));
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (usageMarker.isUsed(programMethod) ^ printUnusedItems)
        {
            printClassNameHeader();
            ps.println("    " +
                       lineNumberRange(programClass, programMethod) +
                       ClassUtil.externalFullMethodDescription(
                           programClass.getName(),
                           programMethod.getAccessFlags(),
                           programMethod.getName(programClass),
                           programMethod.getDescriptor(programClass)));
        }
    }
    private void printClassNameHeader()
    {
        if (className != null)
        {
            ps.println(ClassUtil.externalClassName(className) + ":");
            className = null;
        }
    }
    private static String lineNumberRange(ProgramClass programClass, ProgramMember programMember)
    {
        String range = programMember.getLineNumberRange(programClass);
        return range != null ?
            (range + ":") :
            "";
    }
}
