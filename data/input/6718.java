public class X400Address implements GeneralNameInterface {
    byte[] nameValue = null;
    public X400Address(byte[] value) {
        nameValue = value;
    }
    public X400Address(DerValue derValue) throws IOException {
        nameValue = derValue.toByteArray();
    }
    public int getType() {
        return (GeneralNameInterface.NAME_X400);
    }
    public void encode(DerOutputStream out) throws IOException {
        DerValue derValue = new DerValue(nameValue);
        out.putDerValue(derValue);
    }
    public String toString() {
        return ("X400Address: <DER-encoded value>");
    }
    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        int constraintType;
        if (inputName == null)
            constraintType = NAME_DIFF_TYPE;
        else if (inputName.getType() != NAME_X400)
            constraintType = NAME_DIFF_TYPE;
        else
            throw new UnsupportedOperationException("Narrowing, widening, and match are not supported for X400Address.");
        return constraintType;
    }
    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth not supported for X400Address");
    }
}
