package proguard.classfile.visitor;
import proguard.classfile.*;
public class MultiMemberVisitor implements MemberVisitor
{
    private static final int ARRAY_SIZE_INCREMENT = 5;
    private MemberVisitor[] memberVisitors;
    private int             memberVisitorCount;
    public MultiMemberVisitor()
    {
    }
    public MultiMemberVisitor(MemberVisitor[] memberVisitors)
    {
        this.memberVisitors     = memberVisitors;
        this.memberVisitorCount = memberVisitors.length;
    }
    public void addMemberVisitor(MemberVisitor memberVisitor)
    {
        ensureArraySize();
        memberVisitors[memberVisitorCount++] = memberVisitor;
    }
    private void ensureArraySize()
    {
        if (memberVisitors == null)
        {
            memberVisitors = new MemberVisitor[ARRAY_SIZE_INCREMENT];
        }
        else if (memberVisitors.length == memberVisitorCount)
        {
            MemberVisitor[] newMemberVisitors =
                new MemberVisitor[memberVisitorCount +
                                         ARRAY_SIZE_INCREMENT];
            System.arraycopy(memberVisitors, 0,
                             newMemberVisitors, 0,
                             memberVisitorCount);
            memberVisitors = newMemberVisitors;
        }
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        for (int index = 0; index < memberVisitorCount; index++)
        {
            memberVisitors[index].visitProgramField(programClass, programField);
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        for (int index = 0; index < memberVisitorCount; index++)
        {
            memberVisitors[index].visitProgramMethod(programClass, programMethod);
        }
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        for (int index = 0; index < memberVisitorCount; index++)
        {
            memberVisitors[index].visitLibraryField(libraryClass, libraryField);
        }
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        for (int index = 0; index < memberVisitorCount; index++)
        {
            memberVisitors[index].visitLibraryMethod(libraryClass, libraryMethod);
        }
    }
}
