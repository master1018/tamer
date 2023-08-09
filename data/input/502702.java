import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AllAttributeVisitor;
import proguard.classfile.visitor.*;
public class SubroutineInliner
{
    private final Configuration configuration;
    public SubroutineInliner(Configuration configuration)
    {
        this.configuration = configuration;
    }
    public void execute(ClassPool programClassPool)
    {
        programClassPool.classesAccept(new ClassCleaner());
        ClassVisitor inliner =
            new AllMethodVisitor(
            new AllAttributeVisitor(
            new CodeSubroutineInliner()));
        if (!configuration.microEdition)
        {
            inliner =
                new ClassVersionFilter(ClassConstants.INTERNAL_CLASS_VERSION_1_6,
                                       Integer.MAX_VALUE,
                                       inliner);
        }
        programClassPool.classesAccept(inliner);
    }
}
