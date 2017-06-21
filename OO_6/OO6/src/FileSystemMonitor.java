import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

//对重命名和移动的观测只涉及文件而不涉及文件夹
public class FileSystemMonitor implements Runnable {
    protected Thread t;
    protected ObjFile objFile;
    protected ObjFile workDirectory;
    protected static final int SLEEPTIME = 3000;
    protected boolean[][] mode;
    protected LinkedList<FileMap> renamed = new LinkedList<>();
    protected LinkedList<FileMap> pathChanged = new LinkedList<>();
    protected Summary summary;
    protected Detail detail;
    protected Recover recover;
    protected boolean isFile;
    protected LinkedBlockingQueue<ObjFile> deleteFiles = new LinkedBlockingQueue<>();
    protected LinkedBlockingQueue<ObjFile> newFiles = new LinkedBlockingQueue<>();
    protected LinkedBlockingQueue<ObjFile> modifiedFiles = new LinkedBlockingQueue<>();
    protected LinkedBlockingQueue<ObjFile> sizeChangedFiles = new LinkedBlockingQueue<>();
    FileSystemMonitor(Req req,Summary summary,Detail detail,Recover recover) {
        this.objFile = FileManager.getNewFile(req.getFilePath());//文件的就是文件，目录的监测对象就是目录
        this.isFile = objFile.isFile();
        if (isFile) {//如果是文件，工作目录就是父目录
            workDirectory = new ObjFile(objFile.getParent());
        } else {//如果是目录，工作目录就是本身
            workDirectory = objFile;
        }
        this.mode = new boolean[4][3];
        this.summary = summary;
        this.detail = detail;
        this.recover = recover;
        addTask(req);
        t = new Thread(this);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
    }
    FileSystemMonitor(String pathName,Summary summary,Detail detail,Recover recover) {
        this.objFile = FileManager.getNewFile(pathName);//文件的就是文件，目录的监测对象就是目录
        this.isFile = objFile.isFile();
        if (isFile) {//如果是文件，工作目录就是父目录
            workDirectory = FileManager.getNewFile(objFile.getParent());
        } else {//如果是目录，工作目录就是本身
            workDirectory = objFile;
        }
        this.mode = new boolean[4][3];
        this.summary = summary;
        this.detail = detail;
        this.recover = recover;
        t = new Thread(this);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
    }
    public void addTask(Req req) {
        int i = req.getTrigger().equals("renamed")? 0:
                req.getTrigger().equals("modified")?1:
                        req.getTrigger().equals("path-changed")?2:3;
        int j = req.getTask().equals("record-summary")?0:
                req.getTask().equals("record-detail")?1:2;
        mode[i][j] = true;
    }
    public void addTask(boolean[][] mode) {
    	for (int i=0;i<4;i++) {
    		for (int j=0;j<3;j++) 
    			this.mode[i][j] = mode[i][j];
    	}
    }
    public boolean hasSummary() {
        boolean ans = mode[0][0]||mode[1][0]||mode[2][0]||mode[3][0];
        return ans;
    }
    public void start() {
        objFile.update(workDirectory);
        if (isFile) objFile.update(objFile);
        t.start();
    }
    public String getPath() {
        return objFile.getAbsolutePath();
    }
    public void run() {
        while (true) {
            try {
                Thread.sleep(SLEEPTIME);
            } catch (Exception e) {}
            deleteFiles.clear();
            newFiles.clear();
            modifiedFiles.clear();
            sizeChangedFiles.clear();
            renamed.clear();
            pathChanged.clear();
            objFile.getDeleteFiles(workDirectory, deleteFiles);
            objFile.getNewFiles(workDirectory, newFiles);
            objFile.getModified(workDirectory, modifiedFiles);
            objFile.getSizeChanged(workDirectory, sizeChangedFiles);
            //取消对顶层目录的监视//
            if(modifiedFiles.peek()!=null &&
                    modifiedFiles.peek().getAbsolutePath().equals(workDirectory.getAbsolutePath()))
                modifiedFiles.poll();
            if(sizeChangedFiles.peek()!=null &&
                    sizeChangedFiles.peek().getAbsolutePath().equals(workDirectory.getAbsolutePath()))
                sizeChangedFiles.poll();
            /////////////////////
            for (ObjFile tmp : deleteFiles) {//去重
                boolean find = false;
                for (ObjFile sizetmp:sizeChangedFiles) {
                    if (tmp.getAbsolutePath().equals(sizetmp.getAbsolutePath())) {
                        find = true;
                        break;
                    }
                }
                if (!find) sizeChangedFiles.offer(tmp);
            }
            for (ObjFile tmp : newFiles) {//去重
                boolean find = false;
                for (ObjFile sizetmp:sizeChangedFiles) {
                    if (tmp.getAbsolutePath().equals(sizetmp.getAbsolutePath())) {
                        find = true;
                        break;
                    }
                }
                if (!find) sizeChangedFiles.offer(tmp);
            }
            //获取pathChanged与renamed
            for (ObjFile deleteTemp : deleteFiles) {
                for (ObjFile newTemp : newFiles) {
                    if (deleteTemp.getName()!=null && deleteTemp.getParent()!=null &&
                            deleteTemp.getName().equals(newTemp.getName()) &&
                            deleteTemp.getLastModifiedTime() == newTemp.getModifiedTime() &&
                            !deleteTemp.getParent().equals(newTemp.getParent()) &&
                            deleteTemp.getLastLength() == newTemp.getLength() &&
                            newTemp.getPathchanged() != 1 && newTemp.isFile() && deleteTemp.ifwasFile()
                        //新旧文件都必须是文件，不能是文件夹
                        //不允许多个被删除文件映射到同一个新增文件
                            ) {//名字相同，路径不同，修改时间相同，大小相同，判断为path_changed
                        //此处为引用,可能存在未知问题
                        newTemp.setPathchanged(1);
                        pathChanged.offer(new FileMap(deleteTemp, newTemp));
                        break;//防止同类触发器一对多的情况产生
                    }
                }
                for (ObjFile newTemp : newFiles) {
                    if (deleteTemp.getName()!=null && deleteTemp.getParent()!=null &&
                            !deleteTemp.getName().equals(newTemp.getName()) &&
                            deleteTemp.getLastModifiedTime() == newTemp.getModifiedTime() &&
                            deleteTemp.getParent().equals(newTemp.getParent()) &&
                            deleteTemp.getLastLength() == newTemp.getLength() &&
                            newTemp.getRenamed() != 1 && newTemp.isFile() && deleteTemp.ifwasFile()//新旧文件都必须是文件，不能是文件夹
                            ) {//名字不同，路径相同，修改时间相同，大小相同，判定为renamed
                        newTemp.setRenamed(1);//不允许多个被删除文件映射到同一个新增文件
                        renamed.offer(new FileMap(deleteTemp, newTemp));
                        break;//防止同类触发器一对多的情况产生
                    }
                }
            }
            if (!isFile) {//如果是目录
                if (!objFile.exists()) {
                    System.out.println(objFile.getAbsolutePath()+" doesn't exist.");
                    return;//监控对象消失，则停止监控
                }
            } else {//如果是文件
                //新增文件与被删除文件都算size-changed
                boolean ifrenamed = false;
                boolean ifPathChanged = false;
                boolean ifModified = false;
                boolean ifSizeChanged = false;
                ObjFile oldFile = objFile;
                ObjFile oldRenamed = null;
                ObjFile oldPathChanged = null;
                boolean find = true;
                if (!objFile.exists()) {//如果单个文件不存在，在renamed或者path-changed里面找
                    find = false;
                    for (FileMap fileMap:renamed) {
                        if (objFile.getAbsolutePath().equals(fileMap.getKey().getAbsolutePath())) {
                            oldRenamed = fileMap.getValue();
                            find = true;
                            ifrenamed = true;
                            break;
                        }
                    }
                    for (FileMap fileMap:pathChanged) {
                        if (objFile.getAbsolutePath().equals(fileMap.getKey().getAbsolutePath())) {
                            oldPathChanged = fileMap.getValue();
                            find = true;
                            ifPathChanged = true;
                            break;
                        }
                    }
                }
                for (ObjFile tmp:modifiedFiles) {
                    if (tmp.getAbsolutePath().equals(objFile.getAbsolutePath())) {
                        ifModified = true;
                        break;
                    }
                }
                for (ObjFile tmp:sizeChangedFiles) {
                    if (tmp.getAbsolutePath().equals(objFile.getAbsolutePath())) {
                        ifSizeChanged = true;
                        break;
                    }
                }
                modifiedFiles.clear();
                sizeChangedFiles.clear();
                renamed.clear();
                pathChanged.clear();
                if (ifModified) modifiedFiles.offer(objFile);
                if (ifSizeChanged) sizeChangedFiles.offer(objFile);
                if (ifrenamed) {
                    objFile = oldRenamed;
                    renamed.offer(new FileMap(oldFile,oldRenamed));
                    if (mode[0][2]) objFile = oldFile;//如果可以recover，就恢复成以前
                }
                if (ifPathChanged) {
                    objFile = oldPathChanged;
                    pathChanged.offer(new FileMap(oldFile,oldPathChanged));
                    if (mode[2][2]) objFile = oldFile;
                }
                if (!find) {
                    if (mode[3][0] && !sizeChangedFiles.isEmpty()) {//record-summary
                        summary.register(3,sizeChangedFiles);
                    }
                    if (mode[3][1]) detail.register(3,sizeChangedFiles);//record-detail
                    System.out.println(objFile+" doesn't exist");
                    return;//如果都没找到，那停止监控
                }
                if (ifrenamed && ifPathChanged &&
                		!(mode[0][2] && mode[2][2])) {//开始分身,前提是二者不能都recover,否则一recover就归于同一文件
                	if (mode[0][2]){//仅仅renamed有recover,objFile现在被设置为重命名之前的oldFile
                		FileSystemMonitor fsm = new FileSystemMonitor(oldPathChanged.getAbsolutePath()
                				, summary, detail, recover);
                		fsm.addTask(mode);
                		fsm.start();
                	} else if (mode[2][2]) {//仅仅path-changed有recover,objFile现在被设置为路径改变之前的oldFile
                		FileSystemMonitor fsm = new FileSystemMonitor(oldRenamed.getAbsolutePath()
                				, summary, detail, recover);
                		fsm.addTask(mode);
                		fsm.start();
                	} else {//path-changed和renamed都没有recover，于是新建两个进程
                		FileSystemMonitor fsm1 = new FileSystemMonitor(oldPathChanged.getAbsolutePath()
                				, summary, detail, recover);
                		fsm1.addTask(mode);
                		FileSystemMonitor fsm2 = new FileSystemMonitor(oldRenamed.getAbsolutePath()
                				, summary, detail, recover);
                		fsm2.addTask(mode);
                		fsm1.start();
                		fsm2.start();
                        for (int i = 0; i < 4; i++) {
                            for (int j = 0; j < 2; j++) {
                                if (mode[i][j]) {
                                    if (j == 0) {//record-summary
                                        if (i==0 && !renamed.isEmpty()) summary.register(0,renamed);
                                        if (i==1 && !modifiedFiles.isEmpty()) summary.register(1,modifiedFiles);
                                        if (i==2 && !pathChanged.isEmpty()) summary.register(2,pathChanged);
                                        if (i==3 && !sizeChangedFiles.isEmpty()) summary.register(3,sizeChangedFiles);
                                    } else {//record-detail
                                        if (i == 0) detail.register(0, renamed);
                                        if (i == 1) detail.register(1, modifiedFiles);
                                        if (i == 2) detail.register(2, pathChanged);
                                        if (i == 3) detail.register(3, sizeChangedFiles);
                                    }
                                }
                            }
                        }
                        detail.print();
                		return;//分身结束，此线程终结
					}
                }
            }
            //是否触发summary或者detail
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 2; j++) {
                    if (mode[i][j]) {
                        if (j == 0) {//record-summary
                            if (i==0 && !renamed.isEmpty()) summary.register(0,renamed);
                            if (i==1 && !modifiedFiles.isEmpty()) summary.register(1,modifiedFiles);
                            if (i==2 && !pathChanged.isEmpty()) summary.register(2,pathChanged);
                            if (i==3 && !sizeChangedFiles.isEmpty()) summary.register(3,sizeChangedFiles);
                        } else {//record-detail
                            if (i == 0) detail.register(0, renamed);
                            if (i == 1) detail.register(1, modifiedFiles);
                            if (i == 2) detail.register(2, pathChanged);
                            if (i == 3) detail.register(3, sizeChangedFiles);
                        }
                    }
                }
            }
            recover.recover(mode[0][2], mode[2][2], renamed, pathChanged);
            detail.print();
            objFile.update(workDirectory);
            if (isFile) objFile.update(objFile);
        }
    }
}
