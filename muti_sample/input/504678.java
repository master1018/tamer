package proguard.classfile.visitor;
import proguard.classfile.Clazz;
import proguard.classfile.util.SimplifiedVisitor;
import java.util.Set;
public class ClassCollector
extends      SimplifiedVisitor
implements   ClassVisitor
{
    private final Set set;
    public ClassCollector(Set set)
    {
        this.set = set;
    }
    public void visitAnyClass(Clazz clazz)
    {
        set.add(clazz);
    }
}
