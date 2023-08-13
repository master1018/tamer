class Modifiers implements Comparable {
    public boolean isStatic = false;
    public boolean isFinal = false;
    public boolean isDeprecated = false;
    public String visibility = null;
    public Modifiers() {
    }
    public int compareTo(Object o) {
        Modifiers oModifiers = (Modifiers)o;
        if (isStatic != oModifiers.isStatic)
            return -1;
        if (isFinal != oModifiers.isFinal)
            return -1;
        if (isDeprecated != oModifiers.isDeprecated)
            return -1;
        if (visibility != null) {
            int comp = visibility.compareTo(oModifiers.visibility);
            if (comp != 0)
                return comp;
        }
        return 0;
    }
    public String diff(Modifiers newModifiers) {
        String res = "";
        boolean hasContent = false;
        if (isStatic != newModifiers.isStatic) {
            res += "Change from ";
            if (isStatic) 
                res += "static to non-static.<br>";
            else
                res += "non-static to static.<br>";
            hasContent = true;
        }
        if (isFinal != newModifiers.isFinal) {
            if (hasContent)
                res += " ";
            res += "Change from ";
            if (isFinal) 
                res += "final to non-final.<br>";
            else
                res += "non-final to final.<br>";
            hasContent = true;
        }
        if (isDeprecated != newModifiers.isDeprecated) {
            if (hasContent)
                res += " ";
            if (isDeprecated)
                res += "Change from deprecated to undeprecated.<br>";
            else
                res += "<b>Now deprecated</b>.<br>";
            hasContent = true;
        }
        if (visibility != null) {
            int comp = visibility.compareTo(newModifiers.visibility);
            if (comp != 0) {
                if (hasContent)
                    res += " ";
                res += "Change of visibility from " + visibility + " to " + 
                    newModifiers.visibility + ".<br>";
                hasContent = true;
            }
        }
        if (res.compareTo("") == 0)
            return null;
        return res;
    }
}
