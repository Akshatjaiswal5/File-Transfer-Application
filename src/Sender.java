import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;



class Sender extends JFrame
{
 class FileTableModel extends AbstractTableModel
 {
  ArrayList<File> files;

  public FileTableModel(){
   files=new ArrayList<>();
  }

  public int getRowCount() {
   return files.size();
  }

  public String getColumnName(int columnIndex)
  {
   if(columnIndex==0)
   return "S.no";
   return "File";
  }

  public Class getColumnClass(int c)
  {
   if(c==0)
   return Integer.class;
   return String.class;
  }

  public int getColumnCount() {
   return 2;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
   if(columnIndex==0)
   return rowIndex+1;
   
   return files.get(rowIndex).getName();
  }
 
  public boolean isCellEditable(int rowIndex, int columnIndex){
   return false;
  }

  
  public void add(File file)
  {
   files.add(file);
   fireTableDataChanged();
  }
  public ArrayList<File> getFiles()
  {
   return files;
  }
 }

 class ProgressPanel extends JPanel
 {
  private File file;
  private JLabel fileName;
  private JProgressBar progressBar;
  public ProgressPanel(File file)
  {
   this.file=file;
   fileName=new JLabel("Uploading: "+file.getName());
   progressBar=new JProgressBar(1,100);
   setLayout(new GridLayout(1,2));
   fileName.setFont(new Font("Default",Font.PLAIN,15));
   add(fileName);
   add(progressBar);
  }
  public void updateProgressBar(Long value)
  {
   if(progressBar.getValue()==value.intValue())
   return;

   progressBar.setValue(value.intValue());
   System.out.println(value+"% sent");
   progressBar.revalidate();
   progressBar.repaint();
  }
 }
 
 JPanel leftPanel,rightPanel,progressPanelsContainer;
 JLabel leftLabel,rightLabel,IPLabel,portLabel;
 JTextField IPField,portField;
 JTable filesList;
 FileTableModel model;
 JButton add,upload;
 JScrollPane filesListjsp,progressPaneljsp;
 Map<String,ProgressPanel> progressPanelsList;


 void setUploadProgress(String id,Long percentDone)
 {
  progressPanelsList.get(id).updateProgressBar(percentDone);
 }

 void prepareResources()
 {}
 void initComponents()
 {

  leftPanel=new JPanel();
  leftLabel= new JLabel("Select Files");
  IPLabel= new JLabel("host:");
  IPField= new JTextField();
  portLabel=new JLabel("port:");
  portField= new JTextField();
  model=new FileTableModel();
  filesList= new JTable(model);
  filesListjsp=new JScrollPane(filesList,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
  add=new JButton("Add Files");
  upload= new JButton("Upload");
  
  rightPanel= new JPanel();
  rightLabel= new JLabel("Progress");
 }
 void setAppearance()
 {
  setLayout(new GridLayout(1,2));

  leftPanel.setLayout(null);
  leftPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

  leftLabel.setFont(new Font("",Font.PLAIN,25));
  leftLabel.setBounds(20,20,200,20);
  leftPanel.add(leftLabel);

  IPLabel.setFont(new Font("",Font.PLAIN,15));
  IPLabel.setBounds(20,60,60,20);
  leftPanel.add(IPLabel);

  IPField.setFont(new Font("",Font.PLAIN,15));
  IPField.setBounds(55,60,100,20);
  leftPanel.add(IPField);

  portLabel.setFont(new Font("",Font.PLAIN,15));
  portLabel.setBounds(165,60,100,20);
  leftPanel.add(portLabel);

  portField.setFont(new Font("",Font.PLAIN,15));
  portField.setBounds(200,60,50,20);
  leftPanel.add(portField);

  filesList.setFont(new Font("Default",Font.PLAIN,20));
  filesList.setRowHeight(30);
  filesList.getColumnModel().getColumn(0).setPreferredWidth(100);
  filesList.getColumnModel().getColumn(1).setPreferredWidth(900);
  filesList.setRowSelectionAllowed(true);
  filesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
  centerRenderer.setHorizontalAlignment( JLabel.CENTER );
  filesList.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);   
  JTableHeader tableHeader=filesList.getTableHeader();
  tableHeader.setFont(new Font("Default",Font.PLAIN,15));
  tableHeader.setReorderingAllowed(false);
  tableHeader.setResizingAllowed(false);
  
  filesListjsp.setBounds(20,90,355,470);
  leftPanel.add(filesListjsp);

  add.setFont(new Font("",Font.PLAIN,20));
  add.setBounds(20,570,130,35);
  leftPanel.add(add);

  upload.setFont(new Font("",Font.PLAIN,20));
  upload.setBounds(245,570,130,35);
  leftPanel.add(upload);

  add(leftPanel);

  rightLabel.setFont(new Font("",Font.PLAIN,25));
  rightPanel.add(rightLabel);


  add(rightPanel);
 }
 void addListeners()
 {
  add.addActionListener(new ActionListener(){
   public void actionPerformed(ActionEvent e) {
    JFileChooser jfc= new JFileChooser();
    jfc.setCurrentDirectory(new File("."));
    int selected=jfc.showOpenDialog(Sender.this);
  
    if(selected!=JFileChooser.APPROVE_OPTION)
    return;
  
    File file=jfc.getSelectedFile();
    model.add(file);
   }
  });

  upload.addActionListener(new ActionListener(){
   public void actionPerformed(ActionEvent e) {
    if(model.getFiles().size()==0)
    {
     JOptionPane.showMessageDialog(Sender.this,"No files selected to upload");
     return;
    }
    ArrayList<Client> clientList=new ArrayList<>();
    progressPanelsContainer=new JPanel();
    progressPanelsList=new HashMap<>();
    progressPanelsContainer.setLayout(new GridLayout(model.getFiles().size(),1));
    
    model.getFiles().forEach((file)->{

     String id=UUID.randomUUID().toString();
     
     ProgressPanel p= new ProgressPanel(file);
     progressPanelsList.put(id,p);
     progressPanelsContainer.add(p);
     Client c=new Client(Sender.this, file, id, "localhost", 5500);
     clientList.add(c);
    });
    progressPaneljsp=new JScrollPane(progressPanelsContainer,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    rightPanel.add(progressPaneljsp);
    progressPaneljsp.setBounds(40,190,355,470);
    rightPanel.revalidate();
    rightPanel.repaint();

    clientList.forEach((client)->{
     client.start();
    });
   }
  });
 }



 Sender()
 {
  prepareResources();
  initComponents();
  setAppearance();
  addListeners();

  setDefaultCloseOperation(EXIT_ON_CLOSE);
  this.setVisible(true);
  this.setSize(800,650);
  this.setLocation(100,100);
 }
 public static void main(String[] args)
 {
  Sender s= new Sender(); 
 }
}