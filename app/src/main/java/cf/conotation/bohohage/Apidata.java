package cf.conotation.bohohage;

import com.orm.dsl.Table;

/**
 * Created by Conota on 2016-09-28.
 */
@Table
public class Apidata {
    String sName;
    String sAddress;
    Double sLat;
    Double sLng;
    int sType;

    public Apidata() {

    }

    public Apidata(String sName, String sAddress, Double sLat, Double sLng, int sType) {
        this.sName = sName;
        this.sAddress = sAddress;
        this.sLat = sLat;
        this.sLng = sLng;
        this.sType = sType;
    }
}
