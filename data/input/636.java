public class SelectPath {
    static final String m_namespaceDeclaration = "declare namespace xq='http:
    public boolean updateWorkPhone(XmlObject empDoc) {
        boolean hasResults = false;
        System.out.println("XML as received by updateWorkPhone method: \n\n" + empDoc.toString());
        String pathExpression = "$this/xq:employees/xq:employee/xq:phone[@location='work']";
        XmlObject[] results = empDoc.selectPath(m_namespaceDeclaration + pathExpression);
        if (results.length > 0) {
            hasResults = true;
            PhoneType[] phones = (PhoneType[]) results;
            for (int i = 0; i < phones.length; i++) {
                phones[i].setStringValue("(206)555-1234");
            }
            System.out.println("\nXML as updated by updateWorkPhone method (each work \n" + "phone number has been changed to the same number): \n\n" + empDoc.toString() + "\n");
        }
        return hasResults;
    }
    public boolean collectNames(XmlObject empDoc) {
        boolean hasResults = false;
        XmlCursor pathCursor = empDoc.newCursor();
        pathCursor.toFirstChild();
        pathCursor.selectPath(m_namespaceDeclaration + "$this
        if (pathCursor.getSelectionCount() > 0) {
            hasResults = true;
            XmlObject namesElement = null;
            try {
                namesElement = XmlObject.Factory.parse("<names/>");
            } catch (XmlException e) {
                e.printStackTrace();
            }
            XmlCursor namesCursor = namesElement.newCursor();
            namesCursor.toFirstContentToken();
            namesCursor.toEndToken();
            while (pathCursor.toNextSelection()) {
                namesCursor.insertChars(pathCursor.getTextValue());
                if (pathCursor.hasNextSelection()) {
                    namesCursor.insertChars(", ");
                }
            }
            pathCursor.dispose();
            namesCursor.dispose();
            System.out.println("\nNames collected by collectNames method: \n\n" + namesElement + "\n");
        }
        return hasResults;
    }
}
