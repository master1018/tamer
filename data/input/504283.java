package proguard.classfile.visitor;
import proguard.classfile.ClassPool;
public class AllClassVisitor implements ClassPoolVisitor
{
    private final ClassVisitor classVisitor;
    public AllClassVisitor(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitClassPool(ClassPool classPool)
    {
        classPool.classesAccept(classVisitor);
    }
}
