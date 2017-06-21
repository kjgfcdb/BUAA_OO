import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.*;
public class ElevatorSchedule {
    public static void main(String [] args) {
        Scanner input = new Scanner(System.in);
        ALSScheduler sch = new ALSScheduler();
        Pattern p1 = Pattern.compile("^\\(FR,\\+?\\d+,UP,\\+?\\d+\\)$");
        Pattern p2 = Pattern.compile("^\\(FR,\\+?\\d+,DOWN,\\+?\\d+\\)$");
        Pattern p3 = Pattern.compile("^\\(ER,\\+?\\d+,\\+?\\d+\\)$");
        Matcher m1,m2,m3;
        int instrNum = 0;
        try {
            while (true) {
                String s = input.nextLine();
                s = s.replaceAll(" ", "");

                if (s.equals("q") || instrNum==100000)//超出100000条指令即自动截断
                    break;
                if (illegalStr(s)) {
                    System.out.println("Invalid ["+s+"]");
                    continue;
                }
                instrNum++;
                m1 = p1.matcher(s);//强行匹配，匹配不到就算错
                m2 = p2.matcher(s);
                m3 = p3.matcher(s);
                boolean hasfound = (m1.find()||m2.find()||m3.find());
                String[] strs = s.split("[,()]");
                if (hasfound && strs.length == 5 && strs[0].equals("")) {//可能是楼层命令
                    if (sch.checkFloor(strs[1], strs[2], strs[3], strs[4]) == 0)
                        System.out.println("INVALID ["+s+"]");

                } else if (hasfound && strs.length == 4 && strs[0].equals("")) {//可能是电梯命令
                    if (sch.checkElevator(strs[1], strs[2], strs[3]) == 0) {
                        System.out.println("INVALID ["+s+"]");
                    }
                } else {
                    System.out.println("INVALID ["+s+"]");
                }

            }
            sch.compute();
        } catch (Exception e) {
            System.out.println("Terrible Input");
        }
        input.close();
        System.exit(0);
    }
    private static boolean illegalStr(String s) {
        String eng = "EFRUPDOWN";
        int flag=0;
        for (int i=0;i<s.length();i++) {
            for (int j=0;j<eng.length();j++) {
                if (s.charAt(i)==eng.charAt(j)) {
                    flag=1;
                }
            }
            if (!((s.charAt(i)>='0' && s.charAt(i)<='9')||
                    s.charAt(i)=='(' || s.charAt(i)==')'||
                    s.charAt(i)==',' || s.charAt(i)=='+'||
                    flag==1)) {
                return true;
            }
        }
        return false;
    }
}
class Floor{
    private int up;
    private int down;
}
class ReqQue{
    private ArrayList<Request> que;
    ReqQue() {
        que = new ArrayList<Request>();
    }
    void addReq(Request r) {
        que.add(r);
    }
    int size() {
        return que.size();
    }
    Request get(int i) {
        return que.get(i);
    }
}
class Scheduler{
    protected ReqQue que;//请求队列
    protected Iterator<Request> ite;//请求队列指针
    protected Floor [] floors;//有10层楼
    protected ALSElevator elv;//只有一部电梯
    protected Request Reqtmp;//临时请求
    protected int upDown;
    protected int first;//指出是不是第一条指令
    protected long preTime;//指出之前一条指令的时刻
    Scheduler () {//调度器需要初始化

    }
    int checkFloor(String type,String floor,String Direction,String time) {//检查是不是楼层请求
        try {
            if (type.equals("FR") && (Integer.parseInt(floor)<11 && Integer.parseInt(floor)>0)
                    && (Direction.equals("UP")||Direction.equals("DOWN")) && (Long.parseLong(time)>=0
                    && Long.parseLong(time)<=Integer.MAX_VALUE) ) {
                if (first==1 && !(type.equals("FR") && Long.parseLong(time)==0
                        && Integer.parseInt(floor)==1 && Direction.equals("UP")))
                    return 0;//第一条指令时刻不是0，非法
                if (Long.parseLong(time)<preTime) return 0;//时间乱序，非法
                upDown = Direction.equals("UP")? 1:0;
                //一个楼层按钮同一时刻只能发出一个上行或下行请求
                if ((Integer.parseInt(floor)==1 && Direction.equals("DOWN"))//第1层和第10层的错误请求方向，非法
                        ||(Integer.parseInt(floor)==10 && Direction.equals("UP"))) return 0;
                first = 0;
                preTime = Long.parseLong(time);
                Reqtmp = new Request(type,Integer.parseInt(floor),upDown,Long.parseLong(time));
                que.addReq(Reqtmp);
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }
    int checkElevator(String type,String floor,String time) {//检查是不是电梯请求
        try {
            if (type.equals("ER") && (Integer.parseInt(floor)<11 && Integer.parseInt(floor)>0)
                    && (Long.parseLong(time)>=0 && Long.parseLong(time)<=Integer.MAX_VALUE) ) {
                if (first==1)//如果ER是第一条指令，非法，必须是(FR,1,UP,0)
                    return 0;
                if (Long.parseLong(time)<preTime) return 0;//时间乱序，非法
                first = 0;
                preTime = Long.parseLong(time);
                Reqtmp = new Request(type,Integer.parseInt(floor),Long.parseLong(time));
                que.addReq(Reqtmp);
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
class Request{
    private String _type;
    private int _floor;
    private int _upDown;//upDown=1表示上，upDown=0表示下
    private long _T;//时刻
    private int len;
    private int alreadyOut;//已经输出
    private int duplicatedOut;//重复输出
    private int invalidOut;//无效输出
    private int order;
    Request(String type,int floor,long T) {//电梯内请求
        this._type = type;
        this._floor = floor;
        this._T = T;
        len = 3;
    }
    Request(String type,int floor,int upDown,long T){//楼层请求
        this._type = type;
        this._floor = floor;
        this._upDown = upDown;
        this._T = T;
        len = 4;
    }
    int getOrder() {return order;}
    void setOrder(int o) {order = o;}
    int getLen() {//返回请求长度，相当于返回请求种类
        return len;
    }
    int getFloor() {
        return this._floor;
    }
    long getTime() {
        return this._T;
    }
    int getUpDown() {
        return _upDown;
    }
    int getAlreadyOut() {return alreadyOut;}
    void setAlreadyOut(int x) {alreadyOut = x;}
    int getDuplicatedOut() {return duplicatedOut;}
    void setDuplicatedOut(int x) {duplicatedOut = x;}
    int getInvalidOut() {return invalidOut;}
    void setInvalidOut(int x) {invalidOut = x;}
    String getType() {return _type;}
    public String toString() {
        if (len==4) {
            return "["+_type+","+_floor+","+(_upDown==1?"UP":"DOWN")+","+_T+"]";
        } else return "["+_type+","+_floor+","+_T+"]";
    }
}
class ALSElevator implements movingMachine{
    private int curPos;//当前位置
    private int sta;//当前状态，1表示UP,-1表示DOWN,0表示STILL
    private double curTime;//当前时间
    private double comTime;
    private String lastReq;//上一条请求
    ALSElevator() {
        curPos = 1;
    }
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.0");
        if (sta!=0)
            return lastReq+"/("+curPos+","+(sta==1? "UP":"DOWN")+","+df.format(curTime)+")";
        else return lastReq+"/("+curPos+",STILL,"+df.format(comTime)+")";
    }
    public void update(Request r) {
        if (r.getAlreadyOut()==1) {//对于要输出的指令，我们更新电梯状态
            curTime = Math.abs(r.getFloor() - curPos) * 0.5 + comTime;
            comTime = curTime + 1;
            sta = r.getFloor() > curPos ? 1 : r.getFloor() < curPos ? -1 : 0;
            curPos = r.getFloor();
        }//否则，就是重复输出的指令，我们不更新电梯状态
        if (r.getLen()==4) {
            lastReq = "["+r.getType()+","+r.getFloor()+","+(r.getUpDown()==1? "UP":"DOWN")+","+r.getTime()+"]";
        } else {
            lastReq = "["+r.getType()+","+r.getFloor()+","+r.getTime()+"]";
        }
    }
    public int getCurPos() {
        return curPos;
    }
    public double getComTime() {
        return comTime;
    }
    public int getState(Request r) {//获取当前状态
        if (curPos<r.getFloor() && r.getFloor()<=10) {
            sta = 1;
        }else if (curPos>r.getFloor() && r.getFloor()>=1) {
            sta = -1;
        } else {
            sta = 0;
        }
        return sta;
    }
    public Request getLastReq() {
        if (lastReq==null) return null;
        else {
            String[] ss = lastReq.split("[\\]\\[\\,]");
            if (ss.length==4) {
                return new Request(ss[1],Integer.parseInt(ss[2]),Long.parseLong(ss[3]));
            }else {
                return new Request(ss[1],Integer.parseInt(ss[2]),(ss[3].equals("UP")? 1:0),Long.parseLong(ss[4]));
            }
        }
    }
    public boolean updateComTime(double t) {
        if (t>comTime) {
            comTime = t;
            return true;
        }
        return false;
    }


}
class ALSScheduler extends Scheduler{
    private PriorityQueue<Request> incPriQue;
    private PriorityQueue<Request> decPriQue;
    ALSScheduler() {
        que = new ReqQue();
        elv = new ALSElevator();
        first=1;
        incPriQue = new PriorityQueue<>(new Comparator<Request>() {//递增优先队列
            @Override
            public int compare(Request o1, Request o2) {
                if (o1.getFloor()!=o2.getFloor()) return o1.getFloor()-o2.getFloor();
                else return o1.getOrder()-o2.getOrder();
            }
        });
        decPriQue = new PriorityQueue<>(new Comparator<Request>() {//递降优先队列
            @Override
            public int compare(Request o1, Request o2) {
                if (o1.getFloor()!=o2.getFloor()) return o2.getFloor()-o1.getFloor();
                else return o1.getOrder()-o2.getOrder();
            }
        });
    }
    void compute() {//计算并输出
        for (int i=0;i<que.size();i++) {//逐个遍历请求列表
            Reqtmp = que.get(i);
            if (Reqtmp.getAlreadyOut()==0 && Reqtmp.getDuplicatedOut()==0 && Reqtmp.getInvalidOut()==0) {//请求未输出
                Reqtmp.setOrder(i);
                boolean timeUpdated = elv.updateComTime(Reqtmp.getTime());//用主指令的时间更新电梯的完成时间
                upDown = elv.getState(Reqtmp);
                if (upDown!=0) {//方向为UP/DOWN
                    Reqtmp.setAlreadyOut(1);
                    if(upDown==1) incPriQue.offer(Reqtmp);
                    else decPriQue.offer(Reqtmp);
                    PriorityQueue<Request> PriQue = upDown==1?incPriQue:decPriQue;
                    for (int j = i + 1; j < que.size(); j++) {
                        Request rj = que.get(j);
                        if (rj.getAlreadyOut()==1 ||rj.getDuplicatedOut()==1
                                || rj.getInvalidOut()==1) continue;
                        rj.setOrder(j);
                        Queue<Request> sameReq = new LinkedList<>();
                        boolean sameDir = upDown==1?rj.getFloor()>elv.getCurPos():
                                                     rj.getFloor()<elv.getCurPos();//方向相同
                        boolean FRWarn = false;//注意楼层请求
                        int maxFloor=0;//最高楼层
                        int minFloor = 11;//最底层
                        //计算响应时间resTime
                        double resTime = Math.abs(rj.getFloor()-elv.getCurPos())*0.5
                                +elv.getComTime();
                        for (Request reqPri:PriQue) {
                            if (maxFloor<reqPri.getFloor()) maxFloor = reqPri.getFloor();
                            if (minFloor>reqPri.getFloor()) minFloor = reqPri.getFloor();
                            if (upDown==1 && reqPri.getFloor()<rj.getFloor()) resTime+=1;
                            if (upDown==-1 && reqPri.getFloor()>rj.getFloor()) resTime+=1;
                            if (reqPri.getFloor()==rj.getFloor()) sameReq.offer(reqPri);
                        }
                        if (rj.getType().equals("ER") ||
                                (upDown==1 && rj.getType().equals("FR") && rj.getUpDown()==1 && rj.getFloor()<=maxFloor) ||
                                (upDown==-1 && rj.getType().equals("FR") && rj.getUpDown()==0 && rj.getFloor()>=minFloor)) {
                            FRWarn = true;
                        }
                        double arrTime = 0;//检测主请求到达时间，如果到达时间<=请求产生时间，那么不可能捎带
                        for (Request reqPri:PriQue) {
                            if (upDown==1 && reqPri.getFloor()<maxFloor) arrTime+=1;
                            if (upDown==-1 && reqPri.getFloor()>minFloor) arrTime+=1;
                        }
                        arrTime = upDown==1? arrTime+Math.abs(maxFloor-elv.getCurPos())*0.5+elv.getComTime():
                                arrTime+Math.abs(minFloor-elv.getCurPos())*0.5+elv.getComTime();
                        if (resTime>rj.getTime() && sameDir && FRWarn && arrTime>rj.getTime()) {//可以捎带请求
                            if (sameReq.size()==0) {
                                rj.setAlreadyOut(1);
                                PriQue.offer(rj);
                                //重新从前往后访问找到所有可以被加入优先队列的请求
                                for (int k=i+1;k<j;k++) {
                                    Request rk = que.get(k);
                                    if (rk.getAlreadyOut()==1 ||rk.getDuplicatedOut()==1 || rk.getInvalidOut()==1) continue;
                                    rk.setOrder(k);
                                    if (checkPickUp(rk,PriQue,upDown)) {
                                        rk.setAlreadyOut(1);
                                        PriQue.offer(rk);
                                    }
                                }
                            } else {
                                boolean isInvalid = false;
                                for (Request rtp:sameReq) {//检查是否存在重复请求或者无效请求
                                    if (checkBulb(rtp,rj)) {
                                        isInvalid = true;
                                        rj.setInvalidOut(1);
                                        PriQue.offer(rj);
                                        break;
                                    }
                                }
                                if (!isInvalid) {
                                    rj.setDuplicatedOut(1);
                                    PriQue.offer(rj);
                                }
                            }

                        } else if (rj.getTime()<=resTime+1) {//不可捎带，但可能无效
                            for (Request rtp:sameReq) {
                                if (checkBulb(rtp,rj)) {
                                    rj.setInvalidOut(1);
                                    PriQue.offer(rj);
                                    break;
                                }
                            }
                        }

                        sameReq.clear();
                    }
                    while (!PriQue.isEmpty()) {
                        if (PriQue.peek().getInvalidOut()!=1) {
                            elv.update(PriQue.poll());
                            System.out.println(elv.toString());
                        } else {
                            System.out.println("SAME "+PriQue.poll().toString());//无效输入，不输出结果，只输出请求
                        }
                    }
                } else {//方向为STILL
                    if (timeUpdated) {
                        Reqtmp.setAlreadyOut(1);
                    } else {//这意味着请求的时间≤电梯完成时间
                        Request r = elv.getLastReq();
                        if (checkBulb(r,Reqtmp)) {
                            Reqtmp.setInvalidOut(1);
                        } else {
                            Reqtmp.setAlreadyOut(1);
                        }
                    }
                    Queue<Request> queSTILL = new LinkedList<>();
                    queSTILL.offer(Reqtmp);
                    for (int j = i + 1; j < que.size(); j++) {
                        Request rj = que.get(j);
                        if (rj.getFloor()==Reqtmp.getFloor()) {
                            if (elv.getComTime()>=rj.getTime()) {
                                for (Request req:queSTILL) {
                                    if (checkBulb(req, rj)) {
                                        rj.setInvalidOut(1);
                                        queSTILL.offer(rj);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    while (!queSTILL.isEmpty()) {
                        if (queSTILL.peek().getInvalidOut()!=1) {
                            elv.update(queSTILL.poll());
                            System.out.println(elv.toString());
                        } else {
                            System.out.println("SAME "+queSTILL.poll().toString());//无效输入，不输出结果，只输出请求
                        }
                    }
                }
            }
        }
    }
    private boolean checkBulb(Request r1,Request r2) {//检测是否存在重复灯光
        if (r1==null || r2==null) return false;
        if (r1.getType().equals("FR") && r2.getType().equals("FR")) {//都是楼层灯
            if (r1.getUpDown()==r2.getUpDown() && r1.getFloor()==r2.getFloor()) return true;
        } else if (r1.getType().equals("ER") && r2.getType().equals("ER")) {//都是电梯灯
            if (r1.getFloor()==r2.getFloor()) return true;
        }
        return false;
    }
    private boolean checkPickUp(Request r,PriorityQueue<Request> q,int upDown) {//检查请求r能否加入优先队列q中
        Queue<Request> sameReq = new LinkedList<>();
        boolean sameDir = upDown==1?r.getFloor()>elv.getCurPos():
                r.getFloor()<elv.getCurPos();//方向相同
        boolean FRWarn = false;//注意楼层请求
        int maxFloor=0;//最高楼层
        int minFloor = 11;//最底层
        //计算响应时间resTime
        double resTime = Math.abs(r.getFloor()-elv.getCurPos())*0.5
                +elv.getComTime();
        for (Request reqPri:q) {
            if (maxFloor<reqPri.getFloor()) maxFloor = reqPri.getFloor();
            if (minFloor>reqPri.getFloor()) minFloor = reqPri.getFloor();
            if (upDown==1 && reqPri.getFloor()<r.getFloor()) resTime+=1;
            if (upDown==-1 && reqPri.getFloor()>r.getFloor()) resTime+=1;
            if (reqPri.getFloor()==r.getFloor()) sameReq.offer(reqPri);
        }
        if (r.getType().equals("ER") ||
                (upDown==1 && r.getType().equals("FR") && r.getUpDown()==1 && r.getFloor()<=maxFloor) ||
                (upDown==-1 && r.getType().equals("FR") && r.getUpDown()==0 && r.getFloor()>=minFloor)) {
            FRWarn = true;
        }
        return resTime>r.getTime() && sameDir && FRWarn && sameReq.size()==0;
    }
}
interface movingMachine{
    abstract void update(Request r);
    abstract int getCurPos();
    abstract double getComTime();
    abstract int getState(Request r);
}
