package OO15;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class Map implements Runnable{
    /*
     * @Overview:地图是车辆运行环境的表示，包括结点的连通性与边的流量。
     */
    Edge[] edges = new Edge[TaxiSys.SIZE2];
    Edge[] initEdges = new Edge[TaxiSys.SIZE2];
    int[][] numMap = new int[TaxiSys.SIZE][TaxiSys.SIZE];
    int[][] torrent = new int[TaxiSys.SIZE2][TaxiSys.SIZE2];
    private Thread t;
    private TaxiGUI taxiGUI;

    Map(String mapFile,TaxiGUI gui) {
        /*
         * @REQUIRES:
         *      filename!=null;
         *      File(filename).exists;
         * @MODIFIES:
         *      \this.edges;
         *      \this.numMap;
         *      \this.t;
         *      \this.taxiGUI;
         * @EFFECTS:
         *      \all int i;0<=i<Taxisys.SIZE2;edges[i]==new Edge();
         *      \this.numMap will be initialized int method init(filename);
         *      \this.t == new Thread(\this);
         *      \this.taxiGUI == gui;
         *      if initialization failes, this program will output "Wrong input" and quit;
         */
        for (int i = 0; i < TaxiSys.SIZE2; i++) {
            edges[i] = new Edge();
            initEdges[i] = new Edge();
        }
        if (!init(mapFile)) {//输入错误，立即退出
            System.out.println("Wrong input!");
            System.exit(0);
        }
        this.t = new Thread(this,"Map");
        this.taxiGUI = gui;
        this.t.setUncaughtExceptionHandler(new MyExceptionHandler());
    }

    public boolean repOK() {
         /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==(edges!=null && numMap!=null && torrent!=null &&
                torrent!=null) && (edges[i]!=null for all 0<=i<TaxiSys.SIZE2) &&
                (numMap[i][j]==(0||1||2||3) for all 0<=i<80 && for all 0<=j<80) &&
                (torrent[i][j]>=0 for all 0<=i<6400 && for all 0<=j<6400);
         */
        if (edges==null || numMap==null || torrent==null) return false;
        for (int i=0;i<TaxiSys.SIZE2;i++)
            if (edges[i]==null) return false;
        for (int i=0;i<TaxiSys.SIZE;i++) {
            for (int j=0;j<TaxiSys.SIZE;j++) {
                if (numMap[i][j]!=0 &&
                        numMap[i][j]!=1 &&
                        numMap[i][j]!=2 &&
                        numMap[i][j]!=3) {
                    return false;
                }
            }
        }
        for (int i=0;i<TaxiSys.SIZE2;i++) {
            for (int j=0;j<TaxiSys.SIZE2;j++) {
                if (torrent[i][j]<0) return false;
            }
        }
        return true;
    }

    synchronized void addTorrent(int start, int end) {
        /*
         * @REQUIRES:
         *      0<=start<=6399;
         *      0<= end <=6399;
         * @MODIFIES:
         *      \this.torrent;
         * @EFFECTS:
         *      如果start与end邻接,则
         *          \this.torrent[start][end]++;
         *          \this.torrent[end][start]++;
         *      否则不对torrent做任何操作
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         *
         */
        if (edges[start].getAdjust().stream().anyMatch(o->o==end)) {
            //start与end之间有边才加流量，否则不加
            int dx = Math.abs(start / TaxiSys.SIZE - end / TaxiSys.SIZE);
            int dy = Math.abs((start - end) % TaxiSys.SIZE);
            if (dx + dy == 1) {
                torrent[start][end]++;
                torrent[end][start]++;
            }
        }
    }

    public void run() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.torrent;
         *      \this.tempTorrent;
         * @EFFECTS:
         *      首先等候100ms以便与出租车线程错开，然后每隔200ms，地图自动更新
         *      将流量torrent清零.
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked(\this);
         */
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        long comp;
        long curTime = gv.getTime();
        while (true) {
            comp = gv.getTime()-curTime;
            if (comp>0 && comp<200) comp = 200-comp;
            else comp = 200;
            try{
                Thread.sleep(comp);
            } catch (Exception e) {
            }
            curTime+=200;
            synchronized (this) {
                for (int i = 0; i < TaxiSys.SIZE2; i++) {
                    for (int next : edges[i].getAdjust()) {
                        torrent[i][next] = 0;
                    }
                }
            }
        }
    }

    void start() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.t;
         * @EFFECTS:
         *      \this.t.start();
         */
        t.start();
    }

    void closePath(int start, int end,Taxi[] taxis) {
        /*
         * @REQUIRES:
         *      0<=start<=6399;
         *      0<=end<=6399;
         * @MODIFIES:
         *      \this.edges;
         *      \this.torrent;
         *      \this.taxiGUI;
         * @EFFECTS:
         *      \all int i;i belongs to edges[start].getAdjust && i!=end;System.out.println("The edge you want to remove doesn't exist at all!");
         *      \exits int i;i belongs to edges[start].getAdjust && i==end;edges[start].closePath(end) && edges[end].closePath(start) && return;
         *      \this.torrent[start][end] == \this.torrent[end][start] == 0 && 在gui关闭这条边 &&
         *      (\all int i;0<=i<100 && taxis[i].getState==(State.TODES || State.TOCUS);taxis[i].roadChanged == true;
         */
        Vector<Integer> temp = edges[start].getAdjust();
        for (int i:temp) {
            if (i==end) {
                edges[start].closePath(end);
                edges[end].closePath(start);
                torrent[start][end] = torrent[end][start] = 0;
                taxiGUI.SetRoadStatus(new Point(start/TaxiSys.SIZE,
                        start%TaxiSys.SIZE),new Point(end/TaxiSys.SIZE,
                        end%TaxiSys.SIZE),0);
                for (int j=0;j<TaxiSys.TAXINUM;j++) {
                    if (taxis[j].getState()==State.TODES ||
                            taxis[j].getState()== State.TOCUS) {
                        taxis[j].roadChanged = true;
                    }
                }
                return;
            }
        }
        System.out.println("The edge you want to remove doesn't exist at all!");
    }

    void openPath(int start, int end,Taxi[] taxis) {
        /*
         * @REQUIRES:
         *      0<=start<=6399;
         *      0<=end<=6399;
         * @MODIFIES:
         *      \this.edges;
         *      \this.taxiGUI;
         * @EFFECTS:
         *      (\all int i;i belongs to edges[start].getAdjust && i!=end; && (\exist int i;
         *      i belongs to initEdges[start].getAdjust && i==end;) => edges[start].openPath(end))
         *      && edges[end].openPath(start) && 在gui上连接这条边 &&
         *      (\all int i;0<=i<100 && taxis[i].getState==(State.TODES || State.TOCUS);taxis[i].roadChanged == true;
         *
         *      \exits int i;i belongs to edges[start].getAdjust && i==end;System.out.println("The edge you want to add exits.") && return;
         */
        if (edges[start].getAdjust().stream().anyMatch(o->o==end)) {
            System.out.println("你想打开的边已经存在.");
            return;
        }
        if (initEdges[start].getAdjust().stream().anyMatch(o->o==end)) {
            edges[start].openPath(end);
            edges[end].openPath(start);
            taxiGUI.SetRoadStatus(new Point(start/TaxiSys.SIZE,
                    start%TaxiSys.SIZE),new Point(end/TaxiSys.SIZE,
                    end%TaxiSys.SIZE),1);
            for (int i=0;i<TaxiSys.TAXINUM;i++) {
                if (taxis[i].getState()==State.TOCUS ||
                        taxis[i].getState()==State.TODES) {
                    taxis[i].roadChanged = true;
                }
            }
        } else {
            System.out.println("不能打开初始时地图上没有的边.");
        }
    }

    private boolean init(String filename) {
        /*
         * @REQUIRES:
         *      filename!=null;
         *      File(filename).exits();
         * @MODIFIES:
         *      \this.edges;
         *      \this.numMap;
         * @EFFECTS:
         *      \result==true <==> \this.edges and \this.numMap is initialized correctly.
         *      The input file's format isn't correct ==> \result==false;
         *      如果在处理文件过程中出现异常 ==> 吞掉该异常并且\result==false;
         */
        BufferedReader bufferReader;
        if (filename==null || !new File(filename).exists()) {
            System.out.println("地图文件不存在,程序退出");
            System.exit(1);
        }
        try {
            bufferReader = new BufferedReader(new
                    InputStreamReader(new FileInputStream(new File(filename))));
            ArrayList<String> lines = bufferReader.lines().map(line->line
                    .replaceAll("[ \t]","")).filter(line->!line.equals(""))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (lines.size()!=TaxiSys.SIZE)
                return false;
            if (lines.stream().anyMatch(line->line.length()
                    !=TaxiSys.SIZE || line.matches("[^0123]")))
                return false;
            for (int i=0;i<TaxiSys.SIZE;i++)
                for (int j=0;j<TaxiSys.SIZE;j++)
                    this.numMap[i][j] = lines.get(i).charAt(j)-'0';
            bufferReader.close();
        } catch (Exception e) {
            return false;
        }
        for (int i=0;i<TaxiSys.SIZE;i++) {
            for (int j=0;j<TaxiSys.SIZE;j++) {
                if (numMap[i][j]==1 || numMap[i][j]==3) {
                    edges[i*TaxiSys.SIZE+j].openPath(i*TaxiSys.SIZE+j+1);
                    edges[i*TaxiSys.SIZE+j+1].openPath(i*TaxiSys.SIZE+j);
                    initEdges[i*TaxiSys.SIZE+j].openPath(i*TaxiSys.SIZE+j+1);
                    initEdges[i*TaxiSys.SIZE+j+1].openPath(i*TaxiSys.SIZE+j);
                }
                if (numMap[i][j]==2 || numMap[i][j]==3) {
                    edges[i*TaxiSys.SIZE+j].openPath((i+1)*TaxiSys.SIZE+j);
                    edges[(i+1)*TaxiSys.SIZE+j].openPath(i*TaxiSys.SIZE+j);
                    initEdges[i*TaxiSys.SIZE+j].openPath((i+1)*TaxiSys.SIZE+j);
                    initEdges[(i+1)*TaxiSys.SIZE+j].openPath(i*TaxiSys.SIZE+j);
                }
            }
        }
        return true;
    }

    synchronized String SPFA(int _start, int _end,Edge[] searchEdges) {
        /*
         * @REQUIRES:
         *      0<=start<=6399;
         *      0<=end<=6399;
         *      searchEdges!=null;
         * @MODIFIES:None;
         * @EFFECTS:
         *      令字符串path表示SPFA算法找到的从_start到_end的路径最短且流量之和最小的路径.
         *      \result == path;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         *
         */
        int start = _end;//反转起始点与终止点,从终点开始找最短路径
        int begin = _start;
        if (_start == _end) return "";
        boolean[] inQ = new boolean[TaxiSys.SIZE2];//inQ[i]表示点i是否在队列中
        int[] dis = new int[TaxiSys.SIZE2];//距离数组,表示点i到起点的距离
        int[] flow = new int[TaxiSys.SIZE2];//流量数组,表示点i到起点的流量之和
        int[] next = new int[TaxiSys.SIZE2];//next[i]表示点i的下一个点
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < TaxiSys.SIZE2; i++) {
            dis[i] = Integer.MAX_VALUE;
            flow[i] = Integer.MAX_VALUE;
        }
        inQ[start] = true;
        dis[start] = 0;
        flow[start] = 0;
        queue.offer(start);
        int now, cost, sumFlow;
        while (!queue.isEmpty()) {
            now = queue.poll();
            Vector<Integer> Nexts = searchEdges[now].getAdjust();
            for (int to : Nexts) {
                cost = 1 + dis[now];
                sumFlow = flow[now] + torrent[now][to];
                if (dis[to] > cost || (dis[to] == cost &&
                        flow[to] > sumFlow)) {
                    next[to] = now;
                    dis[to] = cost;
                    flow[to] = sumFlow;
                    if (!inQ[to]) {
                        inQ[to] = true;
                        queue.offer(to);
                    }
                }
            }
            inQ[now] = false;
        }
        StringBuilder sb = new StringBuilder("");
        while (begin != _end) {
            if (next[begin] == begin + 1) sb.append('R');
            if (next[begin] == begin - 1) sb.append('L');
            if (next[begin] == begin + TaxiSys.SIZE) sb.append('D');
            if (next[begin] == begin - TaxiSys.SIZE) sb.append('U');
            begin = next[begin];
        }

        return sb.toString();
    }

}

class Edge {
    /*
     * @Overview:边类，代表一个点周围的边的集合。拥有开闭路以及获取邻接点的功能。
     */
    private Vector<Integer> adjoin;

    Edge() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.adjoin;
         * @EFFECTS:
         *      \this.adjoin == new Vector<>();
         */
        adjoin = new Vector<>();
    }

    public boolean repOK() {
         /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==(adjoin!=null) && (0<=next<6400 for all next belongs to adjoin);
         */
        if (adjoin==null) return false;
        for (int next:adjoin)
            if (next<0 || next>6399) return false; //[0,6399]
        return true;
    }

    synchronized void openPath(int next) {
        /*
         * @REQUIRES:
         *      0<=next<=6399;
         * @MODIFIES:
         *      \this.adjoin;
         * @EFFECTS:
         *      (0<=next<6400) ==> \this.adjoin.addElement(next);
         *      (next<0 || next>6399) ==> do nothing;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        if (next<0 || next>6399) return;
        adjoin.addElement(next);
    }

    synchronized void closePath(int next) {
        /*
         * @REQUIRES:
         *      0<=next<=6399;
         * @MODIFIES:
         *      \this.adjoin;
         * @EFFECTS:
         *      \this.adjoin.removeElement(next);
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        adjoin.removeElement(next);
    }

    synchronized Vector<Integer> getAdjust() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      Vector<Integer> temp == adjoin;
         *      \result==temp;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        Vector<Integer> temp = new Vector<>();
        for (int i:adjoin)
            temp.addElement(i);
        return temp;
    }
}

class TrafficLight implements Runnable{
    /*
     * @Overview:TrafficLight表示地图上的交通信号灯，能按照固定周期变化灯光。
     */
    private int period;//随机产生的变化周期
    private volatile int cycle;//周期性改变
    private TaxiGUI gui;
    private int[] tfLight;
    private ArrayList<Integer> light;
    private Thread t;

    TrafficLight(String filename,TaxiGUI taxiGUI) {
        /*
         * @REQUIRES:
         *      filename!=null;
         * @MODIFIES:
         *      \this.period;\this.cycle;\this.tfLight;\this.t;
         * @EFFECTS:
         *      \this.period == random int x, x belongs to [50,100];
         *      \this.cycle == 1;
         *      \this.tfLight was updated according to the file;
         *      \this.t == new Thread(this."TrafficLight");
         */
        this.period = new Random().nextInt(301)+200;//满足[200,500]=[0,300]+200
        this.cycle = 1;
        this.tfLight = new int[TaxiSys.SIZE2];
        this.gui = taxiGUI;
        this.light = new ArrayList<>();
        if (!init(filename)) {
            System.out.println("红绿灯文件格式错误!");
            System.exit(0);
        }
        this.t = new Thread(this,"TrafficLight");
        this.t.setUncaughtExceptionHandler(new MyExceptionHandler());
    }

    public boolean repOK() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == ((tfLight!=null && t!=null) &&
         *                (tfLight[i] == (0 || 1 || -1)) &&
         *                (50<=period && period<=100) &&
         *                (cycle== (1 || -1));
         */
        if (tfLight==null || t==null) return false;
        for (int i=0;i<TaxiSys.SIZE2;i++) {
            if (tfLight[i]!=0 && tfLight[i]!=1 && tfLight[i]!=-1) {
                return false;
            }
        }
        return (period>=50 && period<=100) &&
                (cycle==1 || cycle==-1);
    }

    int getEastWest(int index) {//如果返回值是1，那么表示为红灯，否则表示为绿灯
        /*
         * @REQUIRES:
         *      0<=index<6400;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == cycle*tfLight[index] if (0<=index<6400);
         *                 2 if (index<0 || index>=6400);
         */
        if (index<0 || index>=6400) return 2;
        return cycle*tfLight[index];
    }

    int getSouthNorth(int index) {//如果返回值为1，表示为红灯，否则表示为绿灯，0表示没有灯
         /*
         * @REQUIRES:
         *      0<=index<6400;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == -cycle*tfLight[index] if (0<=index<6400);
         *                 2 if (index<0 || index>=6400);
         */
        if (index<0 || index>=6400) return 2;
        return -cycle*tfLight[index];
    }

    public void run() {
         /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.cycle;;
         * @EFFECTS:
         *      \this.cycle == -\this.cycle;
         */
        while (true) {
            light.forEach(i->gui.SetLightStatus(
                    new Point(i/TaxiSys.SIZE,i%TaxiSys.SIZE),
                    getEastWest(i)==TaxiSys.RED? 2:1));
            try{
                Thread.sleep(period);
            } catch (Exception e) {
            }
            cycle = -cycle;
        }
    }

    private boolean init(String filename) {
         /*
         * @REQUIRES:
         *      filename!=null;
         * @MODIFIES:
         *      \this.tfLight;
         * @EFFECTS:
         *      根据给定的文件名filename(要求不为null)处理对应文件并初始化信号灯数组tfLight。如果
         *      中途出现任何异常或者给定文件不符合规范则返回false，否则返回true并将tfLight中的非0
         *      值随机地赋为其相反数，使得tfLight存在三种数：0,1,-1，分别表示没有红绿灯，初始为东西红灯，
         *      初始为东西绿灯.
         */
        if (filename==null) return false;
        File inputFile = new File(filename);
        if (!inputFile.exists()) {
            System.out.println("红绿灯文件不存在,程序退出");
            System.exit(0);
        }
        try {
            BufferedReader bufferReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File(filename))));
            ArrayList<String> lines = bufferReader.lines().map(line->line
                    .replace("[ \t]",""))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (lines.size()!=TaxiSys.SIZE)
                return false;
            if (lines.stream().anyMatch(line->line.length()
                    !=TaxiSys.SIZE || line.matches("[^01]")))
                return false;
            for (int i=0;i<TaxiSys.SIZE;i++)
                for (int j=0;j<TaxiSys.SIZE;j++)
                    this.tfLight[i*TaxiSys.SIZE+j] = lines.get(i).charAt(j)-'0';
            bufferReader.close();
        } catch (Exception e) {
            return false;
        }
        Random rd = new Random();
        for (int i=0;i<TaxiSys.SIZE2;i++) {
            if (rd.nextBoolean())
                this.tfLight[i] = -this.tfLight[i];
            if (this.tfLight[i]!=0)
                light.add(i);
        }
        return true;
    }

    void start() {
         /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.t;
         * @EFFECTS:
         *      \this.t.start();
         */
        this.t.start();
    }
}
