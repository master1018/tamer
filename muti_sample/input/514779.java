package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.util.*;
public class MemberDescriptorFilter implements MemberVisitor
{
    private final StringMatcher regularExpressionMatcher;
    private final MemberVisitor memberVisitor;
    public MemberDescriptorFilter(String        regularExpression,
                                  MemberVisitor memberVisitor)
    {
        this(new ClassNameParser().parse(regularExpression), memberVisitor);
    }
    public MemberDescriptorFilter(StringMatcher regularExpressionMatcher,
                                  MemberVisitor memberVisitor)
    {
        this.regularExpressionMatcher = regularExpressionMatcher;
        this.memberVisitor            = memberVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (accepted(programField.getDescriptor(programClass)))
        {
            memberVisitor.visitProgramField(programClass, programField);
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (accepted(programMethod.getDescriptor(programClass)))
        {
            memberVisitor.visitProgramMethod(programClass, programMethod);
        }
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        if (accepted(libraryField.getDescriptor(libraryClass)))
        {
            memberVisitor.visitLibraryField(libraryClass, libraryField);
        }
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (accepted(libraryMethod.getDescriptor(libraryClass)))
        {
            memberVisitor.visitLibraryMethod(libraryClass, libraryMethod);
        }
    }
    private boolean accepted(String name)
    {
        return regularExpressionMatcher.matches(name);
    }
}
