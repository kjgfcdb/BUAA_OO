public class TaxiSys {
    static int SIZE = 80;//地图大小
    static int SIZE2 = SIZE*SIZE;//地图大小的平方，用于在dijkstra等方法中作为常数
    static int TAXINUM = 100;//出租车总数
    public static void main(String args[]) {
        String filename = "map80_2.txt";
        Map map = new Map(filename);//公用地图
        TaxiGUI gui=new TaxiGUI();
        gui.LoadMap(map.numMap, SIZE);
        Taxi[] taxis = new Taxi[TAXINUM];//出租车数组
        RequestQueue requestQueue = new RequestQueue();//用户请求队列
        for (int i=0;i<TAXINUM;i++)
            taxis[i] = new Taxi(i,map.getEdges(),gui,taxis);
        Scheduler scheduler = new Scheduler(requestQueue,taxis);
        scheduler.start();
        InputRequest inputRequest = new InputRequest(requestQueue,gui);
        //读入请求
        inputRequest.parseInput();
    }
}
