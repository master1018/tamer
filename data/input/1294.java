public class StartExampleDebugServer {
    public static void main(String[] args) {
        PoolBringup pool = new PoolBringup();
        pool.portnumber = 8080;
        try {
            pool.startFresh();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
