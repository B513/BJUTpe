package b513.bjutpe.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import b513.bjutpe.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import b513.bjutpe.Logger;

public class CourseListActivity extends AppCompatActivity {
    //ListView CourseList;
	private String[] urls;
	private Logger logger;
	
	static private String MAP_KEY_COURSE="item_courseName";
	static private String MAP_KEY_TEACHER="item_teacher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_list);
		logger=Logger.getInstance(this,getFilesDir());
		try{
			//从Intent里拿到MainActivity传过来的网页
			Document doc=Jsoup.parse(getIntent().getStringExtra("homepage"));
			Element list=doc.getElementById("CourseList");
			urls=new String[list.children().size()-1];
			List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
			for(int i=1;i<=urls.length;i++){
				Elements entry=list.child(i).select("tr td a");
				Element e0=entry.get(0);
				urls[i-1]=e0.attr("href");
				Map<String, Object> map=new HashMap<String,Object>();
				map.put(MAP_KEY_COURSE,e0.ownText());
				map.put(MAP_KEY_TEACHER,entry.get(1).ownText());
				maps.add(map);
			}
			ListView lv= (ListView) findViewById(R.id.course_lvCourse);
			SimpleAdapter adapter = new SimpleAdapter(this, maps, R.layout.item_courselist,
													  new String[]{"item_courseName", "item_teacher"},
													  new int[]{R.id.item_courseName, R.id.item_teacher});
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new ListView.OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> p1,View p2,int p3,long p4){
							startActivity(new Intent(CourseListActivity.this,CourseActivity.class)
						.putExtra("url",urls[p3])
						.putExtra("cookies",getIntent().getStringExtra("cookies")));
					}
				});
		}catch(Exception e){
			return;
		}
    }

    private List<Map<String, Object>> getCourse() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(MAP_KEY_COURSE, "数据结构与算法");
        map.put(MAP_KEY_TEACHER, "a");
		//用常量又方便修改，又不会因为拼错出问题
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_courseName", "毛概");
        map.put("item_teacher", "毛泽东");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_courseName", "马原");
        map.put("item_teacher", "马克思");
        for(int i=0;i<100;i++){list.add(map);}

        return list;
    }
}



