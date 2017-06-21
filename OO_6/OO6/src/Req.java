class Req {
    private String filePath;
    private String trigger;
    private String task;
    Req(String filePath,String trigger,String task) {
        this.filePath = filePath;
        this.trigger = trigger;
        this.task = task;
    }
    //获取文件路径
    public synchronized String getFilePath() {
        return filePath;
    }
    //获取触发器类型
    public synchronized String getTrigger() {
        return trigger;
    }
    //获取任务类型
    public synchronized String getTask() {
        return task;
    }
    //判断两个请求是否相同
    public synchronized boolean isSame(Req r) {
        return this.filePath.equals(r.getFilePath()) &&
                this.trigger.equals(r.getTrigger()) &&
                this.task.equals(r.getTask());
    }
    //toString
    public synchronized String toString() {
        return "IF,"+filePath+","+trigger+",THEN,"+task;
    }
}
