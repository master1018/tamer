package proguard.classfile.attribute.annotation.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.Annotation;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.MemberVisitor;
public class AnnotationToMemberVisitor
extends      SimplifiedVisitor
implements   AnnotationVisitor
{
    private final MemberVisitor memberVisitor;
    private Member lastVisitedMember;
    public AnnotationToMemberVisitor(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitAnnotation(Clazz clazz, Member member, Annotation annotation)
    {
        if (!member.equals(lastVisitedMember))
        {
            member.accept(clazz, memberVisitor);
            lastVisitedMember = member;
        }
    }
}
