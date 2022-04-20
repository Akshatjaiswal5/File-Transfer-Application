import java.net.*;
import javax.swing.*;
import java.io.*;


public class Client extends Thread
{
 String id,host;
 Sender sender;
 Socket socket;
 InputStream is;
 OutputStream os;
 int port;
 File file;
 
 private byte[] createHeader(String fileName,long fileLength)
 {
  while(fileName.length()<1011)
  fileName+=" ";

  String length=String.valueOf(fileLength);
  
  while(length.length()<13)
  length="0"+length;

  String result=fileName+length;

  return result.getBytes();
 }

 private void sendInChunks(byte[] data) throws IOException
 {
  int bytesToSend= data.length;
  int chunkSize=1024;
  for(int i=0;i<bytesToSend;i+=chunkSize)
  {
   if((bytesToSend-i)<chunkSize)
   chunkSize=bytesToSend-i;

   os.write(data,i,chunkSize);
   os.flush();
  } 
 }

 private void waitForAcknowledgement() throws IOException
 {
  int byteReadCount;
  byte ack[]=new byte[1];
  while(true)
  {
   byteReadCount=is.read(ack);
   if(byteReadCount==-1) continue;

   break;
  } 
 }

 public Client(Sender sender,File file,String id,String host,int port)
 {
  this.id=id;
  this.sender=sender;
  this.file=file;
  this.host=host;
  this.port=port;
 }
 
 public void run()
 {
  try
  {
   socket= new Socket(host,port);
   is= socket.getInputStream();
   os= socket.getOutputStream();
   long fileLength=file.length();
   String fileName=file.getName();

   byte header[]= createHeader(fileName,fileLength);

   sendInChunks(header);
 
   waitForAcknowledgement();

   FileInputStream fis= new FileInputStream(file);
   byte bytes[]=new byte[4096];
   int byteReadCount;
   for(long i =0;i<fileLength;i+=byteReadCount)
   {
    byteReadCount=fis.read(bytes);
    os.write(bytes,0,byteReadCount);
    os.flush();

    long percentDone=((i+byteReadCount)*100)/fileLength;
    SwingUtilities.invokeLater(()->{
     sender.setUploadProgress(id,percentDone);
    });
   }
   fis.close();
   
   waitForAcknowledgement();
   socket.close();

  }
  catch(Exception e)
  {
   e.printStackTrace();
  }
 }
}