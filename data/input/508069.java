public class ViewpointAdapter {
    static Map<ITypeVariableDefinition, ITypeReference> createTypeMapping(
            IParameterizedType paramameterizedType,
            IClassDefinition parameterizedTypeDefinition) {
        List<ITypeVariableDefinition> typeParameters =
                parameterizedTypeDefinition.getTypeParameters();
        List<ITypeReference> actualTypeArguments = paramameterizedType
                .getTypeArguments();
        if (actualTypeArguments == null || typeParameters == null) {
            return Collections.emptyMap();
        }
        Map<ITypeVariableDefinition, ITypeReference> substitution =
                new HashMap<ITypeVariableDefinition, ITypeReference>();
        Iterator<ITypeVariableDefinition> paramsIterator = typeParameters
                .iterator();
        Iterator<ITypeReference> argumentsIterator = actualTypeArguments
                .iterator();
        while (paramsIterator.hasNext() && argumentsIterator.hasNext()) {
            substitution.put(paramsIterator.next(), argumentsIterator.next());
        }
        return substitution;
    }
    public static Set<ITypeReference> substitutedTypeReferences(
            Set<ITypeReference> original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        List<ITypeReference> result = new ArrayList<ITypeReference>(original);
        return new HashSet<ITypeReference>(substitutedTypeReferences(result,
                mappings));
    }
    public static List<ITypeReference> substitutedTypeReferences(
            List<ITypeReference> original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        List<ITypeReference> result = new ArrayList<ITypeReference>(original
                .size());
        for (ITypeReference typeReference : original) {
            result.add(substitutedTypeReference(typeReference, mappings));
        }
        return result;
    }
    public static ITypeReference substitutedTypeReference(
            ITypeReference original,
            Map<ITypeVariableDefinition, ITypeReference> mappings) {
        ITypeReference type = original;
        if (type instanceof IClassReference) {
            return new ClassReferenceProjection((IClassReference) original,
                    mappings);
        } else if (type instanceof IPrimitiveType) {
            return type;
        } else if (type instanceof IArrayType) {
            return new ArrayTypeProjection((IArrayType) type, mappings);
        } else if (type instanceof IParameterizedType) {
            return new ParameterizedTypeProjection((IParameterizedType) type,
                    mappings);
        } else if (type instanceof IWildcardType) {
            return new WildcardTypeProjection((IWildcardType) type, mappings);
        } else if (type instanceof ITypeVariableReference) {
            ITypeReference subst = mappings.get(((ITypeVariableReference) type)
                    .getTypeVariableDefinition());
            return subst != null ? subst : type;
        }
        throw new IllegalStateException();
    }
    public static IClassReference getReferenceTo(IClassDefinition definition) {
        return new SigClassReference(new ClassProjection(definition,
                new HashMap<ITypeVariableDefinition, ITypeReference>()));
    }
}
