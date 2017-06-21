import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

class ObjFile {
    private File file;
    private String workingDirectory;//工作区
    private ObjFile[] lastObjFile;//上次子目录中文件
    private long lastModifiedTime;
    private long lastLength;
    private int renamed;
    private int pathchanged;
    private boolean wasFile;//曾经是文件，现在可能被删除了，但是还是可以通过这个知道它
    ObjFile(String filePath) {
        try {
            this.file = new File(filePath);
        } catch (Exception e) {
            this.file = null;
        }
        if (this.file!=null && this.file.exists()) {
            if (this.file.isFile()) {
                workingDirectory = this.file.getParent();
            }
            if (this.file.isDirectory()) {
                workingDirectory = this.file.getAbsolutePath();
            }
        }
    }

    public synchronized void update(ObjFile objFile) {
        /*
         * 递归地获取目录下文件
         */
        if (objFile!=null && objFile.exists()) {
            objFile.lastObjFile = objFile.listFiles();
            if (objFile.lastObjFile!=null) {
                for (ObjFile son : objFile.lastObjFile)
                    update(son);
            }
            objFile.lastModifiedTime = objFile.getModifiedTime();//获取上次修改时间
            objFile.lastLength = objFile.getLength();
            objFile.wasFile = objFile.isFile();
        }
    }
    public synchronized boolean ifwasFile() {
        return wasFile;
    }
    public synchronized ObjFile[] getLastObjFile() {
        return lastObjFile;
    }
    public synchronized long getLastLength() {
        return lastLength;
    }
    //获取文件名
    public synchronized String getName() {
        if (this.file!=null) return this.file.getName();
        else return null;
    }
    //获取文件路径
    public synchronized String getAbsolutePath() {
        try {
            return this.file.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }

    //判断是否为文件
    public synchronized boolean isFile() {
        try {
            return this.file.isFile();
        } catch (Exception e) {
            return false;
        }
    }
    //判断是否为目录
    public synchronized boolean isDirectory() {
        try {
            return this.file.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }
    //获取文件大小
    public synchronized long getLength() {
        try {
            if (this.file.isFile()) {
                return this.file.length();
            } else if (this.file.isDirectory()) {
                long len = 0L;
                File[] files = this.file.listFiles();
                for (int i = 0; i < files.length; i++)
                    if (files[i].isFile()) len += files[i].length();
                return len;
            } else return 0L;
        } catch (Exception e) {
        	return 0L;
		}
    }
    //获取最后修改时间
    public synchronized long getModifiedTime() {
        try {
            return this.file.lastModified();
        } catch (Exception e) {
            return 0L;
        }
    }

    public synchronized long getLastModifiedTime() {
        return this.lastModifiedTime;
    }
    //获取工作目录
    public synchronized String getWorkingDirectory() {
        return this.workingDirectory;
    }
    //获取父目录
    public synchronized String getParent() {
        return this.file.getParent();
    }
    //设置是否被重命名过标记
    public synchronized void setRenamed(int x) {
        this.renamed = x;
    }
    //获取是否被重命名过标记
    public synchronized int getRenamed() {
        return this.renamed;
    }
    //设置是否路径改变过标记
    public synchronized void setPathchanged(int x) {
        this.pathchanged = x;
    }
    //获取是否路径改变过标记
    public synchronized int getPathchanged() {
        return this.pathchanged;
    }
    //获取路径名
    public synchronized String toString() {
        return this.file.getAbsolutePath();
    }
    //判断文件是否存在
    public synchronized boolean exists() {
        try {
            return this.file.exists();
        } catch (Exception e) {
        	return false;
        }
    }
    //获取文件列表
    public synchronized ObjFile[] listFiles() {
        try {
            File[] files = this.file.listFiles();
            ObjFile[] objFiles = new ObjFile[files.length];
            for (int i = 0; i < files.length; i++) {
                objFiles[i] = new ObjFile(files[i].getAbsolutePath());
            }
            return objFiles;
        } catch(Exception e) {
            return null;
        }
    }
    //创建新文件
    public synchronized void createNewFile() {
        try {
            if (!this.file.exists()) {
                boolean b = this.file.createNewFile();
                if (b) System.out.println("File created successfully.");
                else System.out.println("File created failed.");
            } else {
                System.out.println("File already exists!");
            }
        } catch (Exception e) {
        }
    }
    //创建新文件夹
    public synchronized void createNewFolder() {
        try {
            if (!this.file.exists()) {
                boolean b = this.file.mkdirs();
                if (b) System.out.println("Folder created successfully.");
                else System.out.println("Folder created failed.");
            } else {
                System.out.println("Folder already exists!");
            }
        } catch (Exception e) {
        }
    }
    //删除path下文件
    public synchronized void deleteFile(String path) {
        File dFile = new File(path);
        if (dFile.exists()) {
            if (dFile.isFile()) {
                dFile.delete();
            } else {
                File[] dFiles = dFile.listFiles();
                if (dFiles!=null) {
                    for (File tempFile : dFiles) {
                        deleteFile(tempFile.getAbsolutePath());
                        tempFile.delete();
                    }
                }
                dFile.delete();
            }
        }
    }
    public synchronized void suicide() {
        try {
            this.file.delete();
        } catch (Exception e) {
            System.out.println("Failed to suicide.");
        }
    }
    //重命名文件至newName,也具有移动文件的能力，具体需要在newName中说明详细路径
    public synchronized void renameTo(String newName) {
        try {
            if (this.file != null && !this.file.getName().equals(newName)) {
                File newFile = new File(newName);
                if (!this.file.exists()) {
                    System.out.println("This file doesn't exist");
                    return;
                }
                if (newFile.exists()) {
                    System.out.println("New file already exists");
                    return;
                }
                try {
                    this.file.renameTo(newFile);
                    this.file = newFile;
                } catch (Exception e) {
                    System.out.println("Rename Error");
                }
            }
        } catch (Exception e){
        }
    }
    //向文件中写入内容
    public synchronized void rewrite(String data) {
        if (this.file==null) return;
        if (this.file.isFile()) {
            try {
                FileOutputStream o = new FileOutputStream(this.file);
                o.write(data.getBytes());
                o.close();
                System.out.println("Object file was written successfully!");
            } catch (Exception e) {
                System.out.println("Can't write now!");
            }
        }
    }

    public synchronized void getDeleteFiles(ObjFile objFile,LinkedBlockingQueue<ObjFile> deleteList) {
        if (objFile==null || objFile.getLastObjFile()==null) return;
        for (ObjFile file:objFile.getLastObjFile()) {
            getDeleteFiles(file,deleteList);
            if (!file.exists()) {//如果文件不存在,那么表示文件被删除了
                deleteList.offer(file);
            }
        }
    }
    public synchronized void getNewFiles(ObjFile objFile,LinkedBlockingQueue<ObjFile> newList) {
        //获取新增文件列表
        if (objFile==null || newList==null) return;
        //注意，这里得到的curObjFiles是新建出来的，跟原来的ObjFile没有半点关系
        ObjFile[] curObjFiles = objFile.listFiles();//扫描后得到的最新文件
        if (curObjFiles!=null) {
            for (ObjFile tmpFile : curObjFiles) {
                ObjFile sameFile = null;
                if (objFile.getLastObjFile()!=null) {
                    for (ObjFile file : objFile.getLastObjFile()) {
                        if (file.toString().equals(tmpFile.toString())) {
                            sameFile = file;
                            break;
                        }
                    }
                }
                if (sameFile==null) {
                    newList.offer(tmpFile);//文件如果没有找到，那就是新文件
                    getNewFiles(tmpFile, newList);
                } else {
                    getNewFiles(sameFile,newList);
                }
            }
        }
    }
    public synchronized void getModified(ObjFile objFile,LinkedBlockingQueue<ObjFile> modified) {
        //modified只关注同层文件
        if (objFile==null || modified==null) return;
        if (objFile.exists() && objFile.getModifiedTime()!=objFile.getLastModifiedTime() &&
                objFile.getLastModifiedTime()!=0) {//如果这次修改时间和上次不同，那么就是被修改过
            modified.offer(objFile);
        }
        if (objFile.getLastObjFile()!=null) {
            for (ObjFile tmpfile:objFile.getLastObjFile()) {
                getModified(tmpfile,modified);
            }
        }
    }
    public synchronized void getSizeChanged(ObjFile objFile,LinkedBlockingQueue<ObjFile> sizeChangedList) {
        if (objFile==null || sizeChangedList==null) return;
        if (objFile.lastLength!=objFile.getLength()) {
            sizeChangedList.offer(objFile);
        }
        if (objFile.getLastObjFile()!=null) {
            for (ObjFile tmp:objFile.getLastObjFile()) {
                getSizeChanged(tmp,sizeChangedList);
            }
        }
    }
}
