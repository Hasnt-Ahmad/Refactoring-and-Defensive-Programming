import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/*************************/
class MyServer
{
	ArrayList al=new ArrayList();
	
	//Users List
	ArrayList users=new ArrayList();
	//Creating Server Socket class Object
	ServerSocket serverSocket;
	//Creating Client Socket class Object
	Socket socket;

	public final static int PORT=8080;
	public final static String UPDATE_USERS="updateuserslist:";
	public final static String LOGOUT_MESSAGE="@@logoutme@@:";
	public MyServer()
	{
		try{
			
			//Creating server socket with port
			serverSocket=new ServerSocket(PORT);
			
			System.out.println("Server Started "+serverSocket);
			while(true)
			{
				//server socket now will listen for new connections
				socket=serverSocket.accept();
				
				//Runnable used to write applications which can run in a separate thread.
				Runnable runnable=new MyThread(socket,al,users);
				
				//Creating new Thread
				Thread thread=new Thread(runnable);
				//Thread started
				thread.start();
//	System.out.println("Total alive clients : "+serverSocet.);
			}
		}
		catch(Exception e){
			System.err.println("Server constructor"+e);
		}
	}
	
/////////////////////////
	
public static void main(String [] args)
{

new MyServer();

}
/////////////////////////
}

/*************************/
class MyThread implements Runnable
{
Socket socket;
ArrayList al;
ArrayList users;
String username;
///////////////////////
MyThread (Socket socket, ArrayList al,ArrayList users){
	this.socket=socket;
	this.al=al;
	this.users=users;
	try{
		//creating dataInputstream object which returns output stream for this socket
		DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
		
		//convert bytes to UTF8
		username=dataInputStream.readUTF();
		al.add(socket);
		
		//add user to ArrayList
		users.add(username);
		
		tellEveryOne("****** "+ username+" Logged in at "+(new Date())+" ******");
		sendNewUserList();
	    }
	catch(Exception e){
		System.err.println("MyThread constructor  "+e);
		}
	}

///////////////////////
public void run()
{
String message;
try{
	//creating dataInputstream object which will returns input stream  this socket
	DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
	do
	{
		//convert bytes to UTF8
	message=dataInputStream.readUTF();
	
	if(message.toLowerCase().equals(MyServer.LOGOUT_MESSAGE)) break;
//	System.out.println("received from "+s.getPort());
	tellEveryOne(username+" said: "+" : "+message);
	}
	while(true);
	
	//creating dataInputstream object which will returns output stream for  this socket
	DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
	
	//write message on socket
	dataOutputStream.writeUTF(MyServer.LOGOUT_MESSAGE);
	//Flush data
	dataOutputStream.flush();
	users.remove(username);
	tellEveryOne("****** "+username+" Logged out at "+(new Date())+" ******");
	sendNewUserList();
	al.remove(socket);
	
	//close socket
	socket.close();

   }
catch(Exception e){
	System.out.println("MyThread Run"+e);
	}
}
////////////////////////
public void sendNewUserList()
{
	tellEveryOne(MyServer.UPDATE_USERS+users.toString());

}
////////////////////////
public void tellEveryOne(String message)	
{
Iterator iterator=al.iterator();
while(iterator.hasNext())
{
try{
	Socket temp=(Socket)iterator.next();
	DataOutputStream dataOutputStream=new DataOutputStream(temp.getOutputStream());
	dataOutputStream.writeUTF(message);
	dataOutputStream.flush();
	//System.out.println("sent to : "+temp.getPort()+"  : "+ s1);
   }
catch(Exception e){System.err.println("TellEveryOne "+e);}
}
}
///////////////////////
}
/*********************************/
class MyClient implements ActionListener
{
Socket socket;
DataInputStream dataInputStream;
DataOutputStream dataOutputStream;

JButton sendButton, logoutButton,loginButton, exitButton;
JFrame chatWindow;
JTextArea txtBroadcast;
JTextArea txtMessage;
JList usersList;

//////////////////////////
public void displayGUI()
{
chatWindow=new JFrame();
txtBroadcast=new JTextArea(5,30);
txtBroadcast.setEditable(false);
txtMessage=new JTextArea(2,20);
usersList=new JList();

sendButton=new JButton("Send");
logoutButton=new JButton("Log out");
loginButton=new JButton("Log in");
exitButton=new JButton("Exit");

JPanel center1=new JPanel();
center1.setLayout(new BorderLayout());
center1.add(new JLabel("Broad Cast messages from all online users",JLabel.CENTER),"North");
center1.add(new JScrollPane(txtBroadcast),"Center");

JPanel south1=new JPanel();
south1.setLayout(new FlowLayout());
south1.add(new JScrollPane(txtMessage));
south1.add(sendButton);

JPanel south2=new JPanel();
south2.setLayout(new FlowLayout());
south2.add(loginButton);
south2.add(logoutButton);
south2.add(exitButton);

JPanel south=new JPanel();
south.setLayout(new GridLayout(2,1));
south.add(south1);
south.add(south2);

JPanel east=new JPanel();
east.setLayout(new BorderLayout());
east.add(new JLabel("Online Users",JLabel.CENTER),"East");
east.add(new JScrollPane(usersList),"South");

chatWindow.add(east,"East");

chatWindow.add(center1,"Center");
chatWindow.add(south,"South");

chatWindow.pack();
chatWindow.setTitle("Login for Chat");
chatWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
chatWindow.setVisible(true);
sendButton.addActionListener(this);
logoutButton.addActionListener(this);
loginButton.addActionListener(this);
exitButton.addActionListener(this);
sendButton.setEnabled(false);
logoutButton.setEnabled(false);
loginButton.setEnabled(true);
txtMessage.addFocusListener(new FocusAdapter()
{public void focusGained(FocusEvent fe){txtMessage.selectAll();}});

chatWindow.addWindowListener(new WindowAdapter()
{
public void windowClosing(WindowEvent ev)
{
if(socket!=null)
{
JOptionPane.showMessageDialog(chatWindow,"u r logged out right now. ","Exit",JOptionPane.INFORMATION_MESSAGE);
logoutSession();
}
System.exit(0);
}
});
}
///////////////////////////
public void actionPerformed(ActionEvent ev)
{
JButton temp=(JButton)ev.getSource();
if(temp==sendButton)
{

	//Check message is empty or not
	if(txtMessage.getText().length()==0) {
		
			JFrame f;
			f=new JFrame();
			JOptionPane.showMessageDialog(f,"Messgae cannot be Empty");
			return ;	
	}
	
if(socket==null)
 	{JOptionPane.showMessageDialog(chatWindow,"u r not logged in. plz login first"); return;
 	}
try{
	dataOutputStream.writeUTF(txtMessage.getText());
	txtMessage.setText("");
     }
catch(Exception excp){txtBroadcast.append("\nsend button click :"+excp);}
}
if(temp==loginButton)
{
String username=JOptionPane.showInputDialog(chatWindow,"Enter Your lovely nick name: ");


JFrame f;
f=new JFrame();
try 
{ 
	if(username.length()==0) {
		JOptionPane.showMessageDialog(f,"UserName Cannot be Empty");
		return ;	
	}
	
	Integer.parseInt(username); 
	JOptionPane.showMessageDialog(f,"UserName Cannot be Integer");
	return ;
}  
catch (NumberFormatException e)  
{ 
	System.out.println(); 
} 


if(username!=null)
	sendButton.setEnabled(true);
	clientChat(username); 
}
if(temp==logoutButton)
{
if(socket!=null)
	logoutSession();
}
if(temp==exitButton)
{
if(socket!=null)
{
JOptionPane.showMessageDialog(chatWindow,"u r logged out right now. ","Exit",JOptionPane.INFORMATION_MESSAGE);
logoutSession();
}
System.exit(0);
}
}
///////////////////////////
public void logoutSession()
{
if(socket==null) return;
try{
dataOutputStream.writeUTF(MyServer.LOGOUT_MESSAGE);
Thread.sleep(500);
socket=null;
}
catch(Exception e){txtBroadcast.append("\n inside logoutSession Method"+e);}

logoutButton.setEnabled(false);
loginButton.setEnabled(true);
sendButton.setEnabled(false);
chatWindow.setTitle("Login for Chat");
}
//////////////////////////
public void clientChat(String username)
{
try{
	
	//Create socket for the client
     socket=new Socket(InetAddress.getLocalHost(),MyServer.PORT);
     
     dataInputStream=new DataInputStream(socket.getInputStream());
     dataOutputStream=new DataOutputStream(socket.getOutputStream());
     ClientThread clientthread=new ClientThread(dataInputStream,this);
     Thread thread=new Thread(clientthread);
     thread.start();
     dataOutputStream.writeUTF(username);
     chatWindow.setTitle(username+" Chat Window");
    }
catch(Exception e){
	txtBroadcast.append("\nClient Constructor " +e);
	}
logoutButton.setEnabled(true);
loginButton.setEnabled(false);
}
///////////////////////////////
public MyClient()
{
  	displayGUI();
//	clientChat();
}
///////////////////////////////
public static void main(String []args)
{
new MyClient();
}
//////////////////////////
}
/*********************************/
class ClientThread implements Runnable
{
DataInputStream dataInputStream;
MyClient client;

ClientThread(DataInputStream dataInputStream,MyClient client)
{
this.dataInputStream=dataInputStream;
this.client=client;
}
////////////////////////
public void run()
{
String message="";
do
    {
	try{
		message=dataInputStream.readUTF();
		if(message.startsWith(MyServer.UPDATE_USERS))
			updateUsersList(message);
		else if(message.equals(MyServer.LOGOUT_MESSAGE))
			break;
		else
			client.txtBroadcast.append("\n"+message);
		int lineOffset=client.txtBroadcast.getLineStartOffset(client.txtBroadcast.getLineCount()-1);
		client.txtBroadcast.setCaretPosition(lineOffset);
	     }
	catch(Exception e){client.txtBroadcast.append("\nClientThread run : "+e);}
   }
while(true);
}
//////////////////////////
public void updateUsersList(String ul)
{
Vector ulist=new Vector();

ul=ul.replace("[","");
ul=ul.replace("]","");
ul=ul.replace(MyServer.UPDATE_USERS,"");
StringTokenizer st=new StringTokenizer(ul,",");

while(st.hasMoreTokens())
{
String temp=st.nextToken();
ulist.add(temp);
}
client.usersList.setListData(ulist);
}
/////////////////////////
}
/*********************************/
