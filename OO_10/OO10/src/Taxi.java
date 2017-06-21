import java.awt.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class State {//IDLE表示停止,READY表示等待服务,TOCUS&TODES表示正在服务,TOCUS是去接顾客,TODES是去顾客目的地
    /**
     * @Overview:状态类描述车辆运行的状态
     */
    static int IDLE = 0;
    static int TODES = 1;
    static int READY = 2;
    static int TOCUS = 3;

    public boolean repOK() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == (IDLE==0) && (TODES==1) && (READY==2) && (TOCUS)==3);
         */
        return IDLE==0 &&
                TODES==1 &&
                READY==2 &&
                TOCUS==3;
    }
}

class Taxi implements Runnable {
    /**
     * @Overview:出租车类根据分配任务与否选择相应的状态行驶。
     */
    private int id;//出租车编号
    private int location;//出租车当前位置
    private int lastLocation;
    private int newLocation;
    private int state;//出租车当前状态
    private int credit = 0;//出租车信用
    private int tickTock;//出租车计时用变量
    private String path;//当前路径
    private String workPath;//将顾客从起点送到终点的路径
    private int pathIndex;//在当前路径上的位置
    private BlockingQueue<Customer> customers = new LinkedBlockingQueue<>();//顾客队列
    private TaxiGUI taxiGUI;
    private Map map;
    private TrafficLight trafficLight;
    private Vector<Customer> history;//历史订单

    Taxi(int id, TaxiGUI taxiGUI, Map map, TrafficLight trafficLight) {
        /*
         * @REQUIRES:
         *      0<=id<100;
         *      taxiGUI!=null;
         *      map!=null;
         *      trafficLight!=null;
         * @MODIFIES:
         *      \this.id;
         *      \this.location;
         *      \this.lastLocation;
         *      \this.newLocation;
         *      \this.state;
         *      \this.tickTock;
         *      \this.history;
         *      \this.taxiGUI;
         *      \this.map;
         *      \this.trafficLight;
         * @EFFECTS:
         *      \this.id == id;
         *      \this.location == new Random().nextInt(TaxiSys.SIZE2);
         *      \this.lastLocation == location;
         *      \this.newLocation = location;
         *      \this.state == State.READY;
         *      \this.tickTock == 100;
         *      \this.history == new Vector<>();
         *      \this.taxiGUI == taxiGUI;
         *      \this.map == map;
         *      \this.trafficLight = trafficLight;
         */
        this.id = id;
        this.location = new Random().nextInt(TaxiSys.SIZE2);
        this.lastLocation = location;
        this.newLocation = location;
        this.state = State.READY;
        this.tickTock = 100;
        this.history = new Vector<>();
        this.taxiGUI = taxiGUI;
        this.map = map;
        this.trafficLight = trafficLight;
        Thread t = new Thread(this, "Taxi#" + this.id);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
        t.start();
    }

    public boolean repOK() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == (0<=id<100) && (0<=location<6400) &&
         *                 (0<=lastLocation<6400) && (0<=newLocation<6400) &&
         *                 (state == (TOCUS||READY||IDLE||TODES)) &&
         *                 (0<=credit) && (0<=tickTock<=100) &&
         *                 (customers!=null && taxiGUI!=null && map!=null &&
         *                 trafficLight!=null && history!=null);
         */
        return (id>=0 && id<100) &&
                (location>=0 && location<6400) &&
                (lastLocation>=0 && lastLocation<6400) &&
                (newLocation>=0 && newLocation<6400) &&
                (state==State.TOCUS || state==State.READY || state==State.IDLE || state==State.TODES) &&
                (credit>=0) && (tickTock>=0 && tickTock<=100) && (customers!=null) && (taxiGUI!=null) &&
                (map!=null) && (trafficLight!=null) && (history!=null);
    }

    public String toString() {
         /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *     \result=="Taxi#"+id;
         */
        return "Taxi#" + id;
    }

    synchronized String addCust(Customer c) {//将顾客分配给此出租车
        /*
         * @REQUIRES:
         *      c!=null;
         * @MODIFIES:
         *      \this.customers;
         *      \this.path;
         *      \this.workPath;
         *      \this.pathIndex;
         *      \this.state;
         * @EFFECTS:
         *      \this.customers.offer(c);
         *      \this.workPath==map.SPFA(c.getStart(),c.getEnd());
         *      \this.state==State.READY ==> \this.path == map.SPFA(location,c.getStart()) &&
         *                                   \this.pathIndex == 0 &&
         *                                   \this.state == STATE.TOCUS;
         *      \result==workPath;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        if (c==null) return "";
        customers.offer(c);
        if (state == State.READY) {//立即上锁
            path = map.SPFA(location, c.getStart());
            pathIndex = 0;
            state = State.TOCUS;
        }
        workPath = map.SPFA(c.getStart(), c.getEnd());
        return workPath;
    }

    synchronized void addCredit(Customer customer) {
        /*
         * @REQUIRES:
         *      customer!=null;
         * @MODIFIES:
         *      \this.history;
         *      \this.credit;
         * @EFFECTS:
         *      \exists Customer cust;cust belongs to history && cust.id==customer.id;return;
         *      \all Customer cust;cust belongs to history && cust.id!=customer.id;
         *          history.add(customer) && credit++;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        if (customer==null) return;
        for (Customer cust : history)
            if (cust.getId() == customer.getId())
                return;
        history.add(customer);
        credit++;//只要抢单就加一
    }

    int getId() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == \this.id;
         */
        return id;
    }

    long getCurTime() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == gv.getTime();
         */
        return gv.getTime();
    }

    synchronized int getX() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == location/TaxiSys.SIZE+1;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        return location / TaxiSys.SIZE + 1;
    }

    synchronized int getY() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == location%TaxiSys.SIZE+1;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        return location % TaxiSys.SIZE + 1;
    }

    synchronized int getState() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == state;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        return state;
    }

    synchronized int getCredit() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == this.credit;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        return this.credit;
    }

    private synchronized void listening() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.path;
         *      \this.pathIndex;
         *      \this.state;
         * @EFFECTS:
         *      !\this.customers.isEmpty() ==> \this.path == map.SPFA(location,customers.peek.getStart()) &&
         *                                     \this.pathIndex == 0 &&
         *                                     \this.state == State.TOCUS;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        if (!customers.isEmpty()) {
            path = map.SPFA(location, customers.peek().getStart());
            pathIndex = 0;
            state = State.TOCUS;
        }
    }

    private void driving(char c) {//有目的行驶
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.newLocation;
         * @EFFECTS:
         *      \this.newLocation == location - TaxiSys.SIZE if c=='U':
         *                           location + TaxiSys.SIZE if c=='D';
         *                           location - 1 if c=='L';
         *                           location + 1 if c=='R';
         */
        if (c == 'U') newLocation = location - TaxiSys.SIZE;
        else if (c == 'D') newLocation = location + TaxiSys.SIZE;
        else if (c == 'L') newLocation = location - 1;
        else if (c == 'R') newLocation = location + 1;
    }

    private void driving() {//无目的行驶
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.newLocation;
         * @EFFECTS:
         *      从当前位置选出周围流量最小的边所对应的顶点并更新newLocation，如果有多条流量
         *      相同的边则随机选择一条；如果周围没有任何边那么newLocation保持当前位置location
         *      的值。
         */
        boolean first = true;
        boolean changed = false;
        Vector<Integer> Nexts = map.edges[location].getAdjust();
        ArrayList<Integer> randomDirect = new ArrayList<>();
        int tempNext = 0;
        for (int next : Nexts) {//遍历边得到可行驶的点
            changed = true;//当前位置周围存在可连接点
            if (first) {
                tempNext = next;
                randomDirect.add(next);
                first = false;
            } else if (map.torrent[location][tempNext] >
                    map.torrent[location][next]) {
                tempNext = next;
                randomDirect.clear();
                randomDirect.add(next);
            } else if (map.torrent[location][tempNext] ==
                    map.torrent[location][next]) {
                randomDirect.add(next);
            }
        }
        if (changed) {//如果存在可连接点那就一定会移动，否则出租车卡在原地无法行驶
            newLocation = randomDirect.get(new Random().nextInt(randomDirect.size()));
        }
    }

    public void run() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.location;
         *      \this.lastLocation;
         *      \this.newLocation
         *      \this.state;
         *      \this.credit;
         *      \this.tickTock;
         *      \this.customers;
         *      \this.path;
         *      \this.workPath;
         *      \this.pathIndex;
         *      \this.map;
         *      \this.history;
         * @EFFECTS:
         *      normal_behavior:
         *          此方法控制单个出租车的运行状态。不断检查state，如果state为等待服务状态，则
         *          将计时器不断减一(减为0便进入停止状态，停止1s后重新恢复计时器并进入等待服务状
         *          态)，如果在中途出现顾客上车，那么出租车状态转变为准备服务状态去接客，接客后睡
         *          1s状态变为服务，到达服务目的地后睡1s状态继续变为等待服务。在每运行一条边之后
         *          出租车均会修改边的流量信息。在每服务完一位乘客之后出租车的信用均+3。
         *      exception_behavior(Exception e):
         *          do nothing.
         */
        while (true) {
            if (state == State.READY) {//等候顾客中
                tickTock--;//只有在等候或者停止过程中才会计时
                if (tickTock == 0) {
                    state = State.IDLE;//等待服务已经20s,停止服务1s
                } else {
                    driving();
                    checkTrafficLight(lastLocation,location,newLocation);
                    try{
                        Thread.sleep(200);
                    } catch (Exception e) {
                    }
                    synchronized (this) {
                        lastLocation = location;
                        location = newLocation;
                    }
                    map.addTorrent(lastLocation, location);
                    taxiGUI.SetTaxiStatus(id,
                            new Point(location / TaxiSys.SIZE, location % TaxiSys.SIZE), state);
                    listening();
                }
            } else if (state == State.TOCUS) {//正在去接顾客
                if (pathIndex == path.length()) {
                    state = State.IDLE;//状态变为停止服务
                    System.out.println(this + " arrived at " + customers.peek());
                    taxiGUI.SetTaxiStatus(id,
                            new Point(location / TaxiSys.SIZE, location % TaxiSys.SIZE), state);
                    try{
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    System.out.println(this + " heading to " + "(" +
                            (customers.peek().getEnd() / TaxiSys.SIZE + 1) +
                            "," + (customers.peek().getEnd() % TaxiSys.SIZE + 1) + ") ");
                    path = workPath;//获取新路径
                    pathIndex = 0;
                    state = State.TODES;//状态变为去往用户目的地
                } else {
                    if (checkConnected(path.charAt(pathIndex))) {
                        driving(path.charAt(pathIndex++));//路连接才继续行驶
                        checkTrafficLight(lastLocation,location,newLocation);
                        try{
                            Thread.sleep(200);
                        } catch (Exception e) {
                        }
                        synchronized (this) {
                            lastLocation = location;
                            location = newLocation;
                        }
                        map.addTorrent(lastLocation,location);
                        taxiGUI.SetTaxiStatus(id,
                                new Point(location / TaxiSys.SIZE, location % TaxiSys.SIZE), state);
                    }
                }
            } else if (state == State.TODES) {//正在去往用户目的地
                if (pathIndex == path.length()) {
                    Customer c = customers.peek();
                    credit += 3;//成功服务一次,信用加3
                    while (!customers.isEmpty() && customers.peek().equals(c))
                        System.out.println(this + " finished " +
                                path.length() + "m trip ordered by " + customers.poll());
                    state = State.IDLE;
                } else {
                    if (checkConnected(path.charAt(pathIndex))) {
                        driving(path.charAt(pathIndex++));//路连接才继续行驶
                        checkTrafficLight(lastLocation,location,newLocation);
                        try{
                            Thread.sleep(200);
                        } catch (Exception e) {
                        }
                        synchronized (this) {
                            lastLocation = location;
                            location = newLocation;
                        }
                        map.addTorrent(lastLocation,location);
                        taxiGUI.SetTaxiStatus(id,
                                new Point(location / TaxiSys.SIZE, location % TaxiSys.SIZE), state);
                    }
                }
            } else {//停止服务中
                taxiGUI.SetTaxiStatus(id,
                        new Point(location / TaxiSys.SIZE, location % TaxiSys.SIZE), State.IDLE);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                state = State.READY;
                tickTock = 100;
            }
        }
    }

    private boolean checkConnected(char direct) {//检测道路连通性
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      nextLocation == the next location the taxi will reach;
         *      \all int next;next belongs to map.edges[location].getAdjust() && next!=nextLocation;
         *              (\result==false) && (path = map.SPFA(location,customers.peek().getEnd())) &&
         *              (pathIndex = 0);
         *      \exists int next;next belongs to map.edges[location].getAdjust() && next==nextLocation;
         *              \result==true;
         */
        int nextLocation = 0;
        if (direct == 'U') nextLocation = location - TaxiSys.SIZE;
        else if (direct == 'D') nextLocation = location + TaxiSys.SIZE;
        else if (direct == 'L') nextLocation = location - 1;
        else if (direct == 'R') nextLocation = location + 1;
        for (int next:map.edges[location].getAdjust())
            if (next==nextLocation)
                return true;//路没断
        //路断了，需要重新寻路
        path = map.SPFA(location, customers.peek().getEnd());
        pathIndex = 0;
        return false;
    }

    private void checkTrafficLight(int prev,int cur,int next) {
        /*
         * @REQUIRES:
         *      (0<=prev && prev<6400) && (0<=cur && cur<6400) && (0<=next && next<6400);
         * @MODIFIES:None;
         * @EFFECTS:
         *      如果前方是红灯且出租车将要直行，或者前方是绿灯且出租车将要左转，则等红绿灯直到红绿灯变色。
         */
        if (prev==cur || cur==next) return;
        //直行的条件是三者在同一条直线，且next!=prev，即不会掉头
        //东西直行
        boolean eastWestForward = next!=prev &&//←←或者→→
                (prev/TaxiSys.SIZE == cur/TaxiSys.SIZE &&
                cur/TaxiSys.SIZE   == next/TaxiSys.SIZE);
        //南北直行
        boolean southNorthForward = next!=prev &&//↑↑或者↓↓
                (prev%TaxiSys.SIZE == cur%TaxiSys.SIZE &&
                cur%TaxiSys.SIZE   == next%TaxiSys.SIZE);
        //东西左转
        boolean eastWestTurn = (cur == prev+1 && next == cur-TaxiSys.SIZE) ||//→↑
                (cur == prev-1 && next == cur+TaxiSys.SIZE);  //←↓
        //南北左转
        boolean southNorthTurn = (cur == prev+TaxiSys.SIZE && next == cur+1) ||//↓→
                (cur == prev-TaxiSys.SIZE && next == cur-1);//↑←
        if (trafficLight.getEastWest(cur)==TaxiSys.RED) {//东西方向红灯且南北方向绿灯
            if (eastWestForward || southNorthTurn) {
                while (trafficLight.getEastWest(cur)==TaxiSys.RED) {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                    }
                }
            }
        } else if (trafficLight.getSouthNorth(cur)==TaxiSys.RED) {//南北方向红灯且东西方向绿灯
            if (southNorthForward || eastWestTurn) {
                while (trafficLight.getSouthNorth(cur)==TaxiSys.RED) {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    synchronized Object[] getSnapShot(int custPos, boolean necessary) {//为了避免脏读写,同时返回5个信息
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      返回一个包含了当前出租车状态等信息的Ojbect数组，为了避免无谓的计算设置标志位necessary，
         *      仅当necessary被设置时才进行SPFA()计算，否则不进行计算。
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        Object[] objects = new Object[5];
        objects[0] = state;
        objects[1] = credit;
        if (necessary) objects[2] = map.SPFA(location, custPos);
        objects[3] = location;
        objects[4] = id;
        return objects;
    }
}
