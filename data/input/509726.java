import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.info.ReadWriteFieldMarker;
public class WriteOnlyFieldFilter
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private final MemberVisitor writeOnlyFieldVisitor;
    public WriteOnlyFieldFilter(MemberVisitor writeOnlyFieldVisitor)
    {
        this.writeOnlyFieldVisitor = writeOnlyFieldVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (ReadWriteFieldMarker.isWritten(programField) &&
            !ReadWriteFieldMarker.isRead(programField))
        {
            writeOnlyFieldVisitor.visitProgramField(programClass, programField);
        }
    }
}
