package BLEncode;

import BLEncode.util.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


public class BLEncode{
    ArrayList<Byte> loadedData=new ArrayList<Byte>();
    ArrayList<CardNetInfo> cardNetInfos=new ArrayList<CardNetInfo>();
    Connection conn=null;

    public BLEncode(){
    }
    //将压缩数据存入dat文件中
    public void BlockEncode(String databasePath,String databaseName,String tableName,
                int flag ,String compressedDataPath, String compressedDataFileName){

        if(!connectDatabase(databasePath, databaseName)){
            System.out.println("can't connect the database!");
            System.exit(0);
        }
        if(!getCardnetInfo(tableName)){
            System.out.println("can't get the CardNetInfosß!");
            System.exit(0);
        }
        loadedData.clear();
        
        try{
            Statement stmt=conn.createStatement();
            ResultSet rs=null;
            for(int i=0;i<cardNetInfos.size();i++){
                String sql= "SELECT * FROM " + tableName + " WHERE CardNet = "
                            + cardNetInfos.get(i).getCardNet() + " ORDER BY CardID ASC";
                rs=stmt.executeQuery(sql);
                while(rs.next()){
                    CompressionInfo compressionInfo=new CompressionInfo();
                    compressionInfo=compressData(rs.getString("CardID"));
                    if(compressionInfo.getIsCorrect()==false){
                        cardNetInfos.get(i).setCount(cardNetInfos.get(i).getCount()-1);
                        System.out.println(rs.getString("CardID"));
                        continue;
                    }
                    byte[] compressedData=compressionInfo.getCompressedData();
                    for(int j=0;j<8;j++){
                        loadedData.add(compressedData[j]);
                    }
                    int temp=rs.getInt("BListType");
                    loadedData.add((byte)(temp&0xff));
                    if(flag==1){
                        temp=rs.getInt("status");
                        loadedData.add((byte)(temp&0xff));
                    }
                    

                }
            } 
            if(flag==0){
                System.out.println("读取数据行数： "+loadedData.size()/9);
            }else{
                System.out.println("读取数据行数： "+loadedData.size()/10);
            }
            
            stmt.close();
            rs.close();
            conn.close();
        }catch(Exception e){
            System.err.println(e.getClass().getName()+":"+e.getMessage());
            System.exit(0);
        }
        
        try{
            String index = "";
	        for (int i = 0; i < cardNetInfos.size(); i++)
	        {
		        index = index + cardNetInfos.get(i).getCardNet() + ":" + cardNetInfos.get(i).getCount() + "#";
            }
            index=index+'\n';
            FileOutputStream fileOutputStream=new FileOutputStream(compressedDataPath+compressedDataFileName+".txt");
            fileOutputStream.write(index.getBytes());;
            for(int i=0;i<loadedData.size();i++){
                fileOutputStream.write(loadedData.get(i));
            }
            fileOutputStream.close();
        }catch(Exception e){
            System.err.println(e.getClass().getName()+":"+e.getMessage());
            System.exit(0);
        }
        
    }
    //将压缩数据解压缩并存入txt文件中
    public void checkData(String compressedDataPath, String compressedDataFileName,
                            int flag, String decompressedDataPath){
        loadedData.clear();
        cardNetInfos.clear();
        byte[] buffer=new byte[1];
        try{
            String str="";
            int sequence=0;
            FileInputStream fileInputStream=new FileInputStream(compressedDataPath+compressedDataFileName+".txt");
            while(fileInputStream.read(buffer)!=-1){
                CardNetInfo cardNetInfo=new CardNetInfo();
                byte index=buffer[0];
                if (index == ':')
		        {
			        cardNetInfo.setCardNet(Integer.parseInt(str));
			        cardNetInfos.add(cardNetInfo);
			        str = "";
			        continue;
		        }
		        else if (index == '#')
		        {
			        cardNetInfos.get(sequence).setCount(Integer.parseInt(str));
			        sequence++;
			        str = "";
			        continue;
		        }
		        else if (index == '\n')
		        {
			        break;
		        }
		        str = str + (index-'0');
            }
            fileInputStream.close();


        }catch(Exception e){
            System.err.println(e.getClass().getName()+":"+e.getMessage());
            System.exit(0);
        }
        try{
            Boolean isBinary=false;
            FileInputStream fileInputStream=new FileInputStream(compressedDataPath+compressedDataFileName+".txt");
            while(fileInputStream.read(buffer)!=-1){
                byte index=buffer[0];
                if(isBinary==false){
                    if(index=='\n'){
                        isBinary=true;
                    }
                    continue;
                }
                loadedData.add(index);
            }
            fileInputStream.close();
        }catch(Exception e){
            System.err.println(e.getClass().getName()+":"+e.getMessage());
            System.exit(0);
        }
        try{
            FileOutputStream fileOutputStream=new FileOutputStream(decompressedDataPath+"Original"+compressedDataFileName+".txt");
            int dataLength=loadedData.size();
            if(flag==0){
                dataLength=9;
            }else if(flag==1){
                dataLength=10;
            }
            byte[] data=new byte[8];
            int sequence=0;
            int count=0;
            for(int i=0;i<loadedData.size();i=i+dataLength){
                for(int j=0;j<8;j++){
                    data[j]=loadedData.get(i+j);
                }
                String CardID=decompressData(data);
                int BListType=loadedData.get(i+8)&0xff;
                int status=-1;
                if(flag==1){
                    status=loadedData.get(i+9)&0xff;
                }
                int CardNet=cardNetInfos.get(sequence).getCardNet();
                count++;
                if(count>=cardNetInfos.get(sequence).getCount()){
                    sequence++;
                    count=0;
                }
                if(flag==0){
                    String info=CardNet+" "+CardID+" "+BListType+'\n';
                    fileOutputStream.write(info.getBytes());
                }else if(flag==1){
                    String info=CardNet+" "+CardID+" "+BListType+" "+status+'\n';
                    fileOutputStream.write(info.getBytes());
                }
            }
            fileOutputStream.close();
        }catch(Exception e){
            System.err.println(e.getClass().getName()+":"+e.getMessage());
            System.exit(0);
        }
    }

    //链接数据库
    Boolean connectDatabase(String databasePath,String databaseName){
        try{
            Class.forName("org.sqlite.JDBC");
            conn=DriverManager.getConnection("jdbc:sqlite:"+databasePath+databaseName+".db");
        }catch(Exception e){
            System.err.println(e.getClass().getName()+":"+e.getMessage());
            System.exit(0);
        }
        if(conn!=null){
            return true;
        }
        return false;
    }

    //得到cardnet类数以及其值
	Boolean getCardnetInfo(String tableName){
        cardNetInfos.clear();
        
        try{
            String sql="SELECT DISTINCT CardNet FROM " + tableName + " ORDER BY CardNet ASC;";
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql);
            while(rs.next()){
                CardNetInfo cardNetInfo=new CardNetInfo();
                cardNetInfo.setCardNet(rs.getInt("CardNet"));
                cardNetInfos.add(cardNetInfo);
            }
            for(int i=0;i<cardNetInfos.size();i++){
                sql="select COUNT(*) from " + tableName + " where CardNet = " + cardNetInfos.get(i).getCardNet();
                rs=stmt.executeQuery(sql);
                while(rs.next()){
                    cardNetInfos.get(i).setCount(rs.getInt(1));
                }
            }
            if(cardNetInfos.size()!=0){
                return true;
            }
            stmt.close();
            rs.close();
        }catch(Exception e){
            System.err.println(e.getClass().getName()+":"+e.getMessage());
            System.exit(0);
        }
        
        return false;
    }	
    //压缩数据	
	CompressionInfo compressData(String str){
        CompressionInfo compressionInfo=new CompressionInfo();
        int len=str.length();
        if(len>16){
            compressionInfo.setIsCorrect(false);
            return compressionInfo;
        }else if(len<16){
            for(int i=len;i<16;i++){
                str=str+"0";
            }
        }
        byte[] data=new byte[8];
        for(int i=0;i<16;i=i+2){
            int num = 0;
            int temp = 0;
            String s=str.substring(i,i+2);
	        for (int j = 0; j < 2; j++)
	        {
                char c=s.charAt(j);
		        if ((c>='0') && (c<='9'))
		        {
			        temp = c - '0';
			        num = (num << 4) | (temp & 0xf);
		        }
		        else if ((c>='a') && (c<='f'))
		        {
			        temp = 10 + (c - 'a');
			        num = (num << 4) | (temp & 0xf);
		        }
		        else if ((c>='A') && (c<='F'))
		        {
			        temp = 10 + (c - 'A');
			        num = (num << 4) | (temp & 0xf);
		        }
		        else
		        {
                    compressionInfo.setIsCorrect(false);
                    return compressionInfo;
		        }
            }
            data[(i/2)]=(byte)(num&0xff);
        }
        compressionInfo.setCompressedData(data);
        compressionInfo.setIsCorrect(true);
        return compressionInfo;
    }
    //解压数据
	String decompressData(byte[] data){
        String str = "";
        int[] temp=new int[2];
        for(int i=0;i<8;i++){
            temp[0]=((data[i]&0xf0)>>4);
            temp[1]=(data[i]&0xf);
            for (int j = 0; j < 2; j++)
	        {
		        if (temp[j] >= 0 && temp[j] <= 9)
		        {
		    	    str = str + temp[j];
		        }
		        else if (temp[j] >= 10 && temp[j] <= 15)
		        {
		    	    char t = 'A';
		    	    t = (char)(t + (temp[j] - 10));
		    	    str = str + t;
		        }
            }
        }
	    
        return str;
    }
    	
}