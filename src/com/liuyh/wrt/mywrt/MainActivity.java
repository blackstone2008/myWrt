package com.liuyh.wrt.mywrt;
// packages for SSH


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
//import android.R;


//Below is a wrokaround that allow UI executes some time-consuming operation, such as connect to SSH server.
//@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity implements View.OnClickListener{
	
	public final static int OK = 0;
	public final static int FAIL = -1;
	public final static int READ_BUF_SIZE = 10240;
	public final static int REQ_ID_SETUP = 1;
	//Global handle for SSH connection
	private static Connection sshConnection = null;
	private static String hostname = "192.168.1.1";
	private static String username = "root";
	private static String password = "root";
	
	Context mContext;
	
	static void SSHDisConnectHost() {
		sshConnection.close();
		sshConnection = null;
	}
	
	static boolean connected() {
		return sshConnection != null;
	}
	
	static int SSHConnectHost() {
		if (hostname == null) {
			System.out.println("no host name provided.");
			return FAIL;
		}
		if(username == null || password  == null) { 
			System.out.println("username or password provided.");
			return FAIL;
		}
		// if current SSH connection is still connected, it need to be disconnected
		if (sshConnection != null)
			sshConnection.close();
		
		try
		{			
			/* Create a connection instance */
			sshConnection = new Connection(hostname);
			if (sshConnection == null)
				throw new IOException("Fatle Error: fail to create new Connection instance.");
			/* Now connect */
			sshConnection.connect();
			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			boolean isAuthenticated = sshConnection.authenticateWithPassword(username, password);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
		
		return OK;
	}
	
	@SuppressWarnings("resource")
	static String SSHExecuteCmd(String cmd) throws IOException {
		if (sshConnection == null) {
			System.out.println("Input not complete");
			return null;
		}
		if (cmd  == null) {
			System.out.println("Input not complete");
			return null;
		}
		/* Create a session */
		Session sshSession = sshConnection.openSession();
		if (sshSession == null)
			throw new IOException("Fatle Error: fail to create new Connection instance.");		
		sshSession.execCommand(cmd);
		
		System.out.println("Here is some information about the remote host:");
		/* 
		 * This basic example does not handle stderr, which is sometimes dangerous
		 * (please read the FAQ).
		 */
		InputStream stdout = new StreamGobbler(sshSession.getStdout());
		BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

		char[] buf = new char[READ_BUF_SIZE];
		int len = br.read(buf, 0, buf.length);
		if (len < 1) {
			System.out.println("!!!!! br.read returns null");
			return null;
		}
		stdout.close();
		br.close();
		sshSession.close();
		return new String(buf);
	}
	
	
	//below code is from the demo code, leave it here for reference.
	static String SSH2ExecuteCmd(String hostname, String username, String password, String cmd)
	{
		String str = null;
		if (hostname == null || username == null || password  == null || cmd  == null) {
			System.out.println("Input not complete");
		}
		try
		{
			/* Create a connection instance */
			sshConnection = new Connection(hostname);

			/* Now connect */
			sshConnection.connect();
			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			boolean isAuthenticated = sshConnection.authenticateWithPassword(username, password);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");
			/* Create a session */
			Session sess = sshConnection.openSession();
			if (cmd == null)
				sess.execCommand("uname -a && date && uptime && who"); //only for demo
			else
				sess.execCommand(cmd);
			System.out.println("Here is some information about the remote host:");
			/* 
			 * This basic example does not handle stderr, which is sometimes dangerous
			 * (please read the FAQ).
			 */
			InputStream stdout = new StreamGobbler(sess.getStdout());
			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
	
			char[] buf = new char[10000];
			int len = br.read(buf, 0, buf.length);
			if (len < 1) {
				System.out.println("!!!!! br.read returns null");
				System.out.println("length of buffer is " + len);
				System.out.println(len + ":  " + str);
			}
			str = new String(buf);
			//System.out.println(str);
			/* Show exit status, if available (otherwise "null") */
			System.out.println("ExitCode: " + sess.getExitStatus());
			/* Close this session */
			sess.close();
			/* Close the connection */
			sshConnection.close();		
			stdout.close();
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}

		return str;
	}	
	private ToggleButton tbOnOffControll;
	private Button btExecuteCmd;
	private EditText etCmd;
	private TextView tvExecuteResult;
	private static Handler handler=new Handler();
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);    
        //ToggleButton which connect/disconnect gateway
        tbOnOffControll = (ToggleButton)findViewById(R.id.toggleButtonOnOff);
        //Button which execute the command
    	btExecuteCmd = (Button) findViewById(R.id.Execute);
    	//EditTexit which accept command line to be executed
    	etCmd = (EditText) findViewById(R.id.Command);
    	//String which stores and displays result from SSH client
    	tvExecuteResult = (TextView) findViewById(R.id.ExecuteResult);

    	//Content used by Intent, which will open setup dialog
    	mContext = this;

    	btExecuteCmd.setOnClickListener(this);
    	tbOnOffControll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					//Connect to SSH server
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							handler.post(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									SSHConnectHost();
									btExecuteCmd.setEnabled(true);
									Toast msg = Toast.makeText(mContext, "SSH Server connected.",  Toast.LENGTH_LONG);
									tbOnOffControll.setTextColor(Color.GREEN);
									msg.show();
								}
							});
						}
						}).start();
				}
				else {
					//Disconnect 
					new Thread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									SSHDisConnectHost();
									btExecuteCmd.setEnabled(false);
									Toast msg = Toast.makeText(mContext, "SSH Server disconnected.",  Toast.LENGTH_LONG);
									tbOnOffControll.setTextColor(Color.RED);
									msg.show();
								}
							});
						}
					}).start();					
				}
			}
		});

    }
	//Disconnect the SSH session gracefully to avoid junk on SSH server.
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (sshConnection != null) {
			sshConnection.close();
			sshConnection = null;
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
			Intent intent = new Intent(mContext, setupDialog.class);
			int requestCode = REQ_ID_SETUP;
			startActivityForResult(intent, requestCode);
        	
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.Execute:
			String cmd = (String)etCmd.getText().toString();
			String result = null;
			try {
				result = SSHExecuteCmd(cmd);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tvExecuteResult.setText(result);
			
			break;

		default:
			break;
		}			
	}
	/*
	 * 
	 * receive setup configuration for diaglog setup
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQ_ID_SETUP && resultCode == 1) {
			String gatewayIP = data.getStringExtra("gatewayIP");
			String user = data.getStringExtra("username");
			String passwd = data.getStringExtra("password");
			String port = data.getStringExtra("port");
			
			tvExecuteResult.setText("gatewayIP: " + gatewayIP + "\n" + "username: " + username + "\n" + "password: " + password + "\n" + "port: " + port);
			hostname = gatewayIP;
			username = user;
			password = passwd;
		}
		
	}
}
