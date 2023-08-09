    public SigClassDefinition getClass(String packageName, String className);
    public IClassReference getClassReference(String packageName,
            String className);
    public SigArrayType getArrayType(ITypeReference componentType);
    public SigParameterizedType getParameterizedType(ITypeReference ownerType,
            IClassReference rawType, List<ITypeReference> typeArguments);
    public boolean containsTypeVariableDefinition(String name,
            IGenericDeclaration genericDeclaration);
    public SigTypeVariableDefinition getTypeVariable(String name,
            IGenericDeclaration genericDeclaration);
    public ITypeVariableReference getTypeVariableReference(String name,
            IGenericDeclaration genericDeclaration);
    public SigWildcardType getWildcardType(ITypeReference lowerBound,
            List<ITypeReference> upperBounds);
}
