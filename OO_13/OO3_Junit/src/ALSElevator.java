import java.text.DecimalFormat;

class ALSElevator implements movingMachine{
    private int curPos;//当前位置
    private int sta;//当前状态，1表示UP,-1表示DOWN,0表示STILL
    private double curTime;//当前时间
    private double comTime;
    private String lastReq;//上一条请求

    ALSElevator() {
        /**
         * @MODIFIES:\this.curPos;
         * @EFFECTS:
         *      \this.curPos == 1;
         */
        curPos = 1;
    }

    public boolean repOK() {
        /**
         * @invariant:
         *      (1<=curPos) && (curPos<=10) &&
         *      ((sta==1) || (sta==-1) || (sta==0)) &&
         *      (curTime>=0) && (comTime>=0);
         * @EFFECTS:
         *      \result==(1<=curPos) && (curPos<=10) &&
         *      ((sta==1) || (sta==-1) || (sta==0)) &&
         *      (curTime>=0) && (comTime>=0);
         */
        return (1<=curPos) && (curPos<=10) &&
                ((sta==1) || (sta==-1) || (sta==0)) &&
                (curTime>=0) && (comTime>=0);
    }

    public String toString() {
        /**
         * @EFFECTS:
         *      (sta==1) ==> \result==lastReq+"/("+curPos+",UP,"+df.format(curTime)+")";
         *      (sta==-1) ==> \result==lastReq+"/("+curPos+",DOWN,"+df.format(curTime)+")";
         *      (sta==0) ==> \result==lastReq+"/("+curPos+",STILL,"+df.format(comTime)+")";
         */
        DecimalFormat df = new DecimalFormat("#.0");
        if (sta!=0)
            return lastReq+"/("+curPos+","+(sta==1? "UP":"DOWN")+","+df.format(curTime)+")";
        else return lastReq+"/("+curPos+",STILL,"+df.format(comTime)+")";
    }

    public void update(Request r) {
        /**
         * @REQUIRES:r!=null;
         * @MODIFIES:
         *      \this.curTime;\this.comTime;\this.sta;\this.curPos;\this.lastReq;
         * @EFFECTS:
         *      (r.getAlreadyOut()==1) ==> (curTime == Math.abs(r.getFloor() - curPos) * 0.5 + comTime) &&
         *                                 (comTime = curTime+1) && (r.getFloor() < curPos ==> sta==-1) &&
         *                                 (r.getFloor() > curPos ==> sta==1) && (r.getFloor()==curPos ==> sta==0);
         *                                 (curPos = r.getFloor());
         *      (r.getLen()==4) ==> (lastReq == "["+r.getType()+","+r.getFloor()+","+(r.getUpDown()==1? "UP":"DOWN")+","+r.getTime()+"]")
         *      (r.getLen()!=4) ==> lastReq == "["+r.getType()+","+r.getFloor()+","+r.getTime()+"]"
         */
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
        /**
         * @EFFECTS:
         *      \result==curPos;
         */
        return curPos;
    }

    public double getComTime() {
        /**
         * @EFFECTS:
         *      \result==comTime;
         */
        return comTime;
    }

    public int getState(Request r) {//获取当前状态
        /**
         * @REQUIRES:r!=null;
         * @MODIFIES:\this.sta;
         * @EFFECTS:
         *      (curPos<r.getFloor() && r.getFloor()<=10) ==> sta==1;
         *      (curPos>r.getFloor() && r.getFloor()>=1) ==> sta==-1;
         *      (curPos>=r.getFloor() || r.getFloor()>10) && (curPos<=r.getFloor() || r.getFloor()<1) ==> sta==0;
         *      \result==sta;
         */
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
        /**
         * @EFFECTS:
         *      (\this.lastReq==null) ==> \result==null;
         *      (\this.lastReq!=null && lastReq.split("[\\]\\[\\,]").length==4) ==>
         *          \result==new Request(ss[1],Integer.parseInt(ss[2]),Long.parseLong(ss[3]));
         *      (\this.lastReq!=null && lastReq.split("[\\]\\[\\,]").length!=4) ==>
         *          \result==new Request(ss[1],Integer.parseInt(ss[2]),(ss[3].equals("UP")? 1:0),Long.parseLong(ss[4]));
         */
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
        /**
         * @MODIFIES:
         *      \this.comTime;
         * @EFFECTS:
         *      (t>comTime) ==> (comTime == t && \result==true);
         *      (t<=comTime) ==> (\result==false);
         */
        if (t>comTime) {
            comTime = t;
            return true;
        }
        return false;
    }
}
