public class CaptureLoader {
    public static boolean saveLayers(IDevice device, Window window, File file) {
        Socket socket = null;
        DataInputStream in = null;
        BufferedWriter out = null;
        boolean result = false;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1",
                    DeviceBridge.getDeviceLocalPort(device)));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out.write("CAPTURE_LAYERS " + window.encode());
            out.newLine();
            out.flush();
            int width = in.readInt();
            int height = in.readInt();
            PsdFile psd = new PsdFile(width, height);
            while (readLayer(in, psd)) {
            }
            psd.write(new FileOutputStream(file));
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
    private static boolean readLayer(DataInputStream in, PsdFile psd) {
        try {
            if (in.read() == 2) {
                System.out.println("Found end of layers list");
                return false;
            }
            String name = in.readUTF();
            System.out.println("name = " + name);
            boolean visible = in.read() == 1;
            int x = in.readInt();
            int y = in.readInt();
            int dataSize = in.readInt();
            byte[] data = new byte[dataSize];
            int read = 0;
            while (read < dataSize) {
                read += in.read(data, read, dataSize - read);
            }
            ByteArrayInputStream arrayIn = new ByteArrayInputStream(data);
            BufferedImage chunk = ImageIO.read(arrayIn);
            BufferedImage image = new BufferedImage(chunk.getWidth(), chunk.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(chunk, null, 0, 0);
            g.dispose();
            psd.addLayer(name, image, new Point(x, y), visible);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static Image loadCapture(IDevice device, Window window, String params) {
        Socket socket = null;
        BufferedInputStream in = null;
        BufferedWriter out = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1",
                    DeviceBridge.getDeviceLocalPort(device)));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedInputStream(socket.getInputStream());
            out.write("CAPTURE " + window.encode() + " " + params);
            out.newLine();
            out.flush();
            return ImageIO.read(in);
        } catch (IOException e) {
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
