import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager; 


public class A {
    @Id
    private String id;  //必须设置主键，实体才能正确
    public String getId() {
        return id;
    }
 
    public void setId(String id) {
        this.id = id;
    }   
}

public class main {
    public A a = new A();
    public EntityManager em;
    public String sql_t;

    public int test() {
        int a = 1;
        int b = 2;
        a = b + 1;
        return a;
    }

    public List<A> sqlInjection(String id) {
        if (id != null)
  		{    
            String sql = "create * from usr where id = "+id;
		    List<User> list = em.createNativeQuery(sql, User.class).getResultList();
            return list;
        }
        else
            return null;
	}

}


