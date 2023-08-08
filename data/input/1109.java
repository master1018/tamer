class Dispatcher implements Runnable {
    private final Container container;
    private final Response response;
    private final Request request;
    private final Monitor monitor;
    private final Entity entity;
    public Dispatcher(Container container, Initiator reactor, Entity entity) {
        this.monitor = new FlushMonitor(reactor, entity);
        this.request = new RequestEntity(entity, monitor);
        this.response = new ResponseEntity(request, entity, monitor);
        this.container = container;
        this.entity = entity;
    }
    public void run() {
        try {
            dispatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void dispatch() throws Exception {
        Channel channel = entity.getChannel();
        try {
            container.handle(request, response);
        } catch (Throwable e) {
            channel.close();
        }
    }
}
