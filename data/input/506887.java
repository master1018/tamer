public class NameFilter extends FilteredDataEntryReader
{
    public NameFilter(String          regularExpression,
                      DataEntryReader acceptedDataEntryReader)
    {
        this(regularExpression, acceptedDataEntryReader, null);
    }
    public NameFilter(String          regularExpression,
                      DataEntryReader acceptedDataEntryReader,
                      DataEntryReader rejectedDataEntryReader)
    {
        super(new DataEntryNameFilter(new ListParser(new FileNameParser()).parse(regularExpression)),
              acceptedDataEntryReader,
              rejectedDataEntryReader);
    }
    public NameFilter(List            regularExpressions,
                      DataEntryReader acceptedDataEntryReader)
    {
        this(regularExpressions, acceptedDataEntryReader, null);
    }
    public NameFilter(List            regularExpressions,
                      DataEntryReader acceptedDataEntryReader,
                      DataEntryReader rejectedDataEntryReader)
    {
        super(new DataEntryNameFilter(new ListParser(new FileNameParser()).parse(regularExpressions)),
              acceptedDataEntryReader,
              rejectedDataEntryReader);
    }
}