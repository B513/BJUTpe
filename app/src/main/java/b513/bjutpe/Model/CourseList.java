package b513.bjutpe.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by wangh on 2017-03-22.
 */

public class CourseList<Course> extends ArrayList<Course> {

	@Override
	public Stream<Course> stream(){
		// TODO: Implement this method
		return null;
	}

	@Override
	public Stream<Course> parallelStream(){
		// TODO: Implement this method
		return null;
	}
	
    //private static CourseList ourInstance = new CourseList();

    //public static CourseList getInstance() {
    //    return ourInstance;
    //}ul

    public List<Map<String, Object>> getCourse() {
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
