import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

class Map implements Runnable{
    Edge[] edges = new Edge[TaxiSys.SIZE2];
    int[][] numMap = new int[TaxiSys.SIZE][TaxiSys.SIZE];
    int[][] torrent = new int[TaxiSys.SIZE2][TaxiSys.SIZE2];
    private int[][] tempTorrent = new int[TaxiSys.SIZE2][TaxiSys.SIZE2];
    private Thread t;

    Map(String filename) {
        /*
         * @REQUIRES:
         *      filename!=null;
         *      File(filename).exists;
         * @MODIFIES:
         *      \this.edges;
         *      \this.numMap;
         *      \this.t;
         * @EFFECTS:
         *      \all int i;0<=i<Taxisys.SIZE2;edges[i]==new Edge();
         *      \this.numMap will be initialized int method init(filename);
         *      if initialization failes, this program will output "Wrong input" and quit;
         *      \this.t == new Thread(\this);
         */
        for (int i = 0; i < TaxiSys.SIZE2; i++)
            edges[i] = new Edge();
        if (!init(filename)) {//输入错误，立即退出
            System.out.println("Wrong input!");
            System.exit(0);
        }
        t = new Thread(this);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
    }

    public void start() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.t;
         * @EFFECTS:
         *      \this.t.start();
         */
        t.start();
    }

    public void run() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.torrent;
         *      \this.tempTorrent;
         * @EFFECTS:
         *      normal_behavior;
         *          首先等候100ms以便与出租车线程错开，然后每隔200ms，地图自动更新
         *          各边流量至torrent，而将tempTorrent复原。
         *      exception_behavior(Exception e):
         *          do nothing.
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
                        torrent[i][next] = tempTorrent[i][next];
                        tempTorrent[i][next] = 0;
                    }
                }
            }
        }
    }

    synchronized void addTorrent(int start, int end) {
        /*
         * @REQUIRES:
         *      0<=start<=6399;
         *      0<= end <=6399;
         * @MODIFIES:
         *      \this.tempTorrent;
         * @EFFECTS:
         *      如果start与end邻接,则
         *          \this.tempTorrent[start][end]++;
         *          \this.tempTorrent[start][end]++;
         *      否则不对tempTorrent做任何操作
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         *
         */
        int dx = Math.abs(start/TaxiSys.SIZE-end/TaxiSys.SIZE);
        int dy = Math.abs((start-end)%TaxiSys.SIZE);
        if ((dx==1 && dy==0) || (dx==0 && dy==1)) {
            tempTorrent[start][end]++;
            tempTorrent[end][start]++;
        }
    }

    void closePath(int start, int end) {
        /*
         * @REQUIRES:
         *      0<=start<=6399;
         *      0<=end<=6399;
         * @MODIFIES:
         *      \this.edges[start];
         *      \this.edges[end];
         * @EFFECTS:
         *      \all int i;i belongs to edges[start].getAdjust && i!=end;System.out.println("The edge you want to remove doesn't exist at all!");
         *      \exits int i;i belongs to edges[start].getAdjust && i==end;edges[start].closePath(end) && edges[end].closePath(start) && return;
         */
        Vector<Integer> temp = edges[start].getAdjust();
        for (int i:temp) {
            if (i==end) {
                edges[start].closePath(end);
                edges[end].closePath(start);
                return;
            }
        }
        System.out.println("The edge you want to remove doesn't exist at all!");
    }

    void openPath(int start, int end) {
        /*
         * @REQUIRES:
         *      0<=start<=6399;
         *      0<=end<=6399;
         * @MODIFIES:
         *      \this.edges[start];
         *      \this.edges[end];
         * @EFFECTS:
         *      \all int i;i belongs to edges[start].getAdjust && i!=end;edges[start].openPath(end) && edges[end].openPath(start);
         *      \exits int i;i belongs to edges[start].getAdjust && i==end;System.out.println("The edge you want to add exits.") && return;
         *      如果起始点和终止点不相互靠近(这里指的是空间上相互靠近,直线距离只有1)，那么不会连接这两点.
         */
        Vector<Integer> temp = edges[start].getAdjust();
        for (int i:temp) {
            if (i==end) {
                System.out.println("The edge you want to add exits.");
                return;
            }
        }
        int dx = Math.abs(start/TaxiSys.SIZE-end/TaxiSys.SIZE);
        int dy = Math.abs((start-end)%TaxiSys.SIZE);
        if ((dx==1 && dy==0) || (dx==0 && dy==1)) {
            edges[start].openPath(end);
            edges[end].openPath(start);
        } else {
            System.out.println("The points you want to connect aren't close to each other.");
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
         *      normal_behavior:
         *          \result==true <==> \this.edges and \this.numMap is initialized correctly.
         *          The input file's format isn't correct ==> \result==false;
         *      exception_behavior(Exception e):
         *          \result==false;
         */
        BufferedReader bufferReader;
        StringBuilder sb = new StringBuilder();
        if (filename==null || !new File(filename).exists()) {
            System.out.println("地图文件不存在,程序退出");
            System.exit(1);
        }
        try {
            InputStream inputStream = new FileInputStream(new File(filename));
            bufferReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int lineCnt = 0;
            while ((line = bufferReader.readLine()) != null) {
                String _line = line.replaceAll("[ \t]", "");
                if (_line.equals("")) continue;//允许空行
                if (_line.length() != TaxiSys.SIZE) return false;//长度不对报错
                String[] strArray = _line.split("");
                for (int i = 0; i < _line.length(); i++) {//非法字符报错
                    if (_line.charAt(i) != '0' && _line.charAt(i) != '1' &&
                            _line.charAt(i) != '2' && _line.charAt(i) != '3')
                        return false;
                    this.numMap[lineCnt][i] = Integer.parseInt(strArray[i]);
                }
                sb.append(_line);
                lineCnt++;
            }
            if (lineCnt != TaxiSys.SIZE) return false;//行数不对报错
            bufferReader.close();
        } catch (Exception e) {
            return false;
        }
        String data = sb.toString();
        for (int site = 0; site < data.length(); site++) {
            char c = data.charAt(site);
            if (c == '1' || c == '3') {
                edges[site].openPath(site + 1);
                edges[site + 1].openPath(site);
            }
            if (c == '2' || c == '3') {
                edges[site].openPath(site + TaxiSys.SIZE);
                edges[site + TaxiSys.SIZE].openPath(site);
            }
        }
        return true;
    }

    synchronized String SPFA(int _start, int _end) {
        /*
         * @REQUIRES:
         *      0<=start<=6399;
         *      0<=end<=6399;
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
            Vector<Integer> Nexts = edges[now].getAdjust();
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
    private Vector<Integer> adjust;

    Edge() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.adjust;
         * @EFFECTS:
         *      \this.adjust == new Vector<>();
         */
        adjust = new Vector<>();
    }

    synchronized void openPath(int next) {
        /*
         * @REQUIRES:
         *      0<=next<=6399;
         * @MODIFIES:
         *      \this.adjust;
         * @EFFECTS:
         *      \this.adjust.addElement(next);
         */
        adjust.addElement(next);
    }

    synchronized void closePath(int next) {
        /*
         * @REQUIRES:
         *      0<=next<=6399;
         * @MODIFIES:
         *      \this.adjust;
         * @EFFECTS:
         *      \this.adjust.removeElement(next);
         */
        adjust.removeElement(next);
    }

    synchronized Vector<Integer> getAdjust() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      Vector<Integer> temp == adjust;
         *      \result==temp;
         */
        Vector<Integer> temp = new Vector<>();
        for (int i:adjust)
            temp.addElement(i);
        return temp;
    }
}
