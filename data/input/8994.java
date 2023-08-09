public class SpinnerListModel extends AbstractSpinnerModel implements Serializable
{
    private List list;
    private int index;
    public SpinnerListModel(List<?> values) {
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("SpinnerListModel(List) expects non-null non-empty List");
        }
        this.list = values;
        this.index = 0;
    }
    public SpinnerListModel(Object[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("SpinnerListModel(Object[]) expects non-null non-empty Object[]");
        }
        this.list = Arrays.asList(values);
        this.index = 0;
    }
    public SpinnerListModel() {
        this(new Object[]{"empty"});
    }
    public List<?> getList() {
        return list;
    }
    public void setList(List<?> list) {
        if ((list == null) || (list.size() == 0)) {
            throw new IllegalArgumentException("invalid list");
        }
        if (!list.equals(this.list)) {
            this.list = list;
            index = 0;
            fireStateChanged();
        }
    }
    public Object getValue() {
        return list.get(index);
    }
    public void setValue(Object elt) {
        int index = list.indexOf(elt);
        if (index == -1) {
            throw new IllegalArgumentException("invalid sequence element");
        }
        else if (index != this.index) {
            this.index = index;
            fireStateChanged();
        }
    }
    public Object getNextValue() {
        return (index >= (list.size() - 1)) ? null : list.get(index + 1);
    }
    public Object getPreviousValue() {
        return (index <= 0) ? null : list.get(index - 1);
    }
    Object findNextMatch(String substring) {
        int max = list.size();
        if (max == 0) {
            return null;
        }
        int counter = index;
        do {
            Object value = list.get(counter);
            String string = value.toString();
            if (string != null && string.startsWith(substring)) {
                return value;
            }
            counter = (counter + 1) % max;
        } while (counter != index);
        return null;
    }
}
