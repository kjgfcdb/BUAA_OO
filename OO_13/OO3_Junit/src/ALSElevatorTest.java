
import org.junit.*;

public class ALSElevatorTest {
    ALSElevator alsElevator;
    private static int i;

    @BeforeClass
    public static void setReqQue() {
        System.out.println("#####Test begins#####");
    }
    @AfterClass
    public static void tearDown() {
        System.out.println("#####Test ends#####");
    }

    @Before
    public void before() throws Exception {
        alsElevator = new ALSElevator();
        ++i;
        System.out.println("-----test #"+i+" begins-----");
    }

    @After
    public void after() throws Exception {
        alsElevator = null;
        System.out.println("-----test #"+i+" ends-----");
    }

    /**
     * Method:repOK()
     */
    @Test
    public void testRepOK() throws Exception {
        assert alsElevator.repOK();
    }

    /**
     * Method: toString()
     */
    @Test
    public void testToString() throws Exception {
        assert alsElevator.toString().equals("null/(1,STILL,.0)");
        Request request = new Request("FR",3,0,0);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        assert alsElevator.toString().equals("[FR,3,DOWN,0]/(3,UP,1.0)");
        request = new Request("FR",1,0,1);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        assert alsElevator.toString().equals("[FR,1,DOWN,1]/(1,DOWN,3.0)");
        request = new Request("FR",1,0,2);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        assert alsElevator.toString().equals("[FR,1,DOWN,2]/(1,STILL,5.0)");
    }

    /**
     * Method: update(Request r)
     */
    @Test
    public void testUpdate() throws Exception {
        Request request = new Request("FR",5,0,0);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        assert alsElevator.toString().equals("[FR,5,DOWN,0]/(5,UP,2.0)");
        request = new Request("ER",7,0);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        double t = alsElevator.getComTime();
        assert alsElevator.toString().equals("[ER,7,0]/(7,UP,4.0)");
        request = new Request("ER",3,1);
        alsElevator.update(request);
        assert alsElevator.getComTime()==t;
    }

    /**
     * Method: getCurPos()
     */
    @Test
    public void testGetCurPos() throws Exception {
        assert alsElevator.getCurPos()==1;
        Request request = new Request("FR",5,0,0);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        assert alsElevator.getCurPos()==5;
        request = new Request("ER",7,0);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        assert alsElevator.getCurPos()==7;
    }

    /**
     * Method: getComTime()
     */
    @Test
    public void testGetComTime() throws Exception {
        assert alsElevator.getComTime()==0;
        Request request = new Request("FR",5,0,0);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        assert alsElevator.getComTime()==3;
    }

    /**
     * Method: getState(Request r)
     */
    @Test
    public void testGetState() throws Exception {
        Request request = new Request("FR",5,0,0);
        request.setAlreadyOut(1);
        assert alsElevator.getState(request)==1;
        alsElevator.update(request);
        request = new Request("FR",1,1,0);
        request.setAlreadyOut(1);
        assert alsElevator.getState(request)==-1;
        alsElevator.update(request);
        request = new Request("FR",1,1,0);
        request.setAlreadyOut(1);
        assert alsElevator.getState(request)==0;
        alsElevator.update(request);
    }

    /**
     * Method: getLastReq()
     */
    @Test
    public void testGetLastReq() throws Exception {
        assert alsElevator.getLastReq()==null;
        Request request = new Request("FR",5,0,0);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        Request temp = alsElevator.getLastReq();
        assert temp.getType().equals("FR") && temp.getFloor()==5 &&
                temp.getTime()==0 && temp.getUpDown()==0;
        request = new Request("ER",1,0);
        request.setAlreadyOut(1);
        alsElevator.update(request);
        temp = alsElevator.getLastReq();
        assert temp.getType().equals("ER") && temp.getFloor()==1 &&
                temp.getTime()==0;
    }

    /**
     * Method: updateComTime(double t)
     */
    @Test
    public void testUpdateComTime() throws Exception {
        assert alsElevator.updateComTime(10);
        assert alsElevator.getComTime()==10;
        assert !alsElevator.updateComTime(1);
        assert alsElevator.getComTime()!=1;
    }
}
