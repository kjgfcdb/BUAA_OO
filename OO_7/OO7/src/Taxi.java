import java.awt.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class State{//IDLE表示停止,READY表示等待服务,TOCUS&TODES表示正在服务,TOCUS是去接顾客,TODES是去顾客目的地
    static int IDLE = 0;
    static int TODES = 1;
    static int READY = 2;
    static int TOCUS = 3;
}
class Taxi implements Runnable{
    private int id;//出租车编号
    private int location;//出租车当前位置
    private int state;//出租车当前状态
    private int credit = 0;//出租车信用
    private int tickTock;//出租车计时用变量
    private BlockingQueue<Customer> customers = new LinkedBlockingQueue<>();//顾客队列
    private String path;//当前路径
    private int pathIndex;//在当前路径上的位置
    private long startTime;
    private long curTime;
    private Taxi[] taxis;
    private TaxiGUI taxiGUI;
    private Vector<Integer>[] edges;//edges[i]装着与点i相连的点
    Taxi(int id,Vector<Integer>[] edges,TaxiGUI taxiGUI,Taxi[] taxis) {
        this.id = id;
        this.location = new Random().nextInt(TaxiSys.SIZE2);
        this.state = State.READY;
        this.tickTock = 100;
        this.edges = edges;
        this.taxiGUI = taxiGUI;
        this.taxis = taxis;
        this.startTime = gv.getTime();
        this.curTime = startTime;
        Thread t = new Thread(this,"Taxi#"+this.id);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
        t.start();
    }
    public String toString() {return "Taxi#"+id;}
    synchronized String addCust(Customer c) {//将顾客分配给此出租车
        customers.offer(c);
        if (state==State.READY) {//立即上锁
            path = SPFA(location,c.getStart());
            pathIndex = 0;
            state = State.TOCUS;
        }
        credit++;//抢单成功,信用加一
        return SPFA(c.getStart(),c.getEnd());
    }
    long getCurTime() {
        return gv.getTime()-startTime;
    }
    synchronized int getX() {
        return location/TaxiSys.SIZE+1;
    }
    synchronized int getY() {
        return location%TaxiSys.SIZE+1;
    }
    private synchronized void listening() {
        if (!customers.isEmpty()) {
            path = SPFA(location, customers.peek().getStart());
            pathIndex = 0;
            state = State.TOCUS;
        }
    }
    //获取状态
    synchronized int getState() {
        return state;
    }
    //返回相应状态的出租车，存放在ArrayList中
    synchronized ArrayList<Taxi> queryTaxi(int _state) {
        ArrayList<Taxi> temp = new ArrayList<>();
        for (int i=0;i<TaxiSys.TAXINUM;i++) {
            if (taxis[i].getState()==_state)
                temp.add(taxis[i]);
        }
        return temp;
    }
    //获取编号为i的出租车的状态
    synchronized int getState(int i) {
        return taxis[i].getState();
    }
    private synchronized void driving(char c) {//有目的行驶
        if (c=='U') location-=TaxiSys.SIZE;
        else if (c=='D') location+=TaxiSys.SIZE;
        else if (c=='L') location--;
        else if (c=='R') location++;
        taxiGUI.SetTaxiStatus(id,
                new Point(location/TaxiSys.SIZE,location%TaxiSys.SIZE), state);
    }
    private synchronized void driving() {//无目的行驶
        int [] randomDirect = new int[4];
        int index = 0;
        for (int next:edges[location])//遍历边得到可行驶的点
            randomDirect[index++] = next;
        location = randomDirect[new Random().nextInt(index)];
        taxiGUI.SetTaxiStatus(id,
                new Point(location/TaxiSys.SIZE,location%TaxiSys.SIZE), state);
    }
    public void run() {
        long comp;
        while (true) {
            if (state == State.READY) {//等候顾客中
                tickTock--;//只有在等候或者停止过程中才会计时
                if (tickTock == 0) {
                    state = State.IDLE;//等待服务已经20s,停止服务1s
                } else {
                    comp = gv.getTime()-curTime;
                    if (comp>0 && comp<200) comp = 200-comp;
                    else comp = 200;
                    try {Thread.sleep(comp);
                    } catch (Exception e) {}
                    curTime+=200;
                    driving();
                    listening();
                }
            } else if (state == State.TOCUS) {//正在去接顾客
                if (pathIndex == path.length()) {
                    state = State.IDLE;//状态变为停止服务
                    System.out.println(this+" arrived at "+customers.peek());
                    taxiGUI.SetTaxiStatus(id,
                            new Point(location/TaxiSys.SIZE,location%TaxiSys.SIZE), state);
                    comp = gv.getTime()-curTime;
                    if (comp>0 && comp<200) comp = 1000-comp;
                    else comp = 1000;
                    try {Thread.sleep(comp);
                    } catch (Exception e) {}
                    curTime+=1000;
                    Customer c = customers.peek();
                    System.out.println(this+" heading to "+"("+(c.getEnd()/TaxiSys.SIZE+1)+
                            ","+(c.getEnd()%TaxiSys.SIZE+1)+") ");
                    path = SPFA(c.getStart(), c.getEnd());//获取新路径
                    pathIndex = 0;
                    state = State.TODES;//状态变为去往用户目的地
                } else {
                    comp = gv.getTime()-curTime;
                    if (comp>0 && comp<200) comp = 200-comp;
                    else comp = 200;
                    try {Thread.sleep(comp);
                    } catch (Exception e) {}
                    curTime+=200;
                    driving(path.charAt(pathIndex++));
                }
            } else if (state == State.TODES) {//正在去往用户目的地
                if (pathIndex == path.length()) {
                    Customer c = customers.peek();
                    credit += 3;//成功服务一次,信用加3
                    while (!customers.isEmpty() && customers.peek().equals(c))
                        System.out.println(this + " finished "+path.length()+"m trip orderd by " +
                                customers.poll());
                    state = State.IDLE;
                } else {
                    comp = gv.getTime()-curTime;
                    if (comp>0 && comp<200) comp = 200-comp;
                    else comp = 200;
                    try {Thread.sleep(comp);
                    } catch (Exception e) {}
                    curTime+=200;
                    driving(path.charAt(pathIndex++));
                }
            } else {//停止服务中
                taxiGUI.SetTaxiStatus(id,
                        new Point(location/TaxiSys.SIZE,location%TaxiSys.SIZE), State.IDLE);
                comp = gv.getTime()-startTime;
                if (comp>0 && comp<200) comp = 1000-comp;
                else comp = 1000;
                try {Thread.sleep(comp);
                } catch (Exception e) {}
                curTime+=1000;
                state = State.READY;
                tickTock = 100;
            }
        }
    }

    synchronized Object[] getSnapShot(int custPos,boolean w) {//为了避免脏读写,同时返回5个信息
        Object[] objects = new Object[5];
        objects[0] = state;
        objects[1] = credit;
        if (w) objects[2] = SPFA(location,custPos);
        objects[3] = location;
        objects[4] = id;
        return objects;
    }

    private String SPFA(int _start,int _end) {
        int start = _end;//反转起始点与终止点,从终点开始找最短路径
        int begin = _start;
        if (_start==_end) return "";
        boolean[] inQ = new boolean[TaxiSys.SIZE2];//inQ[i]表示点i是否在队列中
        int[] dis = new int[TaxiSys.SIZE2];//距离数组,表示点i到起点的距离
        int[] next = new int[TaxiSys.SIZE2];//next[i]表示点i的下一个点
        Queue<Integer> queue = new LinkedList<>();
        for (int i=0;i<TaxiSys.SIZE2;i++)
            dis[i] = Integer.MAX_VALUE;
        inQ[start]=true;
        dis[start]=0;
        queue.offer(start);
        int now,cost;
        while (!queue.isEmpty()) {
            now=queue.poll();
            for (int to:edges[now]) {
                cost=1+dis[now];
                if (dis[to]>cost) {
                    next[to]=now;
                    dis[to]=cost;
                    if (!inQ[to]) {
                        inQ[to]=true;
                        queue.offer(to);
                    }
                }
            }
            inQ[now]=false;
        }
        StringBuilder sb = new StringBuilder("");
        while (begin!=_end) {
            if (next[begin]==begin+1) sb.append('R');
            if (next[begin]==begin-1) sb.append('L');
            if (next[begin]==begin+TaxiSys.SIZE) sb.append('D');
            if (next[begin]==begin-TaxiSys.SIZE) sb.append('U');
            begin = next[begin];
        }
        return sb.toString();
    }
}
