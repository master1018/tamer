class BroadcastFilter extends IntentFilter {
    final ReceiverList receiverList;
    final String requiredPermission;
    BroadcastFilter(IntentFilter _filter, ReceiverList _receiverList,
            String _requiredPermission) {
        super(_filter);
        receiverList = _receiverList;
        requiredPermission = _requiredPermission;
    }
    public void dump(PrintWriter pw, String prefix) {
        dumpInReceiverList(pw, new PrintWriterPrinter(pw), prefix);
        receiverList.dumpLocal(pw, prefix);
    }
    public void dumpBrief(PrintWriter pw, String prefix) {
        dumpBroadcastFilterState(pw, prefix);
    }
    public void dumpInReceiverList(PrintWriter pw, Printer pr, String prefix) {
        super.dump(pr, prefix);
        dumpBroadcastFilterState(pw, prefix);
    }
    void dumpBroadcastFilterState(PrintWriter pw, String prefix) {
        if (requiredPermission != null) {
            pw.print(prefix); pw.print("requiredPermission="); pw.println(requiredPermission);
        }
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BroadcastFilter{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(receiverList);
        sb.append('}');
        return sb.toString();
    }
}
