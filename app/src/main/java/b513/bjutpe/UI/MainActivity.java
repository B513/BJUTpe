package b513.bjutpe.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import b513.bjutpe.R;
import android.widget.Button;
import android.os.AsyncTask;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

	TextView tvError;
	EditText etUname,etPasswd,etVcode;
	ImageButton ibVcode;
	Button btnLogin;
	
	public void onLoginButtonClicked(View v){
		String uname=etUname.getText().toString();
		String passwd=etPasswd.getText().toString();
		String vcode=etVcode.getText().toString();
		if(uname.length()==0){
			showError("请填写用户名");
			return;
		}if(passwd.length()==0){
			showError("请填写密码");
			return;
		}if(vcode.length()==0){
			showError("请填写验证码");
			return;
		}
		tvError.setVisibility(View.GONE);
		new LoginTask(uname,passwd,vcode,null).execute();
	}
	
	public void onGetVcodeButtonClicked(View v){
		new GetVcodeTask().execute();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		Toast.makeText(this,"WildHunter is a gay.",1).show();
		
		etUname=(EditText) findViewById(R.id.main_etUsername);
		etPasswd=(EditText) findViewById(R.id.main_etPassword);
		etVcode=(EditText) findViewById(R.id.main_etVerify);
		ibVcode=(ImageButton) findViewById(R.id.main_ibVerify);
		btnLogin=(Button) findViewById(R.id.main_btnLogin);
		tvError=(TextView) findViewById(R.id.main_tvError);
		//
    }
	
	private void showError(String err){
		tvError.setVisibility(View.VISIBLE);
		tvError.setText(err);
	}
	
	class LoginTask extends AsyncTask<Void,Void,String> {
		private String uname,passwd,vcode,cookie;
		public LoginTask(String uname,String passwd,String vcode,String cookie){
			this.uname=uname;this.passwd=passwd;
			this.vcode=vcode;this.cookie=cookie;
		}

		@Override
		protected void onPreExecute() {
			// TODO: Implement this method
			super.onPreExecute();
			MainActivity.this.ibVcode.setEnabled(false);
			MainActivity.this.btnLogin.setEnabled(false);
			MainActivity.this.btnLogin.setText("登录中...");
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO: Implement this method
			super.onPostExecute(result);
			MainActivity.this.ibVcode.setEnabled(true);
			MainActivity.this.btnLogin.setEnabled(true);
			MainActivity.this.btnLogin.setText("登录");
		}
		@Override
		protected String doInBackground(Void[] p1) {
			// TODO: Implement this method
			try {
				Thread.currentThread().sleep(1000);
			}
			catch(InterruptedException e) {}
			return null;
		}
	}
	
	class GetVcodeTask extends AsyncTask<Void,Void,String> {
		public GetVcodeTask(){
		}

		@Override
		protected void onPreExecute() {
			// TODO: Implement this method
			super.onPreExecute();
			MainActivity.this.btnLogin.setEnabled(false);
			MainActivity.this.ibVcode.setEnabled(false);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			MainActivity.this.ibVcode.setEnabled(true);
			MainActivity.this.btnLogin.setEnabled(true);
		}
		@Override
		protected String doInBackground(Void[] p1) {
			//
			return null;
		}
	}
	
}
