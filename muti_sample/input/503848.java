import proguard.classfile.util.ClassUtil;
import proguard.util.ListUtil;
import java.util.List;
public class FilterElement extends DataType
{
    private String filter;
    public void appendTo(List filter, boolean internal)
    {
        FilterElement filterElement = isReference() ?
            (FilterElement)getCheckedRef(this.getClass(),
                                         this.getClass().getName()) :
            this;
        String filterString = filterElement.filter;
        if (filterString == null)
        {
            filter.clear();
        }
        else
        {
            if (internal)
            {
                filterString = ClassUtil.internalClassName(filterString);
            }
            filter.addAll(ListUtil.commaSeparatedList(filterString));
        }
    }
    public void setName(String name)
    {
        this.filter = name;
    }
    public void setFilter(String filter)
    {
        this.filter = filter;
    }
}
