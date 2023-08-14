import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
import java.util.Map;
public class MapCleaner
extends      SimplifiedVisitor
implements   ClassVisitor
{
    private final Map map;
    public MapCleaner(Map map)
    {
        this.map = map;
    }
    public void visitAnyClass(Clazz clazz)
    {
        map.clear();
    }
}
