import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

class Elv implements Runnable{
    private int id;
    private int dstFloor;
    private volatile State state;
    DecimalFormat df = new DecimalFormat("0.0");
    private PriorityBlockingQueue<Req> incPriQue = new PriorityBlockingQueue<>(10000, new Comparator<Req>() {
        @Override
        public int compare(Req o1, Req o2) {
            if (o1.getDstFloor()!=o2.getDstFloor()) return o1.getDstFloor()-o2.getDstFloor();
            return (o1.getTime()-o2.getTime())>0? 1:-1;//如果楼层相同，那么按照时间排序
        }
    });
    private PriorityBlockingQueue<Req> decPriQue = new PriorityBlockingQueue<>(10000, new Comparator<Req>() {
        @Override
        public int compare(Req o1, Req o2) {
        	if (o1.getDstFloor()!=o2.getDstFloor()) return o2.getDstFloor()-o1.getDstFloor();
            return (o1.getTime()-o2.getTime())>0? 1:-1;
        }
    });

    private PriorityBlockingQueue<Req> priQue;
    private Thread t;
    private int curFloor;
    private volatile Direction direct;
    private int work;
    private BlockingQueue<Req> turnOffFR = new LinkedBlockingDeque<>();
    private BlockingQueue<Req> turnOffER = new LinkedBlockingDeque<>();
    private BlockingQueue<Req> waitRoom = new LinkedBlockingDeque<>();
    private PrintWriter printWriter;
    private Floor floor;
    private int[] elvLantern;//电梯灯计划
    private long launchTime;//电梯每次被指令惊醒的时间
    Elv(int id, PrintWriter printWriter,Floor floor) {
        this.id = id;
        this.curFloor = 1;
        this.state = State.IDLE;
        this.priQue = incPriQue;
        this.printWriter = printWriter;
        this.direct = Direction.STILL;
        this.floor = floor;
        this.elvLantern = new int [21];
        t = new Thread(this);
        t.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        t.start();
    }
    public int getWork() {return work;}
    public void run() {
        while (true) {
            while (state==State.IDLE) {
                if (priQue.isEmpty()) {
                    if (!waitRoom.isEmpty()) {
                        if (waitRoom.peek().getDstFloor()>curFloor)priQue = incPriQue;
                        else priQue = decPriQue;
                        priQue.offer(waitRoom.poll());
                    }
                } else {
                    dstFloor = priQue.peek().getDstFloor();
                    if (priQue.peek().getDstFloor() != curFloor) {
                        if (priQue.peek().getDstFloor() > curFloor) direct = Direction.UP;
                        else direct = Direction.DOWN;
                        state = State.RUNNING;
                    } else state = State.STILL;
                }
            }
            while (state==State.RUNNING) {//在运行过程中检查能否捎带处于等待状态的指令
                boolean flag = true;
                while (!waitRoom.isEmpty() && flag) {
                    flag = false;
                    if (waitRoom.peek().getDstFloor() > curFloor && direct == Direction.UP) {
                        priQue.offer(waitRoom.poll());
                        flag = true;
                    } else if (waitRoom.peek().getDstFloor() < curFloor && direct == Direction.DOWN) {
                        priQue.offer(waitRoom.poll());
                        flag = true;
                    }
                }
                long st = new Date().getTime();
                long compensation = (st-launchTime-ElevatorSys.getStartTime())%3000;
                if (compensation>1000) compensation = 3000;
                else compensation = 3000-compensation;
                try {Thread.sleep((compensation));} catch (InterruptedException e) {}
                st = new Date().getTime();
                curFloor = direct == Direction.UP ? curFloor + 1 : curFloor - 1;
//                System.out.println(id+" at "+curFloor);
                work++;
                turnOffFR.clear();
                turnOffER.clear();
                while (priQue.peek() != null && curFloor == priQue.peek().getDstFloor()) {
                    if (priQue.peek().getType().equals("FR")) turnOffFR.offer(priQue.peek());
                    else turnOffER.offer(priQue.peek());
                    printWriter.println(st+ ":["+priQue.peek()+"] / (#"+id+", " +curFloor+
                            ", "+direct+", "+work+", "+df.format((st-ElevatorSys.getStartTime())/1000.0)+")");
                    priQue.poll();
                    state = State.SERVING;
                }
            }

            while (state==State.SERVING || state==State.STILL) {
                long st = new Date().getTime();
                long compensation = (st-ElevatorSys.getStartTime()-launchTime)%3000;
                if (compensation>1000) compensation = 6000;
                else compensation = 6000-compensation;
                try{Thread.sleep(compensation);} catch(InterruptedException e) {}
                if (state==State.STILL) {
                    st = new Date().getTime();
                    printWriter.println(st+":["+priQue.peek()+"] / (#"+id+", "+curFloor+
                            ", STILL, "+work+", "+df.format((st-ElevatorSys.getStartTime())/1000.0)+")");
                    if (priQue.peek().getType().equals("FR")) {
                        floor.turnOff(priQue.peek().getDstFloor(), priQue.peek().getDirection());
                    }
                    priQue.poll();
                }
                for (Req toFR : turnOffFR) {//关闭所有到期楼层灯
                    floor.turnOff(toFR.getDstFloor(), toFR.getDirection());
                }
                for (Req toER : turnOffER) {//关闭所有到期电梯灯
                    elvLantern[toER.getDstFloor()] = 0;
                }
                if (priQue.isEmpty()) {
                	if (waitRoom.isEmpty()) state = State.IDLE;
                	else {
                		if (waitRoom.peek().getDstFloor()> curFloor) {
                			priQue = incPriQue;
                			direct = Direction.UP;
                			state = State.RUNNING;
                		} else if (waitRoom.peek().getDstFloor()< curFloor) {
                			priQue = decPriQue;
                			direct = Direction.DOWN;
                			state = State.RUNNING;
                		} else {
                			priQue = incPriQue;
                			state = State.STILL;
                		}
                        priQue.offer(waitRoom.poll());
                	}
                } else {
                	dstFloor = priQue.peek().getDstFloor();
                    if (priQue.peek().getDstFloor() != curFloor) {
                        if (priQue.peek().getDstFloor() > curFloor) direct = Direction.UP;
                        else  direct = Direction.DOWN;
                        state = State.RUNNING;
                    } else state = State.STILL;
                }
            }
        }
    }
    synchronized void fetch(Req r) {
        if (state==State.IDLE) {
            launchTime = r.getTime();
            dstFloor = r.getDstFloor();
            if (r.getDstFloor()!=curFloor) {
                state = State.RUNNING;
                if (r.getDstFloor()>curFloor){
                    priQue = incPriQue;
                    direct = Direction.UP;
                }
                else{
                    priQue = decPriQue;
                    direct = Direction.DOWN;
                }
            }else {
                state = State.STILL;
                direct = Direction.STILL;
            }
            priQue.offer(r);
        } else if (state==State.RUNNING || state==State.SERVING) {
            if ((direct==Direction.UP && r.getDstFloor()>curFloor) ||
                    (direct==Direction.DOWN && r.getDstFloor()<curFloor)) {
                priQue.offer(r);
            } else {
                waitRoom.offer(r);
            }
        } else {//STILL
            priQue.offer(r);
        }
        if (r.getType().equals("FR")) floor.turnOn(r.getDstFloor(),r.getDirection());
        if (r.getType().equals("ER")) elvLantern[r.getDstFloor()] = 1;
    }
    public synchronized boolean isOn(int i) {
        return elvLantern[i]==1;
    }
    public synchronized boolean ifResponsive(Req fr) {//请求都是楼层类请求
        if (state==State.IDLE && priQue.isEmpty() && waitRoom.isEmpty()) return true;
        if (fr.getDirection()==direct) {
            if (direct==Direction.UP && fr.getDstFloor()<= dstFloor && fr.getDstFloor()>curFloor) return true;
            if (direct==Direction.DOWN && fr.getDstFloor()>=dstFloor && fr.getDstFloor()<curFloor) return true;
        }
        return false;
    }
    public synchronized boolean ifCarriable(Req fr) {//请求都是楼层类请求
        if (fr.getDirection()==direct) {
            if (direct==Direction.UP && fr.getDstFloor()<= dstFloor && fr.getDstFloor()>curFloor) return true;
            if (direct==Direction.DOWN && fr.getDstFloor()>=dstFloor && fr.getDstFloor()<curFloor) return true;
        }
        return false;
    }
}
