class Scheduler{
    /**
     * Overview:调度器类，用于实现调度功能
     * AF(c) = {que,elv,Reqtmp,upDown,first,preTime} where c.que = que,
     *      c.elv = elv,c.Reqtmp = Reqtmp,c.upDown = upDown,c.first =
     *      first,c.preTime = preTime;
     */
    public ReqQue que;//请求队列
    public ALSElevator elv;//只有一部电梯
    public Request Reqtmp;//临时请求
    public int upDown;
    public int first;//指出是不是第一条指令
    public long preTime;//指出之前一条指令的时刻
    Scheduler () {//调度器需要初始化
        /**
         * @MODIFIES:\this.que;\this.elv;\this.first;
         * @EFFECTS:;
         *      \this.que == new ReqQue;\this.elv == new ALSElevator();
         *      \this.first == 1;
         */
        que = new ReqQue();
        elv = new ALSElevator();
        first=1;
    }

    public boolean repOK() {
        /**
         * @invariant:
         *      (que!=null) && (elv!=null) &&
         *      (upDown==1 || upDown==0 || upDown==-1) &&
         *      (first==1 || first==0) && preTime>=0;
         * @EFFECTS:
         *      \result==(que!=null) && (elv!=null) &&
         *      (upDown==1 || upDown==0 || upDown==-1) &&
         *      (first==1 || first==0) && preTime>=0;
         */ 
        return (que!=null) && (elv!=null) && (upDown==1 || upDown==0 || upDown==-1) &&
                (first==1 || first==0) && preTime>=0;
    }

    int checkFloor(String type,String floor,String Direction,String time) {//检查是不是楼层请求
        /**
         * @REQUIRES:
         *      type!=null && floor !=null && Direction!=null && time!=null;
         * @MODIFIES:
         *      \this.first;\this.preTime;\this.que;
         *      \this.Reqtmp;\this.upDown;
         * @EFFECTS:
         *      如果输入的四个字符串满足一条合法楼层指令的要求，即标志位为"FR"，楼层在[1,10]之间，方向为UP或者DOWN，
         *      输入时间在[0,Integer.MAX_VALUE]之间，且不为第一条指令或者为第一条指令但是为
         *      [FR,1,UUP,0]，且时间不早于前一条指令时间，且楼层满足在1楼的没有向下，在10楼的没有
         *      向上，则其被添加到que，并且修改preTime,Reqtmp,first，upDown，最终返回1，否则返
         *      回0.中途若有异常，捕获之后返回0.
         */
        try {
            if (type.equals("FR") && (Integer.parseInt(floor)<11 && Integer.parseInt(floor)>0)
                    && (Direction.equals("UP")||Direction.equals("DOWN")) && (Long.parseLong(time)>=0
                    && Long.parseLong(time)<=Integer.MAX_VALUE) ) { 
                if (first==1) { 
                	if (!(Long.parseLong(time)==0 && Integer.parseInt(floor)==1 && Direction.equals("UP")))
                	return 0;//第一条指令时刻不是0，非法
                }
                        
                if (Long.parseLong(time)<preTime) return 0;//时间乱序，非法
                upDown = Direction.equals("UP")? 1:0; 
                //一个楼层按钮同一时刻只能发出一个上行或下行请求
                if (Integer.parseInt(floor)==1 && Direction.equals("DOWN")) return 0;//第1层和第10层的错误请求方向，非法
                if (Integer.parseInt(floor)==10 && Direction.equals("UP")) return 0;
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
        /**
         * @REQUIRES:
         *      type!=null && floor !=null && time!=null;
         * @MODIFIES:
         *      \this.first;\this.preTime;\this.que;
         *      \this.Reqtmp;
         * @EFFECTS:
         *      如果输入的三个字符串满足一条合法电梯指令的要求，即标志位为"ER"，楼层在[1,10]之间，
         *      输入时间在[0,Integer.MAX_VALUE]之间，且不为第一条指令，且时间不早于前一条指令时
         *      间，则其被添加到que，并且修改preTime,Reqtmp,first，最终返回1，否则返回0.中途若
         *      有异常，捕获之后返回0.
         */
        try {
            if (type.equals("ER") && (Integer.parseInt(floor)<11 && Integer.parseInt(floor)>0)
                    && (Long.parseLong(time)>=0 
                    && Long.parseLong(time)<=Integer.MAX_VALUE) ) {
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
