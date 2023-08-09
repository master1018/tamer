package proguard.classfile.util;
import proguard.classfile.*;
import proguard.classfile.visitor.*;
import java.util.*;
public class MethodLinker
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor
{
    private final Map memberMap = new HashMap();
    public void visitAnyClass(Clazz clazz)
    {
        clazz.hierarchyAccept(true, true, true, false,
            new AllMethodVisitor(
            new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
            this)));
        memberMap.clear();
    }
    public void visitAnyMember(Clazz clazz, Member member)
    {
        String name       = member.getName(clazz);
        String descriptor = member.getDescriptor(clazz);
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }
        String key = name + ' ' + descriptor;
        Member otherMember = (Member)memberMap.get(key);
        if (otherMember == null)
        {
            Member thisLastMember = lastMember(member);
            memberMap.put(key, thisLastMember);
        }
        else
        {
            link(member, otherMember);
        }
    }
    private static void link(Member member1, Member member2)
    {
        Member lastMember1 = lastMember(member1);
        Member lastMember2 = lastMember(member2);
        if (!lastMember1.equals(lastMember2))
        {
            if (lastMember2 instanceof LibraryMember)
            {
                lastMember1.setVisitorInfo(lastMember2);
            }
            else
            {
                lastMember2.setVisitorInfo(lastMember1);
            }
        }
    }
    public static Member lastMember(Member member)
    {
        Member lastMember = member;
        while (lastMember.getVisitorInfo() != null &&
               lastMember.getVisitorInfo() instanceof Member)
        {
            lastMember = (Member)lastMember.getVisitorInfo();
        }
        return lastMember;
    }
    public static VisitorAccepter lastVisitorAccepter(VisitorAccepter visitorAccepter)
    {
        VisitorAccepter lastVisitorAccepter = visitorAccepter;
        while (lastVisitorAccepter.getVisitorInfo() != null &&
               lastVisitorAccepter.getVisitorInfo() instanceof VisitorAccepter)
        {
            lastVisitorAccepter = (VisitorAccepter)lastVisitorAccepter.getVisitorInfo();
        }
        return lastVisitorAccepter;
    }
}
