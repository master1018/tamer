import proguard.classfile.*;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class AttributeUsageMarker
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private static final Object USED = new Object();
    public void visitAnyAttribute(Clazz clazz, Attribute attribute)
    {
        markAsUsed(attribute);
    }
    private static void markAsUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(USED);
    }
    static boolean isUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == USED;
    }
}
