import sys
import socket
import select
import time
import re
import os


def readtimetable(station_name):
    timetable = []
    filename = 'tt-' + station_name
    with open(filename) as file:
        for line in file.readlines():
            line = line.strip('\n')
            info = line.split(',')
            timetable.append(info)
    file.close()
    return timetable

def is_timetableChange(stationname,now_time):
    changed_time = time.ctime(os.stat('tt-'+stationname).st_mtime)
    if now_time < changed_time:
        return True
    else:
        return False

def main():
    #Processing parameters
    if len(sys.argv)<5:
        sys.ecit()
    
    station_name = sys.argv[1]
    tcp_port = int(sys.argv[2])
    udp_port = int(sys.argv[3])

    #Stores adjacent port Numbers
    neighbour_port = []
    #Stores the number of paths from any starting point to the station to find the fastest path
    route_num = {}
    #Store the output ready to be sent to the browser to find the fastest path
    save_string = {}
    #The number of buses to the station
    Bus = []
    #Store the final time to the station in order to find the fastest path
    time_compare = []
    #Store the output ready to be sent，To know which socket to send
    result = {}
    #the dictionary which key is socket, value is destination,To know which socket to send
    s = {}
    localhost = '127.0.0.1'
    
    for i in range(4,len(sys.argv)):
        neibour = int(sys.argv[i])
        neighbour_port.append(neibour)
    
    timetable = readtimetable(station_name)
    #Gets the last time the schedule was modified
    modify_time = time.ctime(os.stat('tt-'+station_name).st_mtime)

    #initialises its TCP and UDP ports
    tcpsocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcpsocket.bind((localhost,tcp_port))
    tcpsocket.listen()
    tcpsocket.setblocking(False)
    udpsocket = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    udpsocket.bind((localhost,udp_port))

    #create listenlist and writelist
    listenlist = [tcpsocket,udpsocket,]
    writelist = []

    while True:
        neighbour = []
        for i in neighbour_port:
            ne = i;
            neighbour.append(ne)

        #use select
        read,write,error = select.select(listenlist,writelist,[])

        for skt in read:
            if skt == tcpsocket:
                #tcp accept
                conn, addr = tcpsocket.accept()
                conn.setblocking(False)
                listenlist.append(conn)
            elif skt == udpsocket:
                #udp read
                data, address = udpsocket.recvfrom(1024)
                #decode data
                data_string = data.decode('utf-8')
                print('receive:'+ data_string)
                #Split the received information
                arr = data_string.split(';')

                #Check the prefix
                if int(arr[0]) == 1:
                    print('find route')

                    #Whether to find the destination or not
                    if arr[1]==station_name:
                        print('find the destination')
                        #get the start staion of this route
                        start = arr[2].split(',')[0]

                        #record the num of routes from this start point to the destination
                        if start in route_num:
                            if route_num[start] == None:
                                break
                            else:
                                route = route_num[start]
                                route_num[start] = route+1
                        else:
                            route_num[start] = 1
                        
                        #create data_send,This data means sending the path back to the starting point
                        data_send=''
                        #The format is: 2; The starting point; The path; The port number of the current station
                        data_send = str(2) + ';' + arr[2].split(',')[0] + ';'
                        for j in range(2,len(arr)):
                            data_send = data_send + arr[j] + ';'

                        data_send = data_send + station_name + ',' + str(udp_port) + ';' + str(len(arr))
                        
                        port = int(arr[len(arr)-1].split(',')[1])
                        print('send to:'+ str(port))
                        print('send message is：'+ data_send)
                        skt.sendto(data_send.encode('utf-8'),(localhost,port))
                    elif arr[1]!=station_name:
                        print('not find the destination')

                        #Check which stations exist in the path,Are these stations adjacent to the current station?
                        for j in range(2,len(arr)):
                            n = arr[j].split(',')[1]
                            int_n = int(n)
                            for i in neighbour:
                                if int_n == i:
                                    neighbour.remove(int_n)
                        
                        #Add information about the current station
                        data_send=''
                        for j in range(0,len(arr)):
                            data_send = data_send + arr[j] + ';'
                        data_send = data_send + station_name + ',' + str(udp_port)

                        
                        for j in range(0,len(neighbour)):
                            port = neighbour.pop()
                            print('send to:'+ str(port))
                            print('send message is：'+ data_send)
                            
                            skt.sendto(data_send.encode('utf-8'),(localhost,port))
                elif int(arr[0])==2:
                    print('Return the route')
                    if arr[1]==station_name:
                        print('find the start')
                        b = int(arr[len(arr)-1])
                        #get now time
                        now = time.strftime("%H:%M")
                        time_inital = ''
                        bus = ''
                        Time = ''
                        #find the next bus
                        for j in range(1,len(timetable)):
                            if timetable[j][0]>now and timetable[j][4] == arr[b].split(',')[0] :
                                Time = timetable[j][3]
                                time_inital = timetable[j][0]
                                bus = timetable[j][1]
                                break
                        #create data_send,This data means find time
                        data_send=''
                        #The format is: 3; time; time_inital; route; bus; The port number of the current station 
                        data_send = str(3) + ';' + Time + ';' + time_inital + ';'
                        for j in range(2,len(arr)-1):
                            data_send =data_send +arr[j] + ';'
                        
                        data_send = data_send +bus + ';' + str(3)
                        port = int(arr[3].split(',')[1])
                        print('send to:'+ str(port))
                        print('send message is：'+ data_send)
                        skt.sendto(data_send.encode('utf-8'),(localhost,port))
                    elif arr[1]!=station_name:
                        print('not find the start')
                        data_send=''
                        for j in range(0,len(arr)-1):
                            data_send = data_send + arr[j] + ';'
                        b = int(arr[len(arr)-1])
                        data_send = data_send + str(b-1)
                        port = int(arr[b-2].split(',')[1])
                        print('send to:'+ str(port))
                        print('send message is：'+ data_send)
                        skt.sendto(data_send.encode('utf-8'),(localhost,port))
                elif int(arr[0])==3:
                    print('find time')
                    
                    if arr[len(arr)-3].split(',')[0]==station_name:
                        print('find the destnation')
                        bus_num = arr[len(arr)-2]
                        Time = arr[1]
                        #create data_send,This data means return time
                        data_send=''
                        #The format is: 4; start station; time; time_inital; route; The port number of the current station
                        data_send = str(4) + ';' + arr[3].split(',')[0] + ';' + arr[2] + ';' +Time + ';'
                        for j in range(3,len(arr)-2):
                            data_send = data_send + arr[j] + ';'
                        data_send = data_send + str(len(arr)-2)
                        start = arr[3].split(',')[0]
                        
                        #The data has been sent
                        if route_num[start]==None:
                            break
                        #begin to compare the time
                        if len(Bus) == route_num[start]-1:
                            while len(time_compare)!=0:
                                remove = time_compare.pop()
                                if Time>remove :
                                    data_send = save_string[remove]
                                    Time = remove
                            arr1 = data_send.split(';')
                            b1 = int(arr1[len(arr1)-1])-1
                            port = int(arr1[b1].split(',')[1])
                            Bus.clear()
                            time_compare.clear()
                            save_string.clear()
                            route_num[start]=None
                            print('send to:'+str(port))
                            skt.sendto(data_send.encode('utf-8'),(localhost,port))
                        else:
                            if bus_num not in Bus:
                                Bus.append(bus_num)
                                time_compare.append(Time)
                                save_string[Time] = data_send
                    elif arr[len(arr)-3].split(',')[0]!=station_name:
                        print('not find the destnation')
                        data_send=''
                        data_send = str(3)+';'
                        now = arr[1]
                        BUS = ''
                        b = int(arr[len(arr)-1])
                        #find next bus
                        for j in range(1,len(timetable)):
                            if timetable[j][0]>now:
                                print('j='+str(j))
                                if timetable[j][4]==arr[b+2].split(',')[0]:
                                    now = timetable[j][3]
                                    BUS = timetable[j][1]
                                    break
                        data_send = data_send + now + ';' + arr[2] + ';'
                        for j in range(3,len(arr)-2):
                            data_send = data_send + arr[j] + ';'
                        
                        data_send = data_send +BUS+ ';' + str(b+1)
                        port = int(arr[b+2].split(',')[1])
                        print('send to:'+ str(port))
                        print('send message is：'+ data_send)
                        skt.sendto(data_send.encode('utf-8'),(localhost,port))
                elif int(arr[0])==4:
                    print('return time')
                    if arr[1]==station_name:
                        print('find the start')
                        #start preparing for the HTTP response
                        time_arrive = arr[3]
                        use_bus = ''
                        stop = ''
                        use_time = arr[2]
                        for j in range(0,len(timetable)):
                            if timetable[j][0]==use_time:
                                use_bus = timetable[j][1]
                                stop = timetable[j][2]
                                break
                        #can not find the bus today
                        if use_time=='' or time_arrive=='' or use_bus=='' or stop=='' :
                            output = 'a valid route does not exist on the current day.'
                        else:
                            output = 'At' + use_time + ', catch ' + use_bus + ', from ' + stop + '. You will arrive at your final destination at ' + time_arrive + '.'
                        result[arr[len(arr)-2].split(',')[0]] = output
                    elif arr[1]!=station_name:
                        print('not find the start')
                        data_send = ''
                        for j in range(0,len(arr)-1):
                            data_send = data_send + arr[j] + ';'
                        b = int(arr[len(arr)-1])
                        data_send = data_send + str(b-1)
                        port = int(arr[b-2].split(',')[1])
                        print('send to:'+ str(port))
                        print('send message is：'+ data_send)
                        skt.sendto(data_send.encode('utf-8'),(localhost,port))    
            else:
                #check timetable changed or not
                if is_timetableChange(station_name,modify_time):
                    readtimetable(station_name)
                    modify_time = time.ctime(os.stat('tt-'+station_name).st_mtime)
                #get the destination
                destination = ''
                data = skt.recv(1024).decode('utf-8')
                request_header_lines = data.splitlines()
                http_header_data = request_header_lines[0]
                data_list = re.split(r"/|\?\s|=| ",http_header_data)
                for i in range(len(data_list)):
                    if data_list[i] == 'HTTP':
                        destination = data_list[i-1]
                    else:
                        continue
                #avoid favicon
                if destination == 'favicon.ico':
                    skt.close()
                    listenlist.remove(skt)
                    continue
                result[destination] = 'null'
                s[skt] = destination
                #create data_send,This data means find route
                data_send=''
                #The format is: 1; destination; route
                data_send = str(1) + ';' + destination + ';' + station_name + ',' + str(udp_port)
                for i in neighbour_port:
                    print('send to:'+ str(i))
                    print('send message is：'+ data_send)
                    udpsocket.sendto(data_send.encode('utf-8'),(localhost,i))
                #skt remove from listen append write
                writelist.append(skt)
                listenlist.remove(skt)
        for skt in write:
            destination = s.get(skt)
            if result.get(destination)=='null':
                #not prepare for write
                continue
            elif result.get(destination)!='null':
                #http response
                output = result.get(destination)
                responseHeaderLines = "HTTP/1.1 200 OK\r\n"
                responseHeaderLines += "\r\n"
                responseBody = output
                response = responseHeaderLines + responseBody
                skt.send(response.encode('utf-8'))
                writelist.remove(skt)
                skt.close()
                
    udpsocket.close()
    tcpsocket.close()
if __name__ == '__main__':
    main()
                

                    


                

