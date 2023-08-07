class TransferCall {
    private TransferSocket transferSocket;
    private TransferInvoker invoker;
    private TransferArgument argument;
    public TransferCall(TransferInvoker invoker, TransferArgument argument, TransferSocket transferSocket) {
        this.transferSocket = transferSocket;
        this.invoker = invoker;
        this.argument = argument;
    }
    public void process() throws TransferCallException {
        try {
            this.invoker.invoke(this.argument, this.transferSocket);
        } catch (Exception e) {
            Throwable throwable = e;
            if (e instanceof InvocationTargetException) {
                throwable = ((InvocationTargetException) e).getTargetException();
            }
            throw new TransferCallException(throwable);
        } finally {
            try {
                this.transferSocket.close();
            } catch (IOException e) {
                throw new TransferCallException(e);
            }
        }
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("argument: ");
        sb.append(argument.toString());
        sb.append(", ");
        sb.append("transfer code: ");
        sb.append(this.invoker.getCode());
        sb.append("}");
        return sb.toString();
    }
}
