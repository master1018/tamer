import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.MemberVisitor;
import java.util.Map;
public class MemberNameCollector
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private final boolean allowAggressiveOverloading;
    private final Map     descriptorMap;
    public MemberNameCollector(boolean allowAggressiveOverloading,
                               Map     descriptorMap)
    {
        this.allowAggressiveOverloading = allowAggressiveOverloading;
        this.descriptorMap              = descriptorMap;
    }
    public void visitAnyMember(Clazz clazz, Member member)
    {
        String name = member.getName(clazz);
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }
        String newName = MemberObfuscator.newMemberName(member);
        if (newName != null)
        {
            String descriptor = member.getDescriptor(clazz);
            if (!allowAggressiveOverloading)
            {
                descriptor = descriptor.substring(0, descriptor.indexOf(')')+1);
            }
            Map nameMap = MemberObfuscator.retrieveNameMap(descriptorMap, descriptor);
            String otherName = (String)nameMap.get(newName);
            if (otherName == null                              ||
                MemberObfuscator.hasFixedNewMemberName(member) ||
                name.compareTo(otherName) < 0)
            {
                nameMap.put(newName, name);
            }
        }
    }
}
