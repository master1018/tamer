import proguard.classfile.ClassConstants;
import java.io.IOException;
import java.util.Map;
public class DataEntryRenamer implements DataEntryReader
{
    private final Map             nameMap;
    private final DataEntryReader renamedDataEntryReader;
    private final DataEntryReader missingDataEntryReader;
    public DataEntryRenamer(Map             nameMap,
                            DataEntryReader renamedDataEntryReader)
    {
        this(nameMap, renamedDataEntryReader, null);
    }
    public DataEntryRenamer(Map             nameMap,
                            DataEntryReader renamedDataEntryReader,
                            DataEntryReader missingDataEntryReader)
    {
        this.nameMap                = nameMap;
        this.renamedDataEntryReader = renamedDataEntryReader;
        this.missingDataEntryReader = missingDataEntryReader;
    }
    public void read(DataEntry dataEntry) throws IOException
    {
        String name = dataEntry.getName();
        if (dataEntry.isDirectory() &&
            name.length() > 0)
        {
            name += ClassConstants.INTERNAL_PACKAGE_SEPARATOR;
        }
        String newName = (String)nameMap.get(name);
        if (newName != null)
        {
            if (dataEntry.isDirectory() &&
                newName.length() > 0)
            {
                newName = newName.substring(0, newName.length() -  1);
            }
            renamedDataEntryReader.read(new RenamedDataEntry(dataEntry, newName));
        }
        else if (missingDataEntryReader != null)
        {
            missingDataEntryReader.read(dataEntry);
        }
    }
}