package proguard.classfile.visitor;
import proguard.classfile.*;
public class MultiClassVisitor implements ClassVisitor
{
    private static final int ARRAY_SIZE_INCREMENT = 5;
    private ClassVisitor[] classVisitors;
    private int            classVisitorCount;
    public MultiClassVisitor()
    {
    }
    public MultiClassVisitor(ClassVisitor[] classVisitors)
    {
        this.classVisitors     = classVisitors;
        this.classVisitorCount = classVisitors.length;
    }
    public void addClassVisitor(ClassVisitor classVisitor)
    {
        ensureArraySize();
        classVisitors[classVisitorCount++] = classVisitor;
    }
    private void ensureArraySize()
    {
        if (classVisitors == null)
        {
            classVisitors = new ClassVisitor[ARRAY_SIZE_INCREMENT];
        }
        else if (classVisitors.length == classVisitorCount)
        {
            ClassVisitor[] newClassVisitors =
                new ClassVisitor[classVisitorCount +
                                     ARRAY_SIZE_INCREMENT];
            System.arraycopy(classVisitors, 0,
                             newClassVisitors, 0,
                             classVisitorCount);
            classVisitors = newClassVisitors;
        }
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        for (int index = 0; index < classVisitorCount; index++)
        {
            classVisitors[index].visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        for (int index = 0; index < classVisitorCount; index++)
        {
            classVisitors[index].visitLibraryClass(libraryClass);
        }
    }
}
