package proguard.classfile.visitor;
import proguard.classfile.ClassPool;
public class NamedClassVisitor implements ClassPoolVisitor
{
    private final ClassVisitor classVisitor;
    private final String       name;
    public NamedClassVisitor(ClassVisitor classVisitor,
                             String       name)
    {
        this.classVisitor = classVisitor;
        this.name         = name;
    }
    public void visitClassPool(ClassPool classPool)
    {
        classPool.classAccept(name, classVisitor);
    }
}
