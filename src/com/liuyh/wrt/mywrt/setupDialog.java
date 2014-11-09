package com.liuyh.wrt.mywrt;

import android.R.string;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class setupDialog extends Activity{

	String gatewayIP;
	String username;
	String password;
	String port;
	Button btOk;
	Button btCancel;
	EditText etGatewayIP;
	EditText etUser;
	EditText etPassword;
	EditText etPort;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup_dialog);  
		
		btOk = (Button) findViewById(R.id.okButton);
		btCancel = (Button) findViewById(R.id.cancelButton);
		etGatewayIP = (EditText)findViewById(R.id.gatewayIP);
		etUser = (EditText)findViewById(R.id.username);
		etPassword = (EditText)findViewById(R.id.password);
		etPort = (EditText)findViewById(R.id.portnum);	
		
		btOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				gatewayIP = etGatewayIP.getText().toString();
				username = etUser.getText().toString();
				password = etPassword.getText().toString();
				port = etPort.getText().toString();
				
				Intent data = new Intent();
				data.putExtra("gatewayIP", gatewayIP);
				data.putExtra("username", username);
				data.putExtra("password", password);
				data.putExtra("port", port);
				
				setResult(1,data);
				finish();
			}
		});
		
		btCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
} 
