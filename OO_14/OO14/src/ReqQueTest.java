import org.junit.*;

public class ReqQueTest {
    private ReqQue reqQue;
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
        reqQue = new ReqQue();
        ++i;
        System.out.println("-----test #"+i+" begins-----");
    }

    @After
    public void after() throws Exception {
        reqQue = null;
        System.out.println("-----test #"+i+" ends-----");
    }

    /**
     * Method:repOK()
     */
    @Test
    public void testRepOK() throws Exception {
        assert reqQue.repOK();
        reqQue.que = null;
        assert !reqQue.repOK();
    }

    /**
     *
     * Method: addReq(Request r)
     *
     */
    @Test
    public void testAddReq() throws Exception {
        assert reqQue.size()==0;
        reqQue.addReq(new Request("FR",1,1,0));
        assert reqQue.size()==1;
        reqQue.addReq(new Request("ER",2,1));
        assert reqQue.size()==2;
    }

    /**
     *
     * Method: size()
     *
     */
    @Test
    public void testSize() throws Exception {
        assert reqQue.size()==0;
        reqQue.addReq(new Request("FR",1,1,0));
        assert reqQue.size()==1;
        reqQue.addReq(new Request("FR",1,1,0));
        assert reqQue.size()==2;
    }

    /**
     *
     * Method: get(int i)
     *
     */
    @Test
    public void testGet() throws Exception {
        assert reqQue.size()==0;
        reqQue.addReq(new Request("FR",1,1,0));
        assert reqQue.get(0).getType().equals("FR");
        reqQue.addReq(new Request("ER",2,1));
        assert reqQue.get(1).getType().equals("ER");
        assert reqQue.get(2)==null;
        assert reqQue.get(-1)==null;
    }
}
