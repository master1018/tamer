class SingleComment implements Comparable {
    public String id_ = null;
    public String text_ = null;
    public boolean isUsed_ = true;
    public SingleComment(String id, String text) {
        id_ = id.replaceAll("<", "&lt;").replaceAll(">", "&gt;");;
        text_ = text;
    }
    public int compareTo(Object o) {
        return id_.compareTo(((SingleComment)o).id_);
    }
}
