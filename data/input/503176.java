class StreamReader extends Thread {
    InputStream is_;
    StreamReader(InputStream is) {
        is_ = is;
    }
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is_);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while((line = br.readLine()) != null)
                System.out.println(line);    
        } catch (IOException ioe) {
            System.out.println("IO Error invoking Javadoc");
            ioe.printStackTrace();  
        } catch (Exception e) {
        }
    }
}
