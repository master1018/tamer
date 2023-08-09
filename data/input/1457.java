public class MultipleDocumentHandling extends EnumSyntax
    implements PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = 8098326460746413466L;
    public static final MultipleDocumentHandling
        SINGLE_DOCUMENT = new MultipleDocumentHandling (0);
    public static final MultipleDocumentHandling
       SEPARATE_DOCUMENTS_UNCOLLATED_COPIES = new MultipleDocumentHandling (1);
    public static final MultipleDocumentHandling
        SEPARATE_DOCUMENTS_COLLATED_COPIES = new MultipleDocumentHandling (2);
    public static final MultipleDocumentHandling
        SINGLE_DOCUMENT_NEW_SHEET = new MultipleDocumentHandling (3);
    protected MultipleDocumentHandling(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "single-document",
        "separate-documents-uncollated-copies",
        "separate-documents-collated-copies",
        "single-document-new-sheet"
    };
    private static final MultipleDocumentHandling[] myEnumValueTable = {
        SINGLE_DOCUMENT,
        SEPARATE_DOCUMENTS_UNCOLLATED_COPIES,
        SEPARATE_DOCUMENTS_COLLATED_COPIES,
        SINGLE_DOCUMENT_NEW_SHEET
    };
    protected String[] getStringTable() {
        return (String[])myStringTable.clone();
    }
    protected EnumSyntax[] getEnumValueTable() {
        return (EnumSyntax[])myEnumValueTable.clone();
    }
    public final Class<? extends Attribute> getCategory() {
        return MultipleDocumentHandling.class;
    }
    public final String getName() {
        return "multiple-document-handling";
    }
}
