package b513.bjutpe.UI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import b513.bjutpe.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {
    //
    //界面组件
    TextView tvError, tvGetVcode, tvLogcat;
    EditText etUname, etPasswd, etVcode;
    ImageView ibVcode;
    Button btnLogin, btnTest;
    boolean pwMode;//密码显示模式
    //登录前后的cookie。前者可能无用。
    List<Cookie> cookieso, cookiesp;
    //↓这个用于保存数据
    SharedPreferences sp;
    //所有用户名密码
    List<String> unames, passwds;
	List<Integer> dellist;

    /* 登录按钮按下时触发
     * 这种格式的函数都嫑直接更改名字和参数
     * 要和xml里相应控件的onClick属性一致
     */

    public void onTestButtonClicked(View v){
        Intent i = new Intent(MainActivity.this,CourseListActivity.class);
        startActivity(i);
    }

    public void onLoginButtonClicked(View v){
        String uname = etUname.getText().toString();
        String passwd = etPasswd.getText().toString();
        String vcode = etVcode.getText().toString();
        if(uname.length()==0){
            showError(R.string.prelogerr_name);
            return;
        }
        if(passwd.length()==0){
            showError(R.string.prelogerr_pass);
            return;
        }
        if(vcode.length()==0){
            showError(R.string.prelogerr_veri);
            return;
        }
        //让错误提示文本消失
        tvError.setVisibility(View.GONE);
        //保存用户名密码
        int ind = unames.indexOf(uname);
        if(ind==-1){//如果第一次使用这个用户名
            unames.add(uname);//添加进去
            passwds.add(passwd);
        }else{//替换密码
            passwds.set(ind,passwd);
        }
        Utils.saveLoginInfos(sp,unames,passwds);
        //登录
        new LoginTask(uname,passwd,vcode,null).execute();
    }

    //获取验证码按钮被点击
    public void onGetVcodeButtonClicked(View v){
        //开启后台获取验证码任务
        new GetVcodeTask().execute();
    }
	
	public void onSelAccButtonClicked(View v){
		if(unames.size()==0){
			Toast.makeText(this,"没有记录",0).show();
			return;
		}
		String[] accs=new String[unames.size()+1];
		for(int i=0;i<accs.length-1;i++){
			accs[i]=unames.get(i);
		}
		accs[accs.length-1]="删除...";
		AlertDialog ad=new AlertDialog.Builder(this)
			.setItems(accs,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1,int p2){
					try{
					if(p2>=unames.size()){
						String[] accs=new String[unames.size()];
						for(int i=0;i<accs.length;i++){
							accs[i]=unames.get(i);
						}
						AlertDialog ad=new AlertDialog.Builder(MainActivity.this)
							.setMultiChoiceItems(accs,null,new DialogInterface.OnMultiChoiceClickListener(){
								@Override
								public void onClick(DialogInterface p1,int p2,boolean p3){
									if(dellist==null)dellist=new ArrayList<Integer>();
									if(p3){
										if(-1==dellist.indexOf(p2)) dellist.add(p2);
									}else{
										dellist.remove(dellist.indexOf(p2));
									}
								}
							})
							.setPositiveButton("删除",new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface p1,int p2){
									for(int i:dellist){
										unames.remove(i);
										passwds.remove(i);
									}
									dellist.clear();
									Utils.saveLoginInfos(sp,unames,passwds);
								}
							})
							.create();
							ad.show();
							return;
					}
					etUname.setText(unames.get(p2));
					etPasswd.setText(passwds.get(p2));
					etPasswd.setInputType(129);
					}catch(Exception e){
						log(e);
					}
				}
			})
			.setTitle("选择账号")
			.create();
			ad.show();
	}

    public void onClearEtsButtonClicked(View v){
        etUname.setText("");
        etPasswd.setText("");
    }

    public void onSeePasswdButtonClicked(View v){
        if(pwMode){
            etPasswd.setInputType(129);
        }else{
            etPasswd.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        pwMode=!pwMode;

    }

    @Override//Activity创建时触发
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //初始化界面组件指针
        tvGetVcode=(TextView) findViewById(R.id.main_tvgetvcode);
        etUname=(EditText) findViewById(R.id.main_etUsername);
        etPasswd=(EditText) findViewById(R.id.main_etPassword);
        etVcode=(EditText) findViewById(R.id.main_etVerify);
        ibVcode=(ImageView) findViewById(R.id.main_ibVerify);
        btnLogin=(Button) findViewById(R.id.main_btnLogin);
        tvError=(TextView) findViewById(R.id.main_tvError);
        tvLogcat=(TextView) findViewById(R.id.main_logcat);
        etPasswd.setFilters(new InputFilter[]{new InputFilter() {
									@Override
									public CharSequence filter(CharSequence p1,int p2,int p3,Spanned p4,int p5,int p6){
										return p1.toString().replace(Utils.SPLIT_LOGININFOS,"");
										//Prevent the user from entering seperator, which messes up data structure.
									}
								}});
        pwMode=false;

        //初始化其他变量
        sp=getSharedPreferences(Utils.SPREFNAME_MAIN,0);
        unames=new ArrayList<String>();
        passwds=new ArrayList<String>();

        //加载用户名密码
        Utils.getLoginInfos(sp,unames,passwds);
        if(unames.size()>0){
            etUname.setText(unames.get(0));
        }
        if(passwds.size()>0){
            etPasswd.setText(passwds.get(0));
        }


    }

    //打印一条消息，在调试用的显示框
    private void log(String s){
        tvLogcat.append(">>> ");
        tvLogcat.append(s);
        tvLogcat.append("\n");
    }

    private void log(Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log(sw.toString());
        pw.close();
    }

    //显示登录错误，比如没输密码
	private void showError(int res){
		tvError.setVisibility(View.VISIBLE);
        tvError.setText(res);
	}

    private void showError(String err){
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(err);
    }

    //后台登录任务类
    class LoginTask extends AsyncTask<Void, Void, String>{

        private String uname, passwd, vcode, cookie;

        public LoginTask(String uname,String passwd,String vcode,String cookie){
            this.uname=uname;
            this.passwd=passwd;
            this.vcode=vcode;
            this.cookie=cookie;
        }

        @Override//预处理
        protected void onPreExecute(){
            super.onPreExecute();
            //禁止用户再点击按钮，并显示正在登录
            MainActivity.this.ibVcode.setEnabled(false);
            MainActivity.this.tvGetVcode.setEnabled(false);
            MainActivity.this.btnLogin.setEnabled(false);
            MainActivity.this.btnLogin.setText(R.string.loggingin);
        }

        @Override//后处理，重新允许使用按钮
        protected void onPostExecute(String result){
            MainActivity.this.ibVcode.setEnabled(true);
            MainActivity.this.tvGetVcode.setEnabled(true);
            MainActivity.this.btnLogin.setEnabled(true);
            MainActivity.this.btnLogin.setText(R.string.login);
			if(result==null){
				//网络异常
				showError(R.string.logerr_net); 
				return;  
			} 
			try{ 
				Document doc = Jsoup.parse(result); 
				Elements loginforms = doc.getElementsByAttributeValue("name","LoginForm"); 
				if(loginforms.size()==1){ 
					//登录失败 
					showError(R.string.logerr_login); 
					return;  
				} 
				Elements els0=doc.select("div TABLE TR TD SPAN font"); 
				if(els0.size()<1){ 
					//未知错误 
					showError(R.string.logerr_unk); 
					return; 
				} 
				String s0=els0.get(0).ownText(); 
				int ind0=s0.indexOf("当前用户：")+5; 
				if(ind0<s0.length()&&ind0>=0){ 
					s0=s0.substring(ind0); 
					Toast.makeText(MainActivity.this,"欢迎您，可爱的"+s0+"~",0).show();
				}else{ 
					//Won't happen 
				} 
			} 
			catch(Exception e){ 
				log(e); 
			} 
            startActivity( 
			new Intent(MainActivity.this,CourseListActivity.class) 
			.putExtra("homepage",result)); 
            finish(); 
        } 

        @Override//在后台登录
        protected String doInBackground(Void[] p1){
            /*try {让用户等待一秒钟，将时间续给长者
             (其实是为了测试)
			 Thread.currentThread().sleep(1000);
			 }
			 catch(InterruptedException e) {}*/
			if(cookieso==null) return null;

            Cookie coo = null;
            //从Cookie列表里找出JSESSIONID，不区分大小写
            for(Cookie co : cookieso){
                if(co.name().toLowerCase().equals("jsessionid")){
                    coo=co;
                    break;
                }
            }
            if(coo==null){
                //如果没有，那可真是日了狗了
                return null;
            }
            OkHttpClient hc = new OkHttpClient.Builder()
				.cookieJar(new CookieJar() {
					/* CookieJar用来定义Http客户端对cookie的加载
					 * 和得到cookie后的操作。这里登录时需要提交co
					 * kie，登录后需要保留新的cookie，以便后续使
					 * 用。
					 */
					@Override
					public void saveFromResponse(HttpUrl p1,List<Cookie> p2){
						cookiesp=p2;
					}

					@Override
					public List<Cookie> loadForRequest(HttpUrl p1){
						if(cookieso!=null){
								return cookieso;
							}
						return new ArrayList<Cookie>();
					}
				})
				.build();
            FormBody fb = new FormBody.Builder()//表单数据
				.add("username",uname)
				.add("userpwd",passwd)
				.add("code",vcode)
				.build();
			
            Request req = new Request.Builder()//创建POST请求
				.url("http://eol.bjut.edu.cn/Login.do;"+coo.name()+"="+coo.value())
				.post(fb)
				.build();
            Response res;//准备接收响应
            try{
                res=hc.newCall(req).execute();
				String a=res.body().string();
				FileWriter fos=new FileWriter(
				new File("/sdcard/00.html"));
				fos.write(a);
				fos.close();
                return a;
            }
			catch(Exception e){
                return null;
            }
        }
    }

    /* 习题0.0: 这是后台获取验证码的类，请仿照上面的例子，
     * 为这段代码写注释。
     */
    class GetVcodeTask extends AsyncTask<Void, Void, String>{
        private boolean gotBmp;
        private Bitmap bmp;

        public GetVcodeTask(){
            gotBmp=false;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            MainActivity.this.btnLogin.setEnabled(false);
            MainActivity.this.ibVcode.setEnabled(false);
            MainActivity.this.ibVcode.setEnabled(false);
            MainActivity.this.ibVcode.setVisibility(View.GONE);
            MainActivity.this.tvGetVcode.setVisibility(View.VISIBLE);
            MainActivity.this.tvGetVcode.setText(R.string.plzwait);
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            MainActivity.this.ibVcode.setEnabled(true);
            MainActivity.this.btnLogin.setEnabled(true);
            if(gotBmp){
                ibVcode.setImageBitmap(bmp);
                ibVcode.setVisibility(View.VISIBLE);
                tvGetVcode.setVisibility(View.GONE);
                for(Cookie coo : cookieso){
                    log(coo.name()+":"+coo.value());
                }
            }else{
                //ibVcode.setVisibility(View.GONE);
                //tvGetVcode.setVisibility(View.VISIBLE);
                tvGetVcode.setText(R.string.verification_code_hint);
                Toast.makeText(MainActivity.this,"获取验证码失败",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(Void[] p1){
            //if(true)return null;
            OkHttpClient hc = new OkHttpClient.Builder()
				.cookieJar(new CookieJar() {
					@Override
					public void saveFromResponse(HttpUrl p1,List<Cookie> p2){
						cookieso=p2;
					}

					@Override
					public List<Cookie> loadForRequest(HttpUrl p1){
						return new ArrayList<Cookie>();
					}
				})
				.build();
            Request req = new Request.Builder()
				.url("http://eol.bjut.edu.cn/image.jsp")
				//.url("http://www.baidu.com")
				.build();
            Response res;
            try{
                res=hc.newCall(req).execute();
            }
			catch(Exception e){
                return null;
            }
            try{
                bmp=BitmapFactory.decodeStream(res.body().byteStream());
                gotBmp=true;
            }
			catch(Exception e){
                return null;
            }
            return null;
        }
    }

}
