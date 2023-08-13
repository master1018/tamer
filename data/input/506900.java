public final class ProcessingInstructionImpl extends LeafNodeImpl implements
        ProcessingInstruction {
    private String target;
    private String data;
    ProcessingInstructionImpl(DocumentImpl document, String target, String data) {
        super(document);
        this.target = target; 
        this.data = data;
    }
    public String getData() {
        return data;
    }
    @Override
    public String getNodeName() {
        return target;
    }
    @Override
    public short getNodeType() {
        return Node.PROCESSING_INSTRUCTION_NODE;
    }
    @Override
    public String getNodeValue() {
        return data;
    }
    public String getTarget() {
        return target;
    }
    public void setData(String data) throws DOMException {
        this.data = data;
    }
}
