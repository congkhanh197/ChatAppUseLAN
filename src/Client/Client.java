/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Form.ChatRoomForm;
import Form.LoginForm;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Khanh Tran-Cong
 */
public class Client extends JFrame implements ActionListener{
    private final JButton btn_login,btn_send_room,btn_file_room,btn_logout;
    private JTextField txt_message_room;
    private final JTextArea ta_room;
    private final JList ta_online;
    private ThreadClient dataStream;
    private DataOutputStream dos;
    private DataInputStream dis;
    private final LoginForm loginForm = new LoginForm();
    private final ChatRoomForm chatRoomForm = new ChatRoomForm();
    public Hashtable<String, ThreadChat> listChat;
    public Socket client,sendfile;
    public String txt_ip,txt_name, nameTarget, nameFrom;
    final JFileChooser fc = new JFileChooser();
    DefaultListModel listOnline = new DefaultListModel();
    BufferedInputStream br;
    OutputStream os;
    InputStream is;
    byte[] byteRead;
    int buffer;
    BufferedOutputStream bs;
    File f;
    String filepath;
    public Client (){
        super("Client");
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosed(WindowEvent e) {
                JOptionPane.showConfirmDialog(null, "Không có gì xảy ra với sự kiện này nếu tự đối tượng bắt sự kiện này?",
                            null, JOptionPane.YES_NO_OPTION);
            }
            @Override
            public void windowClosing(WindowEvent e) {
                int hoi = JOptionPane.showConfirmDialog(null, "Bạn có muốn thoát chương trình không?",
                         null, JOptionPane.YES_NO_OPTION);
                if (hoi == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
         
        });
        listChat = new Hashtable<String, ThreadChat>();
	loginForm.setVisible(true);
	chatRoomForm.setVisible(false);
        btn_login = loginForm.getButton();
        btn_login.addActionListener(this);
        btn_logout = chatRoomForm.getButtonLogout();
        btn_logout.addActionListener(this);
        btn_send_room = chatRoomForm.getButtonSend();
        btn_send_room.addActionListener(this);
        btn_file_room = chatRoomForm.getButtonFile();
        btn_file_room.addActionListener(this);
        ta_room = chatRoomForm.getTextArea();
        ta_online = chatRoomForm.getTextAreaOnline();
        ta_online.setModel(listOnline);
        ta_online.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                nameTarget = ta_online.getSelectedValue().toString();
                if(!listChat.containsKey(nameTarget)){
                    starClient(nameTarget);
                }
            }
        });
        txt_message_room = chatRoomForm.getMessage();
        txt_message_room.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt){
                if (evt.getKeyCode() == '\n') {
                    if(txt_message_room.getText() != null ){
                        String a = "<CHAT>"
                            +"<FROM>"+txt_name+"</FROM>"
                            +"<TO>ALL</TO>"
                            +"<MSG>"+txt_message_room.getText()+"</MSG>"
                            +"</CHAT>";
                        sendMSG(a);
                        ta_room.append("Tôi: "+ txt_message_room.getText()+"\n");
                        txt_message_room.setText(null);
                    }
                }
            }
            
        });
    }

    public static void main(String[] args) {
	new Client();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==btn_login){
            txt_name = loginForm.getNameUser(); 
            txt_ip = loginForm.getIP();
            if(txt_name.equals("") || txt_ip.equals("")){
                JOptionPane.showMessageDialog(this,"Vui lòng nhập đầy đủ thông tin!","Message Dialog",JOptionPane.WARNING_MESSAGE);
            }else{
                try {
                    client = new Socket(txt_ip,2207);
                    dos=new DataOutputStream(client.getOutputStream());
                    dis=new DataInputStream(client.getInputStream());
                } catch (IOException er) {
                    JOptionPane.showMessageDialog(this,"Kết nối thât bại, xin hãy kiểm tra lại IP Server.","Message Dialog",JOptionPane.WARNING_MESSAGE);
                }
                try {
                    if(checkLogin(txt_name) == true){
                       chatRoomForm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                       loginForm.setVisible(false);
                       chatRoomForm.setVisible(true);
                       chatRoomForm.setTitle(txt_name);
                       chatRoomForm.setSize(500, 400);
                       ta_room.append("Đã đăng nhập thành công\n");
                       dataStream = new ThreadClient(this, this.dis);
                       listOnline.clear();
                    }else{JOptionPane.showMessageDialog(this,"Đã tồn tại nick này trong room, bạn vui lòng nhập lại.","Message Dialog",JOptionPane.WARNING_MESSAGE);}
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }   
        }
        if(e.getSource()==btn_send_room){
            if(txt_message_room.getText() != null ){
                String a = "<CHAT>"
                    +"<FROM>"+txt_name+"</FROM>"
                    +"<TO>ALL</TO>"
                    +"<MSG>"+txt_message_room.getText()+"</MSG>"
                +"</CHAT>";
                sendMSG(a);
                ta_room.append("Tôi: "+ txt_message_room.getText()+"\n");
                txt_message_room.setText(null);
            }
        }
        if(e.getSource()== btn_file_room){
            sendFileText("ALL");
        }
        if(e.getSource()==btn_logout){
            sendMSG("<LOGOUT></LOGOUT>");
            exit();
        }
    }
    void sendFileText(String to){
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.showOpenDialog(this);
            f = chooser.getSelectedFile();
            if (f != null) {
                if ((f.length() / 1024) > 204800) {
                    JOptionPane.showMessageDialog(null, "Kích thước file chỉ được 200MB!", "Warning", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                ta_room.append("Tôi: Gửi file "+f.getName()+", kích thước "+  f.length()
                            + " Bytes / " + String.valueOf(f.length() / 1024) + " KB\n");
            }else return;
            sendMSG("<FILE>"
                    +"<FROM>"+txt_name+"</FROM>"
                    +"<TO>"+to+"</TO>"
                    +"<NAME>"+f.getName()+"</NAME>"
                    +"<SIZE>"+f.length()+"</SIZE>"
                            +"</FILE>");
            sendfile = new Socket(txt_ip,3333);
            os = sendfile.getOutputStream();
            new Thread(openFile).start();
            new Thread(sendFile).start();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    void saveFileText(){
       //JFileChooser chooser = new JFileChooser("d:/");
       //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
       // int returnVal = fc.showOpenDialog(this);
        //if (returnVal == JFileChooser.APPROVE_OPTION) {
        //    File file = fc.getSelectedFile();
       // }
    }
    
    public void receiveFile(String nameFrom, String nameFile, String size) {
        try {
            File file = new File("D:/"+nameFile);
            if(file.createNewFile())
                new Thread(savefile).start();
                
            else{
                
            }
                //System.out.println
                 //("Error, file already exists.");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    Runnable openFile = new Runnable() {
        @Override
        public void run() {
            try {
                br = new BufferedInputStream(new FileInputStream(f.getPath()));
                byteRead = new byte[(int) f.length()];
                br.read(byteRead, 0, byteRead.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    
    
    Runnable savefile = new Runnable() {

        @Override
        public void run() {
            if (is != null) {
                try {

                    bs = new BufferedOutputStream(new FileOutputStream(filepath));
                    byteRead = new byte[32 * 1024];
                    while ((buffer = is.read(byteRead, 0, byteRead.length)) > 0) {
                        bs.write(byteRead, 0, buffer);
                        bs.flush();
                    }
                    JOptionPane.showMessageDialog(null, "Bạn nhận được file.Lưu tại "+filepath, "Warning", JOptionPane.INFORMATION_MESSAGE);       
            
                    bs.close();
                    is.close();
                    
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                
            }
        }
    };
    Runnable sendFile = new Runnable() {
        @Override
        public void run() {
            if (os != null) {
                try {
                    os.write(byteRead, 0, byteRead.length);
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Không thể gửi!", "Warning", JOptionPane.INFORMATION_MESSAGE);       
            }
            ta_room.append("Gửi file thành công\n");
        }
    };
    
    private boolean checkLogin(String nick) throws IOException{
        String a ="<LOGIN>" + nick + "</LOGIN>";
        sendMSG(a);
        a = "";
        do{
             a = getMSG();
        }while((!a.equals("<SESSION_DENY/>"))&&(!a.equals("<SESSION_ACCEPT/>")));
        return a.equals("<SESSION_DENY/>") != true;	
    }
    
    public void starClient(String nameTarget){
        new ThreadChat(this, nameTarget, dos);
    }
    
    private void exit(){
        try {
            dataStream.stopThread();
            dos.close();
            dis.close();
            client.close();
	} catch (IOException e1) {
            e1.printStackTrace();
	}
	System.exit(0);
    }
    public void setListOnline(String list){
        if(listOnline != null)
            listOnline.clear();
        int vitri = 0;
        while(list.indexOf("<PEER>",vitri)!= -1){
            vitri = list.indexOf("<PEER>",vitri)+6;
            String name  = list.substring(vitri,list.indexOf("</PEER>",vitri));
            if(!name.equals(txt_name))
                listOnline.addElement(name);
        }  
    }
       
    private void sendMSG(String data){
	try {
            System.out.println("Du lieu ra"+ data);
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
    
    public void processData(String data) {
        if(data.contains("<LIST>"))setListOnline(data);
        if(data.contains("<CHAT>")){
            if(data.contains("<FROM>")){
                nameFrom  = data.substring(data.indexOf("<FROM>")+6,data.indexOf("</FROM>"));
                if(data.contains("<TO>")){
                    nameTarget  = data.substring(data.indexOf("<TO>")+4,data.indexOf("</TO>"));
                    if(data.contains("<MSG>")){
                        String mes = data.substring(data.indexOf("<MSG>")+5,data.indexOf("</MSG>"));
                        if ("ALL".equals(nameTarget)){
                            ta_room.append(nameFrom + ": " + mes+"\n");
                            if("SERVEROFF".equals(mes)){
                                JOptionPane.showMessageDialog(null, "Không thể gửi!", "Warning", JOptionPane.INFORMATION_MESSAGE);       
                                System.exit(0);
                            }
                        }else{
                            Boolean run = false;
                            Enumeration e = listChat.keys();
                            String name = null;
                            String s = nameFrom + ": "+mes;
                            while(e. hasMoreElements()){
                                name=(String) e.nextElement();
                                if(name.compareTo(nameFrom)==0){
                                    if("STOP_CHAT".equals(mes)){
                                        listChat.get(name).showMessage(mes);
                                    }else{ 
                                        listChat.get(name).showMessage(s);
                                    }
                                    run = true;
                                    break;
                                    
                                }
                            }
                            if(run == false){
                                if(!"STOP_CHAT".equals(mes)){
                                    ThreadChat threadChat = new ThreadChat(this, nameFrom, dos);
                                    threadChat.showMessage(s);
                                }
                            }
                        }
                    }
                }
            }
        }
        if(data.contains("<CHECKONLINE/>")){
            sendMSG("<ONLINE>"+txt_name+"</ONLINE>");
        }
        if (data.contains("<FILE>")){
            if(data.contains("<FROM>")){
                    nameFrom  = data.substring(data.indexOf("<FROM>")+6,data.indexOf("</FROM>"));
                    if(data.contains("<TO>")){
                        nameTarget  = data.substring(data.indexOf("<TO>")+4,data.indexOf("</TO>"));
                        if(data.contains("<NAME>")){
                            String nameFile  = data.substring(data.indexOf("<NAME>")+6,data.indexOf("</NAME>"));
                            if(data.contains("<SIZE>")){
                                String size  = data.substring(data.indexOf("<SIZE>")+6,data.indexOf("</SIZE>"));
                                if("ALL".equals(nameTarget)){
                                    //TODO
                                }   
                                else {
                                    receiveFile(nameFrom,nameFile,size);
                                }
                                    
                            }
                            
                        }
                        
                    }
                            
                        
                }
        }
    }

    


    
}