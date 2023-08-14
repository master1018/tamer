public class Deadlock2 {
    public static void main(String[] args) throws Exception {
        File file = new File("object.tmp");
        final byte[] bytes = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        int read = fileInputStream.read(bytes);
        if (read != file.length()) {
            throw new Exception("Didn't read all");
        }
        Thread.sleep(1000);
        Runnable xmlRunnable = new Runnable() {
                public void run() {
                    try {
                        DocumentBuilderFactory.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        Runnable readObjectRunnable = new Runnable() {
                public void run() {
                    try {
                        ObjectInputStream objectInputStream =
                            new ObjectInputStream(new ByteArrayInputStream(bytes));
                        Object o = objectInputStream.readObject();
                        System.out.println(o.getClass());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        Thread thread1 = new Thread(readObjectRunnable, "Read Object");
        Thread thread2 = new Thread(xmlRunnable, "XML");
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }
}
