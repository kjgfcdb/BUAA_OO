import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

class ALSScheduler extends Scheduler{
    /**
     * Overview:ALS调度器类，用于实现电梯调度功能
     * AF(c) = {que,elv,Reqtmp,upDown,first,preTime,incPriQue,decPriQue}
     *      where c.que = que,c.elv = elv,c.Reqtmp = Reqtmp,c.upDown =
     *      upDown,c.first =first,c.preTime = preTime,c.incPriQue = incPriQue,
     *      c.decPriQue = decPriQue;
     */
    public PriorityQueue<Request> incPriQue;
    public PriorityQueue<Request> decPriQue;

    ALSScheduler() {
        /**
         * @MODIFIES:\this.incPriQue;\this.decPriQue;\this.que;\this.elv;\this.first;
         * @EFFECTS:
         *      \this.incPriQue == new PriorityQueue();
         *      \this.decPriQue == new PriorityQueue();
         *      \this.que == new ReqQue();
         *      \this.elv == new ALSElevator();
         *      \this.first == 1;
         */
        super();
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

    public boolean repOK() {
        /**
         * @invariant:
         *      super.repOK() && incPriQue!=null && decPriQue!=null;
         * @EFFECTS:
         *      \result==super.repOK() && incPriQue!=null && decPriQue!=null;
         */
        return super.repOK() && incPriQue!=null && decPriQue!=null;
    }

    void compute() {//计算并输出
        /**
         * @MODIFIES:
         *      \this.Reqtmp;\this.elv;\this.upDown;\this.incPriQue;\this.decPriQue;
         * @EFFECTS:
         *      若que不为空，进行电梯调度
         *      若que为空，则返回
         */
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
                        if (rj.getAlreadyOut()==1 ||
                        		rj.getDuplicatedOut()==1 ||
                                rj.getInvalidOut()==1) continue; 
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
                        ArrayList<Integer> floors = new ArrayList<>();
                        for (Request reqPri:PriQue) {
                            if (maxFloor<reqPri.getFloor()) maxFloor = reqPri.getFloor();
                            if (minFloor>reqPri.getFloor()) minFloor = reqPri.getFloor();
                            if (upDown==1 && reqPri.getFloor()<rj.getFloor()) {
                                if (floors.stream().noneMatch(floor->floor==reqPri.getFloor())) {
                                    resTime+=1;
                                    floors.add(reqPri.getFloor());
                                }
                            }
                            if (upDown==-1 && reqPri.getFloor()>rj.getFloor()) {
                                if (floors.stream().noneMatch(floor->floor==reqPri.getFloor())) {
                                    resTime+=1;
                                    floors.add(reqPri.getFloor());
                                }
                            }
                            if (reqPri.getFloor()==rj.getFloor()) sameReq.offer(reqPri);
                        }
                        if (rj.getType().equals("ER") ||
                                (upDown==1 && rj.getUpDown()==1 && rj.getFloor()<=maxFloor) ||
                                (upDown==-1 && rj.getUpDown()==0 &&rj.getFloor()>=minFloor)) {
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
                                    if (rk.getAlreadyOut()==1 ||
                                    		rk.getDuplicatedOut()==1 ||
                                    		rk.getInvalidOut()==1) continue;
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
                            boolean flag = false;
                            for (Request rtp:sameReq) {
                                if (checkBulb(rtp,rj)) {
                                    rj.setInvalidOut(1);
                                    if (!flag) {
                                        PriQue.offer(rj);
                                        flag = true;
                                    }
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
                    BlockingQueue<Request> queSTILL = new LinkedBlockingDeque<>();
                    queSTILL.offer(Reqtmp);
                    for (int j = i + 1; j < que.size(); j++) {
                        Request rj = que.get(j);
                        if (rj.getFloor()==Reqtmp.getFloor()) {
                            if (elv.getComTime()>=rj.getTime()) {
                                boolean tot = false;
                                for (Request req:queSTILL) {
                                    if (checkBulb(req, rj) && !tot) {
                                        rj.setInvalidOut(1);
                                        queSTILL.offer(rj);
                                        tot = true;
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

    boolean checkBulb(Request r1,Request r2) {//检测是否存在重复灯光
        /**
         * @EFFECTS:
         *      (r1==null || r2==null) ==> \result==false;
         *      (r1.getType().equals("FR") && r2.getType().equals("FR") &&
         *          r1.getUpDown()==r2.getUpDown() && r1.getFloor()==r2.getFloor()) ==> \result==true;
         *      (r1.getType().equals("ER") && r2.getType().equals("ER") && r1.getFloor()==r2.getFloor()) ==> \result==true;
         *      other conditions ==> \result==false;
         */
        if (r1==null || r2==null) return false;
        if (r1.getType().equals("FR") && r2.getType().equals("FR")) {//都是楼层灯
            if (r1.getUpDown()==r2.getUpDown() && 
            		r1.getFloor()==r2.getFloor()) return true;
        } else if (r1.getType().equals("ER") && r2.getType().equals("ER")) {//都是电梯灯
            if (r1.getFloor()==r2.getFloor()) return true;
        }
        return false;
    }

    boolean checkPickUp(Request r,PriorityQueue<Request> q,int upDown) {//检查请求r能否加入优先队列q中
        /**
         * @REQUIRES:r!=null;q!=null;
         * @MODIFIES:None;
         * @EFFECTS:
         *      如果请求r能够被加入当前优先队列中，则返回true，否则返回false;
         */
        if (r==null || q==null) return false;
        Queue<Request> sameReq = new LinkedList<>();
        boolean sameDir = false;
        if (upDown==1) {
        	sameDir = r.getFloor()>elv.getCurPos(); 
        } else {
        	sameDir = r.getFloor()<elv.getCurPos();
        }
        boolean FRWarn = false;//注意楼层请求
        int maxFloor=0;//最高楼层
        int minFloor = 11;//最底层
        //计算响应时间resTime
        double resTime = Math.abs(r.getFloor()-elv.getCurPos())*0.5
                +elv.getComTime();
        for (Request reqPri:q) {
            if (maxFloor<reqPri.getFloor())
                maxFloor = reqPri.getFloor();
            if (minFloor>reqPri.getFloor())
                minFloor = reqPri.getFloor();
            if (upDown==1 && reqPri.getFloor()<r.getFloor())
                resTime+=1;
            if (upDown==-1 && reqPri.getFloor()>r.getFloor())
                resTime+=1;
            if (reqPri.getFloor()==r.getFloor())
                sameReq.offer(reqPri);
        }
        if (r.getType().equals("ER") ||
                (upDown==1 && r.getUpDown()==1 && r.getFloor()<=maxFloor) ||
                (upDown==-1 && 
                r.getUpDown()==0 && 
                r.getFloor()>=minFloor)) {
            FRWarn = true;
        }
        return resTime>r.getTime() && 
        		sameDir && 
        		FRWarn && 
        		sameReq.size()==0;
    }
}
