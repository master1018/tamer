public class RFC822Name implements GeneralNameInterface
{
    private String name;
    public RFC822Name(DerValue derValue) throws IOException {
        name = derValue.getIA5String();
        parseName(name);
    }
    public RFC822Name(String name) throws IOException {
        parseName(name);
        this.name = name;
    }
    public void parseName(String name) throws IOException {
        if (name == null || name.length() == 0) {
            throw new IOException("RFC822Name may not be null or empty");
        }
        String domain = name.substring(name.indexOf('@')+1);
        if (domain.length() == 0) {
            throw new IOException("RFC822Name may not end with @");
        } else {
            if (domain.startsWith(".")) {
                if (domain.length() == 1)
                    throw new IOException("RFC822Name domain may not be just .");
            }
        }
    }
    public int getType() {
        return (GeneralNameInterface.NAME_RFC822);
    }
    public String getName() {
        return name;
    }
    public void encode(DerOutputStream out) throws IOException {
        out.putIA5String(name);
    }
    public String toString() {
        return ("RFC822Name: " + name);
    }
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof RFC822Name))
            return false;
        RFC822Name other = (RFC822Name)obj;
        return name.equalsIgnoreCase(other.name);
    }
    public int hashCode() {
        return name.toUpperCase().hashCode();
    }
    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        int constraintType;
        if (inputName == null)
            constraintType = NAME_DIFF_TYPE;
        else if (inputName.getType() != (GeneralNameInterface.NAME_RFC822)) {
            constraintType = NAME_DIFF_TYPE;
        } else {
            String inName =
                (((RFC822Name)inputName).getName()).toLowerCase(Locale.ENGLISH);
            String thisName = name.toLowerCase(Locale.ENGLISH);
            if (inName.equals(thisName)) {
                constraintType = NAME_MATCH;
            } else if (thisName.endsWith(inName)) {
                if (inName.indexOf('@') != -1) {
                    constraintType = NAME_SAME_TYPE;
                } else if (inName.startsWith(".")) {
                    constraintType = NAME_WIDENS;
                } else {
                    int inNdx = thisName.lastIndexOf(inName);
                    if (thisName.charAt(inNdx-1) == '@' ) {
                        constraintType = NAME_WIDENS;
                    } else {
                        constraintType = NAME_SAME_TYPE;
                    }
                }
            } else if (inName.endsWith(thisName)) {
                if (thisName.indexOf('@') != -1) {
                    constraintType = NAME_SAME_TYPE;
                } else if (thisName.startsWith(".")) {
                    constraintType = NAME_NARROWS;
                } else {
                    int ndx = inName.lastIndexOf(thisName);
                    if (inName.charAt(ndx-1) == '@') {
                        constraintType = NAME_NARROWS;
                    } else {
                        constraintType = NAME_SAME_TYPE;
                    }
                }
            } else {
                constraintType = NAME_SAME_TYPE;
            }
        }
        return constraintType;
    }
    public int subtreeDepth() throws UnsupportedOperationException {
        String subtree=name;
        int i=1;
        int atNdx = subtree.lastIndexOf('@');
        if (atNdx >= 0) {
            i++;
            subtree=subtree.substring(atNdx+1);
        }
        for (; subtree.lastIndexOf('.') >= 0; i++) {
            subtree=subtree.substring(0,subtree.lastIndexOf('.'));
        }
        return i;
    }
}
