package b513.bjutpe;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class Logger{
	
	static private Logger instance=null;
	
	private boolean usable;
	private File path;
	private PrintWriter pw;
	private FileWriter fw;
	
	static public Logger getInstance(Object invoker,File path){
		if(instance==null){
			instance=new Logger(path);
		}
		if(!instance.usable){
			instance=new Logger(path);
		}
		if(!instance.usable)return null;
		return instance;
	}
	
	public void log(String s){
		pw.append(">>> ").append(s).append("\n");
		pw.flush();
	}
	
	public void log(Exception e){
		pw.append(">>> Exception details:\n");
		e.printStackTrace(pw);
		pw.flush();
	}
	
	private Logger(File path){
		if(path.isDirectory()){
			path=new File(path,"logs.txt");
		}
		this.path=path;
		usable=false;
		try{
			fw=new FileWriter(path);
			pw=new PrintWriter(fw);
			usable=true;
		}
		catch(IOException e){}
	}
	
}
