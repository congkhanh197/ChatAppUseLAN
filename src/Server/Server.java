/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Server;


import Form.ServerForm;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 *
 * @author Khanh Tran-Cong
 */

public class Server extends JFrame implements ActionListener{
        private final JButton close;
	public JTextArea user;
	private ServerSocket server;
	public Hashtable<String, ThreadServer> listUser;
        ServerForm serverForm ;
        String nameCheck;
        Socket client;
        InputStream is;
        OutputStream os;
        BufferedOutputStream bs;
        byte[] buffer;
        int bytesRead;

	public Server(){
		super("Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
                        @Override
			public void windowClosing(WindowEvent e){
				try {
					sendChat("server", "ALL", "SERVEROFF");
					server.close();
					System.exit(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
                serverForm = new ServerForm();
                serverForm.setVisible(true);
                user = serverForm.getTextArea();
                close = serverForm.getButton();
                close.addActionListener(this);
                new java.util.Timer().schedule( 
                    new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Enumeration e = listUser.keys();
                        String name=null;
                        while(e. hasMoreElements()){
                            name=(String) e.nextElement();
                            listUser.get(name).sendMSG("<CHECKONLINE/>");
                            if (!name.equals(nameCheck)){
                                listUser.get(name).run=false;
                                listUser.get(name).exit();
                                listUser.remove(name);
                                sendAllUpdate(name);
                            }
                        }
                    }},1000 
                );
	}
        


        
    private void go(){
	try {
            listUser = new Hashtable<String, ThreadServer>();
            server = new ServerSocket(2207);
            user.append("Máy chủ bắt đầu phục vụ\n");
            while(true){
                client = server.accept();
		new ThreadServer(this,client);     
            }
	} catch (IOException e) {
            user.append("Không thể khởi động máy chủ\n");
	}
    }
    
    public static void main(String[] args) {
        new Server().go();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            server.close();
        } catch (IOException e1) {
            user.append("Không thể dừng được máy chủ\n");
        }
        System.exit(0);
    }

    public void sendAllUpdate(String from){
        Enumeration e = listUser.keys();
	String name=null;
	while(e. hasMoreElements()){
            name=(String) e.nextElement();
            listUser.get(name).sendMSG(getAllName());
	}
    }

    public String getAllName(){
	Enumeration e = listUser.keys();
	String name="<LIST>";
	while(e. hasMoreElements()){
            name+="<PEER>" + (String) e.nextElement()+"</PEER>";
	}
        name += "</LIST>";
        return name;
    } 

    public void sendChat(String from, String to, String mes) {
        Enumeration e = listUser.keys();
        String name=null;
        String data= "<CHAT>"
                +"<FROM>"+from+"</FROM>"
                +"<TO>"+to+"</TO>"
                +"<MSG>"+mes+"</MSG>"
                +"</CHAT>";
        System.out.println("gui tin nhan"+data);
        if ("ALL".equals(to)){
            while(e. hasMoreElements()){
                name=(String) e.nextElement();
                if(name.compareTo(from)!=0){
                    listUser.get(name).sendMSG(data);
                }
            }
        }else{
            while(e. hasMoreElements()){
                name=(String) e.nextElement();
                if(name.compareTo(to)==0){
                    listUser.get(name).sendMSG(data);
                    System.out.println("ten chat:"+name);
                    break;
                }
            }
        }
    }
    //gui file toi tat ca moi nguoi.
    public void sendFile(String nickName,String nameFile) {
        //TODO
    }
    public void sendFile(String from,String to,String nameFiles,String Size){
        try {
            is = client.getInputStream();
            os = client.getOutputStream();
            
            
            //buffer  = new byte[32 * 1024];
            //while ((bytesRead = is.read(buffer, 0, buffer.length)) > 0) {
            //    os.write(buffer, 0, bytesRead);
            //}
            Enumeration e = listUser.keys();
            String name=null;
            while(e. hasMoreElements()){
                name=(String) e.nextElement();
                if (!name.equals(to)){
                } else {
                    listUser.get(name).sendMSG("<FILE>"
                            +"<FROM>"+from+"</FROM>"
                                    +"<TO>"+to+"</TO>"
                                            +"<NAME>"+nameFiles+"</NAME>"
                                                    +"<SIZE>"+Size+"</SIZE>"
                                                            +"</FILE>");
                    break;
                }
            }
            int size = Integer.parseInt(Size);
            buffer = new byte[size];
            is.read(buffer, 0, buffer.length);
            os.write(buffer,0,buffer.length);
            os.flush();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
