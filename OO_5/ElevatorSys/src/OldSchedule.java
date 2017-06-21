class OldSchedule {
    protected ReqQue Old_que;//请求队列
    protected Elv Old_elv;//只有一部电梯
    protected Req Old_req;//临时请求
    void FoolSchedule() {
        for (int i=0;i<Old_que.size();i++) {
        	while (!Old_elv.ifResponsive(Old_que.get(i))) {};
            Old_elv.fetch(Old_que.get(i));
        }
    }
    void ALSSchedule() {
        for (int i=0;i<Old_que.size();i++) {
        	while (!Old_elv.ifResponsive(Old_que.get(i))) {};
            Old_req = Old_que.get(i);
            Old_elv.fetch(Old_req);
            Old_que.remove(Old_req);
            for (int j=i+1;j< Old_que.size();j++) {
                if (Old_elv.ifCarriable(Old_que.get(j))) {
                    Old_elv.fetch(Old_que.get(j));
                    Old_que.remove(Old_que.get(j));
                }
            }
        }
    }
}
