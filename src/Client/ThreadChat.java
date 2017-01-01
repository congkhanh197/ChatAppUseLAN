/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Form.ChatForm;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Khanh Tran-Cong
 */
public class ThreadChat extends Thread implements ActionListener{
    private final Client client;
    private final ChatForm chatForm;
    private final JButton btn_send,btn_file,btn_stop;
    private final JTextArea ta_chat;
    private JTextField txt_message;
    private final String nameTarget;
    private final DataOutputStream dos;
    private boolean run;
    
    public ThreadChat(Client client,String nameTarget, DataOutputStream dos){
        this.chatForm = new ChatForm();
        chatForm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        chatForm.setVisible(true);
        this.btn_send = chatForm.getButtonSend();
        btn_send.addActionListener(this);
        this.btn_file = chatForm.getButtonFile();
        btn_file.addActionListener(this);
        this.ta_chat = chatForm.getTextArea();
	this.client=client;
        this.nameTarget = nameTarget;
        chatForm.setTitle("Chat with "+nameTarget);
        this.dos = dos;
        this.btn_stop = chatForm.getButtonStop();
        btn_stop.addActionListener(this);
	this.start();
        run= true;
        txt_message = chatForm.getMessage();
        txt_message.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt){
                if (evt.getKeyCode() == '\n') {
                    if(txt_message.getText() != null){
                        try {
                            sendMSG(nameTarget,txt_message.getText());
                            ta_chat.append("Tôi: "+ txt_message.getText()+"\n");
                            txt_message.setText("");
                        } catch (IOException ex) {
                                Logger.getLogger(ThreadChat.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            
        });
    }
    
    @Override
    public void run(){
        client.listChat.put(nameTarget,this);
        while(run){
            
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== btn_send){
           txt_message = chatForm.getMessage();
           if(txt_message.getText() != null){
               try {
                   sendMSG(nameTarget,txt_message.getText());
                   ta_chat.append("Tôi: "+ txt_message.getText()+"\n");
                   txt_message.setText("");
               } catch (IOException ex) {
                   Logger.getLogger(ThreadChat.class.getName()).log(Level.SEVERE, null, ex);
               }
           }
        }
        if(e.getSource()== btn_stop){
            try {
                sendMSG(nameTarget,"STOP_CHAT");
                stopThread();
            } catch (IOException ex) {
                Logger.getLogger(ThreadChat.class.getName()).log(Level.SEVERE, null, ex);
            }
}
        if(e.getSource()== btn_file){
            client.sendFileText(nameTarget);
        }
    }
    public void showMessage(String message){
        if("STOP_CHAT".equals(message)){
            try {
                stopThread();
            } catch (IOException ex) {
                Logger.getLogger(ThreadChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            ta_chat.append(message+"\n");
        }
    }
    
    public void stopThread() throws IOException{
        run = false;
        client.listChat.remove(nameTarget);
        chatForm.setVisible(false);
    }
    public void sendMSG(String to, String data) throws IOException{
        String a = "<CHAT>"
            +"<FROM>"+client.txt_name+"</FROM>"
            +"<TO>"+to+"</TO>"
            +"<MSG>"+data+"</MSG>"
            +"</CHAT>";
        dos.writeUTF(a); 
        System.out.println("Du lieu ra Thread:"+a);
        dos.flush();
    }
}
    
