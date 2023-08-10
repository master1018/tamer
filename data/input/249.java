public class ChainedProcessor<O extends Serializable, R extends Serializable, N extends Serializable> extends AbstractBaseProcessor<O, R, N> {
    private static final Logger log = LoggerFactory.getLogger(ChainedProcessor.class);
    private List<IProcessor> processorChain;
    @Override
    public List<IElement<R>> process(IElement<O> e) {
        List<IElement<R>> result = null;
        if (this.processorChain != null) {
            int step = 1;
            List partialResult = new ArrayList<IElement>();
            partialResult.add(e);
            for (IProcessor p : processorChain) {
                partialResult = p.process(partialResult);
                log.debug("Obtained {} results in step {}", (partialResult != null) ? partialResult.size() : "null", step);
                step++;
            }
            result = partialResult;
        }
        return result;
    }
    public List<IProcessor> getProcessorChain() {
        return processorChain;
    }
    public void setProcessorChain(List<IProcessor> processorChain) {
        this.processorChain = processorChain;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Chain:").append((processorChain != null) ? processorChain.size() : "null");
        return sb.toString();
    }
}
