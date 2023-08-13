class ClassDiff {
    public String name_;
    public boolean isInterface_;
    public String inheritanceChange_ = null;
    public String documentationChange_ = null;
    public String modifiersChange_ = null;
    public List ctorsAdded = null;
    public List ctorsRemoved = null;
    public List ctorsChanged = null;
    public List methodsAdded = null;
    public List methodsRemoved = null;
    public List methodsChanged = null;
    public List fieldsAdded = null;
    public List fieldsRemoved = null;
    public List fieldsChanged = null;
    public double pdiff = 0.0;
    public ClassDiff(String name) {
        name_ = name;
        isInterface_ = false;
        ctorsAdded = new ArrayList(); 
        ctorsRemoved = new ArrayList(); 
        ctorsChanged = new ArrayList(); 
        methodsAdded = new ArrayList(); 
        methodsRemoved = new ArrayList(); 
        methodsChanged = new ArrayList(); 
        fieldsAdded = new ArrayList(); 
        fieldsRemoved = new ArrayList(); 
        fieldsChanged = new ArrayList(); 
    }   
    public static String diff(ClassAPI oldClass, ClassAPI newClass) {
        Collections.sort(oldClass.implements_);
        Collections.sort(newClass.implements_);
        String res = "";
        boolean hasContent = false;
        if (oldClass.extends_ != null && newClass.extends_ != null &&
            oldClass.extends_.compareTo(newClass.extends_) != 0) {
            res += "The superclass changed from <code>" + oldClass.extends_ + "</code> to <code>" + newClass.extends_ + "</code>.<br>";
            hasContent = true;
        }
        String removedInterfaces = "";
        int numRemoved = 0;
        Iterator iter = oldClass.implements_.iterator();
        while (iter.hasNext()) {
            String oldInterface = (String)(iter.next());
            int idx = Collections.binarySearch(newClass.implements_, oldInterface);
            if (idx < 0) {
                if (numRemoved != 0)
                    removedInterfaces += ", ";
                removedInterfaces += oldInterface;
                numRemoved++;
            }
        }
        String addedInterfaces = "";
        int numAdded = 0;
        iter = newClass.implements_.iterator();
        while (iter.hasNext()) {
            String newInterface = (String)(iter.next());
            int idx = Collections.binarySearch(oldClass.implements_, newInterface);
            if (idx < 0) {
                if (numAdded != 0)
                    addedInterfaces += ", ";
                addedInterfaces += newInterface;
                numAdded++;
            }
        }
        if (numRemoved != 0) {
            if (hasContent)
                res += " ";
            if (numRemoved == 1)
                res += "Removed interface <code>" + removedInterfaces + "</code>.<br>";
            else
                res += "Removed interfaces <code>" + removedInterfaces + "</code>.<br>";
            hasContent = true;
        }
        if (numAdded != 0) {
            if (hasContent)
                res += " ";
            if (numAdded == 1)
                res += "Added interface <code>" + addedInterfaces + "</code>.<br>";
            else
                res += "Added interfaces <code>" + addedInterfaces + "</code>.<br>";
            hasContent = true;
        }
        if (res.compareTo("") == 0)
            return null;
        return res;
    }
    public void addModifiersChange(String commonModifierChanges) {
        if (commonModifierChanges != null) {
            if (modifiersChange_ == null)
                modifiersChange_ = commonModifierChanges;
            else
                modifiersChange_ += " " + commonModifierChanges;
        }
    }
}
