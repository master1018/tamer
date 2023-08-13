public abstract class PermissionCollection implements Serializable {
    private static final long serialVersionUID = -6727011328946861783L;
    private boolean readOnly; 
    public abstract void add(Permission permission);
    public abstract Enumeration<Permission> elements();
    public abstract boolean implies(Permission permission);
    public boolean isReadOnly() {
        return readOnly;
    }
    public void setReadOnly() {
        readOnly = true;
    }
    @Override
    public String toString() {
        List<String> elist = new ArrayList<String>(100);
        Enumeration<Permission> elenum = elements();
        String superStr = super.toString();
        int totalLength = superStr.length() + 5;
        if (elenum != null) {
            while (elenum.hasMoreElements()) {
                String el = elenum.nextElement().toString();
                totalLength += el.length();
                elist.add(el);
            }
        }
        int esize = elist.size();
        totalLength += esize * 4;
        StringBuilder result = new StringBuilder(totalLength).append(superStr)
            .append(" ("); 
        for (int i = 0; i < esize; i++) {
            result.append("\n ").append(elist.get(i).toString()); 
        }
        return result.append("\n)\n").toString(); 
    }
}
