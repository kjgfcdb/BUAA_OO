import java.awt.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class State {//IDLE表示停止,READY表示等待服务,TOCUS&TODES表示正在服务,TOCUS是去接顾客,TODES是去顾客目的地
    static int IDLE = 0;
    static int TODES = 1;
    static int READY = 2;
    static int TOCUS = 3;
}

class Taxi implements Runnable {
    private int id;//出租车编号
    private int location;//出租车当前位置
    private int lastLocation;
    private int state;//出租车当前状态
    private int credit = 0;//出租车信用
    private int tickTock;//出租车计时用变量
    private BlockingQueue<Customer> customers = new LinkedBlockingQueue<>();//顾客队列
    private String path;//当前路径
    private String workPath;//将顾客从起点送到终点的路径
    private int pathIndex;//在当前路径上的位置
    private static long startTime = gv.getTime();
    private long curTime;
    private TaxiGUI taxiGUI;
    private Map map;
    private Vector<Customer> history;//历史订单

    Taxi(int id, TaxiGUI taxiGUI, Map map) {
        /*
         * @REQUIRES:
         *      0<=id<100;
         *      taxiGUI!=null;
         *      map!=null;
         * @MODIFIES:
         *      \this.id;
         *      \this.location;
         *      \this.lastLocation;
         *      \this.state;
         *      \this.tickTock;
         *      \this.history;
         *      \this.taxiGUI;
         *      \this.map;
         *      \this.curTime;
         * @EFFECTS:
         *      \this.id == id;
         *      \this.location == new Random().nextInt(TaxiSys.SIZE2);
         *      \this.lastLocation == location;
         *      \this.state == State.READY;
         *      \this.tickTock == 100;
         *      \this.history == new Vector<>();
         *      \this.taxiGUI == taxiGUI;
         *      \this.map == map;
         *      \this.curTime == startTime;
         */
        this.id = id;
        this.location = new Random().nextInt(TaxiSys.SIZE2);
        this.lastLocation = location;
        this.state = State.READY;
        this.tickTock = 100;
        this.history = new Vector<>();
        this.taxiGUI = taxiGUI;
        this.map = map;
        this.curTime = startTime;
        Thread t = new Thread(this, "Taxi#" + this.id);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
        t.start();
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

    synchronized long getCurTime() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == curTime-startTime;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        return curTime - startTime;
    }

    synchronized int getX() {
        /**
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
        /**
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
        /**
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

    private synchronized void listening() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.path;
         *      \this.pathIndex;
         *      \this.state;
         * @EFFECTS:
         *      !\this.customers.isEmpty() ==> \this.path == map.SPFA(location,customers.peek.getStart());
         *                                     \this.pathIndex == 0;
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

    private synchronized void driving(char c) {//有目的行驶
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.lastLocation;
         *      \this.location;
         *      \this.map;
         * @EFFECTS:
         *      首先更新lastLocation，然后根据输入的字符c更新location，随后更新地图
         *      中对应道路的流量，最后在GUI上画出当前位置。
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        lastLocation = location;
        if (c == 'U') location -= TaxiSys.SIZE;
        else if (c == 'D') location += TaxiSys.SIZE;
        else if (c == 'L') location--;
        else if (c == 'R') location++;
        map.addTorrent(lastLocation, location);
        taxiGUI.SetTaxiStatus(id,
                new Point(location / TaxiSys.SIZE, location % TaxiSys.SIZE), state);
    }

    private synchronized void driving() {//无目的行驶
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.lastLocation;
         *      \this.location;
         *      \this.map;
         * @EFFECTS:
         *      首先更新lastLocation，随后从当前位置附件的边中随机选取一条流量最小的边，
         *      更新当前位置(如果当前道路都断了，那么当前位置无法更新)，再更新map中的流
         *      量，最后在taxiGUI中将当前位置标志出来。
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        lastLocation = location;
        boolean first = true;
        boolean changed = false;
        int newLocation = 0;
        Vector<Integer> Nexts = map.edges[location].getAdjust();
        ArrayList<Integer> randomDirect = new ArrayList<>();
        for (int next : Nexts) {//遍历边得到可行驶的点
            changed = true;//当前位置周围存在可连接点
            if (first) {
                newLocation = next;
                randomDirect.add(next);
                first = false;
            } else if (map.torrent[location][newLocation] >
                    map.torrent[location][next]) {
                newLocation = next;
                randomDirect.clear();
                randomDirect.add(next);
            } else if (map.torrent[location][newLocation] ==
                    map.torrent[location][next]) {
                randomDirect.add(next);
            }
        }
        if (changed) {//如果存在可连接点那就一定会移动，否则出租车卡在原地无法行驶
            location = randomDirect.get(new Random().nextInt(randomDirect.size()));
            map.addTorrent(lastLocation, location);
        }
        taxiGUI.SetTaxiStatus(id,
                new Point(location / TaxiSys.SIZE, location % TaxiSys.SIZE), state);
    }

    public void run() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.location;
         *      \this.lastLocation;
         *      \this.state;
         *      \this.credit;
         *      \this.tickTock;
         *      \this.customers;
         *      \this.path;
         *      \this.workPath;
         *      \this.pathIndex;
         *      \this.curTime;
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
        long comp;
        while (true) {
            if (state == State.READY) {//等候顾客中
                tickTock--;//只有在等候或者停止过程中才会计时
                if (tickTock == 0) {
                    state = State.IDLE;//等待服务已经20s,停止服务1s
                } else {
                    comp = gv.getTime() - curTime;
                    if (comp > 0 && comp < 200) comp = 200 - comp;
                    else comp = 200;
                    try {
                        Thread.sleep(comp);
                    } catch (Exception e) {
                    }
                    curTime += 200;
                    driving();
                    listening();
                }
            } else if (state == State.TOCUS) {//正在去接顾客
                if (pathIndex == path.length()) {
                    state = State.IDLE;//状态变为停止服务
                    System.out.println(this + " arrived at " + customers.peek());
                    taxiGUI.SetTaxiStatus(id,
                            new Point(location / TaxiSys.SIZE, location % TaxiSys.SIZE), state);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    curTime += 1000;
                    Customer c = customers.peek();
                    System.out.println(this + " heading to " + "(" + (c.getEnd() / TaxiSys.SIZE + 1) +
                            "," + (c.getEnd() % TaxiSys.SIZE + 1) + ") ");
                    path = workPath;//获取新路径
                    pathIndex = 0;
                    state = State.TODES;//状态变为去往用户目的地
                } else {
                    if (!checkConnected(path.charAt(pathIndex))) {//道路损坏，更换路径
                        path = map.SPFA(location,customers.peek().getStart());
                        pathIndex = 0;
                    } else {
                        comp = gv.getTime() - curTime;
                        if (comp > 0 && comp < 200) comp = 200 - comp;
                        else comp = 200;
                        try {
                            Thread.sleep(comp);
                        } catch (Exception e) {
                        }
                        curTime += 200;
                        driving(path.charAt(pathIndex++));
                    }
                }
            } else if (state == State.TODES) {//正在去往用户目的地
                if (pathIndex == path.length()) {
                    Customer c = customers.peek();
                    credit += 3;//成功服务一次,信用加3
                    while (!customers.isEmpty() && customers.peek().equals(c))
                        System.out.println(this + " finished " +
                                path.length() + "m trip orderd by " + customers.poll());
                    state = State.IDLE;
                } else {
                    if (!checkConnected(path.charAt(pathIndex))) {//道路损坏，更换路径
                        path = map.SPFA(location, customers.peek().getEnd());
                        pathIndex = 0;
                    } else {
                        comp = gv.getTime() - curTime;
                        if (comp > 0 && comp < 200) comp = 200 - comp;
                        else comp = 200;
                        try {
                            Thread.sleep(comp);
                        } catch (Exception e) {
                        }
                        curTime += 200;
                        driving(path.charAt(pathIndex++));
                    }
                }
            } else {//停止服务中
                taxiGUI.SetTaxiStatus(id,
                        new Point(location / TaxiSys.SIZE, location % TaxiSys.SIZE), State.IDLE);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                curTime += 1000;
                state = State.READY;
                tickTock = 100;
            }
        }
    }

    private boolean checkConnected(char direct) {//检测道路连通性
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      newLocation == the next location the taxi will reach;
         *      \all int next;next belongs to map.edges[location].getAdjust() && next!=newLocation;
         *              \result==false;
         *      \exists int next;next belongs to map.edges[location].getAdjust() && next==newLocation;
         *              \result==true;
         */
        int newLocation = 0;
        if (direct == 'U') newLocation = location - TaxiSys.SIZE;
        else if (direct == 'D') newLocation = location + TaxiSys.SIZE;
        else if (direct == 'L') newLocation = location - 1;
        else if (direct == 'R') newLocation = location + 1;
        Vector<Integer> temp = map.edges[location].getAdjust();
        for (int next:temp){
            if (next==newLocation) return true;
        }
        return false;
    }

    synchronized Object[] getSnapShot(int custPos, boolean necessary) {//为了避免脏读写,同时返回5个信息
        /**
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
