package BLEncode;


/**
 * Hello world!
 */
public final class test {
    private test() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        BLEncode blEncode=new BLEncode();
        //blEncode.BlockEncode("/Users/user/database/", "70092001_BList", "tbl_ParamInfo", 0, "/Users/user/", "BasicCompressedData");
        //blEncode.BlockEncode("/Users/user/database/", "10716135_BIncList", "tbl_ParamInfo", 1, "/Users/user/", "ExtraCompressedData");
        blEncode.checkData("/Users/user/", "BasicCompressedData", 0, "/Users/user/");
        //blEncode.checkData("/Users/user/", "ExtraCompressedData", 1, "/Users/user/");
        
    }
}
