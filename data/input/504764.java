public class ParameterInfo
{
    ParameterInfo(String name, String typeName, TypeInfo type, SourcePositionInfo position)
    {
        mName = name;
        mTypeName = typeName;
        mType = type;
        mPosition = position;
    }
    TypeInfo type()
    {
        return mType;
    }
    String name()
    {
        return mName;
    }
    String typeName()
    {
        return mTypeName;
    }
    SourcePositionInfo position()
    {
        return mPosition;
    }
    public void makeHDF(HDF data, String base, boolean isLastVararg,
            HashSet<String> typeVariables)
    {
        data.setValue(base + ".name", this.name());
        type().makeHDF(data, base + ".type", isLastVararg, typeVariables);
    }
    public static void makeHDF(HDF data, String base, ParameterInfo[] params,
            boolean isVararg, HashSet<String> typeVariables)
    {
        for (int i=0; i<params.length; i++) {
            params[i].makeHDF(data, base + "." + i,
                    isVararg && (i == params.length - 1), typeVariables);
        }
    }
    String mName;
    String mTypeName;
    TypeInfo mType;
    SourcePositionInfo mPosition;
}
