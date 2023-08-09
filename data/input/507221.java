import proguard.classfile.ClassConstants;
import proguard.util.ExtensionMatcher;
import java.io.IOException;
public class DirectoryFilter extends FilteredDataEntryReader
{
    public DirectoryFilter(DataEntryReader directoryReader)
    {
        this (directoryReader, null);
    }
    public DirectoryFilter(DataEntryReader directoryReader,
                           DataEntryReader otherReader)
    {
        super(new DataEntryDirectoryFilter(),
              directoryReader,
              otherReader);
    }
}