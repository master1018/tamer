public class SynthTest {
    public static void main(String[] args) throws Exception {
        SynthLookAndFeel laf = new SynthLookAndFeel();
        InputStream in = SynthTest.class.getResourceAsStream(
                "synthconfig.xml");
        laf.load(in, SynthTest.class);
        UIManager.setLookAndFeel(laf);
        if (!Color.RED.equals(new JButton().getForeground())) {
            throw new RuntimeException("The wrong foreground color!");
        }
    }
}
