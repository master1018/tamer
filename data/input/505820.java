class AnnotationInstanceInfo
{
    private ClassInfo mType;
    private AnnotationValueInfo[] mElementValues;
    public AnnotationInstanceInfo(ClassInfo type, AnnotationValueInfo[] elementValues)
    {
        mType = type;
        mElementValues = elementValues;
    }
    ClassInfo type()
    {
        return mType;
    }
    AnnotationValueInfo[] elementValues()
    {
        return mElementValues;
    }
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        str.append("@");
        str.append(mType.qualifiedName());
        str.append("(");
        AnnotationValueInfo[] values = mElementValues;
        final int N = values.length;
        for (int i=0; i<N; i++) {
            AnnotationValueInfo value = values[i];
            str.append(value.element().name());
            str.append("=");
            str.append(value.valueString());
            if (i != N-1) {
                str.append(",");
            }
        }
        str.append(")");
        return str.toString();
    }
}
