public abstract class ASN1Choice extends ASN1Type {
    public final ASN1Type[] type;
    private final int[][] identifiers;
    public ASN1Choice(ASN1Type[] type) {
        super(TAG_CHOICE); 
        if (type.length == 0) {
            throw new IllegalArgumentException(Messages.getString("security.10E", 
                    getClass().getName()));
        }
        TreeMap map = new TreeMap();
        for (int index = 0; index < type.length; index++) {
            ASN1Type t = type[index];
            if (t instanceof ASN1Any) {
                throw new IllegalArgumentException(Messages.getString("security.10F", 
                        getClass().getName())); 
            } else if (t instanceof ASN1Choice) {
                int[][] choiceToAdd = ((ASN1Choice) t).identifiers;
                for (int j = 0; j < choiceToAdd[0].length; j++) {
                    addIdentifier(map, choiceToAdd[0][j], index);
                }
                continue;
            }
            if (t.checkTag(t.id)) {
                addIdentifier(map, t.id, index);
            }
            if (t.checkTag(t.constrId)) {
                addIdentifier(map, t.constrId, index);
            }
        }
        int size = map.size();
        identifiers = new int[2][size];
        Iterator it = map.entrySet().iterator();
        for (int i = 0; i < size; i++) {
        	Map.Entry entry = (Map.Entry) it.next();
            BigInteger identifier = (BigInteger) entry.getKey();
            identifiers[0][i] = identifier.intValue();
            identifiers[1][i] = ((BigInteger) entry.getValue()).intValue();
        }
        this.type = type;
    }
    private void addIdentifier(TreeMap map, int identifier, int index){
        if (map.put(BigInteger.valueOf(identifier), BigInteger.valueOf(index)) != null) {
            throw new IllegalArgumentException(Messages.getString("security.10F", 
                    getClass().getName())); 
        }
    }
    public final boolean checkTag(int identifier) {
        return Arrays.binarySearch(identifiers[0], identifier) >= 0;
    }
    public Object decode(BerInputStream in) throws IOException {
        int index = Arrays.binarySearch(identifiers[0], in.tag);
        if (index < 0) {
            throw new ASN1Exception(Messages.getString("security.110", 
                    getClass().getName()));
        }
        index = identifiers[1][index];
        in.content = type[index].decode(in);
        in.choiceIndex = index;
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public void encodeASN(BerOutputStream out) {
        encodeContent(out);
    }
    public final void encodeContent(BerOutputStream out) {
        out.encodeChoice(this);
    }
    public abstract int getIndex(Object object);
    public abstract Object getObjectToEncode(Object object);
    public final void setEncodingContent(BerOutputStream out) {
        out.getChoiceLength(this);
    }
}
