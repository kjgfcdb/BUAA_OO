import java.util.*;
import java.util.regex.*;
public class ElevatorSchedule {
    public static void main(String [] args) {
        Scanner input = new Scanner(System.in);
        Scheduler sch = new Scheduler();
        Pattern p1 = Pattern.compile("^\\(FR\\,\\d{1,2}\\,UP\\,\\d{1,20}\\)$");
        Pattern p2 = Pattern.compile("^\\(FR\\,\\d{1,2}\\,DOWN\\,\\d{1,20}\\)$");
        Pattern p3 = Pattern.compile("^\\(ER\\,\\d{1,2}\\,\\d{1,20}\\)$");
        Matcher m1,m2,m3;
        int instrNum = 0;
        try {
            while (true) {
                String s = input.nextLine();
                s = s.replaceAll(" ", "");
                if (s.equals("q") || instrNum==100000)//超出100000条指令即自动截断
                    break;
                instrNum++;
                m1 = p1.matcher(s);//强行匹配，匹配不到就算错
                m2 = p2.matcher(s);
                m3 = p3.matcher(s);
                boolean hasfound = (m1.find()||m2.find()||m3.find());
                String[] strs = s.split("[\\,\\(\\)]");
                if (hasfound && strs.length == 5 && strs[0].equals("")) {//可能是楼层命令
                    if (sch.checkFloor(strs[1], strs[2], strs[3], strs[4]) == 0)
                        System.out.printf("Line %d : Illegal Input\n",instrNum);

                } else if (hasfound && strs.length == 4 && strs[0].equals("")) {//可能是电梯命令
                    if (sch.checkElevator(strs[1], strs[2], strs[3]) == 0) {
                        System.out.printf("Line %d : Illegal Input\n",instrNum);
                    }
                } else {
                    System.out.printf("Line %d : Illegal Input\n",instrNum);
                }

            }
            sch.compute();
        } catch (Exception e) {
            System.out.println("Terrible Input");
        }
        input.close();
        System.exit(0);
    }
}
class Elevator{
    private int pos;//当前位置
    private int direction;//运行方向,1表示向上，0表示向下
    private double arrTime;//到达时间
    private double comTime;//完成时间
    private Queue<BulbState> bulbStates;
    private BulbState bulbtmp;
    private int [] bulbs;//10个电梯灯
    Elevator() {
        bulbs = new int [11];
        bulbStates = new LinkedList<BulbState>();
        this.pos = 1;//电梯在1层
    }

    void update(int floor,long time){
        //本次命令的抵达时间和完成时间
        if (time<=comTime)//如果当前动作没有完成，那么抵达时间只能从完成时间算起
            arrTime = abs(floor - pos) * 0.5 + comTime;//加上完成时间
        else//否则，抵达时间从命令输入时间算起
            arrTime = abs(floor-pos)*0.5+time;

        comTime = arrTime + 1;
        direction = floor>=pos? 1:0;

        if (floor==pos)
            System.out.printf("(%d,STILL,%.1f)\n",floor,comTime);//位置没有改变，输出STILL
        else
            System.out.printf("(%d,%s,%.1f)\n",floor,direction==1? "UP":"DOWN",arrTime);//位置改变，输出UP/DOWN

        pos = floor;
    }
    int scanBulbQueue(long Time) {//扫描灯泡队列，把过时的灯全关掉
        if (bulbStates.size()!=0) {
            bulbtmp = bulbStates.peek();
            if (bulbtmp.getTime()<Time) {
                int floor = bulbtmp.getFloor();
                int flag = bulbtmp.getFlag();
                bulbStates.poll();
                return floor*10+flag;
            }
        }
        return -1;
    }
    void addBulbQueue(int floor,int flag) {
        bulbtmp = new BulbState(floor,comTime,flag);
        bulbStates.offer(bulbtmp);
    }
    boolean isOn(int i) {
        return bulbs[i]==1;
    }
    void turnOn(int i) {
        bulbs[i] = 1;
    }
    void turnOff(int i) {
        bulbs[i] = 0;
    }

    private int abs(int i) {
        return i>0? i:-i;
    }
}
class Floor{
    private int up;
    private int down;
    boolean isOn(int upDown) {
        if (upDown==1) return up==1;
        else return down==1;
    }
    void turnOn(int upDown) {
        if (upDown==1) up = 1;
        else down = 1;
    }
    void turnOff(int upDown) {//关灯
        if (upDown==1) up = 0;
        else down = 0;
    }

}
class ReqQue{
    private ArrayList<Request> que;
    ReqQue() {
        que = new ArrayList<Request>();
    }
    void addReq(Request r) {
        que.add(r);
    }
    Iterator<Request> getIte() {
        return que.iterator();
    }
}
class Scheduler{
    private ReqQue que;//请求队列
    private Iterator<Request> ite;//请求队列指针
    private Floor [] floors;//有10层楼
    private Elevator elv;//只有一部电梯
    private Request Reqtmp;//临时请求
    private int upDown;
    private int first;//指出是不是第一条指令
    private long preTime;//指出之前一条指令的时刻
    Scheduler () {//调度器需要初始化
        que = new ReqQue();//请求队列
        floors = new Floor[11];//楼层数组
        for (int index = 0;index<=10;index++) {//产生10个楼层
            floors[index] = new Floor();
        }
        elv = new Elevator();

        first=1;
    }
    int checkFloor(String type,String floor,String Direction,String time) {//检查是不是楼层请求
        try {
            if (type.equals("FR") && (Integer.parseInt(floor)<11 && Integer.parseInt(floor)>0)
                    && (Direction.equals("UP")||Direction.equals("DOWN")) && (Long.parseLong(time)>=0
                    && Long.parseLong(time)<=4294967295L) ) {
                if (first==1 && Long.parseLong(time)!=0)
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
                    && (Long.parseLong(time)>=0 && Long.parseLong(time)<=4294967295L) ) {
                if (first==1 && Long.parseLong(time)!=0)
                    return 0;//第一条指令时刻不是0，非法
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
    void compute() {//计算并输出
        //逐个遍历请求列表
        ite = que.getIte();
        while (ite.hasNext()) {
            Reqtmp= ite.next();//下一条请求
            while (true) {//不断地关灯
                int floor_flag = elv.scanBulbQueue(Reqtmp.getTime());
                if (floor_flag==-1) break;
                if (floor_flag%10==0)//关闭向下灯
                    floors[floor_flag/10].turnOff(0);
                else if (floor_flag%10==1)//关闭向上灯
                    floors[floor_flag/10].turnOff(1);
                else elv.turnOff(floor_flag/10);//关闭电梯灯
            }
            if (Reqtmp.getLen()==4) {//楼层请求
                if (!floors[Reqtmp.getFloor()].isOn(Reqtmp.getUpDown())) {//如果要去的方向按钮没有被按下，那么更新电梯
                    elv.update(Reqtmp.getFloor(),Reqtmp.getTime());
                    floors[Reqtmp.getFloor()].turnOn(Reqtmp.getUpDown());
                    elv.addBulbQueue(Reqtmp.getFloor(),Reqtmp.getUpDown());
                }

            } else if (Reqtmp.getLen()==3){//电梯请求
                if (!elv.isOn(Reqtmp.getFloor())) {//如果要去的那个楼层的按钮没有被按下，那么更新电梯
                    elv.update(Reqtmp.getFloor(),Reqtmp.getTime());
                    elv.turnOn(Reqtmp.getFloor());//点灯
                    elv.addBulbQueue(Reqtmp.getFloor(),2);
                }
            }
        }
    }

}
class Request{
    private String _type;
    private int _floor;
    private int _upDown;//upDown=1表示上，upDown=0表示下
    private long _T;//时刻
    private int len;
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
}
class BulbState{//灯泡状态类，用来添加进灯泡状态队列
    private int floor;//楼层信息
    private double endtime;//灯泡到期时间
    private int flag;//flag=0表示楼层向下灯，flag=1表示楼层向上灯,flag=2表示电梯内灯
    BulbState(int f,double t,int flg) {
        floor = f;
        endtime = t;
        flag = flg;
    }
    int getFloor() {
        return floor;
    }
    double getTime() {
        return endtime;
    }
    int getFlag() {return flag;}
}
