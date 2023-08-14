import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import java.util.*;
public class MemberObfuscator
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private final boolean        allowAggressiveOverloading;
    private final NameFactory    nameFactory;
    private final Map            descriptorMap;
    public MemberObfuscator(boolean        allowAggressiveOverloading,
                            NameFactory    nameFactory,
                            Map            descriptorMap)
    {
        this.allowAggressiveOverloading = allowAggressiveOverloading;
        this.nameFactory                = nameFactory;
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
        String descriptor = member.getDescriptor(clazz);
        if (!allowAggressiveOverloading)
        {
            descriptor = descriptor.substring(0, descriptor.indexOf(')')+1);
        }
        Map nameMap = retrieveNameMap(descriptorMap, descriptor);
        String newName = newMemberName(member);
        if (newName == null)
        {
            nameFactory.reset();
            do
            {
                newName = nameFactory.nextName();
            }
            while (nameMap.containsKey(newName));
            nameMap.put(newName, name);
            setNewMemberName(member, newName);
        }
    }
    static Map retrieveNameMap(Map descriptorMap, String descriptor)
    {
        Map nameMap = (Map)descriptorMap.get(descriptor);
        if (nameMap == null)
        {
            nameMap = new HashMap();
            descriptorMap.put(descriptor, nameMap);
        }
        return nameMap;
    }
    static void setFixedNewMemberName(Member member, String name)
    {
        VisitorAccepter lastVisitorAccepter = MethodLinker.lastVisitorAccepter(member);
        if (!(lastVisitorAccepter instanceof LibraryMember) &&
            !(lastVisitorAccepter instanceof MyFixedName))
        {
            lastVisitorAccepter.setVisitorInfo(new MyFixedName(name));
        }
        else
        {
            lastVisitorAccepter.setVisitorInfo(name);
        }
    }
    static void setNewMemberName(Member member, String name)
    {
        MethodLinker.lastVisitorAccepter(member).setVisitorInfo(name);
    }
    static boolean hasFixedNewMemberName(Member member)
    {
        VisitorAccepter lastVisitorAccepter = MethodLinker.lastVisitorAccepter(member);
        return lastVisitorAccepter instanceof LibraryMember ||
               lastVisitorAccepter instanceof MyFixedName;
    }
    static String newMemberName(Member member)
    {
        return (String)MethodLinker.lastVisitorAccepter(member).getVisitorInfo();
    }
    private static class MyFixedName implements VisitorAccepter
    {
        private String newName;
        public MyFixedName(String newName)
        {
            this.newName = newName;
        }
        public Object getVisitorInfo()
        {
            return newName;
        }
        public void setVisitorInfo(Object visitorInfo)
        {
            newName = (String)visitorInfo;
        }
    }
}
