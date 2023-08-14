public class BadIPv6Addresses {
    public static void main(String[] args) throws Exception {
        String[] badAddresses = new String[] {
            "0:1:2:3:4:5:6:7:8",        
            "0:1:2:3:4:5:6",            
            "0:1:2:3:4:5:6:x",          
            "0:1:2:3:4:5:6::7",         
            "0:1:2:3:4:5:6:789abcdef",  
            "0:1:2:3::x",               
            "0:1:2:::3",                
            "0:1:2:3::abcde",           
            "0:1",                      
            "0:0:0:0:0:x:10.0.0.1",     
            "0:0:0:0:0:0:10.0.0.x",     
            "0:0:0:0:0::0:10.0.0.1",    
            "0:0:0:0:0:fffff:10.0.0.1", 
            "0:0:0:0:0:0:0:10.0.0.1",   
            "0:0:0:0:0:10.0.0.1",       
            "0:0:0:0:0:0:10.0.0.0.1",   
            "0:0:0:0:0:0:10.0.1",       
            "0:0:0:0:0:0:10..0.0.1",    
            "::fffx:192.168.0.1",       
            "::ffff:192.168.0.x",       
            ":::ffff:192.168.0.1",      
            "::fffff:192.168.0.1",      
            "::ffff:1923.168.0.1",      
            ":ffff:192.168.0.1",        
            "::ffff:192.168.0.1.2",     
            "::ffff:192.168.0",         
            "::ffff:192.168..0.1"       
        };
        List<String> failedAddrs = new ArrayList<String>();
        for (String addrStr : badAddresses) {
            try {
                InetAddress addr = InetAddress.getByName(addrStr);
                failedAddrs.add(addrStr);
            } catch (UnknownHostException e) {
            }
        }
        if (failedAddrs.size() > 0) {
            System.out.println("We should reject following ipv6 addresses, but we didn't:");
            for (String addr : failedAddrs) {
                System.out.println("\t" + addr);
            }
            throw new RuntimeException("Test failed.");
        }
    }
}
