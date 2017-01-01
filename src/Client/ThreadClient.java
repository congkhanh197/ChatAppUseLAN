/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author Khanh Tran-Cong
 */
public class ThreadClient extends Thread{
    private boolean run;
    private final DataInputStream dis;
    private final Client client;

    public ThreadClient(Client client,DataInputStream dis){
	run=true;
	this.client=client;
	this.dis=dis;
	this.start();
    }
    @Override
    public void run(){
	String data;
	while(run){
            try {
		data=dis.readUTF();
                System.out.println("Du lieu vao:"+data);
                client.processData(data);
            } catch (IOException e) {
		e.printStackTrace();
            }
        }
    }
    public void stopThread(){
        this.run=false;
    }
}
