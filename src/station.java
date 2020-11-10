import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;

public class station{

    static String station_name;
    static int tcp_port;
    static int udp_port;
    //Stores adjacent port Numbers
    static int[] neighbor_port;
    static double[] location = new double[2];
    

    static String[][] timetable;
    
    static LinkedList<Integer> neighbor = new LinkedList<Integer>();
    //Store the final time to the station in order to find the fastest path
    static LinkedList<String> time_compare = new LinkedList<String>();
    //Store the output ready to be sent to the browser to find the fastest path
    static Map<String,String> save_string = new HashMap<String,String>();
    //The number of buses to the station
    static LinkedList<String> bus = new LinkedList<String>();
    //Store the output ready to be sent，To know which socket to send
    static Map<String,String> result = new HashMap<String,String>();
    //the dictionary which key is socket, value is destination,To know which socket to send
    static Map<SocketChannel,String> s = new HashMap<SocketChannel,String>();
    //Stores the number of paths from any starting point to the station to find the fastest path
    static Map<String,Integer> route_num = new HashMap<String,Integer>();


    //read timetable
    public static void read_timetable(String filename) throws Exception {

        File directory = new File("");
        String courseFile = directory.getCanonicalPath();
        String path = courseFile + "/" + "tt-"+ filename;

        InputStream is = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        FileReader fileReader = new FileReader(path);
        LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
        lineNumberReader.skip(Long.MAX_VALUE);
        long lines = lineNumberReader.getLineNumber() + 1;
        fileReader.close();
        lineNumberReader.close();

        timetable = new String[(int) lines - 2][5];

        String line;
        line = br.readLine();
        String[] arr = line.split(",");
        location[0] = Double.valueOf(arr[1]);
        location[1] = Double.valueOf(arr[2]);
        int j = 0;

        while ((line = br.readLine()) != null) {
            arr = line.split(",");
            timetable[j] = arr;
            
            j++;
        }

        br.close();

    }

    //Gets the last time the schedule was modified
    public static String get_modifytime(String filename) throws Exception{
        File directory = new File("");
        String courseFile = directory.getCanonicalPath();
        String path = courseFile + "/" + "tt-"+ filename;
        File file = new File(path);
        Long lastModified = file.lastModified();
        Date date = new Date(lastModified);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String mo_time = formatter.format(date);
        return mo_time;
    }

    //check the timetable changed or not
    public static boolean is_timetableChange(String stationname,String now_time) throws Exception{
        String changed_time = get_modifytime(stationname);
        Integer bool = changed_time.compareTo(now_time);
        if(bool>0){
            return true;
        }
        else{
            return false;
        }

    }

    public static void main(String[] args) throws Exception{
        //Processing parameters
        station_name = args[0];
        tcp_port = Integer.valueOf(args[1]);
        udp_port = Integer.valueOf(args[2]);
        int m=0;
        neighbor_port = new int [args.length-3];
        
        for(int i=3;i<args.length;i++){
            neighbor_port[m]=Integer.valueOf(args[i]);
            neighbor.add(neighbor_port[m]);
            m++;
        }
        read_timetable(station_name);
        String modify_time = get_modifytime(station_name);

        //initialises its TCP and UDP ports
        //Create a service
        ServerSocketChannel server = ServerSocketChannel.open();
        //Specified as non-blocking, default to blocking
        server.configureBlocking(false);
        //create channel
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        DatagramSocket socket = channel.socket();

        //bind port
        server.socket().bind(new InetSocketAddress(tcp_port));
        //bind ip and port
        InetSocketAddress address = new InetSocketAddress(udp_port);
        socket.bind(address);

        //create selector
        Selector selector = Selector.open();
        Selector selector1 = Selector.open();

        //Register the connection ready event
        server.register(selector, SelectionKey.OP_ACCEPT);
        channel.register(selector1, SelectionKey.OP_READ);


        while(true){
            neighbor = new LinkedList<Integer>();
            for(int j=0;j<neighbor_port.length;j++){
                neighbor.add(neighbor_port[j]);
            }

            //Found the event
            if (selector1.select(1000) < 1 && selector.select(1000)<1) {
                continue;
            }
            //Put the selected into the set and iterate
            Set<SelectionKey> keys = selector.selectedKeys();
            Set<SelectionKey> keys1 = selector1.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            Iterator<SelectionKey> it1 = keys1.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isAcceptable()) {  //Receive events
                    //Gets the key associated with the ServerSocketChannel
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    //Gets the SocketChannel for the key connection
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    //The request is received, the connection is registered to the selector, and the listener type is set to read
                    sc.register(selector,SelectionKey.OP_READ);
                }
                else if(key.isReadable()){
                    //check modified or not
                    if(is_timetableChange(station_name,modify_time)){
                        read_timetable(station_name);
                        modify_time = get_modifytime(station_name);
                    }

                    //get destination
                    SocketChannel ch = (SocketChannel)key.channel();
                    ByteBuffer dest = ByteBuffer.allocate(1024);
                    int read = ch.read(dest);
                    String data = new String(dest.array(),0,read);
                    String[] arr = data.split("\n");
                    String line = arr[0];
                    String[] array = line.split(" ");
                    String destination = array[1].substring(5);

                    //avoid favicon
                    String ico = "con.ico";
                    if(destination.equals(ico)){
                        ch.close();
                        destination = "";
                        continue;
                    }
                    
                    result.put(destination, null);
                    s.put(ch,destination);
                        
                    //create data_send,This data means find route
                    //The format is: 1; destination; route
                    String string_send = String.valueOf(1)+ ";" + destination + ";" + station_name + "," + String.valueOf(udp_port);
                    byte[] bys = string_send.getBytes();
                        
                    for(int i=0;i<neighbor_port.length;i++){
                        ByteBuffer buf=ByteBuffer.allocate(1024);
                        buf.clear();
                        buf.put(bys);
                        buf.flip();
                        channel.send(buf, new InetSocketAddress("127.0.0.1",neighbor_port[i]));
                    }
                    //skt remove from listen append write
                    ch.register(selector, SelectionKey.OP_WRITE);
                    
                    


                }
                else if(key.isWritable()){
                    SocketChannel ch =(SocketChannel)key.channel();
                    String destination = s.get(ch);
                    if(result.get(destination)==null){
                        //not prepare for write
                        continue;
                    }
                    if(result.get(destination)!=null){
                        //http response
                        String output = result.get(destination);

                        ch.write(ByteBuffer.wrap("HTTP/1.1 200 OK\r\n".getBytes()));
                        ch.write(ByteBuffer.wrap("Content-Type:text/plain\r\n".getBytes()));
                        ch.write(ByteBuffer.wrap("\r\n".getBytes()));
                        ch.write(ByteBuffer.wrap(output.getBytes()));
                        ch.close();
                        
                    }
                }
            }

            while(it1.hasNext()){
                //udp event
                SelectionKey key1 = it1.next();
                it1.remove();
                if(key1.isReadable()){
                    DatagramChannel datagramChannel =(DatagramChannel)key1.channel();
                    //decode data
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    readBuffer.clear();
                    datagramChannel.receive(readBuffer);
                    readBuffer.flip();
                    CharBuffer charBuffer = StandardCharsets.UTF_8.decode(readBuffer); 
                    String data_string = charBuffer.toString(); 
                    //Split the received information
                    String[] arr = data_string.split(";");
                    
                    
                    System.out.println("receive:"+data_string);
                        //Check the prefix
                        if(Integer.parseInt(arr[0]) == 1){
                            System.out.println("find route");

                            int a = arr.length-1;

                            if(arr[1].equals(station_name)){//Whether to find the destination or not
                                if(route_num.containsKey(arr[2].split(",")[0])){//record the num of routes from this start point to the destination
                                    if(route_num.get(arr[2].split(",")[0])==null){
                                        break;
                                    }
                                    else{
                                        int route = route_num.get(arr[2].split(",")[0]);
                                        route_num.replace(arr[2].split(",")[0],route++);
                                    }
                                    
                                }
                                else{
                                    route_num.put(arr[2].split(",")[0],1);
                                }
                                
                                //create data_send,This data means sending the path back to the starting point
                                //The format is: 2; The starting point; The path; The port number of the current station
                                String data_send = String.valueOf(2) + ";" + arr[2].split(",")[0] + ";";
                                for(int j=2;j<=a;j++){
                                    data_send = data_send + arr[j] + ";";
                                }
                                data_send = data_send + station_name + "," + udp_port + ";" + String.valueOf(arr.length);
                                int port = Integer.parseInt(arr[a].split(",")[1]);

                                byte[] by = data_send.getBytes(StandardCharsets.UTF_8); 
                                ByteBuffer buf=ByteBuffer.allocate(1024);
		                        buf.clear();
		                        buf.put(by);
		                        buf.flip();
                                channel.send(buf, new InetSocketAddress("127.0.0.1",port));
                                System.out.println("send to:"+ port);
                                System.out.println("send message is:"+data_send);
                                

                            }
                            if(!arr[1].equals(station_name)){//not find the destination
                                for(int j=2;j<=a;j++){//Check which stations exist in the path,Are these stations adjacent to the current station?
                                    if(neighbor.contains(Integer.parseInt(arr[j].split(",")[1]))){
                                        Object o = Integer.parseInt(arr[j].split(",")[1]);
                                        neighbor.remove(o);
                                    }
                                }
                                //Add information about the current station
                                String data_send = new String();
                                for(int j=0;j<=a;j++){
                                    data_send=data_send + arr[j] + ";";
                                }
                                data_send = data_send + station_name + "," + udp_port;

                                byte[] by = data_send.getBytes(StandardCharsets.UTF_8); 
          
                                int size = neighbor.size();
                                for(int j=0;j<size;j++){
                                    ByteBuffer buf=ByteBuffer.allocate(1024);
                                    buf.clear();
                                    buf.put(by);
                                    buf.flip();
                                    System.out.println("buf:"+ buf);
                                    System.out.println("j="+j);
                                    int port = (int) neighbor.remove();
                                    channel.send(buf, new InetSocketAddress("127.0.0.1",port));
                                    System.out.println("send to:"+ port);
                                    System.out.println("send message is:"+data_send);
                                    
                                }
                                
                                
                            }
                        }
                        if(Integer.parseInt(arr[0]) == 2){//Return the route
                            System.out.println("Return the route");
                            
                            int a = arr.length-2;
                            if(arr[1].equals(station_name)){
                                
                                int b = Integer.parseInt(arr[arr.length-1]);
                                //find time
                                SimpleDateFormat sdf = new SimpleDateFormat();//Formatting time  
                                sdf.applyPattern("HH:mm"); 
                                Date now = new Date();// get current time
                                String now_string = sdf.format(now);
                                
                                String time = new String();
                                String time_inital = new String();
                                String bus = new String();

                                //find the next bus
                                for(int j=0;j<timetable.length;j++){
                                    
                                    Integer bool = timetable[j][0].compareTo(now_string);

                                    if(bool >= 0){
                                        if(timetable[j][4].equals(arr[b].split(",")[0])){
                                    
                                            time = timetable[j][3];
                                            time_inital = timetable[j][0];
                                            bus = timetable[j][1];
                                            break;
                                        }
                                        
                                    }
                                    
                                }

                                //create data_send,This data means find time
                                //The format is: 3; time; time_inital; route; bus; The port number of the current station
                                String data_send = String.valueOf(3) + ";" + time + ";" + time_inital + ";";
                                for(int j=2;j<=a;j++){
                                    data_send = data_send + arr[j] + ";";
                                }
                                data_send = data_send + bus + ";" + String.valueOf(3);

                                int port = Integer.parseInt(arr[3].split(",")[1]);
                                System.out.println("send to:"+ port);
                                System.out.println("send message is:"+data_send);
                                byte[] by = data_send.getBytes(); 
                                ByteBuffer buf=ByteBuffer.allocate(1024);
		                        buf.clear();
		                        buf.put(by);
		                        buf.flip();
                                channel.send(buf, new InetSocketAddress("127.0.0.1",port));
                                

                            }
                            if(!arr[1].equals(station_name)){//not find the start
                                System.out.println("not find the destnation");
                                String data_send = new String();
                                for(int j=0;j<=a;j++){
                                    data_send=data_send + arr[j] + ";";
                                }
                                int b = Integer.valueOf(arr[arr.length-1]);
                                data_send = data_send + String.valueOf(b-1);

                                int port = Integer.parseInt(arr[b-2].split(",")[1]);
                                System.out.println("send to:"+ port);
                                System.out.println("send message is:"+data_send);

                                byte[] by = data_send.getBytes(StandardCharsets.UTF_8); 
                                ByteBuffer buf=ByteBuffer.allocate(1024);
		                        buf.clear();
		                        buf.put(by);
		                        buf.flip();
                                channel.send(buf, new InetSocketAddress("127.0.0.1",port));
                                
                                
                            }
                        }
                        if(Integer.parseInt(arr[0]) == 3){
                            System.out.println("find time");
                            
                            if(arr[arr.length-3].split(",")[0].equals(station_name)){//find the destnation
                                System.out.println("find the destnation");
                                String bus_num = arr[arr.length-2];
                                String time = arr[1];
                                //create data_send,This data means return time
                                //The format is: 4; start station; time; time_inital; route; The port number of the current station
                                String data_send = String.valueOf(4) + ";" + arr[3].split(",")[0] + ";" + arr[2] + ";" + time + ";";
                                for(int j = 3;j<=arr.length-3;j++){
                                    data_send=data_send+arr[j]+";";
                                }
                                data_send = data_send + String.valueOf(arr.length-2);
                                String start = arr[3].split(",")[0];
                                
                                //The data has been sent
                                if(route_num.get(start)==null){
                                    break;
                                }
                                //begin to compare the time
                                if(bus.size()==route_num.get(start)-1){
                                    while(!time_compare.isEmpty()){
                                        String remove = time_compare.remove();
                                        Integer bool = time.compareTo(remove);
                                        if(bool>0){
                                            data_send = save_string.get(remove);
                                            time = remove;
                                        }
                                    }
                                    String[] arr2 = data_send.split(";");
                                    int b1 = Integer.parseInt(arr2[arr2.length-1])-1;
                                    
                                    int port = Integer.parseInt(arr2[b1].split(",")[1]);
                                    
                                    
                                    bus.clear();
                                    time_compare.clear();
                                    save_string.clear();
                                    route_num.replace(start,null);
                                    byte[] by = data_send.getBytes(StandardCharsets.UTF_8); 
                                    ByteBuffer buf=ByteBuffer.allocate(1024);
                                    buf.clear();
                                    buf.put(by);
                                    buf.flip();
                                    channel.send(buf, new InetSocketAddress("127.0.0.1",port));
                                    System.out.println("send to:"+ port);
                                    System.out.println("send message is:"+data_send);
                                }
                                else{
                                    if(bus.contains(bus_num)){
                                        bus.add(bus_num);
                                        time_compare.add(time);
                                        save_string.put(time,data_send);
                                    }
                                }
                            }
                            if(!arr[arr.length-3].split(",")[0].equals(station_name)){//not find the destnation
                                System.out.println("not find the destnation");
                                String data_send = String.valueOf(3) + ";";
                                String now = arr[1];
                                String bus = new String();
                                int b = Integer.parseInt(arr[arr.length-1]);
                                //find next bus
                                for(int j=0;j<timetable.length;j++){
                                    Integer bool = timetable[j][0].compareTo(now);
                                    if(bool >= 0){
                                        if(timetable[j][4].equals(arr[b+2].split(",")[0])){
                                            now = timetable[j][3];
                                            bus = timetable[j][1];
                                            break;
                                        }
                                        
                                    }
                                }
                                data_send = data_send + now + ";" + arr[2] + ";";
                                for(int j=3;j<=arr.length-3;j++){
                                    data_send = data_send + arr[j] +";";
                                }
                                
                                data_send = data_send + bus+ ";" + String.valueOf(b+1);
                                int port = Integer.parseInt(arr[b+2].split(",")[1]);
                                System.out.println("send to:"+ port);
                                System.out.println("send message is:"+data_send);

                                byte[] by = data_send.getBytes(); 
                                ByteBuffer buf=ByteBuffer.allocate(1024);
		                        buf.clear();
		                        buf.put(by);
		                        buf.flip();
                                channel.send(buf, new InetSocketAddress("127.0.0.1",port));
                                
                            }
                        }
                        if(Integer.parseInt(arr[0]) == 4){
                            System.out.println("return time");
                            if(arr[1].equals(station_name)){
                                //start preparing for the HTTP response
                                String time_arrive = arr[3];
                                String bus = new String();
                                String stop = new String();
                                String time = arr[2];
                                String output = new String();
                                for(int j=0;j<timetable.length;j++){
                                    
                                    if(timetable[j][0].equals(time)){
                                        bus = timetable[j][1];
                                        stop = timetable[j][2];
                                        break;
                                    }
                                }
                                if(time_arrive.equals("")||bus.equals("")||stop.equals("")){
                                    //can not find the bus today
                                    output = "a valid route does not exist on the current day.";
                                }
                                else{
                                    output = "At " + time + ", catch " + bus + ", from " + stop +". You will arrive at your final destination at "+ time_arrive +".";
                                }
                                result.put(arr[arr.length-2].split(",")[0], output);

                                

                            }
                            if(!arr[1].equals(station_name)){
                                //not find the start
                                String data_send = new String();
                                for(int j=0;j<=arr.length-2;j++){
                                    data_send=data_send + arr[j] + ";";
                                }
                                int b = Integer.valueOf(arr[arr.length-1]);
                                data_send = data_send + String.valueOf(b-1);                                

                                int port = Integer.parseInt(arr[b-2].split(",")[1]);
                                System.out.println("send to:"+ port);
                                System.out.println("send message is:"+data_send);
                                byte[] by = data_send.getBytes(StandardCharsets.UTF_8); 
                                ByteBuffer buf=ByteBuffer.allocate(1024);
		                        buf.clear();
		                        buf.put(by);
		                        buf.flip();
                                channel.send(buf, new InetSocketAddress("127.0.0.1",port));
                            }
                        


                    }
                }
            }
            
            

        }
    }
}
