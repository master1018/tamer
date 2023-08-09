public class AgentCheckTest {
    public static void main(String[] args){
        String [][] testStrings = {
            {"agent.err.error", "", ""},
            {"jmxremote.ConnectorBootstrap.initialize", "", ""},
            {"jmxremote.ConnectorBootstrap.initialize.noAuthentication", "", ""},
            {"jmxremote.ConnectorBootstrap.initialize.ready", "Phony JMXServiceURL", ""},
            {"jmxremote.ConnectorBootstrap.initialize.password.readonly", "Phony passwordFileName", ""},
            {"jmxremote.AdaptorBootstrap.getTargetList.processing", "", ""},
            {"jmxremote.AdaptorBootstrap.getTargetList.adding", "Phony target", ""},
            {"jmxremote.AdaptorBootstrap.getTargetList.starting", "", ""},
            {"jmxremote.AdaptorBootstrap.getTargetList.initialize1", "", ""},
            {"jmxremote.AdaptorBootstrap.getTargetList.initialize2", "Phony hostname", "Phony port"},
            {"jmxremote.AdaptorBootstrap.getTargetList.terminate", "Phony exception", ""},
        };
        boolean pass = true;
        System.out.println("Start...");
        for (int ii = 0; ii < testStrings.length; ii++) {
            String key = testStrings[ii][0];
            String p1 = testStrings[ii][1];
            String p2 = testStrings[ii][2];
            String ss = Agent.getText(key, p1, p2);
            if (ss.startsWith("missing resource key")) {
                pass = false;
                System.out.println("    lookup failed for key = " + key);
            }
        }
        if (!pass) {
            throw new Error ("Resource lookup(s) failed; Test failed");
        }
        System.out.println("...Finished.");
    }
}
