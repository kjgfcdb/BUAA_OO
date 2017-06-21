public class TaxiSys {
    /**
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
        Map map = new Map(mapFile);//公用地图
        TrafficLight trafficLight = new TrafficLight(trafficLightFile);
        TaxiGUI gui = new TaxiGUI();
        gui.LoadMap(map.numMap, SIZE);
        /*请求队列*/
        RequestQueue requestQueue = new RequestQueue();//用户请求队列
        /*出租车数组*/
        Taxi[] taxis = new Taxi[TAXINUM];//出租车数组
        for (int i = 0; i < TAXINUM; i++)
            taxis[i] = new Taxi(i, gui, map,trafficLight);
        map.start();
        trafficLight.start();
        TaxiManager taxiManager = new TaxiManager(taxis);
        /*调度器*/
        Scheduler scheduler = new Scheduler(requestQueue,taxis);
        scheduler.start();
        /*读入请求*/
        InputRequest inputRequest = new InputRequest(requestQueue,map,gui);
        inputRequest.parseInput();
    }
}
