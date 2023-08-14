import proguard.classfile.ClassConstants;
import proguard.util.ExtensionMatcher;
import java.io.IOException;
public class ClassFilter extends FilteredDataEntryReader
{
    public ClassFilter(DataEntryReader classReader)
    {
        this(classReader, null);
    }
    public ClassFilter(DataEntryReader classReader,
                       DataEntryReader dataEntryReader)
    {
        super(new DataEntryNameFilter(
              new ExtensionMatcher(ClassConstants.CLASS_FILE_EXTENSION)),
              classReader,
              dataEntryReader);
    }
}
