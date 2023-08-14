public class ExecutableMemberComparator implements
        Comparator<IExecutableMemberDelta<? extends IExecutableMember>> {
    public int compare(IExecutableMemberDelta<? extends IExecutableMember> a,
            IExecutableMemberDelta<? extends IExecutableMember> b) {
        assert a.getType() == b.getType();
        IExecutableMember aMember = null;
        IExecutableMember bMember = null;
        if (a.getFrom() != null) {
            aMember = a.getFrom();
            bMember = b.getFrom();
        } else {
            aMember = a.getTo();
            bMember = b.getTo();
        }
        int returnValue = aMember.getName().compareTo(bMember.getName());
        return returnValue != 0 ? returnValue : compareParameterLists(aMember
                .getParameters(), bMember.getParameters());
    }
    private int compareParameterLists(List<IParameter> a, List<IParameter> b) {
        if (a.size() != b.size()) {
            return a.size() - b.size();
        }
        Iterator<IParameter> aIt = a.iterator();
        Iterator<IParameter> bIt = b.iterator();
        int returnValue = 0;
        while (aIt.hasNext() && bIt.hasNext()) {
            returnValue += getTypeName(aIt.next().getType()).compareTo(
                    getTypeName(bIt.next().getType()));
        }
        return returnValue;
    }
    private String getTypeName(ITypeReference type) {
        if (type instanceof IClassReference) {
            return getTypeName(((IClassReference) type).getClassDefinition());
        }
        if (type instanceof ITypeVariableReference) {
            return getTypeName(((ITypeVariableReference) type)
                    .getTypeVariableDefinition());
        }
        if (type instanceof IParameterizedType) {
            return getTypeName(((IParameterizedType) type).getRawType());
        }
        if (type instanceof IArrayType) {
            return getTypeName(((IArrayType) type).getComponentType());
        }
        if (type instanceof IPrimitiveType) {
            return ((IPrimitiveType) type).getName();
        }
        return type.toString();
    }
    private String getTypeName(ITypeDefinition type) {
        if (type instanceof IClassDefinition) {
            return ((IClassDefinition) type).getName();
        }
        if (type instanceof ITypeVariableDefinition) {
            return ((ITypeVariableDefinition) type).getName();
        }
        return type.toString();
    }
}
