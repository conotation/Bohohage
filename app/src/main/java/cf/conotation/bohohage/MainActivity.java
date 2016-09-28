package cf.conotation.bohohage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarContext;
import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    protected Context context;
    protected SharedPreferences s;
    protected SharedPreferences.Editor editor;
    protected TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void init() throws IOException {
        context = this;
        s = getSharedPreferences("v", 0);
        editor = s.edit();
        tv = (TextView) findViewById(R.id.tv);
        SugarContext.init(context);

        if (s.getLong("LastUpdate", 0) == 0 || s.getLong("LastUpdate", 0) + 120960000 > System.currentTimeMillis()) {

            try {
                new DataDownload().execute("clinic");
//                Log.e("Exists Tag", "1");

                new DataDownload().execute("pharmacy");
//                Log.e("Exists Tag", "2");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void db_init(ArrayList<Apidata> aList) {
        SugarRecord.saveInTx(aList);
        Toast.makeText(context, "Complete....", Toast.LENGTH_SHORT).show();

        editor.putLong("LastUpdate", System.currentTimeMillis());
        editor.commit();
    }

    public class DataDownload extends AsyncTask<String, String, String> {
        String s;
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(context);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setMessage("Init...");
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                return (String) getData((String) strings[0]);
            } catch (Exception e) {
                return "Download Failed";
            }
        }

        protected void onPostExecute(String result) {
            ArrayList<Apidata> aList = new ArrayList<>();
            try {
                JSONObject topObject = new JSONObject(result);
                JSONObject object = topObject.getJSONObject("localdata-asfp-animal_" + s);
                JSONArray dataArray = (JSONArray) object.get("row");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject data = (JSONObject) dataArray.get(i);

                    String aName = data.get("BIZPLC_NM").toString();
                    String aAddress = data.get("LOCPLC_LOTNO_ADDR").toString();
                    LatLng latLng = null;
                    if (data.get("LAT").toString().equals("")) {
                        latLng = new LatLng(0f, 0f);
                    } else {
                        latLng = new LatLng(Float.parseFloat(data.get("LAT").toString()), Float.parseFloat(data.get("LNG").toString()));
                    }

                    int z = 0;
                    if (data.get("STOCKRS_DUTY_DIV_NM").toString().equals("동물약국")) {
                        z = 1;
                    } else {
                        z = 0;
                    }
//                    if (i == 0) {
//                        Log.e("Tag", aName + "\n" + aAddress + "\n" + latLng.toString() + "\n" + z);
//                        Apidata apidata = new Apidata(aName, aAddress, latLng.latitude, latLng.longitude, z);
//                        SugarRecord.save(apidata);
//                    }
                }

            } catch (Exception e) {
                aList = null;
                e.printStackTrace();
            }
            db_init(aList);

            Apidata a1 = SugarRecord.findById(Apidata.class, 1);
            Apidata a2 = SugarRecord.findById(Apidata.class, 2);
            Apidata a3 = SugarRecord.findById(Apidata.class, 3);
            Apidata a4 = SugarRecord.findById(Apidata.class, 4);
            tv.setText(a1.sName + " " + a1.sAddress + " " + a1.sLat + " " + a1.sLng + "\n" + a2.sType + a3.sAddress + a4.sName);

            pd.dismiss();



        }

        private String getData(String str) {
            this.s = str;
            StringBuilder sb = new StringBuilder();
            String inUrl = "";
            inUrl = "http://data.gwd.go.kr/apiservice/45416f416a6b6b6135314d6b734371/json/localdata-asfp-animal_" + str + "/1/200";

            try {
                BufferedInputStream bis = null;
                URL url = new URL(inUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                int resCode;

                con.setConnectTimeout(3000);
                con.setReadTimeout(36000);

                resCode = con.getResponseCode();

                if (resCode == 200) {
                    bis = new BufferedInputStream(con.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"));
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    bis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return sb.toString();
        }


    }


}
