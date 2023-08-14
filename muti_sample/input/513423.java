package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
import java.util.Set;
public class MemberCollector
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private final Set set;
    public MemberCollector(Set set)
    {
        this.set = set;
    }
    public void visitAnyMember(Clazz clazz, Member member)
    {
        set.add(member.getName(clazz) + member.getDescriptor(clazz));
    }
}