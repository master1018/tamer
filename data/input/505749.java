public class AndroidSourceViewerConfig extends StructuredTextViewerConfigurationXML {
    private IContentAssistProcessor mProcessor;
    public AndroidSourceViewerConfig(IContentAssistProcessor processor) {
        super();
        mProcessor = processor;
    }
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        return super.getContentAssistant(sourceViewer);
    }
    @Override
    protected IContentAssistProcessor[] getContentAssistProcessors(
            ISourceViewer sourceViewer, String partitionType) {
        ArrayList<IContentAssistProcessor> processors = new ArrayList<IContentAssistProcessor>();
        if (partitionType == IStructuredPartitions.UNKNOWN_PARTITION ||
            partitionType == IStructuredPartitions.DEFAULT_PARTITION ||
            partitionType == IXMLPartitions.XML_DEFAULT) {
            if (sourceViewer instanceof IInputProvider) {
                IInputProvider input = (IInputProvider) sourceViewer;
                Object a = input.getInput();
                if (a != null)
                    a.toString();
            }
            IDocument doc = sourceViewer.getDocument();
            if (doc != null)
                doc.toString();
            processors.add(mProcessor);
        }
        IContentAssistProcessor[] others = super.getContentAssistProcessors(sourceViewer,
                partitionType);
        if (others != null && others.length > 0) {
            for (IContentAssistProcessor p : others) {
                processors.add(p);
            }
        }
        if (processors.size() > 0) {
            return processors.toArray(new IContentAssistProcessor[processors.size()]);
        } else {
            return null;
        }
    }
    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return super.getTextHover(sourceViewer, contentType);
    }
    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(
            ISourceViewer sourceViewer, String contentType) {
        return super.getAutoEditStrategies(sourceViewer, contentType);
    }
    @Override
    public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
        return super.getContentFormatter(sourceViewer);
    }
}
