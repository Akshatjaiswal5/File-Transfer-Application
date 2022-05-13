import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;


public class Reciever extends JFrame
{
 JLabel title;
 JTextArea logBox;
 JButton button;
 JScrollPane jsp;
 Server server;
 Boolean serverState=false;

 void log(String s)
 {
  this.logBox.append(s+"\n");
 }

 void prepareResources()
 {
 }
 void initComponents()
 {
  title=new JLabel("Reciever");
  logBox= new JTextArea();
  button= new JButton("Start");
 }
 void setAppearance()
 {
  setLayout(null);

  title.setFont(new Font("",Font.PLAIN,25));
  title.setBounds(20,20,200,20);
  add(title);

  logBox.setFont(new Font("",Font.PLAIN,20));
  jsp=new JScrollPane(logBox,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
  jsp.setBounds(20,60,550,420);
  add(jsp);

  button.setFont(new Font("",Font.PLAIN,25));
  button.setBounds(20,500,100,40);
  add(button);
  
 }
 void addListeners()
 {
  button.addActionListener(new ActionListener(){
   public void actionPerformed(ActionEvent ev)
   {
    if(!serverState)
    {
     server=new Server(Reciever.this);
     server.start();
     serverState=true;
     button.setText("Stop");
    }
    else
    {
     server.shutdown();
     serverState=false;
     button.setText("Start");
    }
   }
  });
 }

 Reciever()
 {
  prepareResources();
  initComponents();
  setAppearance();
  addListeners();


  setVisible(true);
  setSize(600,600);
  setLocation(100,100);
  setDefaultCloseOperation(EXIT_ON_CLOSE);
 }
 public static void main(String[] args)
 {
  Reciever r= new Reciever(); 
 }
}
