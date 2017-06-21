package OO15;

interface deIterator<E> {//双向迭代器
    /*
     * @Overview: 双向迭代器
     */

    /**
     * @effect: \result == if the iterator has next element;
     */
    public boolean hasNext();

    /**
     * @modifies: \this;
     * @effects: \result == the next element of the iterator;
     */
    E next();

    /**
     * @effects: \result == if the iterator has previous element;
     */
    public boolean hasPrevious();

    /**
     * @modifies: \this;
     * @effects: \result == the previous element of the iterator;
     */
    E previous();
}
public class TaxiSys {
    /*
     * @Overview:出租车系统模拟整个打车过程.
     */
    static int SIZE = 80;//地图大小
    static int SIZE2 = SIZE * SIZE;//地图大小的平方，用于在SPFA等方法中作为常数
    static int TAXINUM = 100;//出租车总数
    static int RED = 1;
    static int GREEN = -1;
    public boolean repOK() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == (SIZE>0)&&(SIZE2==SIZE*SIZE)&&(TAXINUM>0);
         */
        return (SIZE>0) && (SIZE2==SIZE*SIZE) && (TAXINUM>0);
    }
    public static void main(String args[]) {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      Initialize all classes, start the threads, and begin to process the input.
         */
        String mapFile = "map80_2.txt";
        String trafficLightFile = "trafficLight.txt";
        /*地图与GUI*/
        TaxiGUI gui = new TaxiGUI();
        Map map = new Map(mapFile,gui);//公用地图
        TrafficLight trafficLight = new TrafficLight(trafficLightFile,gui);
        gui.LoadMap(map.numMap, SIZE);
        /*请求队列*/
        RequestQueue requestQueue = new RequestQueue();//用户请求队列
        InputRequest inputRequest = new InputRequest(requestQueue,map,gui);
        /*出租车数组*/
        /*这段可以被测试者实现的init_taxi()代替*/
//        -------------------------------------------
        Taxi[] taxis = new Taxi[TAXINUM];//出租车数组
        for (int i=0;i<70;i++)
            taxis[i] = new Taxi(i,gui,map,trafficLight);
        for (int i=70;i<100;i++)
            taxis[i] = new TrackableTaxi(i,gui,map,trafficLight);
//        -------------------------------------------
//        Taxi[] taxis = init_taxi(gui,map,trafficLight);
//        -------------------------------------------
        for (int i=0;i<TAXINUM;i++)
            taxis[i].start();
        map.start();
        trafficLight.start();
        TaxiManager taxiManager = new TaxiManager(taxis);
        /*调度器*/
        Scheduler scheduler = new Scheduler(requestQueue,taxis);
        scheduler.start();
        /*读入请求*/
        inputRequest.parseInput(taxis);
    }

    public static Taxi[] init_taxi(TaxiGUI gui,Map map,TrafficLight trafficLight) {
        /*
         * @REQUIRES: gui!=null && map!=null && trafficLight!=null;
         * @EFFECTS:返回一个有100个Taxi类对象的数组，其中有70个对象是Taxi对象，30个对象是TrackableTaxi对象，且这些对象
         * 都是经过正确初始化之后的。其中Taxi与TrackableTaxi的参数都是(i,gui,map,trafficLight)，其中i表示该出租车对象
         * 的编号(0<=i<100);
         */
        if (gui==null || map==null || trafficLight==null)
            return null;
        Taxi[] taxis = new Taxi[100];
        /* 示例
        for (int i=0;i<70;i++)
            taxis[i] = new Taxi(i,gui,map,trafficLight);
        for (int i=70;i<100;i++)
            taxis[i] = new TrackableTaxi(i,gui,map,trafficLight);
        */
        return taxis;
    }
}
