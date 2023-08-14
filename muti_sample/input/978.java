public class SignatureParser {
    private char[] input; 
    private int index = 0; 
    private static final char EOI = ':';
    private static final boolean DEBUG = false;
    private SignatureParser(){}
    private char getNext(){
        assert(index <= input.length);
        try {
            return input[index++];
        } catch (ArrayIndexOutOfBoundsException e) { return EOI;}
    }
    private char current(){
        assert(index <= input.length);
        try {
            return input[index];
        } catch (ArrayIndexOutOfBoundsException e) { return EOI;}
    }
    private void advance(){
        assert(index <= input.length);
        index++;
    }
    private boolean matches(char c, char... set) {
        for (char e : set) {
            if (c == e) return true;
        }
        return false;
    }
    private Error error(String errorMsg) {
        if (DEBUG) System.out.println("Parse error:" + errorMsg);
        return new GenericSignatureFormatError();
    }
    public static SignatureParser make() {
        return new SignatureParser();
    }
    public ClassSignature parseClassSig(String s) {
        if (DEBUG) System.out.println("Parsing class sig:" + s);
        input = s.toCharArray();
        return parseClassSignature();
    }
    public MethodTypeSignature parseMethodSig(String s) {
        if (DEBUG) System.out.println("Parsing method sig:" + s);
        input = s.toCharArray();
        return parseMethodTypeSignature();
    }
    public TypeSignature parseTypeSig(String s) {
        if (DEBUG) System.out.println("Parsing type sig:" + s);
        input = s.toCharArray();
        return parseTypeSignature();
    }
    private ClassSignature parseClassSignature() {
        assert(index == 0);
        return ClassSignature.make(parseZeroOrMoreFormalTypeParameters(),
                                   parseClassTypeSignature(),
                                   parseSuperInterfaces());
    }
    private FormalTypeParameter[] parseZeroOrMoreFormalTypeParameters(){
        if (current() == '<') { return parseFormalTypeParameters();}
        else {return new FormalTypeParameter[0];}
    }
    private FormalTypeParameter[] parseFormalTypeParameters(){
        Collection<FormalTypeParameter> ftps =
            new ArrayList<FormalTypeParameter>(3);
        assert(current() == '<'); 
        if (current() != '<') { throw error("expected <");}
        advance();
        ftps.add(parseFormalTypeParameter());
        while (current() != '>') {
            ftps.add(parseFormalTypeParameter());
        }
        advance();
        FormalTypeParameter[] ftpa = new FormalTypeParameter[ftps.size()];
        return ftps.toArray(ftpa);
    }
    private FormalTypeParameter parseFormalTypeParameter(){
        String id = parseIdentifier();
        FieldTypeSignature[] bs = parseZeroOrMoreBounds();
        return FormalTypeParameter.make(id, bs);
    }
    private String parseIdentifier(){
        StringBuilder result = new StringBuilder();
        while (!Character.isWhitespace(current())) {
            char c = current();
            switch(c) {
            case ';':
            case '.':
            case '/':
            case '[':
            case ':':
            case '>':
            case '<': return result.toString();
            default:{
                result.append(c);
                advance();
            }
            }
        }
        return result.toString();
    }
    private FieldTypeSignature parseFieldTypeSignature() {
        switch(current()) {
        case 'L':
           return parseClassTypeSignature();
        case 'T':
            return parseTypeVariableSignature();
        case '[':
            return parseArrayTypeSignature();
        default: throw error("Expected Field Type Signature");
        }
    }
    private ClassTypeSignature parseClassTypeSignature(){
        assert(current() == 'L');
        if (current() != 'L') { throw error("expected a class type");}
        advance();
        List<SimpleClassTypeSignature> scts =
            new ArrayList<SimpleClassTypeSignature>(5);
        scts.add(parseSimpleClassTypeSignature(false));
        parseClassTypeSignatureSuffix(scts);
        if (current() != ';')
            throw error("expected ';' got '" + current() + "'");
        advance();
        return ClassTypeSignature.make(scts);
    }
    private SimpleClassTypeSignature parseSimpleClassTypeSignature(boolean dollar){
            String id = parseIdentifier();
            char c = current();
            switch (c) {
            case ';':
            case '/':
                return SimpleClassTypeSignature.make(id, dollar, new TypeArgument[0]) ;
            case '<': {
                return SimpleClassTypeSignature.make(id, dollar, parseTypeArguments());
            }
            default: {throw error("expected < or ; or /");}
            }
    }
    private void parseClassTypeSignatureSuffix(List<SimpleClassTypeSignature> scts) {
        while (current() == '/' || current() == '.') {
            boolean dollar = (current() == '.');
            advance();
            scts.add(parseSimpleClassTypeSignature(dollar));
        }
    }
    private TypeArgument[] parseTypeArgumentsOpt() {
        if (current() == '<') {return parseTypeArguments();}
        else {return new TypeArgument[0];}
    }
    private TypeArgument[] parseTypeArguments() {
        Collection<TypeArgument> tas = new ArrayList<TypeArgument>(3);
        assert(current() == '<');
        if (current() != '<') { throw error("expected <");}
        advance();
        tas.add(parseTypeArgument());
        while (current() != '>') {
            tas.add(parseTypeArgument());
        }
        advance();
        TypeArgument[] taa = new TypeArgument[tas.size()];
        return tas.toArray(taa);
    }
    private TypeArgument parseTypeArgument() {
        FieldTypeSignature[] ub, lb;
        ub = new FieldTypeSignature[1];
        lb = new FieldTypeSignature[1];
        TypeArgument[] ta = new TypeArgument[0];
        char c = current();
        switch (c) {
        case '+': {
            advance();
            ub[0] = parseFieldTypeSignature();
            lb[0] = BottomSignature.make(); 
            return Wildcard.make(ub, lb);
        }
        case '*':{
            advance();
            ub[0] = SimpleClassTypeSignature.make("java.lang.Object", false, ta);
            lb[0] = BottomSignature.make(); 
            return Wildcard.make(ub, lb);
        }
        case '-': {
            advance();
            lb[0] = parseFieldTypeSignature();
            ub[0] = SimpleClassTypeSignature.make("java.lang.Object", false, ta);
            return Wildcard.make(ub, lb);
        }
        default: return parseFieldTypeSignature();
        }
    }
    private TypeVariableSignature parseTypeVariableSignature(){
        assert(current() == 'T');
        if (current() != 'T') { throw error("expected a type variable usage");}
        advance();
        TypeVariableSignature ts =
            TypeVariableSignature.make(parseIdentifier());
        if (current() != ';') {
            throw error("; expected in signature of type variable named" +
                  ts.getIdentifier());
        }
        advance();
        return ts;
    }
    private ArrayTypeSignature parseArrayTypeSignature() {
        if (current() != '[') {throw error("expected array type signature");}
        advance();
        return ArrayTypeSignature.make(parseTypeSignature());
    }
    private TypeSignature parseTypeSignature() {
        switch (current()) {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':return parseBaseType();
        default: return parseFieldTypeSignature();
        }
    }
    private BaseType parseBaseType() {
        switch(current()) {
        case 'B':
            advance();
            return ByteSignature.make();
        case 'C':
            advance();
            return CharSignature.make();
        case 'D':
            advance();
            return DoubleSignature.make();
        case 'F':
            advance();
            return FloatSignature.make();
        case 'I':
            advance();
            return IntSignature.make();
        case 'J':
            advance();
            return LongSignature.make();
        case 'S':
            advance();
            return ShortSignature.make();
        case 'Z':
            advance();
            return BooleanSignature.make();
        default: {
            assert(false);
            throw error("expected primitive type");
        }
    }
    }
    private FieldTypeSignature[] parseZeroOrMoreBounds() {
        Collection<FieldTypeSignature> fts =
            new ArrayList<FieldTypeSignature>(3);
        if (current() == ':') {
            advance();
            switch(current()) {
            case ':': 
                break;
            default: 
                fts.add(parseFieldTypeSignature());
            }
            while (current() == ':') {
                advance();
                fts.add(parseFieldTypeSignature());
            }
        }
        FieldTypeSignature[] fta = new FieldTypeSignature[fts.size()];
        return fts.toArray(fta);
    }
    private ClassTypeSignature[] parseSuperInterfaces() {
        Collection<ClassTypeSignature> cts =
            new ArrayList<ClassTypeSignature>(5);
        while(current() == 'L') {
            cts.add(parseClassTypeSignature());
        }
        ClassTypeSignature[] cta = new ClassTypeSignature[cts.size()];
        return cts.toArray(cta);
    }
    private MethodTypeSignature parseMethodTypeSignature() {
        FieldTypeSignature[] ets;
        assert(index == 0);
        return MethodTypeSignature.make(parseZeroOrMoreFormalTypeParameters(),
                                        parseFormalParameters(),
                                        parseReturnType(),
                                        parseZeroOrMoreThrowsSignatures());
    }
    private TypeSignature[] parseFormalParameters() {
        if (current() != '(') {throw error("expected (");}
        advance();
        TypeSignature[] pts = parseZeroOrMoreTypeSignatures();
        if (current() != ')') {throw error("expected )");}
        advance();
        return pts;
    }
    private TypeSignature[] parseZeroOrMoreTypeSignatures() {
        Collection<TypeSignature> ts = new ArrayList<TypeSignature>();
        boolean stop = false;
        while (!stop) {
            switch(current()) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 'L':
            case 'T':
            case '[': {
                    ts.add(parseTypeSignature());
                    break;
                }
            default: stop = true;
            }
        }
        TypeSignature[] ta = new TypeSignature[ts.size()];
        return ts.toArray(ta);
    }
    private ReturnType parseReturnType(){
        if  (current() == 'V') {
            advance();
            return VoidDescriptor.make();
        } else return parseTypeSignature();
    }
    private FieldTypeSignature[] parseZeroOrMoreThrowsSignatures(){
        Collection<FieldTypeSignature> ets =
            new ArrayList<FieldTypeSignature>(3);
        while( current() == '^') {
            ets.add(parseThrowsSignature());
        }
        FieldTypeSignature[] eta = new FieldTypeSignature[ets.size()];
        return ets.toArray(eta);
    }
    private FieldTypeSignature parseThrowsSignature() {
        assert(current() == '^');
        if (current() != '^') { throw error("expected throws signature");}
        advance();
        return parseFieldTypeSignature();
    }
 }
