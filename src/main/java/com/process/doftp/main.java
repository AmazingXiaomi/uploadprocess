package com.process.doftp;

import com.process.doftp.Exception.CreateException;
import com.process.doftp.util.ContinueFTP;
import com.process.doftp.util.FileOperateByFtp;

import java.io.IOException;

/**
 * @author ：mayy
 * @date ：Created in 2019/4/18
 */
public class main {

    public static void main(String[] args) throws IOException {
        ContinueFTP ftp = new ContinueFTP();
        try {
            ftp.connect("192.168.3.20", 21, "ftpuser", "123456");
            FileOperateByFtp fileOperateByFtp = new FileOperateByFtp(ftp.getFtpClient());
            fileOperateByFtp.upload("D:\\elasticsearch-6.2.2.zip","/upload2/f3/elasticsearch-6.2.2.zip");
            //fileOperateByFtp.upload("F:\\upload6.temp","/upload2/f3/upload6.temp");
           /* fileOperateByFtp.download("/upload2/f3/upload7.temp","F:\\upload6.temp");
            fileOperateByFtp.download("//upload2/f3/upload6.temp","F:\\upload7.temp");*/
            if(ftp.getFtpClient() != null){
                ftp.getFtpClient().disconnect();
            }
        } catch (Exception e) {
            if(e instanceof CreateException){
                System.out.println(((CreateException) e).getErrMessage());
            }
        }
    }


    private static void testConnection() throws IOException {
        ContinueFTP ftp = new ContinueFTP();
        System.out.println("<<<<<<<<<<<<<<<<<1"+ftp.getFtpClient().isConnected());
        ftp.connect("192.168.3.20", 21, "ftpuser", "123456");
        System.out.println("<<<<<<<<<<<<<<<<<3"+ftp.getFtpClient().isConnected());
        ftp.getFtpClient().disconnect();
    }
}
