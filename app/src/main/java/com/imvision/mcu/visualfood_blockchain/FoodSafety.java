package com.imvision.mcu.visualfood_blockchain;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FoodSafety extends AppCompatActivity {
    static private Context context;
    //static private TextView textNews;
    static private ListView jasonNews;

    static private String mJasonUrlString="https://www.fda.gov.tw/DataAction?keyword=%E9%A3%9F%E5%93%81&startdate=2018/1/1";
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        //下置選單功能
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.HomePage:

                    return true;
                case R.id.FoodPage:
                    Intent intent1 = new Intent();
                    intent1.setClass(FoodSafety.this,Food.class);
                    startActivity(intent1);
                    finish();
                    return true;
                case R.id.FoodSafetyPage:
                    Intent intent2 = new Intent();
                    intent2.setClass(FoodSafety.this,FoodSafety.class);
                    startActivity(intent2);
                    finish();
                    return true;
                case R.id.AccountPage:
                    Intent intent3 = new Intent();
                    intent3.setClass(FoodSafety.this,Account.class);
                    startActivity(intent3);
                    finish();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_safety);
        context=getApplicationContext();
        ImageView img=(ImageView)findViewById(R.id.img);
        //textNews=(TextView)findViewById(R.id.textNews);
        jasonNews=(ListView)findViewById(R.id.jasonNews);
        jasonNews();

        //下置選單功能
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    static protected void jasonNews(){
        //textNews.setText("");
        RequestQueue requestQueue =Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(
                Request.Method.GET,
                mJasonUrlString,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try{
                            ArrayList<String> list = new ArrayList<>();
                            for(int i=0;i<response.length();i++){
                                JSONObject news = response.getJSONObject(i);

                                String title=news.getString("標題");
                                String detail=news.getString("內容");
                                String detail2= Html.fromHtml(detail).toString();
                                //String detail2=delHTMLTag(detail);

                                String date=news.getString("發布日期");

                                String str="標題:"+title+"\n"+
                                        "內容:"+detail2+"\n"+
                                        "發布日期"+date;

                                list.add(str);
                            }
                            jasonNews.setAdapter(new ArrayAdapter<String>(context,
                                    android.R.layout.simple_list_item_1,list));
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred
                        Toast toast;
                        toast=Toast.makeText(context,"Error",Toast.LENGTH_LONG);
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);

    }



}