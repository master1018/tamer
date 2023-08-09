public class AnnotationValueInfo
{
    private Object mValue;
    private String mString;
    private MethodInfo mElement;
    public AnnotationValueInfo(MethodInfo element)
    {
        mElement = element;
    }
    public void init(Object value)
    {
        mValue = value;
    }
    public MethodInfo element()
    {
        return mElement;
    }
    public Object value()
    {
        return mValue;
    }
    public String valueString()
    {
        Object v = mValue;
        if (v instanceof TypeInfo) {
            return ((TypeInfo)v).fullName();
        }
        else if (v instanceof FieldInfo) {
            StringBuilder str = new StringBuilder();
            FieldInfo f = (FieldInfo)v;
            str.append(f.containingClass().qualifiedName());
            str.append('.');
            str.append(f.name());
            return str.toString();
        }
        else if (v instanceof AnnotationInstanceInfo) {
            return v.toString();
        }
        else if (v instanceof AnnotationValueInfo[]) {
            StringBuilder str = new StringBuilder();
            AnnotationValueInfo[] array = (AnnotationValueInfo[])v;
            final int N = array.length;
            str.append("{");
            for (int i=0; i<array.length; i++) {
                str.append(array[i].valueString());
                if (i != N-1) {
                    str.append(",");
                }
            }
            str.append("}");
            return str.toString();
        }
        else {
            return FieldInfo.constantLiteralValue(v);
        }
    }
}
