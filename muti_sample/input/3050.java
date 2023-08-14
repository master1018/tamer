class RedefineSubTarg {
    String foo(String prev) {
        StringBuffer buf = new StringBuffer(prev);
        buf.append("Different ");
        return buf.toString();
    }
}
