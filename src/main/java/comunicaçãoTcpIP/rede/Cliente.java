package comunicaçãoTcpIP.rede;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) throws IOException {
        
    	 Scanner scan = new Scanner(System.in);
         Socket socket = new Socket("127.0.0.1", 54321);
         ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
         

         
         
     }
 }