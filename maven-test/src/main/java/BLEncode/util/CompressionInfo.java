package BLEncode.util;
public class CompressionInfo{
    Boolean isCorrect;
    byte[] compressedData;
    public CompressionInfo(){
        isCorrect=true;
        compressedData=new byte[8];
    }
    public void setIsCorrect(Boolean isCorrect){
        this.isCorrect=isCorrect;
    }
    public Boolean getIsCorrect(){
        return isCorrect;
    }
    public void setCompressedData(byte[] compressedData){
        for(int i=0;i<8;i++){
            this.compressedData[i]=compressedData[i];
        }
    }
    public byte[] getCompressedData(){
        return compressedData;
    }
}