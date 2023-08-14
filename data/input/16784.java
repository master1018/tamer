class TryData {
    Vector catches = new Vector();
    Label endLabel = new Label();
    public CatchData add(Object type) {
        CatchData cd = new CatchData(type);
        catches.addElement(cd);
        return cd;
    }
    public CatchData getCatch(int n) {
        return (CatchData)catches.elementAt(n);
    }
    public Label getEndLabel() {
        return endLabel;
    }
}
