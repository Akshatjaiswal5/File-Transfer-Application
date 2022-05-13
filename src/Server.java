import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;

import java.io.*;

class RequestProcessor extends Thread
{
 Socket socket;
 InputStream is;
 OutputStream os;
 String id;
 Reciever reciever;

 private long parseHeaderLength(byte[] header)
 {
  byte a[]= new byte[13];
  
  for(int i=1011,j=0;i<1024;i++,j++)
  {
   a[j]=header[i];
  }

  String l= new String(a);
  
  return Long.parseLong(l);
 }

 private String parseHeaderName(byte[] header)
 {
  byte a[]= new byte[1011];
  
  for(int i=0;i<1011;i++)
  {
   a[i]=header[i];
  }

  String name= new String(a);
  return name.trim();
 }

 private byte[] recieveChunks(int bytesToRecieve) throws IOException
 {
  int bytesRead;
  byte[] recievedData=new byte[bytesToRecieve];
  byte[] tmp= new byte[1024];

  for(int bytesRecieved=0;bytesRecieved<bytesToRecieve;)
  {
   bytesRead=is.read(tmp);
   if(bytesRead==-1)
   continue;
  
   for(int i=0;i<bytesRead;i++)
   {
    recievedData[bytesRecieved]=tmp[i];
    bytesRecieved++;
   }
  }
  return recievedData;
 }

 private void sendAcknowledgement() throws IOException
 {
  byte ack[]=new byte[1];
  os.write(ack,0,1);
  os.flush();
 }
 
 public void run()
 {
  try
  {
   SwingUtilities.invokeLater(new Runnable() {
    public void run() {
     reciever.log("Client connected and id is:"+id);
    }
   });

   is=socket.getInputStream();
   os=socket.getOutputStream();

   byte headerRecieved[]=recieveChunks(1024);

   String fileName= parseHeaderName(headerRecieved);
   long fileLength= parseHeaderLength(headerRecieved);

   File file= new File("uploads"+File.separator+fileName);
   if(file.exists())
   file.delete();

   sendAcknowledgement();

   FileOutputStream fos= new FileOutputStream(file);
   int chunkSize=4096;
   byte bytes[];
   SwingUtilities.invokeLater(()->{
    reciever.log("Recieving file: "+fileName+" size:"+(fileLength/1024)+"kb.");
   });
   for(long i =0;i<fileLength;i+=chunkSize)
   {
    if(fileLength-i<chunkSize)
    chunkSize=(int)(fileLength-i);
    
    bytes=recieveChunks(chunkSize);
    fos.write(bytes,0,chunkSize);
    fos.flush();
   }

   fos.close();
   sendAcknowledgement();
   socket.close();
   SwingUtilities.invokeLater(()->{
    reciever.log("Recieved file saved to:"+file.getAbsolutePath());
    reciever.log("Connection with Client id:"+id+" closed.");
   });
  }
  catch(Exception e)
  {
   e.printStackTrace();
  }  
 }

 RequestProcessor(Socket socket, String id, Reciever reciever)
 {
  this.socket=socket;
  this.id=id;
  this.reciever=reciever;
  start();
 }
}

public class Server extends Thread
{
 ServerSocket serverSocket;
 Reciever reciever;

 public void shutdown()
 {
  try
   {
    serverSocket.close();
   }
   catch(Exception e)
   {
    e.printStackTrace();
   }  
 }
 public void run()
 {
  try
   {
    serverSocket= new ServerSocket(5500);
    startListening();
   }
   catch(Exception e)
   {
    reciever.log("Server stopped");
   } 
 }
 public void startListening()
 {
  try 
  {
   Socket socket;
   RequestProcessor requestProcessor;
   while(true)
   {
    SwingUtilities.invokeLater(new Thread(){
     public void run()
     {
      reciever.log("Server is started and serving on port 5500");
     }
    });
    socket=serverSocket.accept();
    requestProcessor= new RequestProcessor(socket,UUID.randomUUID().toString(),reciever);
   }
  } 
  catch(Exception e)
  {
   reciever.log("Server stopped.");
  }
 }
 Server(Reciever reciever)
 {
  this.reciever=reciever;
 }
}

