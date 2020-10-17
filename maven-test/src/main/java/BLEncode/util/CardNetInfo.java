package BLEncode.util;
public class CardNetInfo{
    public int CardNet;
    public int count;
    
    public CardNetInfo(){
        CardNet=-1;
        count=0;
    }
    public void setCardNet(int CardNet){
        this.CardNet=CardNet;
    }
    public int getCardNet(){
        return this.CardNet;
    }
    public void setCount(int count){
        this.count=count;
    }
    public int getCount(){
        return this.count;
    }
}