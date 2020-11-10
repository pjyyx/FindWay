

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class test {

    public static void read() throws IOException, ParseException {
        File directory = new File("");// 参数为空
        String courseFile = directory.getCanonicalPath();
        String name = "adjacency";
        String path = courseFile + "/" + name;
        InputStream is = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        FileReader fileReader = new FileReader(path);
        LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
        lineNumberReader.skip(Long.MAX_VALUE);
        long lines = lineNumberReader.getLineNumber() + 1;
        fileReader.close();
        lineNumberReader.close();
        
        String[][] timetable = new String[(int)lines-1][5];
        
        String line;
        line = br.readLine();
        String[] arr = line.split(";");
        
        int j=0;

        //while((line = br.readLine()) != null) {
          //  arr = line.split(",");
            //timetable[j]=arr;
            //j++;
        //}


        //int[][] a = new int[2][3];

        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间 
        sdf.applyPattern("HH:mm");// a为am/pm的标记  
        Date now = new Date();// 获取当前时间
        String now_string = sdf.format(now); 
        
        String time = "13:51";
        String time1= "22:51";
        Integer i=time.compareTo(time1);
        //Date date = sdf.parse(time);
        
        
        int[] a = {1,2,3,4,5};
        System.out.println(i);
        //System.out.println(time);
        

        br.close();
        is.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        File directory = new File("");
        String courseFile = directory.getCanonicalPath();
        String path = courseFile + "/" + "tt-BusportD";
        File file = new File(path);
        Long lastModified = file.lastModified();
        Date date = new Date(lastModified);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String changed_time = formatter.format(date);
        System.out.println(changed_time);


    }
}