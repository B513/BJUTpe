package b513.bjutpe.UI;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import b513.bjutpe.R;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //界面组件
    TextView tvError, tvGetVcode, tvLogcat;
    EditText etUname, etPasswd, etVcode;
    ImageView ibVcode;
    Button btnLogin;
    boolean pwMode;//密码显示模式
    //登录前后的cookie。前者可能无用。
    List<Cookie> cookieso, cookiesp;
    //↓这个用于保存数据
    SharedPreferences sp;
    //所有用户名密码
    List<String> unames, passwds;

    /* 登录按钮按下时触发
     * 这种格式的函数都嫑直接更改名字和参数
     * 要和xml里相应控件的onClick属性一致
     */
    public void onLoginButtonClicked(View v) {
        String uname = etUname.getText().toString();
        String passwd = etPasswd.getText().toString();
        String vcode = etVcode.getText().toString();
        if (uname.length() == 0) {
            showError("请填写用户名");
            return;
        }
        if (passwd.length() == 0) {
            showError("请填写密码");
            return;
        }
        if (vcode.length() == 0) {
            showError("请填写验证码");
            return;
        }
        //让错误提示文本消失
        tvError.setVisibility(View.GONE);
        //保存用户名密码
        int ind = unames.indexOf(uname);
        if (ind == -1) {//如果第一次使用这个用户名
            unames.add(uname);//添加进去
            passwds.add(passwd);
        } else {//替换密码
            passwds.set(ind, passwd);
        }
        Utils.saveLoginInfos(sp, unames, passwds);
        //登录
        //new LoginTask(uname,passwd,vcode,null).execute();
    }

    //获取验证码按钮被点击
    public void onGetVcodeButtonClicked(View v) {
        //开启后台获取验证码任务
        //new GetVcodeTask().execute();

    }

    public void onClearEtsButtonClicked(View v) {
        etUname.setText("");
        etPasswd.setText("");
    }

    public void onSeePasswdButtonClicked(View v) {
        if (pwMode) {
            etPasswd.setInputType(129);
        } else {
            etPasswd.setInputType(InputType.TYPE_NULL);
        }
        pwMode = !pwMode;

    }

    @Override//Activity创建时触发
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化界面组件指针
        tvGetVcode = (TextView) findViewById(R.id.main_tvgetvcode);
        etUname = (EditText) findViewById(R.id.main_etUsername);
        etPasswd = (EditText) findViewById(R.id.main_etPassword);
        etVcode = (EditText) findViewById(R.id.main_etVerify);
        ibVcode = (ImageView) findViewById(R.id.main_ibVerify);
        btnLogin = (Button) findViewById(R.id.main_btnLogin);
        tvError = (TextView) findViewById(R.id.main_tvError);
        tvLogcat = (TextView) findViewById(R.id.main_logcat);
        //不允许输入分隔符，因为密码不可能包含它，而它会干扰程序
        InputFilter Separator = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence p1, int p2, int p3, Spanned p4, int p5, int p6) {
                //log(""+p1+","+p2+","+p3+","+p4+","+p5+","+p6);
                return p1.toString().replace(Utils.SPLIT_LOGININFOS, "");
            }
        };
        etPasswd.setFilters(new InputFilter[]{Separator});
        pwMode = false;

        //初始化其他变量
        sp = getSharedPreferences(Utils.SPREFNAME_MAIN, 0);
        unames = new ArrayList<String>();
        passwds = new ArrayList<String>();

        //加载用户名密码
        Utils.getLoginInfos(sp, unames, passwds);
        if (unames.size() > 0) {
            etUname.setText(unames.get(0));
        }
        if (passwds.size() > 0) {
            etPasswd.setText(passwds.get(0));
        }

    }

    //打印一条消息，在调试用的显示框
    private void log(String s) {
        tvLogcat.append(">>> ");
        tvLogcat.append(s);
        tvLogcat.append("\n");
    }

    //显示登录错误，比如没输密码
    private void showError(String err) {
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(err);
    }

    //后台登录任务类
    class LoginTask extends AsyncTask<Void, Void, String> {

        private String uname, passwd, vcode, cookie;

        public LoginTask(String uname, String passwd, String vcode, String cookie) {
            this.uname = uname;
            this.passwd = passwd;
            this.vcode = vcode;
            this.cookie = cookie;
        }

        @Override//预处理
        protected void onPreExecute() {
            super.onPreExecute();
            //禁止用户再点击按钮，并显示正在登录
            MainActivity.this.ibVcode.setEnabled(false);
            MainActivity.this.tvGetVcode.setEnabled(false);
            MainActivity.this.btnLogin.setEnabled(false);
            MainActivity.this.btnLogin.setText(R.string.loggingin);
        }

        @Override//后处理，重新允许使用按钮
        protected void onPostExecute(String result) {
            // TODO: Implement this method
            super.onPostExecute(result);
            MainActivity.this.ibVcode.setEnabled(true);
            MainActivity.this.ibVcode.setEnabled(true);
            MainActivity.this.btnLogin.setEnabled(true);
            MainActivity.this.btnLogin.setText(R.string.login);
            ;
        }

        @Override//在后台登录
        protected String doInBackground(Void[] p1) {
            /*try {让用户等待一秒钟，将时间续给长者
			 (其实是为了测试)
			 Thread.currentThread().sleep(1000);
			 }
			 catch(InterruptedException e) {}*/
            Cookie coo = null;
            //从Cookie列表里找出JSESSIONID，不区分大小写
            for (Cookie co : cookieso) {
                if (co.name().toLowerCase().equals("jsessionid")) {
                    coo = co;
                    break;
                }
            }
            if (coo == null) {
                //如果没有，那可真是日了狗了
                return "0";
            }

			/* OkHttp大量采用Builder这一概念进行类
			 * 实例的创建和设置。体会这样做的方便
			 * 原本是
			 * A a=new A(xx,xx);
			 * a.setXx(xx);
			 * 或者
			 * A a=new A();
			 * a.setXx(xx);
			 * a.setXx(xx);
			 * 对于一些需要参数多的类，
			 * 内部实现和外部调用都会麻烦。
			 * 现在的格式是
			 * A a=new A.Builder()
			 * .xx(xx)
			 * .xx(xx)
			 * .build();
			 * 最开始可能不习惯，适应了会发现真的好用。
			 */
            OkHttpClient hc = new OkHttpClient.Builder()
                    .cookieJar(new CookieJar() {
                        /* CookieJar用来定义Http客户端对cookie的加载
                         * 和得到cookie后的操作。这里登录时需要提交co
                         * kie，登录后需要保留新的cookie，以便后续使
                         * 用。
                         */
                        @Override
                        public void saveFromResponse(HttpUrl p1, List<Cookie> p2) {
                            cookiesp = p2;
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl p1) {
                            return cookieso;
                        }
                    })
                    .build();
            FormBody fb = new FormBody.Builder()//表单数据
                    .add("username", uname)
                    .add("userpwd", passwd)
                    .add("code", vcode)
                    .build();
            Request req = new Request.Builder()//创建POST请求
                    .url("http://eol.bjut.edu.cn/Login.do;" + coo.name() + "=" + coo.value())
                    .post(fb)
                    .build();
            Response res;//准备接收响应
            try {
                res = hc.newCall(req).execute();
                String data = res.body().string();
                FileWriter fos = new FileWriter(
                        new File("/sdcard/whig.html"));
                fos.write(data);
                fos.close();
            } catch (Exception e) {
                return null;
            }
            return null;
        }
    }

    /* 习题0.0: 这是后台获取验证码的类，请仿照上面的例子，
     * 为这段代码写注释。
     */
    class GetVcodeTask extends AsyncTask<Void, Void, String> {
        private boolean gotBmp;
        private Bitmap bmp;

        public GetVcodeTask() {
            gotBmp = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity.this.btnLogin.setEnabled(false);
            MainActivity.this.ibVcode.setEnabled(false);
            MainActivity.this.ibVcode.setEnabled(false);
            MainActivity.this.ibVcode.setVisibility(View.GONE);
            MainActivity.this.tvGetVcode.setVisibility(View.VISIBLE);
            MainActivity.this.tvGetVcode.setText(R.string.plzwait);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            MainActivity.this.ibVcode.setEnabled(true);
            MainActivity.this.btnLogin.setEnabled(true);
            if (gotBmp) {
                ibVcode.setImageBitmap(bmp);
                ibVcode.setVisibility(View.VISIBLE);
                tvGetVcode.setVisibility(View.GONE);
                for (Cookie coo : cookieso) {
                    log(coo.name() + ":" + coo.value());
                }
            } else {
                //ibVcode.setVisibility(View.GONE);
                //tvGetVcode.setVisibility(View.VISIBLE);
                tvGetVcode.setText(R.string.verification_code_hint);
                Toast.makeText(MainActivity.this, "获取验证码失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(Void[] p1) {
            //if(true)return null;
            OkHttpClient hc = new OkHttpClient.Builder()
                    .cookieJar(new CookieJar() {
                        @Override
                        public void saveFromResponse(HttpUrl p1, List<Cookie> p2) {
                            cookieso = p2;
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl p1) {
                            return new ArrayList<Cookie>();
                        }
                    })
                    .build();
            Request req = new Request.Builder()
                    .url("http://eol.bjut.edu.cn/image.jsp")
                    //.url("http://www.baidu.com")
                    .build();
            Response res;
            try {
                res = hc.newCall(req).execute();
            } catch (Exception e) {
                return null;
            }
            try {
                bmp = BitmapFactory.decodeStream(res.body().byteStream());
                gotBmp = true;
            } catch (Exception e) {
                return null;
            }
            return null;
        }
    }

}
