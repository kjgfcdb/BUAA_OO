package OO15;

import java.awt.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static OO15.TaxiSys.SIZE2;
import static OO15.TaxiSys.SIZE;

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
    /*
     * @Overview:出租车类根据分配任务与否选择相应的状态行驶。
     */
    protected int id;//出租车编号
    protected int location;//出租车当前位置
    protected int lastLocation;
    protected int newLocation;
    protected int state;//出租车当前状态
    protected int credit = 0;//出租车信用
    protected int tickTock;//出租车计时用变量
    protected String path;//当前路径
    protected String workPath;//将顾客从起点送到终点的路径
    protected int pathIndex;//在当前路径上的位置
    protected BlockingQueue<Customer> customers = new LinkedBlockingQueue<>();//顾客队列
    protected TaxiGUI taxiGUI;
    protected Map map;
    protected Edge[] taxiEdges;
    protected TrafficLight trafficLight;
    protected Vector<Customer> history;//历史订单
    public volatile boolean roadChanged;
    protected Thread t;
    protected StringBuilder toCusPath;
    protected StringBuilder toDesPath;

    Taxi(int id, TaxiGUI taxiGUI, Map map, TrafficLight trafficLight) {
        /*
         * @REQUIRES:
         *      0<=id<100;
         *      taxiGUI!=null;
         *      map!=null;
         *      trafficLight!=null;
         * @MODIFIES:
         *      \this.id;\this.location;\this.lastLocation;\this.newLocation;
         *      \this.state;\this.tickTock;\this.history;\this.taxiGUI;\this.map;
         *      \this.trafficLight;\this.taxiEdges;\this.toCusPath;\this.toDesPath;
         * @EFFECTS:
         *      \this.id == id;\this.location == new Random().nextInt(SIZE2);\this.lastLocation == location;
         *      \this.newLocation == location;\this.state == State.READY;\this.tickTock == 100;
         *      \this.history == new Vector<>();\this.taxiGUI == taxiGUI;\this.map == map;
         *      \this.trafficLight == trafficLight;\this.taxiEdges == map.edges;
         *      \this.toCusPath = new StringBuilder();\this.toDesPath = new StringBuilder();
         */
        this.id = id;
        this.location = new Random().nextInt(SIZE2);
        this.lastLocation = location;
        this.newLocation = location;
        this.state = State.READY;
        this.tickTock = 100;
        this.history = new Vector<>();
        this.taxiGUI = taxiGUI;
        this.map = map;
        this.taxiEdges = map.edges;
        this.trafficLight = trafficLight;
        toCusPath = new StringBuilder();
        toDesPath = new StringBuilder();
        taxiGUI.SetTaxiType(id,0);
        t = new Thread(this, "Taxi#" + this.id);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
    }

    public void start() {
        /**
         * @MODIFIES:\this.t;
         * @EFFECTS:
         *      \this.t.start();
         */
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
         *                 trafficLight!=null && history!=null) && toCusPath!=null &&
         *                 toDesPath!=null;
         */
        return (id>=0 && id<100) &&
                (location>=0 && location<6400) &&
                (lastLocation>=0 && lastLocation<6400) &&
                (newLocation>=0 && newLocation<6400) &&
                (state==State.TOCUS || state==State.READY || state==State.IDLE || state==State.TODES) &&
                (credit>=0) && (tickTock>=0 && tickTock<=100) && (customers!=null) && (taxiGUI!=null) &&
                (map!=null) && (trafficLight!=null) && (history!=null) &&
                (toCusPath!=null) && (toDesPath!=null);
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

    synchronized void addCust(Customer c) {//将顾客分配给此出租车
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
         *      \this.workPath==map.SPFA(c.getStart(),c.getEnd(),taxiEdges);
         *      \this.state==State.READY ==> \this.path == map.SPFA(location,c.getStart(),taxiEdges) &&
         *                                   \this.pathIndex == 0 &&
         *                                   \this.state == STATE.TOCUS;
         *      \result==workPath;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        if (c==null) return ;
        customers.offer(c);
        if (state == State.READY) {//立即上锁
            path = map.SPFA(location, c.getStart(),taxiEdges);
            pathIndex = 0;
            state = State.TOCUS;
        }
        workPath = map.SPFA(c.getStart(), c.getEnd(),taxiEdges);
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
         *      \result == location/SIZE+1;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        return location / SIZE + 1;
    }

    synchronized int getY() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == location%SIZE+1;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        return location % SIZE + 1;
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
         *      !\this.customers.isEmpty() ==> \this.path == map.SPFA(location,customers.peek.getStart(),taxiEdges) &&
         *                                     \this.pathIndex == 0 &&
         *                                     \this.state == State.TOCUS;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        if (!customers.isEmpty()) {
            path = map.SPFA(location, customers.peek().getStart(),taxiEdges);
            pathIndex = 0;
            state = State.TOCUS;
        }
    }

    protected void driving(char c,int curState) {//有目的行驶
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.newLocation;
         *      \this.toCusPath;
         *      \this.toDesPath;
         * @EFFECTS:
         *      \this.newLocation == location - SIZE if c=='U':
         *                           location + SIZE if c=='D';
         *                           location - 1 if c=='L';
         *                           location + 1 if c=='R';
         *      (curState==State.TOCUS) => toCusPath.append(c);
         *      (curState==State.TODES) => toDesPath.append(c);
         */
        if (curState==State.TOCUS)
            toCusPath.append(c);
        else if (curState==State.TODES)
            toDesPath.append(c);
        if (c == 'U') newLocation = location - SIZE;
        else if (c == 'D') newLocation = location + SIZE;
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
        Vector<Integer> Nexts = taxiEdges[location].getAdjust();
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
         *      此方法控制单个出租车的运行状态。不断检查state，如果state为等待服务状态，则
         *      将计时器不断减一(减为0便进入停止状态，停止1s后重新恢复计时器并进入等待服务状
         *      态)，如果在中途出现顾客上车，那么出租车状态转变为准备服务状态去接客，接客后睡
         *      1s状态变为服务，到达服务目的地后睡1s状态继续变为等待服务。在每运行一条边之后
         *      出租车均会修改边的流量信息。在每服务完一位乘客之后出租车的信用均+3。
         * @THREAD_REQUIRES:
         *      \locked(lastLocation,location);
         * @THREAD_EFFECTS:
         *      \locked();
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
                            new Point(location / SIZE, location % SIZE), state);
                    listening();
                }
            } else if (state == State.TOCUS) {//正在去接顾客
                if (pathIndex == path.length()) {
                    state = State.IDLE;//状态变为停止服务
                    System.out.println(this + " arrived at " + customers.peek());
                    taxiGUI.SetTaxiStatus(id,
                            new Point(location / SIZE, location % SIZE), state);
                    try{
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    System.out.println(this + " heading to " + "(" +
                            (customers.peek().getEnd() / SIZE + 1) +
                            "," + (customers.peek().getEnd() % SIZE + 1) + ") ");
                    path = workPath;//获取新路径
                    pathIndex = 0;
                    state = State.TODES;//状态变为去往用户目的地
                    taxiGUI.SetTaxiStatus(id,
                            new Point(location / SIZE, location % SIZE), state);
                } else {
                    working(State.TOCUS);
                }
            } else if (state == State.TODES) {//正在去往用户目的地
                if (pathIndex == path.length()) {
                    Customer c = customers.peek();
                    finishedTask(c);
                    credit += 3;//成功服务一次,信用加3
                    while (!customers.isEmpty() && customers.peek().equals(c))
                        System.out.println(this + " finished " +
                                "the order made by " + customers.poll());
                    state = State.IDLE;
                } else {
                    working(State.TODES);
                }
            } else {//停止服务中
                taxiGUI.SetTaxiStatus(id,
                        new Point(location / SIZE, location % SIZE), state);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                state = State.READY;
                tickTock = 100;
            }
        }
    }

    protected void finishedTask(Customer c) {
        /*
         * @REQUIRES:c!=null
         * @EFFECTS:
         *      让c输出它这趟行程经过的所有点到文件中
         */
        c.arrivedDes(toDesPath.toString());
        toCusPath = new StringBuilder();
        toDesPath = new StringBuilder();
    }

    private void working(int curState) {
        /**
         * @MODIFIES:
         *      newLocation;lastLocation;location;map;taxiGUI;pathIndex;path;
         * @EFFECTS:
         *      检测前方道路是否联通，联通则继续等红绿灯然后行驶，再更新流量以及设置GUI，否则重新计算道路并运行
         * @THREAD_REQUIRES:
         *      \locked(lastLocation,location);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        if (checkConnected(curState)) {
            driving(path.charAt(pathIndex++),curState);//路连接才继续行驶
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
                    new Point(location / SIZE, location % SIZE), state);
        }
    }

    private boolean checkConnected(int curState) {//检测道路连通性
        /*
         * @REQUIRES:None;
         * @MODIFIES:pathIndex;path;
         * @EFFECTS:
         *      roadChanged => (roadChanged==false) &&
         *           (path==map.SPFA(location,customers.peek().getEnd(),taxiEdges) &&
         *           (pathIndex == 0) && (\result==false);
         *      roadChanged => \result==true;
         */
        if (roadChanged) {
            //路断了，需要重新寻路
            roadChanged = false;
            if (curState==State.TOCUS)
                path = map.SPFA(location, customers.peek().getStart(),taxiEdges);
            else if (curState==State.TODES)
                path = map.SPFA(location, customers.peek().getEnd(),taxiEdges);
            pathIndex = 0;
            return false;
        } else return true;
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
                (prev/SIZE == cur/SIZE &&
                cur/SIZE   == next/SIZE);
        //南北直行
        boolean southNorthForward = next!=prev &&//↑↑或者↓↓
                (prev%SIZE == cur%SIZE &&
                cur%SIZE   == next%SIZE);
        //东西左转
        boolean eastWestTurn = (cur == prev+1 && next == cur-SIZE) ||//→↑
                (cur == prev-1 && next == cur+SIZE);  //←↓
        //南北左转
        boolean southNorthTurn = (cur == prev+SIZE && next == cur+1) ||//↓→
                (cur == prev-SIZE && next == cur-1);//↑←
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
        if (necessary) objects[2] = map.SPFA(location, custPos,taxiEdges);
        objects[3] = location;
        objects[4] = id;
        return objects;
    }
}

class TrackableTaxi extends Taxi {
    /**
     * @Overview:可追踪出租车，能以迭代方式访问历次服务记录，还能迭代访问服务轨迹
     */
    private ArrayList<Record> records;//每次记录

    class Record {
        /**
         * @Overview:记录类，存储可追踪出租车每次服务的记录
         */
        private Customer c;
        private int startLocation;
        private String toCusPath;
        private String toDesPath;

        Record(Customer customer,int startLocation,String toCusPath,String toDesPath) {
            /**
             * @REQUIRES:
             *      customer!=null && startLocation!=null &&
             *      toCusPath!=null && toDesPath!=null;
             * @MODIFIES:
             *      \this.c;\this.startLocation;\this.toCusPath;\this.toDesPath;
             * @EFFECTS:
             *      \this.c == customer;\this.startLocation == startLocation;
             *      \this.toCusPath == toCusPath;\this.toDesPath == toDesPath;
             */
            this.c = customer;
            this.startLocation = startLocation;
            this.toCusPath = toCusPath;
            this.toDesPath = toDesPath;
        }

        public boolean repOK() {
            /*
             * @EFFECTS:
             *      \result==c!=null && startLocation>=0 && startLocation<6400 &&
             *      toCusPath!=null && toDesPath!=null;
             */
            return c!=null && startLocation>=0 && startLocation<6400 &&
                    toCusPath!=null && toDesPath!=null;
        }

        public deIterator<Point> getPoint() {//提供一个能迭代访问轨迹点的迭代器
            /*
             * @EFFECTS:
             *      \result==new PointManager(\this);
             */
            return new PointManager(this);
        }

        class PointManager implements deIterator{
            /**
             * @Overview:点管理类，存储一次服务中出租车行驶的轨迹点
             */
            private ArrayList<Point> points;
            private int n;

            PointManager(Record record) {
                /**
                 * @REQUIRES:record!=null;
                 * @MODIFIES:\this.n;\this.points;
                 * @EFFECTS:
                 *      \this.n == 0;
                 *      \this.points == 出租车在从抢单位置到服务完该次请求所经过的所有的Point序列;
                 */
                this.points = new ArrayList<>();
                n = 0;
                points.add(new Point(record.startLocation/SIZE+1
                        ,record.startLocation%SIZE+1));
                int begin = record.startLocation;
                for (char c:(record.toCusPath+record.toDesPath).toCharArray()) {
                    switch (c) {
                        case 'U':begin = begin-SIZE;break;
                        case 'D':begin = begin+SIZE;break;
                        case 'L':begin = begin-1; break;
                        case 'R':begin = begin+1; break;
                        default:break;
                    }
                    points.add(new Point(begin/SIZE+1
                            ,begin%SIZE+1));
                }
            }

            public boolean repOK() {
                /*
                 * @EFFECTS:
                 *      \result==points!=null;
                 */
                return points!=null;
            }

            public boolean hasNext() {
                /**
                 * @EFFECTS:
                 *      \result==n<points.size();
                 */
                return n<points.size();
            }
            public Point next() {
                /**
                 * @MODIFIES:\this.n;
                 * @EFFECTS:
                 *      如果中途不产生异常，则\result==points.get(n++);
                 *      如果中途出现异常，吞掉该异常并且\result==null;
                 */
                try {
                    return points.get(n++);
                } catch (Exception e) {
                    return null;
                }
            }
            public boolean hasPrevious() {
                /**
                 * @EFFECTS:
                 *      \result==(n-1>=0);
                 */
                return n-1>=0;
            }
            public Point previous() {
                /**
                 * @MODIFIES:\this.n;
                 * @EFFECTS:
                 *      如果中途不产生异常，则\result==points.get(--n);
                 *      如果中途出现异常，吞掉该异常并且\result==null;
                 */
                try {
                    return points.get(--n);
                } catch (Exception e) {
                    return null;
                }
            }
        }

        public long getCustomerTime() {
            /**
             * @EFFECTS:
             *      \result==c.getTime();
             */
            return c.getTime();
        }

        public Point getCustomerStartPoint() {
            /**
             * @EFFECTS:
             *      \result==new Point(c.getStart()/SIZE+1,c.getStart()%SIZE+1);
             */
            return new Point(c.getStart()/SIZE+1,c.getStart()%SIZE+1);
        }

        public Point getCustomerEndPoint() {
            /**
             * @EFFECTS:
             *      \result==new Point(c.getEnd()/SIZE+1,c.getEnd()%SIZE+1);
             */
            return new Point(c.getEnd()/SIZE+1,c.getEnd()%SIZE+1);
        }
    }

    class RecordManager implements deIterator{
        /**
         * @Overview:记录管理类，实际上提供可迭代访问记录的能力
         */
        private int n;
        private ArrayList<Record> records;

        RecordManager(TrackableTaxi trackableTaxi) {
            /**
             * @REQUIRES:trackableTaxi!=null;
             * @MODIFIES:\this.record;\this.n;
             * @EFFECTS:
             *      \this.records == trackableTaxi.records;
             *      \this.n == 0;
             */
            this.records = trackableTaxi.records;
            this.n = 0;
        }

        public boolean repOK() {
            /*
             * @EFFECTS:
             *      \result==records!=null;
             */
            return records!=null;
        }

        public boolean hasNext() {
            /**
             * @EFFECTS:
             *      \result==n<records.size();
             */
            return n<records.size();
        }
        public Record next() {
            /**
             * @MODIFIES:\this.n;
             * @EFFECTS:
             *      如果中途不出现异常，\result==records.get(n++);
             *      如果中途出现异常\result==null;
             */
            try {
                return records.get(n++);
            } catch (Exception e) {
                return null;
            }
        }
        public boolean hasPrevious() {
            /**
             * @EFFECTS:
             *      \result==n-1>=0;
             */
            return n-1>=0;
        }
        public Record previous() {
            /**
             * @MODIFIES:\this.n;
             * @EFFECTS:
             *      如果中途不出现异常，则\result==records.get(--n);
             *      如果中途出现异常，吞掉异常并且\result==null;
             */
            try {
                return records.get(--n);
            } catch (Exception e) {
                return null;
            }
        }
    }

    TrackableTaxi (int id, TaxiGUI taxiGUI, Map map, TrafficLight trafficLight) {
        /*
         * @REQUIRES:
         *      0<=id<100 && taxiGUI!=null && map!=null && trafficLight!=null;
         * @MODIFIES:
         *      \this.id;\this.location;\this.lastLocation;\this.newLocation;
         *      \this.state;\this.tickTock;\this.history;\this.taxiGUI;\this.map;
         *      \this.trafficLight;\this.taxiEdges;\this.records;
         * @EFFECTS:
         *      \this.id == id;\this.location == new Random().nextInt(SIZE2);\this.lastLocation == location;
         *      \this.newLocation == location;\this.state == State.READY;\this.tickTock == 100;
         *      \this.history == new Vector<>();\this.taxiGUI == taxiGUI;\this.map == map;
         *      \this.trafficLight == trafficLight;\this.taxiEdges == map.initEdges;
         *      \this.records == new ArrayList<>();
         */
        super(id,taxiGUI,map,trafficLight);
        records = new ArrayList<>();
        taxiEdges = map.initEdges;
        taxiGUI.SetTaxiType(id,1);
    }

    public boolean repOK() {
        /*
         * @EFFECTS:
         *      \result==super.repOK() && records!=null;
         */
        return super.repOK() && records!=null;
    }

    protected void finishedTask(Customer c) {
        /*
         * @REQUIRES:c!=null;
         * @MODIFIES:records;toCusPath;toDesPath;
         * @EFFECTS:
         *      在完成请求的那一刻根据用户请求的起始点算出出租车接单位置，随后新增一个记录项至records中，
         *      然后更新toCusPath与toDesPath，并将整个行程输出到文件中
         */
        String tCP = toCusPath.toString();
        int curLocation = c.getStart();
        for (int i=0;i<tCP.length();i++) {
            switch (tCP.charAt(i)) {
                case 'U':curLocation = curLocation+SIZE;break;
                case 'D':curLocation = curLocation-SIZE;break;
                case 'L':curLocation = curLocation+1;break;
                case 'R':curLocation = curLocation-1;break;
                default:break;
            }
        }
        records.add(new Record(c,curLocation,tCP,toDesPath.toString()));
        super.finishedTask(c);
    }

    public deIterator<Record> getRecords() {
        /*
         * @EFFECTS:
         *      \result==new RecordManager(\this);
         */
        return new RecordManager(this);
    }
}
