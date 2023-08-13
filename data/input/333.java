public class Ser {
    public static void main(String[] args) throws Exception {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(Collections.EMPTY_SET);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            if (!Collections.EMPTY_SET.equals(in.readObject()))
                throw new RuntimeException("empty set Ser/Deser failure.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize empty set:" + e);
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(Collections.EMPTY_LIST);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            if (!Collections.EMPTY_LIST.equals(in.readObject()))
                throw new RuntimeException("empty list Ser/Deser failure.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize empty list:" + e);
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            Set gumby = Collections.singleton("gumby");
            out.writeObject(gumby);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            if (!gumby.equals(in.readObject()))
                throw new RuntimeException("Singleton Ser/Deser failure.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize singleton:" + e);
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            List gumbies = Collections.nCopies(50, "gumby");
            out.writeObject(gumbies);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            if (!gumbies.equals(in.readObject()))
                throw new RuntimeException("nCopies Ser/Deser failure.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize nCopies:" + e);
        }
    }
}
