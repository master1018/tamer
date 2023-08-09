package proguard.classfile.visitor;
import proguard.classfile.ClassPool;
public class MultiClassPoolVisitor implements ClassPoolVisitor
{
    private static final int ARRAY_SIZE_INCREMENT = 5;
    private ClassPoolVisitor[] classPoolVisitors;
    private int                classPoolVisitorCount;
    public MultiClassPoolVisitor()
    {
    }
    public MultiClassPoolVisitor(ClassPoolVisitor[] classPoolVisitors)
    {
        this.classPoolVisitors     = classPoolVisitors;
        this.classPoolVisitorCount = classPoolVisitors.length;
    }
    public void addClassPoolVisitor(ClassPoolVisitor classPoolVisitor)
    {
        ensureArraySize();
        classPoolVisitors[classPoolVisitorCount++] = classPoolVisitor;
    }
    private void ensureArraySize()
    {
        if (classPoolVisitors == null)
        {
            classPoolVisitors = new ClassPoolVisitor[ARRAY_SIZE_INCREMENT];
        }
        else if (classPoolVisitors.length == classPoolVisitorCount)
        {
            ClassPoolVisitor[] newClassPoolVisitors =
                new ClassPoolVisitor[classPoolVisitorCount +
                                     ARRAY_SIZE_INCREMENT];
            System.arraycopy(classPoolVisitors, 0,
                             newClassPoolVisitors, 0,
                             classPoolVisitorCount);
            classPoolVisitors = newClassPoolVisitors;
        }
    }
    public void visitClassPool(ClassPool classPool)
    {
        for (int index = 0; index < classPoolVisitorCount; index++)
        {
            classPoolVisitors[index].visitClassPool(classPool);
        }
    }
}
