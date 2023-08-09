package proguard.classfile.util;
import proguard.classfile.*;
import proguard.classfile.visitor.*;
public class MemberFinder
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private static class MemberFoundException extends RuntimeException {}
    private static final MemberFoundException MEMBER_FOUND = new MemberFoundException();
    private Clazz  clazz;
    private Member member;
    public Field findField(Clazz  referencingClass,
                           Clazz  clazz,
                           String name,
                           String descriptor)
    {
        return (Field)findMember(referencingClass, clazz, name, descriptor, true);
    }
    public Method findMethod(Clazz  referencingClass,
                             Clazz  clazz,
                             String name,
                             String descriptor)
    {
        return (Method)findMember(referencingClass, clazz, name, descriptor, false);
    }
    public Member findMember(Clazz   referencingClass,
                             Clazz   clazz,
                             String  name,
                             String  descriptor,
                             boolean isField)
    {
        try
        {
            this.clazz  = null;
            this.member = null;
            clazz.hierarchyAccept(true, true, true, false, isField ?
                (ClassVisitor)new NamedFieldVisitor(name, descriptor,
                              new MemberClassAccessFilter(referencingClass, this)) :
                (ClassVisitor)new NamedMethodVisitor(name, descriptor,
                              new MemberClassAccessFilter(referencingClass, this)));
        }
        catch (MemberFoundException ex)
        {
        }
        return member;
    }
    public Clazz correspondingClass()
    {
        return clazz;
    }
    public boolean isOverriden(Clazz  clazz,
                               Method method)
    {
        String name       = method.getName(clazz);
        String descriptor = method.getDescriptor(clazz);
        try
        {
            this.clazz  = null;
            this.member = null;
            clazz.hierarchyAccept(false, false, false, true,
                new NamedMethodVisitor(name, descriptor,
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE, this)));
        }
        catch (MemberFoundException ex)
        {
            return true;
        }
        return false;
    }
    public boolean isShadowed(Clazz clazz,
                              Field field)
    {
        String name       = field.getName(clazz);
        String descriptor = field.getDescriptor(clazz);
        try
        {
            this.clazz  = null;
            this.member = null;
            clazz.hierarchyAccept(false, false, false, true,
                new NamedFieldVisitor(name, descriptor,
                new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE, this)));
        }
        catch (MemberFoundException ex)
        {
            return true;
        }
        return false;
    }
    public void visitAnyMember(Clazz clazz, Member member)
    {
        this.clazz  = clazz;
        this.member = member;
        throw MEMBER_FOUND;
    }
}
