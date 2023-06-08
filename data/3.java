import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager; 

public class User {
    @Id
    private String id;  //必须设置主键，实体才能正确
 
    public String getId() {
        return id;
    }
 
    public void setId(String id) {
        this.id = id;
    }
}

public class Expolit_map {
    User user = new User();
    private EntityManager em;
    public double[][] A;
    
    // sql inject expolit
    public List<User> sqlInjection(String id) {
		String sql = "select * from usr where id = "+id;
		List<User> list = em.createNativeQuery(sql, User.class).getResultList();
		return list;
	}    

}


