package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.util.*;
import java.util.List;
public class ClassNameFilter implements ClassVisitor
{
    private final StringMatcher regularExpressionMatcher;
    private final ClassVisitor  classVisitor;
    public ClassNameFilter(String       regularExpression,
                           ClassVisitor classVisitor)
    {
        this(new ListParser(new ClassNameParser()).parse(regularExpression),
             classVisitor);
    }
    public ClassNameFilter(List         regularExpression,
                           ClassVisitor classVisitor)
    {
        this(new ListParser(new ClassNameParser()).parse(regularExpression),
             classVisitor);
    }
    public ClassNameFilter(StringMatcher regularExpressionMatcher,
                           ClassVisitor  classVisitor)
    {
        this.regularExpressionMatcher = regularExpressionMatcher;
        this.classVisitor             = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (accepted(programClass.getName()))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (accepted(libraryClass.getName()))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
    private boolean accepted(String name)
    {
        return regularExpressionMatcher.matches(name);
    }
}
