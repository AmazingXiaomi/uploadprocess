package com.process.doftp.util;

/**
 * @author ：mayy
 * @date ：Created in 2019/4/18
 */

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by yugaofeng on 2017/9/5.
 */
public class ContinueFTP {



    //定义一个客户端
    private static FTPClient ftpClient ;

    //单例模式
    public  FTPClient getFtpClient(){
        if(ftpClient == null){
            ftpClient = new FTPClient();
        }
        return ftpClient;
    }

    public ContinueFTP(){
        getFtpClient().addProtocolCommandListener(new PrintCommandListener(
                new PrintWriter(System.out)));
    }


    /**
     * 连接到FTP服务器
     * @param hostname  主机名
     * @param port 端口
     * @param username 用户名
     * @param password 密码
     * @return 是否连接成功
     * @throws IOException
     */
    public boolean connect(String hostname, int port, String username, String password) throws IOException {
        getFtpClient().connect(hostname, port);
        if (FTPReply.isPositiveCompletion(getFtpClient().getReplyCode())) {
            if (getFtpClient().login(username, password)) {
                getFtpClient().enterLocalPassiveMode();
                getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);
                return true;
            }
        }
        disconnect();
        return false;
    }

    /**
     * 断开与服务器的连接
     * @throws IOException
     */
    public void disconnect() throws IOException {
        if (getFtpClient().isConnected()) {
            getFtpClient().disconnect();
            System.out.println("ftp is disconnect!");
        }
    }
}
