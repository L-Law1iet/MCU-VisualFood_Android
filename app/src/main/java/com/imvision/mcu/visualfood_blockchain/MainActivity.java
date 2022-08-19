package com.imvision.mcu.visualfood_blockchain;

import android.content.Intent;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    static final int GET_PIN_REQUEST = 101;  // The request code
    private static final String TAG = "MainActivity";
    public static String access_token=""; //透過Oauth2取得的Fitbit憑證
    public static String user_id="";
    private TextView txtWelcome,txtShow;
    //以下為Fitbit手環取得的使用者資料，已設定數值。
    public String Steps,Calories,StepsGoal,CaloriesGoal,Weight;
    //以下為使用者自己在spinner選擇的運動
    private Spinner activity_spin,time_spin;
    private Button activity_btn;
    private String activities=""; //運動類別
    private double activity_Cal=0.0; //計算出來的消耗熱量
    private int time = 0; //運動分鐘數
    private String activitiesArray[] = new String[10];
    private String timeArray[] = new String[10];
    private String dateArray[] = new String[10];
    private String calArray[] = new String[10];
    private int activity_time = -1;
    private String[] texttitle = {"今日步數", "今日消耗熱量", "目標一天步數", "目標熱量"};
    private String[] textcounts = {"0", "0", "0", "0"};
    private GridView grid;
    private ImageView img;
    private ViewPager myViewPager;
    private ArrayList<View> pagerList;

        //下置選單功能
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.HomePage:
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.FoodPage:
                    Intent intent1 = new Intent();
                    intent1.setClass(MainActivity.this,Food.class);
                    startActivity(intent1);
                    finish();
                    return true;
                case R.id.FoodSafetyPage:
                    Intent intent2 = new Intent();
                    intent2.setClass(MainActivity.this,FoodSafety.class);
                    startActivity(intent2);
                    finish();
                    return true;
                case R.id.AccountPage:
                    Intent intent3 = new Intent();
                    intent3.setClass(MainActivity.this,Account.class);
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
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics=FirebaseAnalytics.getInstance(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        txtWelcome=(TextView)findViewById(R.id.txtWelcome);
        activity_spin=(Spinner)findViewById(R.id.activity_spin);
        activity_btn=(Button)findViewById(R.id.activity_btn);
        txtShow=(TextView)findViewById(R.id.txtShow);
        time_spin=(Spinner)findViewById(R.id.time_spin);
        img=(ImageView)findViewById(R.id.img);

        //下置選單功能
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //spinner功能
        ArrayAdapter<CharSequence> acadapter=ArrayAdapter.createFromResource(this,R.array.activity,android.R.layout.simple_spinner_item);
        acadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activity_spin.setAdapter(acadapter);
        activity_spin.setOnItemSelectedListener(acspinListener);

        ArrayAdapter<CharSequence> timeadapter=ArrayAdapter.createFromResource(this,R.array.time,android.R.layout.simple_spinner_item);
        timeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time_spin.setAdapter(timeadapter);
        time_spin.setOnItemSelectedListener(tmspinListener);

        activity_btn.setOnClickListener(btnListener);
        getGrid();
        setContentView(R.layout.activity_main);
        //換頁功能
        myViewPager = (ViewPager) findViewById(R.id.pager);

        final LayoutInflater mInflater = getLayoutInflater().from(this);
        View v1 = mInflater.inflate(R.layout.activity_record, null);
        pagerList = new ArrayList<View>();
        pagerList.add(v1);

        myViewPager.setAdapter(new myViewPagerAdapter(pagerList));
        myViewPager.setCurrentItem(0);

    }

    public void getGrid(){
        List<Map<String, Object>> items = new ArrayList<>();
        for (int s=0;s<4;s++){
            Map<String,Object> it=new HashMap<>();
            it.put("text1",texttitle[s]);
            it.put("text2",textcounts[s]);
            items.add(it);
        }
        SimpleAdapter sadpter = new SimpleAdapter(this,items,R.layout.grid_item,new String[]{"text1","text2"},new int[]{R.id.text1,R.id.text2});
        grid=(GridView)findViewById(R.id.grid);
        grid.setNumColumns(2);
        grid.setAdapter(sadpter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent it=new Intent();
        it.setClass(MainActivity.this,Network_Fitbit.class);
        startActivityForResult(it,GET_PIN_REQUEST);
    }

    private AdapterView.OnItemSelectedListener acspinListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            activities=parent.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener tmspinListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getSelectedItem().toString()){
                case "10分鐘":
                    time=1;
                    break;
                case "20分鐘":
                    time=2;
                    break;
                case "30分鐘":
                    time=3;
                    break;
                case "40分鐘":
                    time=4;
                    break;
                case "50分鐘":
                    time=5;
                    break;
                case "1小時":
                    time=6;
                    break;
                case "2小時":
                    time=12;
                    break;
                case "3小時":
                    time=18;
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private Button.OnClickListener btnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            //以下資料根據國民健康署網站：各類運動消耗熱量表運動30分鐘消耗的熱量(大卡)(部分資料)
            if(activities != "" && time != 0){
                double [] CalPerMin;
                if(Integer.valueOf(Weight)<=40)
                {
                    CalPerMin = new double[]{23.33,36.67,54.67,84.67,56.0,42.0,31.33,28.0,34.0,44.0,42.0,34.0}; //以40KG每10分鐘消耗熱量計算
                }
                else if(Integer.valueOf(Weight)<=50)
                {
                    CalPerMin = new double[]{29.17,48.3,68.33,105.77,70.0,52.5,39.17,35.0,42.5,55.0,52.5,30.0}; //以50KG每10分鐘消耗熱量計算
                }
                else if(Integer.valueOf(Weight)<=60)
                {
                    CalPerMin = new double[]{35.0,55.0,88.0,127.0,84.0,63.0,47.0,43.0,51.0,66.0,36.0,32.0}; //以60KG每10分鐘消耗熱量計算
                }
                else
                {
                    CalPerMin = new double[]{40.67,64.17,95.67,148.17,98.0,96.83,54.83,49.0,59.60,98.33,73.50,42.0}; //以70KG每10分鐘消耗熱量計算
                }
                switch (activities){
                    case "慢走":
                        activity_Cal = time*CalPerMin[0];
                        break;
                    case "快走":
                        activity_Cal = time*CalPerMin[1];
                        break;
                    case "慢跑":
                        activity_Cal = time*CalPerMin[2];
                        break;
                    case "快跑":
                        activity_Cal = time*CalPerMin[3];
                        break;
                    case "腳踏車":
                        activity_Cal = time*CalPerMin[4];
                        break;
                    case "籃球":
                        activity_Cal = time*CalPerMin[5];
                        break;
                    case "棒壘球":
                        activity_Cal = time*CalPerMin[6];
                        break;
                    case "乒乓球":
                        activity_Cal = time*CalPerMin[7];
                        break;
                    case "羽毛球":
                        activity_Cal = time*CalPerMin[8];
                        break;
                    case "網球":
                        activity_Cal = time*CalPerMin[9];
                        break;
                    case "游泳":
                        activity_Cal = time*CalPerMin[10];
                        break;
                    case "排球":
                        activity_Cal = time*CalPerMin[11];
                        break;
                }
                putArray();
                txtShow.setText(activities+"："+time*10+"分鐘，消耗熱量："+activity_Cal);
                double cal = Double.valueOf(Calories) + activity_Cal; //資料型態要注意
                Calories=String.valueOf(cal);
                textcounts[1]=Calories;
                getGrid();
            }


        }
    };

    public void putArray(){
        activity_time++;
        String ACDatetime;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat acdateformat = new SimpleDateFormat("yyyy-MM-dd 'at' hh:mm aaa");
        ACDatetime = acdateformat.format(c.getTime());
        Log.d(TAG, "Datetime = " + ACDatetime);

        activitiesArray[activity_time] = activities;
        timeArray[activity_time] = String.valueOf(time*10);
        dateArray[activity_time] = ACDatetime;
        calArray[activity_time] = String.valueOf(activity_Cal);

    }

    public void btnRetrieveData() {
        String Datetime;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        Datetime = dateformat.format(c.getTime());
        Log.d(TAG, "DatetimeDatetimeDatetime = " + Datetime);

        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.addHeader("Authorization", "Bearer "+access_token);

        //拿取使用者資料
        client.get(MainActivity.this, "https://api.fitbit.com/1/user/"+user_id+"/profile.json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.d(TAG, "4devon check responseBody = " + response);
                try {
                    JSONObject jsob = new JSONObject(response);
                    JSONObject user = jsob.getJSONObject("user");
                    String name = user.getString("displayName");
                    txtWelcome.setText("您好!，" + name);
                } catch (Exception e) {

                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                txtWelcome.setText("與Fitbit連線發生問題");
            }
        });

        //拿取當日步數
        client.get(MainActivity.this, "https://api.fitbit.com/1/user/"+user_id+"/activities/steps/date/"+Datetime+"/1d"+".json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.d(TAG, "getSteps = " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("activities-steps");
                    JSONObject activities = jsonArray.getJSONObject(0);
                    Steps = activities.getString("value");
                    textcounts[0]=Steps;
                } catch (Exception e) {

                }
                Steps = textcounts[0]; //避免丟資料給歷史紀錄出現bug多寫這行
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                txtWelcome.setText("與Fitbit連線發生問題");
            }
        });

        //拿取當日消耗熱量
        client.get(MainActivity.this, "https://api.fitbit.com/1/user/"+user_id+"/activities/calories/date/"+Datetime+"/1d"+".json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.d(TAG, "getCalories = " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("activities-calories");
                    JSONObject activities = jsonArray.getJSONObject(0);
                    Calories = activities.getString("value");
                    textcounts[1]=Calories;
                } catch (Exception e) {

                }
                Calories = textcounts[1]; //避免丟資料給歷史紀錄出現bug多寫這行

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                txtWelcome.setText("與Fitbit連線發生問題");
            }
        });

        //拿取使用者體重
        client.get(MainActivity.this, "https://api.fitbit.com/1/user/"+user_id+"/body/log/weight/date/2018-12-12/2018-12-30.json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.d(TAG, "getWeight = " + response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("weight");
                    JSONObject activities = jsonArray.getJSONObject(0);
                    Weight = activities.getString("weight");
                    Log.d(TAG, "Weight = " + Weight);
                } catch (Exception e) {

                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                txtWelcome.setText("與Fitbit連線發生問題");
            }
        });

        //拿取目標一天步數
        client.get(MainActivity.this, "https://api.fitbit.com/1/user/"+user_id+"/activities/goals/daily"+".json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.d(TAG, "goalsWalk = " + response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject goals = jsonObject.getJSONObject("goals");
                    StepsGoal = goals.getString("steps");
                    textcounts[2]=StepsGoal;
                } catch (Exception e) {

                }
                StepsGoal = textcounts[2]; //避免丟資料給歷史紀錄出現bug多寫這行

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                txtWelcome.setText("與Fitbit連線發生問題");
            }
        });

        //拿取目標一天消耗熱量
        client.get(MainActivity.this, "https://api.fitbit.com/1/user/"+user_id+"/activities/goals/daily"+".json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.d(TAG, "goalsCal = " + response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject goals = jsonObject.getJSONObject("goals");
                    CaloriesGoal = goals.getString("caloriesOut");
                    textcounts[3]=CaloriesGoal;
                } catch (Exception e) {

                }
                CaloriesGoal = textcounts[3]; //避免丟資料給歷史紀錄出現bug多寫這行

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                txtWelcome.setText("與Fitbit連線發生問題");
            }
        });
        getGrid();
    }



    //從AuthenticationActivity取得回來的fitbit憑證
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_PIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                String access_token = extras.getString("ACCESS_TOKEN");
                String user_id = extras.getString("USER_ID");

                MainActivity.access_token = access_token;
                MainActivity.user_id = user_id;

                Log.d(TAG, "access_token = " + access_token);
                //呼叫方法拿使用者資料
                btnRetrieveData();
            }
        }
    }

    //頁面滑動功能
    public class myViewPagerAdapter extends PagerAdapter {
        private ArrayList<View> mListViews;
        public myViewPagerAdapter(ArrayList<View> mListViews){
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object){
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position){
            View view = mListViews.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}