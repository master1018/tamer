package proguard.classfile.util;
import proguard.util.*;
import java.io.PrintStream;
import java.util.List;
public class WarningPrinter
{
    private final PrintStream   printStream;
    private final StringMatcher classFilter;
    private int                 warningCount;
    public WarningPrinter()
    {
        this(System.err);
    }
    public WarningPrinter(PrintStream printStream)
    {
        this.printStream = printStream;
        this.classFilter = null;
    }
    public WarningPrinter(PrintStream printStream, List classFilter)
    {
        this.printStream = printStream;
        this.classFilter = classFilter == null ? null :
            new ListParser(new ClassNameParser()).parse(classFilter);
    }
    public void print(String className, String warning)
    {
        if (accepts(className))
        {
            print(warning);
        }
    }
    public boolean accepts(String className)
    {
        return classFilter == null ||
            !classFilter.matches(className);
    }
    public void print(String className1, String className2, String warning)
    {
        if (accepts(className1, className2))
        {
            print(warning);
        }
    }
    public boolean accepts(String className1, String className2)
    {
        return classFilter == null ||
            !(classFilter.matches(className1) ||
              classFilter.matches(className2));
    }
    private void print(String warning)
    {
        printStream.println(warning);
        warningCount++;
    }
    public int getWarningCount()
    {
        return warningCount;
    }
}
