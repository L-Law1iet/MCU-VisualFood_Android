package com.imvision.mcu.visualfood_blockchain;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class Food extends AppCompatActivity {
    private Button btnAddFood;
    static final int GET_PIN_REQUEST = 102;  // The request code
    public Long hash1,hash2;
    private TextView txtBreakfast,txtLunch,txtDinner,txtOther;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private SignInButton signInButton;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private String EatFood,EatTIme,EatDate,EatCal;
    private int addTimes;
    String userId;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        //下置選單功能
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.HomePage:
                    Intent intent = new Intent();
                    intent.setClass(Food.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.FoodPage:
                    Intent intent1 = new Intent();
                    intent1.setClass(Food.this,Food.class);
                    startActivity(intent1);
                    finish();
                    return true;
                case R.id.FoodSafetyPage:
                    Intent intent2 = new Intent();
                    intent2.setClass(Food.this,FoodSafety.class);
                    startActivity(intent2);
                    finish();
                    return true;
                case R.id.AccountPage:
                    Intent intent3 = new Intent();
                    intent3.setClass(Food.this,Account.class);
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
        setContentView(R.layout.activity_food);

        //下置選單功能
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        signInButton = (SignInButton)findViewById(R.id.signInButton);
        btnAddFood = (Button)findViewById(R.id.btnAddFood);
        ImageView img=(ImageView)findViewById(R.id.img);
        btnAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it=new Intent();
                it.setClass(Food.this,Vision.class);
                startActivityForResult(it,GET_PIN_REQUEST);
            }
        });

        mAuth=FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(Food.this,"Google 連線異常",Toast.LENGTH_LONG).show();
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

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        txtBreakfast=(TextView)findViewById(R.id.txtBreakfast);
        txtDinner=(TextView)findViewById(R.id.txtDinner);
        txtLunch=(TextView)findViewById(R.id.txtLunch);
        txtOther=(TextView)findViewById(R.id.txtOther);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_PIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                try {
                    EatFood = extras.getString("EatFood");
                    EatTIme = extras.getString("EatTime");
                    EatDate = extras.getString("EatDate");
                    EatCal = extras.getString("EatCal");
                }catch (Exception e){

                }


                switch (EatTIme){
                    case "早餐":
                        txtBreakfast.setText("早餐："+EatFood+"，熱量："+EatCal);
                        break;
                    case "午餐":
                        txtLunch.setText("午餐："+EatFood+"，熱量："+EatCal);
                        break;
                    case "晚餐":
                        txtDinner.setText("晚餐："+EatFood+"，熱量："+EatCal);
                        break;
                    case "其他":
                        txtOther.setText("其他："+EatFood+"，熱量："+EatCal);
                        break;
                }
            }
        }
        else if(requestCode == RC_SIGN_IN){
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
                            Toast.makeText(Food.this,"Failed",Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(Food.this,"SignIn name:"+account.getDisplayName(),Toast.LENGTH_LONG).show();
                            String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();






                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(uid);
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    addTimes=Integer.valueOf(dataSnapshot.child("食物歷程").child("資料數量").getValue().toString());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            ContactsInfo(uid);
                        }
                    }
                });
    }

    private void ContactsInfo(String uid){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        String id=uid;

        try {
            databaseReference.child(id).child("食物歷程").child(String.valueOf(addTimes)).child("食物").setValue(EatFood);
            databaseReference.child(id).child("食物歷程").child(String.valueOf(addTimes)).child("用餐時段").setValue(EatTIme);
            databaseReference.child(id).child("食物歷程").child(String.valueOf(addTimes)).child("用餐時間").setValue(EatDate);
            databaseReference.child(id).child("食物歷程").child(String.valueOf(addTimes)).child("攝取熱量").setValue(EatCal);
            addTimes++;
            Map<String,Object> child=new HashMap<>();
            child.put("/"+id+"/食物歷程/資料數量/",addTimes);
            databaseReference.updateChildren(child);
        }catch (Exception e){

        }

    }

}