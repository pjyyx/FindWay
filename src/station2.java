

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.text.SimpleDateFormat;


public class station2 {
    static String station_name;
    static int tcp_port;
    static int udp_port;
    static int[] neighbor_port;
    static double[] location = new double[2];

    static String[][] timetable;
    
    static LinkedList neighbor = new LinkedList<Integer>();
    static LinkedList compare = new LinkedList<String>();
    static LinkedList bus = new LinkedList<String>();

    public station2(String station_name, int tcp_port, int udp_port, int[] neighbor_port) throws Exception {
        if (station_name == null)
            throw new Exception();
        station.neighbor_port = neighbor_port;
        station.station_name = station_name;
        station.tcp_port = tcp_port;
        station.udp_port = udp_port;


    }

   /* public void TCPServer(InputStream is) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        String[] arr = line.split(" ");
        destination = arr[1].substring(5);

    }
    
    public static byte[] UDPServer(DatagramSocket ds) throws IOException {

        byte[] bys = new byte[1024];
        DatagramPacket dp = new DatagramPacket(bys, bys.length);
        ds.receive(dp);

        byte[] data = dp.getData();
        return data;

    }
    

    public static void UDPClient(byte[] bys, DatagramSocket ds, int port) throws IOException {

        int length = bys.length;
        InetAddress address = InetAddress.getLocalHost();
        DatagramPacket dp = new DatagramPacket(bys, length, address, port);

        ds.send(dp);
        ds.close();

    }
    */

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

        timetable = new String[(int) lines - 1][5];

        String line;
        line = br.readLine();
        String[] arr = line.split(",");
        location[0] = Double.valueOf(arr[1]);
        location[1] = Double.valueOf(arr[2]);
        int j = 0;

        while ((line = br.readLine()) != null) {
            arr = line.split(",");
            if(!bus.contains(arr[1])){
                bus.add(arr[1]);
            }
            timetable[j] = arr;
            
            j++;
        }

        br.close();

    }

    public static void main(String[] args) throws Exception {
        /*ServerSocket tcp_server = new ServerSocket(tcp_port);
        Socket tcp_socket = tcp_server.accept();
        InputStream is = tcp_socket.getInputStream();

        DatagramSocket ds = new DatagramSocket(udp_port);
        read_timetable(station_name);

        // 1. 判断浏览器端有没有传输数据过来
        if (destination != "initial") {
            if (destination == station_name) {// 判断目的地是否是该站点
                tcp_server.close();
                tcp_socket.close();
                is.close();
                ds.close();
                throw new Exception("The starting point is the same as the destination");
            } else {
                for (int i = 0; i < neighbor_port.length; i++) {

                    //拼接传输的字符串，（目的地；发送方站点，离站时间，车，离站站台，到达时间，到达地点；...）
                    SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间 
                    sdf.applyPattern("HH:mm");// a为am/pm的标记  
                    Date now = new Date();// 获取当前时间
                    String now_string = sdf.format(now); 
                    //Integer bool = time.compareTo(now_string);

                    for(int j=0;j<timetable.length;j++){
                        Integer bool = timetable[j][0].compareTo(now_string);
                        if(bool >= 0){
                            if(arrive_station.contains(timetable[j][4])){
                                String string_send = destination + ";" + station_name + ";" + timetable[j][0] + "," + timetable[j][1] + "," + timetable[j][2] + "," + timetable[j][3] + "," + timetable[j][4] + ";";
                                UDPClient(string_send.getBytes(), ds, port);
                            }
                        }
                    }

                    String string_send = destination + timetable[]
                    UDPClient(destination.getBytes(), ds, neighbor_port[i]);
                }

            }
        }

        byte[] data_recive = UDPServer(ds);
        if(data_recive != null){
            String string_recive = new String(data_recive);
            String[] arr = string_recive.split(",");
            arr[]
        }
        
        
        

        tcp_server.close();
        tcp_socket.close();
        is.close();
        ds.close();
        */
        
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
        System.out.println("0000000000000000");

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(tcp_port));
            System.out.println(tcp_port);
            serverSocketChannel.configureBlocking(false);//非阻塞
            // 注册选择器,设置选择器选择的操作类型
            Selector selector = Selector.open();
            //将serverSocketChannel注册在selector中
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);

            DatagramSocket ds = new DatagramSocket(udp_port);
            read_timetable(station_name);

            while(true) {
                System.out.println("11111111111111111111111");
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) { 
                    System.out.println("接受到请求..."); //设置为非阻塞 
                    socketChannel.configureBlocking(false); 
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    selector.select();//如果没有任何事件可以处理，则该方法处于阻塞状态
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while(iterator.hasNext()){//这里的事件都是已经筛选好的可以处理的事件
                        System.out.println("处理事件");
                        System.out.println("----"+selectionKeys.size()+"----");
                        SelectionKey next = iterator.next();
                        iterator.remove();//删除key防止重复处理
                        if(next.isReadable()){
                            System.out.println("数据读取事件");
                        
                        //接收到数据读取事件
                            SocketChannel channel = (SocketChannel)next.channel();
                            ByteBuffer dest = ByteBuffer.allocate(1024);
                            int read = channel.read(dest);
                            String data = new String(dest.array(),0,read);
                            System.out.println(data);
                            String[] arr = data.split("\n");
                            String line = arr[0];
                            System.out.println(line);
                            String[] array = line.split(" ");
                            String destination = array[1].substring(5);
                            System.out.println(destination);


                            String string_send = String.valueOf(1)+ ";" + destination + "," + String.valueOf(udp_port) + ";";
                            byte[] bys = string_send.getBytes();
                            int length = bys.length;
                            InetAddress address = InetAddress.getLocalHost();
                            for(int i=0;i<neighbor_port.length;i++){
                            DatagramPacket dp_send = new DatagramPacket(bys, length, address, neighbor_port[i]);
                            ds.send(dp_send);
                            }

                        }
                    }
                }
               /* //选择事件,这里会阻塞
                selector.select();//如果没有任何事件可以处理，则该方法处于阻塞状态
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                System.out.println("22222222222222222");
                while(iterator.hasNext()){//这里的事件都是已经筛选好的可以处理的事件
                    System.out.println("处理事件");
                    System.out.println("----"+selectionKeys.size()+"----");
                    SelectionKey next = iterator.next();
                    iterator.remove();//删除key防止重复处理
                    if(next.isReadable()){
                        System.out.println("数据读取事件");
                        
                        //接收到数据读取事件
                        SocketChannel channel = (SocketChannel)next.channel();
                        ByteBuffer dest = ByteBuffer.allocate(1024);
                        int read = channel.read(dest);
                        String data = new String(dest.array(),0,read);
                        System.out.println(data);
                        String[] arr = data.split("\n");
                        String line = arr[0];
                        System.out.println(line);
                        String[] array = line.split(" ");
                        String destination = array[1].substring(5);
                        System.out.println(destination);


                        String string_send = String.valueOf(1)+ ";" + destination + "," + String.valueOf(udp_port) + ";";
                        byte[] bys = string_send.getBytes();
                        int length = bys.length;
                        InetAddress address = InetAddress.getLocalHost();
                        for(int i=0;i<neighbor_port.length;i++){
                            DatagramPacket dp_send = new DatagramPacket(bys, length, address, neighbor_port[i]);
                            ds.send(dp_send);
                        }

                    }else  if(next.isAcceptable()){
                        System.out.println("连接请求事件");
                        SocketChannel accept = serverSocketChannel.accept();
                        accept.configureBlocking(false);
                        //接收到请求，将连接注册到选择器中，并且设置监听类型为read
                        accept.register(selector,SelectionKey.OP_READ);
                    }
                }*/
                System.out.println("33333333");
                int i =0;
                while(i<neighbor_port.length){
                    System.out.println("i ="+ i );
                    
                    byte[] bys = new byte[1024];

                    DatagramPacket dp = new DatagramPacket(bys, bys.length);
                    System.out.println("44444444444");
                    ds.receive(dp);
                    System.out.println("44444444444");
                    byte[] data = dp.getData();
                    
                    if(data == null){
                        System.out.println("555");
                        break;
                    }
                    if(data != null){
                        String data_string = new String(data);
                        String[] arr = data_string.split(";");
                        if(Integer.parseInt(arr[0]) == 1){//找路径
                            int a = arr.length-1;
                            if(arr[1]==station_name){//这一站是目的地
                                
                                String data_send = String.valueOf(2) + ";" + arr[2].split(",")[0] + ";";
                                for(int j=2;j<=a;j++){
                                    data_send = data_send + arr[j] + ";";
                                }
                                data_send = data_send + String.valueOf(a);
                                int port = Integer.parseInt(arr[a].split(",")[1]);
                                byte[] by = data_send.getBytes(); 
                                int length = by.length;
                                InetAddress address = InetAddress.getLocalHost();
                                DatagramPacket dp_send = new DatagramPacket(by, length, address, port);

                                ds.send(dp_send);
                                

                            }
                            if(arr[1]!=station_name){//这一站不是目的地
                                for(int j=2;j<=a;j++){
                                    if(neighbor.contains(arr[j].split(",")[0])){
                                        neighbor.remove(arr[j].split(",")[0]);
                                    }
                                }
                                String data_send = new String();
                                for(int j=0;j<=a;j++){
                                    data_send=data_send + arr[j] + ";";
                                }
                                data_send = data_send + station_name + "," + udp_port + ";";
                                
                                byte[] by = data_send.getBytes(); 
                                int length = by.length;
                                InetAddress address = InetAddress.getLocalHost();
                                
                                for(int j=0;j<neighbor.size();j++){
                                    DatagramPacket dp_send = new DatagramPacket(by, length, address, (int) neighbor.remove());
                                    ds.send(dp_send);
                                }
                                
                            }
                        }
                        if(Integer.parseInt(arr[0]) == 2){//找起点
                            int a = arr.length-2;
                            if(arr[1]==station_name){//这一站是起点
                                //找时间
                                SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间 
                                sdf.applyPattern("HH:mm");// a为am/pm的标记  
                                Date now = new Date();// 获取当前时间
                                String now_string = sdf.format(now);
                                String time = new String();

                                for(int j=0;j<timetable.length;j++){
                                    Integer bool = timetable[j][0].compareTo(now_string);
                                    if(bool >= 0){
                                        if(timetable[j][4]==arr[3].split(",")[0]){
                                            time = timetable[j][0];
                                            break;
                                        }
                                        
                                    }
                                }

                                
                                String data_send = String.valueOf(3) + ";" + time + ";" + time + ";";
                                for(int j=2;j<=a;j++){
                                    data_send = data_send + arr[j] + ";";
                                }
                                data_send = data_send + String.valueOf(3);
                                int port = Integer.parseInt(arr[4].split(",")[1]);
                                byte[] by = data_send.getBytes(); 
                                int length = by.length;
                                InetAddress address = InetAddress.getLocalHost();
                                DatagramPacket dp_send = new DatagramPacket(by, length, address, port);

                                ds.send(dp_send);
                                

                            }
                            if(arr[1]!=station_name){//这一站不是目的地
                                String data_send = new String();
                                for(int j=0;j<=a;j++){
                                    data_send=data_send + arr[j] + ";";
                                }
                                int b = Integer.valueOf(arr[arr.length-1]);
                                data_send = data_send + ";" + String.valueOf(b-1) + ";";
                                int port = Integer.parseInt(arr[b-1].split(",")[1]);
                                byte[] by = data_send.getBytes(); 
                                int length = by.length;
                                InetAddress address = InetAddress.getLocalHost();
                                
                                
                                DatagramPacket dp_send = new DatagramPacket(by, length, address, port);
                                ds.send(dp_send);
                                
                                
                            }
                        }
                        if(Integer.parseInt(arr[0]) == 3){
                            int a = arr.length-2;
                            if(arr[arr.length-2].split(",")[0]==station_name){//这一站是目的地
                                String bus_num = new String();
                                String data_send = String.valueOf(4) + ";" + arr[3].split(",")[0] + ";" + arr[2].split(",")[0] + ";";
                                String time = arr[1];
                                for(int j=0;j<timetable.length;j++){
                                    if(timetable[j][0]==time){
                                        bus_num = timetable[j][1];
                                        break;
                                    }
                                }
                                if(bus.contains(bus_num)){
                                    if(bus_num.isEmpty()){
                                        for(int k=0;k<compare.size();k++){
                                            String out = (String) compare.remove();
                                            Integer bool = time.compareTo(out);
                                            if(bool<0){
                                                time = out;
                                            }
                                        }
                                        data_send = data_send + time + ";";
                                        for(int j=3;j<=a;j++){
                                            data_send=data_send + arr[j] + ";";
                                        }
                                        data_send = data_send + String.valueOf(a)+ ";";
                                        int port = Integer.parseInt(arr[a].split(",")[1]);
                                        byte[] by = data_send.getBytes(); 
                                        int length = by.length;
                                        InetAddress address = InetAddress.getLocalHost();
                                
                                
                                        DatagramPacket dp_send = new DatagramPacket(by, length, address, port);
                                        ds.send(dp_send);
                                    }
                                    if(!bus_num.isEmpty()){
                                        compare.add(arr[1]);
                                    }
                                }
                                if(!bus.contains(bus_num)){
                                    continue;
                                }
                            }
                            if(arr[1]!=station_name){//这一站不是目的地
                                String data_send = String.valueOf(3) + ";";
                                String now = arr[1];
                                for(int j=0;j<timetable.length;j++){
                                    Integer bool = timetable[j][0].compareTo(now);
                                    if(bool >= 0){
                                        if(timetable[j][4]==arr[Integer.parseInt(arr[arr.length-1])+1].split(",")[0]){
                                            now = timetable[j][0];
                                            break;
                                        }
                                        
                                    }
                                }
                                data_send = data_send + now + ";";
                                for(int j=2;j<a;j++){
                                    data_send = data_send + arr[j] +";";
                                }
                                int b = Integer.valueOf(arr[arr.length-1]);
                                data_send = data_send + String.valueOf(b+1) + ";";
                                int port = Integer.parseInt(arr[b+1].split(",")[1]);
                                byte[] by = data_send.getBytes(); 
                                int length = by.length;
                                InetAddress address = InetAddress.getLocalHost();
                                DatagramPacket dp_send = new DatagramPacket(by, length, address, port);
                                ds.send(dp_send);
                                
                            }
                        }
                        if(Integer.parseInt(arr[0]) == 4){
                            if(arr[1]==station_name){
                                String time_arrive = arr[3];
                                String bus = new String();
                                String stop = new String();
                                String time = arr[2];
                                for(int j=0;j<timetable.length;j++){
                                    if(timetable[j][0]==time){
                                        bus = timetable[j][1];
                                        stop = timetable[j][2];
                                    }
                                }
                                String output = "At" + time + ", catch" + bus + ", from" + stop +". You will arrive at your final destination at "+ time_arrive +"'";
                                //http response

                                OutputStream os = serverSocketChannel.socket().accept().getOutputStream();
                                // 写入HTTP协议响应头,固定写法
                                os.write("HTTP/1.1 200 OK\r\n".getBytes());
                                os.write("Content-Type:text/html\r\n".getBytes());
                                // 必须要写入空行,否则浏览器不解析
                                os.write("\r\n".getBytes());
                                byte[] bytes = output.getBytes();
                                os.write(bytes);
                                

                            }
                            if(arr[1]!= station_name){
                                String data_send = new String();
                                for(int j=0;j<=arr.length-2;j++){
                                    data_send=data_send + arr[j] + ";";
                                }
                                int b = Integer.valueOf(arr[arr.length-1]);
                                data_send = data_send + ";" + String.valueOf(b-1) + ";";
                                int port = Integer.parseInt(arr[b-1].split(",")[1]);
                                byte[] by = data_send.getBytes(); 
                                int length = by.length;
                                InetAddress address = InetAddress.getLocalHost();
                                
                                
                                DatagramPacket dp_send = new DatagramPacket(by, length, address, port);
                                ds.send(dp_send);
                            }
                        }
                    }
                    i++;
                    neighbor = new LinkedList<Integer>();
                    compare = new LinkedList<String>();
                    bus = new LinkedList<String>();
                    for(int j=0;j<neighbor_port.length;j++){
                        neighbor.add(neighbor_port[j]);
                    }
                    read_timetable(station_name);
                }
                //恢复邻居队列
                neighbor = new LinkedList<Integer>();
                    compare = new LinkedList<String>();
                    bus = new LinkedList<String>();
                    for(int j=0;j<neighbor_port.length;j++){
                        neighbor.add(neighbor_port[j]);
                    }
                    read_timetable(station_name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}