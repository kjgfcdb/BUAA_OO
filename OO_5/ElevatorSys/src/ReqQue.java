import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

class ReqQue{
    private BlockingQueue<Req> _que = new LinkedBlockingQueue<>();
    private DecimalFormat df = new DecimalFormat("0.0");
    private PrintWriter printWriter;
    ReqQue(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }
    public synchronized Req fetch() {
        if (!_que.isEmpty()) return _que.poll();
        else return null;
    }
    public synchronized void append(String[] strs,long curTime) {
        int lineCnt = 0;
        for (String s : strs) {
            if (InputHandler.parseString(s) && lineCnt < 10) {
                String[] command = s.split("[,()]");
                _que.offer(new Req(command[1], command[2], command[3], curTime));
                lineCnt++;
            } else printWriter.println((curTime+ElevatorSys.getStartTime()) + ":INVALID [" + s + ", " + df.format(curTime / 1000.0) + "]");
        }
    }
    public synchronized Req get(int i) {
        if(i>=0 && i<_que.size()) {
            int j=0;
            for (Req req:_que) {
                if (j==i) return req;
                j++;
            }
        }
        return null;
    }
    public synchronized int size() {
        return _que.size();
    }
    public synchronized void remove(Req req) {
        _que.remove(req);
    }
}
