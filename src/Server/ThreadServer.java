/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;

/**
 *
 * @author Khanh Tran-Cong
 */

public class ThreadServer extends Thread{
        public Socket client;
	public Server server;
	private String nickName;
        private String targetName;
	private DataOutputStream dos;
	private DataInputStream dis;
	public boolean run;
        

	public ThreadServer(Server server, Socket client){
		try {
			this.server=server;
			this.client=client;
			dos= new DataOutputStream(client.getOutputStream());
			dis= new DataInputStream(client.getInputStream());
			run=true;
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
    @Override
        @SuppressWarnings("null")
    public void run(){
        
	String data = null;
	while(run){
            data =getMSG();
            System.out.println("data"+ data);
            if(data.startsWith("<LOGIN>")&&data.endsWith("</LOGIN>")){
                nickName = data.substring(7, data.indexOf("</LOGIN>"));
                if(checkNick(nickName)){
                    sendMSG("<SESSION_DENY/>");
                }else{
                    sendMSG("<SESSION_ACCEPT/>");
                    server.user.append(nickName+" đã kết nối với room\n");
                    server.listUser.put(nickName, this);
                    server.sendAllUpdate(nickName);
                    System.err.println("Nick Name: "+  nickName);
                }
            }
            if(data.contains("<CHAT>")){
                if(data.contains("<FROM>")){
                    nickName  = data.substring(data.indexOf("<FROM>")+6,data.indexOf("</FROM>"));
                    if(data.contains("<TO>")){
                        targetName  = data.substring(data.indexOf("<TO>")+4,data.indexOf("</TO>"));
                        if(data.contains("<MSG>")){
                            String mes = data.substring(data.indexOf("<MSG>")+5,data.indexOf("</MSG>"));
                            server.sendChat(nickName, targetName, mes);
                        }
                    }
                }
            }
            if(data.contains("<CHECKONLINE/>")){
                server.nameCheck = data.substring(data.indexOf("<ONLINE>")+8,data.indexOf("</ONLINE>"));
            }
            if (data == null){
                run=false;
                server.listUser.remove(this.nickName);
                server.sendAllUpdate(nickName);
                exit();
            }
            if(data.contains("<LOGOUT>")){
                run=false;
                server.listUser.remove(this.nickName);
                server.sendAllUpdate(nickName);
                exit();
            }
            if(data.contains("<FILE>")){
                if(data.contains("<FROM>")){
                    nickName  = data.substring(data.indexOf("<FROM>")+6,data.indexOf("</FROM>"));
                    if(data.contains("<TO>")){
                        targetName  = data.substring(data.indexOf("<TO>")+4,data.indexOf("</TO>"));
                        if(data.contains("<NAME>")){
                            String nameFile  = data.substring(data.indexOf("<NAME>")+6,data.indexOf("</NAME>"));
                            if(data.contains("<SIZE>")){
                                String size  = data.substring(data.indexOf("<SIZE>")+6,data.indexOf("</SIZE>"));
                                if(targetName == "ALL")
                                    server.sendFile(nickName,nameFile);
                                else
                                    server.sendFile(nickName, targetName,nameFile,size);
                            }
                            
                        }
                        
                    }
                            
                        
                }
            }
	}
    }
    public void exit(){
	try {
            server.sendAllUpdate(nickName);
            dos.close();
            dis.close();
            client.close();
            server.user.append(nickName+" đã thoát\n");
            server.sendChat(nickName,"ALL",nickName+" đã thoát");
        } catch (IOException e) {
            e.printStackTrace();
        }            
    }
    public void checkonline(String name){
        Enumeration e = server.listUser.keys();
                        //String name=null;
                        while(e. hasMoreElements()){
                            name=(String) e.nextElement();
                            server.listUser.get(name).sendMSG("<CHECKONLINE/>");
                            if (!name.equals(name)){
                                server.listUser.get(name).run=false;
                                server.listUser.get(name).exit();
                                server.listUser.remove(name);
                                server.sendAllUpdate(name);
                            }
                        }
    }
    private boolean checkNick(String nick){
	return server.listUser.containsKey(nick);
    }
    public void sendMSG(String data){
        try {
            dos.writeUTF(data);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getMSG(){
        String data=null;
        try {
            data=dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }     
}
