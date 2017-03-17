package b513.bjutpe.UI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import b513.bjutpe.R;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

	TextView tvError,tvGetVcode,tvLogcat;
	EditText etUname,etPasswd,etVcode;
	ImageView ibVcode;
	Button btnLogin;
	
	List<Cookie> cookieso,cookiesp;
	
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
		
		tvGetVcode=(TextView) findViewById(R.id.main_tvgetvcode);
		etUname=(EditText) findViewById(R.id.main_etUsername);
		etPasswd=(EditText) findViewById(R.id.main_etPassword);
		etVcode=(EditText) findViewById(R.id.main_etVerify);
		ibVcode=(ImageView) findViewById(R.id.main_ibVerify);
		btnLogin=(Button) findViewById(R.id.main_btnLogin);
		tvError=(TextView) findViewById(R.id.main_tvError);
		tvLogcat=(TextView) findViewById(R.id.main_logcat);
		//
    }
	
	private void log(String s){
		tvLogcat.append(">>> ");
		tvLogcat.append(s);
		tvLogcat.append("\n");
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
			MainActivity.this.tvGetVcode.setEnabled(false);
			MainActivity.this.btnLogin.setEnabled(false);
			MainActivity.this.btnLogin.setText("登录中...");
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO: Implement this method
			super.onPostExecute(result);
			MainActivity.this.ibVcode.setEnabled(true);
			MainActivity.this.ibVcode.setEnabled(true);
			MainActivity.this.btnLogin.setEnabled(true);
			MainActivity.this.btnLogin.setText("登录");
		}
		@Override
		protected String doInBackground(Void[] p1) {
			// TODO: Implement this method
			/*try {
				Thread.currentThread().sleep(1000);
			}
			catch(InterruptedException e) {}*/
			Cookie coo=null;
			for(Cookie co:cookieso){
				if(co.name().toLowerCase().equals("jsessionid")){
					coo=co;
					break;
				}
			}
			if(coo==null){
				return "0";
			}
			
			OkHttpClient hc=new OkHttpClient.Builder()
				.cookieJar(new CookieJar(){
					@Override
					public void saveFromResponse(HttpUrl p1,List<Cookie> p2) {
						cookiesp=p2;
					}
					@Override
					public List<Cookie> loadForRequest(HttpUrl p1) {
						return cookieso;
					}
				})
				.build();
			FormBody fb=new FormBody.Builder()
			.add("username",uname)
			.add("userpwd",passwd)
			.add("code",vcode)
			.build();
			Request req=new Request.Builder()
				.url("http://eol.bjut.edu.cn/Login.do;"+coo.name()+"="+coo.value())
				.post(fb)
				.build();
			Response res;
			try {
				res=hc.newCall(req).execute();
				String data=res.body().string();
				FileWriter fos=new FileWriter(
				new File("/sdcard/whig.html"));
				fos.write(data);
				fos.close();
			}
			catch(Exception e) {
				return null;
			}
			return null;
		}
	}
	
	class GetVcodeTask extends AsyncTask<Void,Void,String> {
		private boolean gotBmp;
		private Bitmap bmp;
		public GetVcodeTask(){
			gotBmp=false;
		}
		@Override
		protected void onPreExecute() {
			// TODO: Implement this method
			super.onPreExecute();
			MainActivity.this.btnLogin.setEnabled(false);
			MainActivity.this.ibVcode.setEnabled(false);
			MainActivity.this.ibVcode.setEnabled(false);
			MainActivity.this.ibVcode.setVisibility(View.GONE);
			MainActivity.this.tvGetVcode.setVisibility(View.VISIBLE);
			MainActivity.this.tvGetVcode.setText("加载中..");
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			MainActivity.this.ibVcode.setEnabled(true);
			MainActivity.this.btnLogin.setEnabled(true);
			if(gotBmp){
				ibVcode.setImageBitmap(bmp);
				ibVcode.setVisibility(View.VISIBLE);
				tvGetVcode.setVisibility(View.GONE);
				for(Cookie coo:cookieso){
					log(coo.name()+":"+coo.value());
				}
			}else{
				//ibVcode.setVisibility(View.GONE);
				//tvGetVcode.setVisibility(View.VISIBLE);
				tvGetVcode.setText("获取验证码");
				Toast.makeText(MainActivity.this,"获取验证码失败",0).show();
			}
		}
		@Override
		protected String doInBackground(Void[] p1) {
			//if(true)return null;
			OkHttpClient hc=new OkHttpClient.Builder()
				.cookieJar(new CookieJar(){
					@Override
					public void saveFromResponse(HttpUrl p1,List<Cookie> p2) {
						cookieso=p2;
					}
					@Override
					public List<Cookie> loadForRequest(HttpUrl p1) {
						return new ArrayList<Cookie>();
					}
			})
			.build();
			Request req=new Request.Builder()
			.url("http://eol.bjut.edu.cn/image.jsp")
			//.url("http://www.baidu.com")
			.build();
			Response res;
			try {
				res=hc.newCall(req).execute();
			}
			catch(Exception e) {
				return null;
			}
			try {
				bmp=BitmapFactory.decodeStream(res.body().byteStream());
				gotBmp=true;
			}
			catch(Exception e) {
				return null;
			}
			return null;
		}
	}
	
}
