import org.junit.Test;
import org.junit.Before; 
import org.junit.After; 

public class SchedulerTest {
    private Scheduler scheduler;

    @Before
    public void before() throws Exception {
        scheduler = new Scheduler();
        System.out.println("#####Test Begins#####");
    }

    @After
    public void after() throws Exception {
        scheduler = null;
        System.out.println("#####Test Ends#####");
    }

    /**
     * Method:repOK()
     */
    @Test
    public void testRepOK() throws Exception {
        assert scheduler.repOK();
        scheduler.que = null;
        assert !scheduler.repOK();
        scheduler.elv = null;
        assert !scheduler.repOK();
        scheduler.que = new ReqQue();
        assert !scheduler.repOK();
        
        scheduler.elv = new ALSElevator();
        scheduler.upDown = 1;
        assert scheduler.repOK();
        scheduler.upDown = 0;
        assert scheduler.repOK();
        scheduler.upDown = -1;
        assert scheduler.repOK();
        scheduler.upDown = 2;
        assert !scheduler.repOK();
        scheduler.upDown = 1;
        scheduler.first = 2;
        assert !scheduler.repOK();
        scheduler.first = 1;
        assert scheduler.repOK();
        scheduler.first = 0;
        assert scheduler.repOK();
        scheduler.preTime = -1;
        assert !scheduler.repOK();
    }

    /**
     * Method: checkFloor(String type, String floor, String Direction, String time)
     */
    @Test
    public void testCheckFloor() throws Exception {
        assert scheduler.checkFloor("FFR","10","UP","0")==0;
        assert scheduler.checkFloor("FR","11","UP","0")==0;
        assert scheduler.checkFloor("FR","0","UP","0")==0;
        assert scheduler.checkFloor("FR","1","UUP","0")==0;
        assert scheduler.checkFloor("FR","1","UP","-1")==0;
        assert scheduler.checkFloor("FR","1","UP","9223372036854775806")==0;
        assert scheduler.checkFloor("FR","1","UP","1")==0;
        assert scheduler.checkFloor("FR","1","UP","0")==1; 
        scheduler.first = 1;
        assert scheduler.checkFloor("FR","1","DOWN","0")==0;
        scheduler.first = 1;
        assert scheduler.checkFloor("FR","2","DOWN","0")==0;
        scheduler.first = 1;
        assert scheduler.checkFloor("FR","2","UP","0")==0;
        assert scheduler.checkFloor("FR","1","UP","0")==1;
        assert scheduler.checkFloor("FR","1","DOWN","1")==0;
        assert scheduler.checkFloor("FR","10","UP","1")==0;
        assert scheduler.checkFloor("FR","10","DOWN","1")==1;
        assert scheduler.checkFloor("FR","3","DOWN","3")==1;
        assert scheduler.checkFloor("FR","3","UP","2")==0;
        assert scheduler.checkFloor("FR","3","UP","3.5")==0;
    }

    /**
     * Method: checkElevator(String type, String floor, String time)
     */
    @Test
    public void testCheckElevator() throws Exception {
        assert scheduler.checkElevator("EER","10","0")==0;
        assert scheduler.checkElevator("ER","11","0")==0;
        assert scheduler.checkElevator("ER","0","0")==0;
        assert scheduler.checkElevator("ER","0","-1")==0;
        assert scheduler.checkElevator("ER","0","9223372036854775806")==0;
        assert scheduler.checkElevator("ER","1","0")==0;
        assert scheduler.checkElevator("ER","1","9223372036854775806")==0;
        assert scheduler.checkElevator("ER","1","-1")==0;
        assert scheduler.checkFloor("FR","1","UP","0")==1;
        assert scheduler.checkElevator("ER","1","0")==1;
        assert scheduler.checkElevator("ER","1","3")==1;
        assert scheduler.checkElevator("ER","1","2")==0;
        assert scheduler.checkElevator("ER","1","2.5")==0;

    }
}
