public class SimpleComputer implements IJavaCompletionProposalComputer {
    public List<ICompletionProposal> computeCompletionProposals(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        List<ICompletionProposal> ret = new Vector<ICompletionProposal>();
        try {
            int offs = context.getInvocationOffset();
            String buffer = context.getDocument().get(0, offs);
            String keyWord = ":";
            String keyWordInfo = "':': noser: autofills the annotation";
            int idx = 0;
            int klen = keyWord.length();
            for (int i = 0; i < klen; i++) {
                String test = keyWord.substring(0, klen - i);
                if (buffer.endsWith(test)) {
                    idx = klen - i;
                    break;
                }
            }
            if (idx != 0) {
                System.out.println("idx:"+idx);
                String replace ="hi there! a longer sample text\nnew line";
                    ICompletionProposal ci = new MyCompletion(buffer, replace,
                            context.getInvocationOffset() - idx, idx, replace
                                    .length(), null, keyWordInfo, null, null);
                    ret.add(ci);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public List<ICompletionProposal> computeContextInformation(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return new Vector<ICompletionProposal>();
    }
    public String getErrorMessage() {
        return "Error from SimpleComputer";
    }
    public void sessionEnded() {
    }
    public void sessionStarted() {
    }
}
