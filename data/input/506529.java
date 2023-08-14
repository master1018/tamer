import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AllAttributeVisitor;
import proguard.classfile.visitor.*;
public class Preverifier
{
    private final Configuration configuration;
    public Preverifier(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public void execute(ClassPool programClassPool)
    {
        programClassPool.classesAccept(new ClassCleaner());
        ClassVisitor preverifier =
            new AllMethodVisitor(
            new AllAttributeVisitor(
            new CodePreverifier(configuration.microEdition)));
        if (!configuration.microEdition)
        {
            preverifier =
                new ClassVersionFilter(ClassConstants.INTERNAL_CLASS_VERSION_1_6,
                                       Integer.MAX_VALUE,
                                       preverifier);
        }
        programClassPool.classesAccept(preverifier);
    }
}
