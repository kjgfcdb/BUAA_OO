import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

class Schedule extends OldSchedule implements Runnable{
    private Thread t = new Thread(this);
    private ReqQue reqQue;
    private Elv elv1;
    private Elv elv2;
    private Elv elv3;
    private PriorityBlockingQueue<Elv> workRank = new PriorityBlockingQueue<>(4, new Comparator<Elv>() {
        @Override
        public int compare(Elv o1, Elv o2) {
            return o1.getWork()-o2.getWork();
        }
    });
    private Floor floor;
    private PrintWriter printWriter;
    Schedule(ReqQue reqQue,Elv elv1,Elv elv2,Elv elv3,Floor floor,PrintWriter printWriter) {
        this.reqQue = reqQue;
        this.elv1 = elv1;
        this.elv2 = elv2;
        this.elv3 = elv3;
        this.floor = floor;
        this.printWriter = printWriter;
        t.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        t.start();
    }
    public synchronized void run() {
        /*
        逻辑：
        如果没有电梯可以响应，则等待
        如果有电梯可以响应
            如果有电梯可以捎带
                选择可响应电梯中累计运动量最小的
            如果没有电梯可以捎带
                选择可响应电梯中累计运动量最小的
         */
        while (true) {
            Req r = reqQue.fetch();
            if (r!=null) {
                if (r.getType().equals("ER")) {//若楼层灯亮，不予调度
                    long st = new Date().getTime();
                    if (r.getElvId()==1) {
                        if (elv1.isOn(r.getDstFloor())) printWriter.println(st+":SAME [" +r+ "]");
                        else elv1.fetch(r);
                    }
                    if (r.getElvId()==2) {
                        if (elv2.isOn(r.getDstFloor())) printWriter.println(st+":SAME [" +r+ "]");
                        else elv2.fetch(r);
                    }
                    if (r.getElvId()==3) {
                        if (elv3.isOn(r.getDstFloor())) printWriter.println(st+":SAME [" +r+ "]");
                        else elv3.fetch(r);
                    }
                } else { //FR请求，开始调度
                    if (floor.isOn(r.getDstFloor(),r.getDirection())) {//若楼层灯亮，则不予调度
                        long st = new Date().getTime();
                        printWriter.println(st+":SAME [" +r+ "]");
                    } else {
                        workRank.clear();
                        boolean fetched = false;
                        while (!fetched) {//强制性保护措施
                            while (!elv1.ifResponsive(r) && !elv2.ifResponsive(r) && !elv3.ifResponsive(r)) {}
                            if (elv1.ifCarriable(r) || elv2.ifCarriable(r) || elv3.ifCarriable(r)) {
                                if (elv1.ifCarriable(r)) workRank.offer(elv1);
                                if (elv2.ifCarriable(r)) workRank.offer(elv2);
                                if (elv3.ifCarriable(r)) workRank.offer(elv3);
                                if (workRank.peek() != null) {
                                    workRank.peek().fetch(r);
                                    fetched = true;
                                }
                            } else {
                                if (elv1.ifResponsive(r)) workRank.offer(elv1);
                                if (elv2.ifResponsive(r)) workRank.offer(elv2);
                                if (elv3.ifResponsive(r)) workRank.offer(elv3);
                                if (workRank.peek() != null) {
                                    workRank.peek().fetch(r);
                                    fetched = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
