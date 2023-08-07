public class PersonName extends StringElement {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonName.class);
    public PersonName(DicomTag aTag) {
        super(aTag);
    }
    public PersonName(DicomTag t, SpecificCharacterSet specificCharacterSet) {
        super(t, specificCharacterSet);
    }
    @Override
    public ValueRepresentation getValueRepresentation() {
        return ValueRepresentation.PersonName;
    }
    public byte[] getVR() {
        return ValueRepresentation.PN;
    }
    @Override
    public void setValue(String aValue) {
        LOGGER.debug("setValue: " + aValue);
        super.setValue(aValue);
    }
}
