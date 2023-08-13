public class GenericSignatureParser {
    public List<ITypeReference> exceptionTypes;
    public List<ITypeReference> parameterTypes;
    public List<ITypeVariableDefinition> formalTypeParameters;
    public ITypeReference returnType;
    public ITypeReference fieldType;
    public List<ITypeReference> interfaceTypes;
    public ITypeReference superclassType;
    private IGenericDeclaration genericDecl;
    private char symbol; 
    private String identifier;
    private boolean eof;
    private char[] buffer;
    private int pos;
    private final ITypeFactory factory;
    private final IClassInitializer classFinder;
    private boolean parseForField;
    public GenericSignatureParser(ITypeFactory factory,
            IClassInitializer classFinder) {
        this.factory = factory;
        this.classFinder = classFinder;
    }
    private void setInput(IGenericDeclaration genericDecl, String input) {
        if (input != null) {
            this.genericDecl = genericDecl;
            this.buffer = input.toCharArray();
            this.eof = false;
            scanSymbol();
        } else {
            this.eof = true;
        }
    }
    public ITypeReference parseNonGenericType(String typeSignature) {
        setInput(null, typeSignature);
        ITypeReference type = parsePrimitiveType();
        if (type == null) {
            type = parseFieldTypeSignature();
        }
        return type;
    }
    public ITypeReference parseNonGenericReturnType(String typeSignature) {
        setInput(null, typeSignature);
        ITypeReference returnType = parsePrimitiveType();
        if (returnType == null) {
            returnType = parseReturnType();
        }
        return returnType;
    }
    private ITypeReference parsePrimitiveType() {
        switch (symbol) {
        case 'B':
            scanSymbol();
            return SigPrimitiveType.BYTE_TYPE;
        case 'C':
            scanSymbol();
            return SigPrimitiveType.CHAR_TYPE;
        case 'D':
            scanSymbol();
            return SigPrimitiveType.DOUBLE_TYPE;
        case 'F':
            scanSymbol();
            return SigPrimitiveType.FLOAT_TYPE;
        case 'I':
            scanSymbol();
            return SigPrimitiveType.INT_TYPE;
        case 'J':
            scanSymbol();
            return SigPrimitiveType.LONG_TYPE;
        case 'S':
            scanSymbol();
            return SigPrimitiveType.SHORT_TYPE;
        case 'Z':
            scanSymbol();
            return SigPrimitiveType.BOOLEAN_TYPE;
        default:
            return null;
        }
    }
    public void parseForClass(IClassDefinition classToProcess,
            String signature) {
        setInput(classToProcess, signature);
        if (!eof) {
            parseClassSignature();
        } else {
            throw new IllegalStateException("Generic signature is invalid!");
        }
    }
    public void parseForMethod(IMethod genericDecl, String signature) {
        setInput(genericDecl, signature);
        if (!eof) {
            parseMethodTypeSignature();
        } else {
            throw new IllegalStateException("Generic signature is invalid!");
        }
    }
    public void parseForConstructor(IConstructor genericDecl,
            String signature) {
        setInput(genericDecl, signature);
        if (!eof) {
            parseMethodTypeSignature();
        } else {
            throw new IllegalStateException("Generic signature is invalid!");
        }
    }
    public void parseForField(IClassDefinition genericDecl, String signature) {
        parseForField = true;
        setInput(genericDecl, signature);
        try {
            if (!eof) {
                this.fieldType = parseFieldTypeSignature();
            } else {
                throw new IllegalStateException(
                        "Generic signature is invalid!");
            }
        } finally {
            parseForField = false;
        }
    }
    private void parseClassSignature() {
        parseOptFormalTypeParameters();
        this.superclassType = parseClassTypeSignature();
        interfaceTypes = new ArrayList<ITypeReference>(16);
        while (symbol > 0) {
            interfaceTypes.add(parseClassTypeSignature());
        }
    }
    private void parseOptFormalTypeParameters() {
        List<ITypeVariableDefinition> typeParameters =
                new ArrayList<ITypeVariableDefinition>();
        if (symbol == '<') {
            scanSymbol();
            typeParameters.add(parseFormalTypeParameter());
            while ((symbol != '>') && (symbol > 0)) {
                typeParameters.add(parseFormalTypeParameter());
            }
            expect('>');
        }
        formalTypeParameters = typeParameters;
    }
    private SigTypeVariableDefinition parseFormalTypeParameter() {
        scanIdentifier();
        String name = identifier.intern();
        SigTypeVariableDefinition typeVariable = factory.getTypeVariable(name,
                genericDecl);
        List<ITypeReference> bounds = new ArrayList<ITypeReference>();
        expect(':');
        if (symbol == 'L' || symbol == '[' || symbol == 'T') {
            bounds.add(parseFieldTypeSignature());
        }
        while (symbol == ':') {
            scanSymbol();
            bounds.add(parseFieldTypeSignature());
        }
        typeVariable.setUpperBounds(bounds);
        return typeVariable;
    }
    private IGenericDeclaration getDeclarationOfTypeVariable(
            String variableName, IClassDefinition declaration) {
        assert variableName != null;
        assert declaration != null;
        if (!Uninitialized.isInitialized(declaration.getTypeParameters())) {
            declaration = classFinder.initializeClass(declaration
                    .getPackageName(), declaration.getName());
        }
        for (ITypeVariableDefinition typeVariable : declaration
                .getTypeParameters()) {
            if (variableName.equals(typeVariable.getName())) {
                return declaration;
            }
        }
        return getDeclarationOfTypeVariable(variableName, declaration
                .getDeclaringClass());
    }
    private ITypeReference parseFieldTypeSignature() {
        switch (symbol) {
        case 'L':
            return parseClassTypeSignature();
        case '[':
            scanSymbol();
            SigArrayType arrayType = factory.getArrayType(parseTypeSignature());
            return arrayType;
        case 'T':
            return parseTypeVariableSignature();
        default:
            throw new GenericSignatureFormatError();
        }
    }
    private ITypeReference parseClassTypeSignature() {
        expect('L');
        StringBuilder qualIdent = new StringBuilder("L");
        scanIdentifier();
        while (symbol == '/') {
            scanSymbol();
            qualIdent.append(identifier).append("/");
            scanIdentifier();
        }
        qualIdent.append(this.identifier);
        List<ITypeReference> typeArgs = parseOptTypeArguments();
        ITypeReference parentType = null;
        String packageName = getPackageName(qualIdent.toString() + ";");
        String className = getClassName(qualIdent.toString() + ";");
        if (typeArgs.isEmpty()) {
            parentType = factory.getClassReference(packageName, className);
        } else {
            IClassReference rawType = factory.getClassReference(packageName,
                    className);
            SigParameterizedType parameterizedType = factory
                    .getParameterizedType(null, rawType, typeArgs);
            parentType = parameterizedType;
        }
        ITypeReference typeToReturn = parentType;
        while (symbol == '.') {
            scanSymbol();
            scanIdentifier();
            qualIdent.append("$").append(identifier);
            typeArgs = parseOptTypeArguments();
            ITypeReference memberType = null;
            packageName = getPackageName(qualIdent.toString() + ";");
            className = getClassName(qualIdent.toString() + ";");
            if (typeArgs.isEmpty()) {
                memberType = factory.getClassReference(packageName, className);
            } else {
                IClassReference rawType = factory.getClassReference(
                        packageName, className);
                SigParameterizedType parameterizedType = factory
                        .getParameterizedType(parentType, rawType, typeArgs);
                memberType = parameterizedType;
            }
            typeToReturn = memberType;
        }
        expect(';');
        return typeToReturn;
    }
    private List<ITypeReference> parseOptTypeArguments() {
        List<ITypeReference> typeArgs = new ArrayList<ITypeReference>(8);
        if (symbol == '<') {
            scanSymbol();
            typeArgs.add(parseTypeArgument());
            while ((symbol != '>') && (symbol > 0)) {
                typeArgs.add(parseTypeArgument());
            }
            expect('>');
        }
        return typeArgs;
    }
    private ITypeReference parseTypeArgument() {
        List<ITypeReference> extendsBound = new ArrayList<ITypeReference>(1);
        ITypeReference superBound = null;
        if (symbol == '*') {
            scanSymbol();
            extendsBound.add(factory.getClassReference("java.lang", "Object"));
            SigWildcardType wildcardType = factory.getWildcardType(superBound,
                    extendsBound);
            return wildcardType;
        } else if (symbol == '+') {
            scanSymbol();
            extendsBound.add(parseFieldTypeSignature());
            SigWildcardType wildcardType = factory.getWildcardType(superBound,
                    extendsBound);
            return wildcardType;
        } else if (symbol == '-') {
            scanSymbol();
            superBound = parseFieldTypeSignature();
            extendsBound.add(factory.getClassReference("java.lang", "Object"));
            SigWildcardType wildcardType = factory.getWildcardType(superBound,
                    extendsBound);
            return wildcardType;
        } else {
            return parseFieldTypeSignature();
        }
    }
    private ITypeVariableReference parseTypeVariableSignature() {
        expect('T');
        scanIdentifier();
        expect(';');
        IGenericDeclaration declaration = genericDecl;
        if (!factory.containsTypeVariableDefinition(identifier, declaration)) {
            if (parseForField) {
                declaration = getDeclarationOfTypeVariable(identifier,
                        (IClassDefinition) genericDecl);
            } else {
                declaration = getDeclarationOfTypeVariable(identifier,
                        genericDecl.getDeclaringClass());
            }
            factory.getTypeVariable(identifier, declaration);
        }
        return factory.getTypeVariableReference(identifier, declaration);
    }
    private ITypeReference parseTypeSignature() {
        switch (symbol) {
        case 'B':
            scanSymbol();
            return SigPrimitiveType.BYTE_TYPE;
        case 'C':
            scanSymbol();
            return SigPrimitiveType.CHAR_TYPE;
        case 'D':
            scanSymbol();
            return SigPrimitiveType.DOUBLE_TYPE;
        case 'F':
            scanSymbol();
            return SigPrimitiveType.FLOAT_TYPE;
        case 'I':
            scanSymbol();
            return SigPrimitiveType.INT_TYPE;
        case 'J':
            scanSymbol();
            return SigPrimitiveType.LONG_TYPE;
        case 'S':
            scanSymbol();
            return SigPrimitiveType.SHORT_TYPE;
        case 'Z':
            scanSymbol();
            return SigPrimitiveType.BOOLEAN_TYPE;
        default:
            return parseFieldTypeSignature();
        }
    }
    private void parseMethodTypeSignature() {
        parseOptFormalTypeParameters();
        parameterTypes = new ArrayList<ITypeReference>(16);
        expect('(');
        while (symbol != ')' && (symbol > 0)) {
            parameterTypes.add(parseTypeSignature());
        }
        expect(')');
        returnType = parseReturnType();
        exceptionTypes = new ArrayList<ITypeReference>(8);
        while (symbol == '^') {
            scanSymbol();
            if (symbol == 'T') {
                exceptionTypes.add(parseTypeVariableSignature());
            } else {
                exceptionTypes.add(parseClassTypeSignature());
            }
        }
    }
    private ITypeReference parseReturnType() {
        if (symbol != 'V') {
            return parseTypeSignature();
        } else {
            scanSymbol();
            return SigPrimitiveType.VOID_TYPE;
        }
    }
    private void scanSymbol() {
        if (!eof) {
            if (pos < buffer.length) {
                symbol = buffer[pos];
                pos++;
            } else {
                symbol = 0;
                eof = true;
            }
        } else {
            throw new GenericSignatureFormatError();
        }
    }
    private void expect(char c) {
        if (symbol == c) {
            scanSymbol();
        } else {
            throw new GenericSignatureFormatError();
        }
    }
    private boolean isStopSymbol(char ch) {
        switch (ch) {
        case ':':
        case '/':
        case ';':
        case '<':
        case '.':
            return true;
        }
        return false;
    }
    private void scanIdentifier() {
        if (!eof) {
            StringBuilder identBuf = new StringBuilder(32);
            if (!isStopSymbol(symbol)) {
                identBuf.append(symbol);
                do {
                    char ch = buffer[pos];
                    if ((ch >= 'a') && (ch <= 'z') || (ch >= 'A')
                            && (ch <= 'Z') || !isStopSymbol(ch)) {
                        identBuf.append(buffer[pos]);
                        pos++;
                    } else {
                        identifier = identBuf.toString();
                        scanSymbol();
                        return;
                    }
                } while (pos != buffer.length);
                identifier = identBuf.toString();
                symbol = 0;
                eof = true;
            } else {
                symbol = 0;
                eof = true;
                throw new GenericSignatureFormatError();
            }
        } else {
            throw new GenericSignatureFormatError();
        }
    }
}
