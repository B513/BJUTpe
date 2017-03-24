package b513.bjutpe;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import b513.bjutpe.Logger;
import b513.bjutpe.R;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CourseActivity extends Activity implements ViewPager.OnPageChangeListener{

	private List<Cookie> cookies;
	private Logger logger;
	
	@Override
	public void onPageScrolled(int p1,float p2,int p3){
	}@Override
	public void onPageSelected(int p1){
		// TODO: Implement this method
	}@Override
	public void onPageScrollStateChanged(int p1){
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		logger=Logger.getInstance(this,getFilesDir());
		try{
			setContentView(R.layout.activity_course);
			ViewPager vp=(ViewPager) findViewById(R.id.activitycourseViewPager1);
			CoursePagerAdapter ada=new CoursePagerAdapter();
			LayoutInflater li=getLayoutInflater();
			ada.views.add(li.inflate(R.layout.page_course_main,null,false));
			ada.views.add(li.inflate(R.layout.page_course_main,null,false));
			ada.views.add(li.inflate(R.layout.page_course_main,null,false));
			ada.titles.add("课程信息");
			ada.titles.add("课程作业");
			ada.titles.add("课件资料");
			vp.setAdapter(ada);
			vp.setOnPageChangeListener(this);
			String url=getIntent().getStringExtra("url");
			String[] tmp0=getIntent().getStringExtra("cookies").split("\n");
			cookies=new ArrayList<Cookie>();
			for(String s:tmp0){
				String[] ss=s.split("=");
				Cookie co=new Cookie.Builder()
				.name(ss[0]).value(ss[1]).domain(ss[2]).path(ss[3]).expiresAt(Long.parseLong(ss[4])
				).build();
				logger.log(co.toString());
				logger.log(co.domain());
				cookies.add(co);
			}
			new LoadInfoTask(url).execute();
			((TextView)(ada.views.get(0).findViewById(R.id.pagecoursemainTextView1))).setText(url);
		}catch(Exception e){
			logger.log(e);
		}
	}
	
	class CoursePagerAdapter extends PagerAdapter{
		public List<String>  titles;
		public List<View> views;
		//private ViewPager vpager;
		public CoursePagerAdapter(){//}ViewPager vp){
			views=new ArrayList<View>();
			titles=new ArrayList<String>();
			//vpager=vp;
		}@Override
		public int getCount(){
			return views.size();
		}@Override
		public Object instantiateItem(View container,int position){
			((ViewPager)container).addView(views.get(position));
			return views.get(position);
		}@Override
		public void destroyItem(ViewGroup container, int position,Object object) {
			((ViewPager) container).removeView(views.get(position));
		}@Override
		public boolean isViewFromObject(View p1,Object p2){
			return p1==p2;
		}@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}@Override
		public CharSequence getPageTitle(int position) {
			return titles.get(position);
		}
	}
	
	class LoadInfoTask extends AsyncTask<Void,Void,Void>{
		private String url;
		public LoadInfoTask(String url){
			this.url=url;
		}
		@Override
		protected Void doInBackground(Void[] p1){
			OkHttpClient client=new OkHttpClient.Builder()
				.cookieJar(new CookieJar(){
					@Override
					public void saveFromResponse(HttpUrl p1,List<Cookie> p2){
					}
					@Override
					public List<Cookie> loadForRequest(HttpUrl p1){
						return cookies;
					}
				})
			.build();
			Request req=new Request.Builder()
			.url("http://eol.bjut.edu.cn/"+url)
			.build();
			Response res;
			try{
				res=client.newCall(req).execute();
				FileWriter fw=new FileWriter(new File("/sdcard/0.html"));
				fw.write(res.body().string());
				fw.close();
			}catch(Exception e){
				logger.log(e);
			}
			return null;
		}
	}
}
