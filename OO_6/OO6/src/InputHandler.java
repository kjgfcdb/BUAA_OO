import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;

class InputHandler {
    private static int FILENUM = 0;
    static boolean parseInput(String string, LinkedBlockingQueue<Req> reqs) {
        String[] strings = string.trim().split(",");
        if (strings.length!=5) return false;
        if (FILENUM>=8) {
            System.out.println("You can only monitor at most 8 files/folders.");
            return false;//工作区数量不得超过8个
        }
        if (strings[0].equals("IF") && strings[3].equals("THEN")) {
            if (parseDir(strings[1]) && parseTrigger(strings[2]) &&
                    parseTask(strings[4])) {
                //recover只能与重命名或者路径改变触发器结合
                if (strings[4].equals("recover") &&
                        (strings[2].equals("size-changed") || strings[2].equals("modified"))
                        )
                    return false;
                //去重
                Req curReq = new Req(new File(strings[1]).toString(),strings[2],strings[4]);
                boolean sameReq = false;
                boolean samePath = false;
                for (Req req:reqs) {
                    if (req.isSame(curReq))
                        sameReq = true;
                    if (req.getFilePath().equals(curReq.getFilePath()))
                        samePath = true;
                }
                if (!samePath) FILENUM++;
                if (!sameReq) reqs.offer(curReq);
                return true;
            }
        }
        return false;
    }

    static boolean parseDir(String dir) {//分析文件名或者目录
        try {
            File temp = new File(dir);
            if (!temp.exists()) System.out.println("File/Folder doesn't exist!");
            return temp.exists();//大小写不区分,这是操作系统的锅
        } catch (Exception e) {
            return false;
        }
    }

    static boolean parseTrigger(String trigger) {//分析触发器
        //支持大小写混用
        trigger = trigger.toLowerCase();
        if (trigger.equals("renamed") ||
                trigger.equals("modified")||
                trigger.equals("path-changed")||
                trigger.equals("size-changed")) {
            return true;
        }
        return false;
    }

    static boolean isDirectory(String dir) {
        File file = new File(dir);
        return file.exists()&&file.isDirectory();
    }
    static boolean parseTask(String task) {//分析任务
        //支持大小写混用
        task = task.toLowerCase();
        return (task.equals("record-summary")||
                task.equals("record-detail")||
                task.equals("recover"));
    }

}
