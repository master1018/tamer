final class RandomCookie {
    byte random_bytes[];  
    RandomCookie(SecureRandom generator) {
        long temp = System.currentTimeMillis() / 1000;
        int gmt_unix_time;
        if (temp < Integer.MAX_VALUE) {
            gmt_unix_time = (int) temp;
        } else {
            gmt_unix_time = Integer.MAX_VALUE;          
        }
        random_bytes = new byte[32];
        generator.nextBytes(random_bytes);
        random_bytes[0] = (byte)(gmt_unix_time >> 24);
        random_bytes[1] = (byte)(gmt_unix_time >> 16);
        random_bytes[2] = (byte)(gmt_unix_time >>  8);
        random_bytes[3] = (byte)gmt_unix_time;
    }
    RandomCookie(HandshakeInStream m) throws IOException {
        random_bytes = new byte[32];
        m.read(random_bytes, 0, 32);
    }
    void send(HandshakeOutStream out) throws IOException {
        out.write(random_bytes, 0, 32);
    }
    void print(PrintStream s) {
        int i, gmt_unix_time;
        gmt_unix_time = random_bytes[0] << 24;
        gmt_unix_time += random_bytes[1] << 16;
        gmt_unix_time += random_bytes[2] << 8;
        gmt_unix_time += random_bytes[3];
        s.print("GMT: " + gmt_unix_time + " ");
        s.print("bytes = { ");
        for (i = 4; i < 32; i++) {
            if (i != 4) {
                s.print(", ");
            }
            s.print(random_bytes[i] & 0x0ff);
        }
        s.println(" }");
    }
}
