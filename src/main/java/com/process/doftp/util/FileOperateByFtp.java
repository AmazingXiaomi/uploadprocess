package com.process.doftp.util;

/**
 * @author ：mayy
 * @date ：Created in 2019/4/18
 */
import com.process.doftp.Exception.CreateException;
import com.process.doftp.emun.UploadStatus;
import com.process.doftp.model.FileObserverAble;
import com.process.doftp.model.FilePercentObserver;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;

/**
 * Created by yugaofeng on 2017/9/5.
 */
public class FileOperateByFtp {

    private FTPClient ftpClient;

    FileObserverAble fileObserverAble;

    public FileOperateByFtp(FTPClient ftpClient) {
        //添加观察者对象
        fileObserverAble = new FileObserverAble();
        FilePercentObserver filePercentObserver = new FilePercentObserver(fileObserverAble);
        this.ftpClient = ftpClient;
    }

    /**
     * 上传文件到FTP服务器，支持断点续传 并返回上传文件进度
     * @param local 本地文件名称，绝对路径
     * @param remote 远程文件路径，使用/home/directory1/subdirectory/file.ext
     *               按照Linux上的路径指定方式，支持多级目录嵌套，支持递归创建不存在的目录结构
     * @return 上传结果
     * @throws IOException
     */
    public UploadStatus upload(String local, String remote) throws Exception{
        try {
            if (!ftpClient.isConnected()) {
                throw new CreateException(-1, "远程服务器相应目录创建失败");
            }
            UploadStatus result;
            // 对远程目录的处理  并返回文件的名称
            String remoteFileName = createDirectory(remote, ftpClient);
            // 检查远程是否存在文件
            FTPFile[] files = ftpClient.listFiles(remoteFileName);
            File localFile = new File(local);
            if(localFile.length() <=0){
                throw new CreateException(-1,"本地文件不存在");
            }
            if (files.length == 1) {
                //判断文件是否存在
                long remoteSize = files[0].getSize();
                long localSize = localFile.length();
                if(remoteSize==localSize){
                    return UploadStatus.File_Exits;
                }else if(remoteSize > localSize){
                    return UploadStatus.Remote_Bigger_Local;
                }
                result = this.writeByUnit(remoteFileName,localFile,ftpClient,remoteSize,localFile.length());
            } else {
                result = this.writeByUnit(remoteFileName,localFile,ftpClient,0,localFile.length());
            }
            return result;
        }catch (CreateException e){
            throw e;
        }finally {
            //上传完成之后 切回到根目录
            ftpClient.changeWorkingDirectory("/");
        }
    }

    /**
     * 判断目录
     * @param remoteFilePath 远程服务器上面的 文件目录
     * @param ftpClient ftp客户端
     * @return
     * @throws Exception
     */
    private String createDirectory(String remoteFilePath,FTPClient ftpClient) throws Exception {
        if(ftpClient == null){
            throw new CreateException(-1,"FTP客户端为空,请先连接到客户端");
        }
        String fileName = remoteFilePath;
        if(remoteFilePath.contains("/")){
            fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
            String directory = remoteFilePath.substring(0, remoteFilePath.lastIndexOf("/") + 1);
            if(directory.startsWith("/")){
                directory = directory.substring(1);
            }
            while (true){
                if(!directory.contains("/")){
                    break;
                }
                String subDirectory = directory.substring(0, directory.indexOf("/"));
                directory = directory.substring(directory.indexOf("/")+1);
                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                    if (ftpClient.makeDirectory(subDirectory)) {
                        ftpClient.changeWorkingDirectory(subDirectory);
                    } else {
                        throw new CreateException(-1,"创建目录失败");
                    }
                }
            }
        }
        return fileName;
    }


    /**
     * 上传文件到服务器,新上传和断点续传
     * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变
     * @param localFile 本地文件File句柄，绝对路径
     * @param ftpClient FTPClient引用 beginSize是指文件长传开始指针位置  endSize是结束的位置 为多线程上传下载提供接口 不过该方法还需要修改
     * @return
     * @throws IOException
     */

    private  UploadStatus writeByUnit(String remoteFile,File localFile,FTPClient ftpClient,long beginSize,long endSize) throws Exception {
        long localSize = localFile.length();
        if(endSize > localSize){
            endSize = localSize;
        }
        if(beginSize < 0){
            beginSize = 0;
        }
        //等待写入的文件大小
        long writeSize = endSize - beginSize;
        if(writeSize <= 0){
            throw new CreateException(1,"文件指针参数出错");
        }
        //获取百分单位是 1-100
        RandomAccessFile raf = new RandomAccessFile(localFile,"r");
        OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"),"iso-8859-1"));
        //把文件指针移动到 开始位置
        ftpClient.setRestartOffset(beginSize);
        raf.seek(beginSize);
        //定义最小移动单位是 1024字节 也就是1kb
        byte[] bytes = new byte[1024];
        int c;
        double finishSize = 0;
        double finishPercent = 0;
        //存在一个bug 当分布移动的时候  可能会出现下载重复的问题 后期需要修改
        while ((c = raf.read(bytes)) != -1) {
            out.write(bytes, 0, c);
            finishSize += c;
            if(finishSize > writeSize){
                finishPercent = 1;
                System.out.println(">>>>>完成进度:" + finishPercent);
                fileObserverAble.setKeyValue(localFile.getName(),finishPercent,"upload");
                break;
            }
            if ((finishSize / writeSize) - finishPercent > 0.01) {
                finishPercent = finishSize / writeSize;
                System.out.println(">>>>>完成进度:" + finishPercent);
                fileObserverAble.setKeyValue(localFile.getName(),finishPercent,"upload");
            }
        }
        out.flush();
        raf.close();
        out.close();
        boolean result =ftpClient.completePendingCommand();
        return  result?UploadStatus.Upload_From_Break_Success:UploadStatus.Upload_From_Break_Failed;
    }


    /**
     * 从FTP服务器上下载文件
     * @param remote 远程文件路径
     * @param local 本地文件路径
     * @return 是否成功
     * @throws IOException
     */
    public boolean download(String remote,String local) throws Exception{
        FTPFile[] files = ftpClient.listFiles(remote);
        if(files == null || files.length < 0){
            throw new CreateException(-1,"远程文件不存在");
        }
        if(files.length != 1){
            throw new CreateException(-1,"远程文件不唯一");
        }
        File localFile = new File(local);
        if(localFile.exists()){
            long localBeginSize = localFile.length();
            if(localBeginSize == files[0].getSize()){
                throw new CreateException(-1,"文件已经存在");
            }else if(localBeginSize > files[0].getSize()){
                throw new CreateException(-1,"下载文件出错");
            }
            return downloadByUnit(remote,local,localBeginSize,files[0].getSize());
        }else {
            return downloadByUnit(remote,local,0,files[0].getSize());
        }
    }
    private Boolean downloadByUnit(String remote,String local,long beginSize,long endSize) throws Exception {
        File localFile = new File(local);
        long waitSize = endSize - beginSize;
        //进行断点续传，并记录状态
        FileOutputStream out = new FileOutputStream(localFile,true);
        //把文件指针移动到 开始位置
        ftpClient.setRestartOffset(beginSize);
        InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes("GBK"),"iso-8859-1"));
        byte[] bytes = new byte[1024];
        int c;
        double finishSize =0;
        double finishPercent = 0;
        while((c = in.read(bytes))!= -1){
            out.write(bytes,0,c);
            finishSize += c;
            if(finishSize > waitSize){
                //System.out.println(">>>>>完成进度:" + 1);
                fileObserverAble.setKeyValue(localFile.getName(),1,"download");

            }
            if ((finishSize / waitSize) - finishPercent > 0.01) {
                finishPercent = finishSize / waitSize;
                //System.out.println(">>>>>完成进度:" + finishPercent);
                fileObserverAble.setKeyValue(localFile.getName(),finishPercent,"download");
            }
        }
        in.close();
        out.close();
        return ftpClient.completePendingCommand();
    }


}