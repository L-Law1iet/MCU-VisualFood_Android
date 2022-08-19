package com.imvision.mcu.visualfood_blockchain;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Record extends AppCompatActivity {
    private final String TAG = "";
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private TextView accountLpgin,myWalklog,myCallog,myWalkgoal,myCalgoal;
    public String oWalklog,oCallog,oWalkgoal,oCalgoal;
    public String[]activitiesArray,timeArray,dateArray,calArray;
    private int activity_time,addTimes;
    private ListView list;
    private String uid;
    private SignInButton signInButton;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    //下置選單功能
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.HomePage:
                Intent intent = new Intent();
                intent.setClass(Record.this,MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.FoodPage:
                Intent intent1 = new Intent();
                intent1.setClass(Record.this,Food.class);
                startActivity(intent1);
                finish();
                return true;
            case R.id.FoodSafetyPage:
                Intent intent2 = new Intent();
                intent2.setClass(Record.this,FoodSafety.class);
                startActivity(intent2);
                finish();
                return true;
            case R.id.AccountPage:
                Intent intent3 = new Intent();
                intent3.setClass(Record.this,Account.class);
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
        setContentView(R.layout.activity_record);

        accountLpgin = (TextView)findViewById(R.id.accountLpgin);
        myWalklog = (TextView)findViewById(R.id.myWalk);
        myCallog = (TextView)findViewById(R.id.myCal);
        myWalkgoal = (TextView)findViewById(R.id.myWalkGoal);
        myCalgoal = (TextView)findViewById(R.id.myCalGoal);
        list = (ListView)findViewById(R.id.list);
        ImageView img=(ImageView)findViewById(R.id.img);

        signInButton = (SignInButton)findViewById(R.id.signInButton);


        myWalklog.setText(oWalklog);
        myWalkgoal.setText(oWalkgoal);
        myCallog.setText(oCallog);
        myCalgoal.setText(oCalgoal);

        mAuth=FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(Record.this,"Google 連線異常",Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                //取得使用者並測試登入
                firebaseAuthWithGoogle(account);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(Record.this,"Failed",Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(Record.this,"SignIn name:"+account.getDisplayName(),Toast.LENGTH_LONG).show();
                            accountLpgin.setText("登入成功\n"+account.getEmail()+"\n"+account.getDisplayName());
                            uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                            readData();

                            SetList();
                        }

                    }
                });
    }

    private void readData(){
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference itemsRef = firebaseDatabase.getReference(uid);

        Log.d("listener1","before attaching the listener");
        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                addTimes=Integer.valueOf(dataSnapshot.child("運動歷程").child("資料數量").getValue().toString());
                Log.d("addtimes",String.valueOf(addTimes));
                if(activity_time!=-1){
                    setData(uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,databaseError.getMessage());
            }
        });



    }


    private void setData(String uid){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        String id=uid;

        databaseReference.child(id).child("使用者資料").child("Steps").setValue(oWalklog);
        databaseReference.child(id).child("使用者資料").child("Calories").setValue(oCallog);
        databaseReference.child(id).child("使用者資料").child("StepsGoal").setValue(oWalkgoal);
        databaseReference.child(id).child("使用者資料").child("CaloriesGoal").setValue(oCalgoal);

        try {
            for(int i=0;i<=activity_time;i++){
                databaseReference.child(id).child("運動歷程").child(String.valueOf(addTimes)).child("運動類型").setValue(activitiesArray[i]);
                databaseReference.child(id).child("運動歷程").child(String.valueOf(addTimes)).child("運動時間").setValue(timeArray[i]);
                databaseReference.child(id).child("運動歷程").child(String.valueOf(addTimes)).child("消耗熱量").setValue(calArray[i]);
                databaseReference.child(id).child("運動歷程").child(String.valueOf(addTimes)).child("新增時間").setValue(dateArray[i]);
                addTimes=addTimes+1;
            }
            Log.d("addtimes++",String.valueOf(addTimes));
            //更新資料數量
            Map<String,Object> child=new HashMap<>();
            child.put("/"+id+"/運動歷程/資料數量/",addTimes);
            databaseReference.updateChildren(child);
        } catch (Exception e){

        }


    }

    private void SetList(){
        final ArrayList<String> alist = new ArrayList<>();
        DatabaseReference viewRef = FirebaseDatabase.getInstance().getReference(uid);

        Log.d("listView","I am alive!");
        viewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    for(int a=0;a<addTimes;a++) {

                        String str=(dataSnapshot.child("運動歷程").child(String.valueOf(a)).child("運動類型").getValue().toString() + "\n" +
                                dataSnapshot.child("運動歷程").child(String.valueOf(a)).child("運動時間").getValue().toString() + "分\n" +
                                dataSnapshot.child("運動歷程").child(String.valueOf(a)).child("消耗熱量").getValue().toString() + "大卡\n" +
                                dataSnapshot.child("運動歷程").child(String.valueOf(a)).child("新增時間").getValue().toString());
                        alist.add(str);
                    }
                    Context context=getApplicationContext();
                    list.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,alist));
                }catch (Exception e){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
