import proguard.classfile.*;
import proguard.classfile.io.ProgramClassWriter;
import java.io.*;
public class ClassRewriter implements DataEntryReader
{
    private final ClassPool       classPool;
    private final DataEntryWriter dataEntryWriter;
    public ClassRewriter(ClassPool       classPool,
                         DataEntryWriter dataEntryWriter)
    {
        this.classPool       = classPool;
        this.dataEntryWriter = dataEntryWriter;
    }
    public void read(DataEntry dataEntry) throws IOException
    {
        String inputName = dataEntry.getName();
        String className = inputName.substring(0, inputName.length() - ClassConstants.CLASS_FILE_EXTENSION.length());
        ProgramClass programClass = (ProgramClass)classPool.getClass(className);
        if (programClass != null)
        {
            String newClassName = programClass.getName();
            if (!className.equals(newClassName))
            {
                dataEntry = new RenamedDataEntry(dataEntry, newClassName + ClassConstants.CLASS_FILE_EXTENSION);
            }
            OutputStream outputStream = dataEntryWriter.getOutputStream(dataEntry);
            if (outputStream != null)
            {
                DataOutputStream classOutputStream = new DataOutputStream(outputStream);
                new ProgramClassWriter(classOutputStream).visitProgramClass(programClass);
                classOutputStream.flush();
            }
        }
    }
}
