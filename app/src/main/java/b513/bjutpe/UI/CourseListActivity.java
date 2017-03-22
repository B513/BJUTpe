package b513.bjutpe.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import b513.bjutpe.R;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;

public class CourseListActivity extends AppCompatActivity {
    ListView CourseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		try{
			Document doc=Jsoup.parse(getIntent().getStringExtra("homepage"));
		}catch(Exception e){
			return;
		}
        CourseList = new ListView(this);
        SimpleAdapter adapter = new SimpleAdapter(this, getCourse(), R.layout.item_courselist,
                new String[]{"item_courseName", "item_teacher"},
                new int[]{R.id.item_courseName, R.id.item_teacher});
        CourseList.setAdapter(adapter);
        setContentView(CourseList);
    }

    private List<Map<String, Object>> getCourse() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("item_courseName", "数据结构与算法");
        map.put("item_teacher", "a");
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



