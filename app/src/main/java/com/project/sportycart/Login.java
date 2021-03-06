package com.project.sportycart;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.project.sportycart.entity.AccessTokenDTO;
import com.project.sportycart.entity.RegisterUser;
import com.project.sportycart.retrofit.GetProductsService;
import com.project.sportycart.retrofit.RetrofitClientInstance;
import org.json.JSONException;
import java.util.Arrays;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private RegisterUser registerUser = new RegisterUser();
    Boolean facebookLoginCheck = false;
    private Context context;
    private AccessTokenDTO accessTokenDTO;
    private GoogleSignInClient mGoogleSignInClient;
    private LoginButton facebookloginButton;
    private GoogleSignInOptions gso;
    private int RC_SIGN_IN;
    private CallbackManager callbackManager;
    private String cartValue = "";
    String TAG = "logCheck";
    SharedPreferences sp;

    //GOOGLE INIT 0
    {
        RC_SIGN_IN = 9001;
        context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //LOGIN NOW FROM CART
        Intent loginIntent = getIntent();
        String cartEmpty = loginIntent.getStringExtra("CartPerson");
        if (cartEmpty != null) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.login_layout), "Login First to Order", Snackbar.LENGTH_LONG);
            snackbar.show();
            cartValue = loginIntent.getStringExtra("GuestUserId");
            sp = getSharedPreferences("LoginData", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("User", "").apply();
            editor.commit();
        }

        //REGISTER
        Button register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(Login.this, Register.class);
                startActivity(registerIntent);
            }

        });

        //CUSTOM LOGIN
        Button loginButton = findViewById(R.id.login);
        sp = getSharedPreferences("LoginData", MODE_PRIVATE);
        String check = sp.getString("LoginCheck", "false");
        if (check.equals("false")) {
            if (!sp.getBoolean("LogInData", false)) {
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager inputManager = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                        EditText user = findViewById(R.id.username);
                        EditText pass = findViewById(R.id.password);
                        final String user1 = String.valueOf(user.getText());
                        final String pw = String.valueOf(pass.getText());
                        if (user1.length() == 0 || pw.length() == 0) {
                            Toast.makeText(getApplicationContext(), "Enter Login Details", Toast.LENGTH_SHORT).show();
                        } else {
                            registerUser.setEmail(user1);
                            registerUser.setPassword(pw);
                            GetProductsService getProductsService = RetrofitClientInstance.getRetrofitInstance().create(GetProductsService.class);
                            Call<AccessTokenDTO> call = getProductsService.loginUser(registerUser);
                            call.enqueue(new Callback<AccessTokenDTO>() {
                                @Override
                                public void onResponse(Call<AccessTokenDTO> call, Response<AccessTokenDTO> response) {
                                    accessTokenDTO = response.body();
                                    sp = getSharedPreferences("LoginData", MODE_PRIVATE);
                                    if(accessTokenDTO!=null) {
                                        if (accessTokenDTO.getCheck()) {
                                            System.out.println(accessTokenDTO.getCheck() + "CHECK");
                                            System.out.println("LOGIN DONE");
                                            String userId = accessTokenDTO.getUserId();
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString("UserId", userId).apply();
                                            String email = registerUser.getEmail();
                                            editor.putString("Email", email).apply();
                                            editor.putString("LoginCheck", "true").apply();
                                            editor.commit();
                                            Intent loginIntent = new Intent(Login.this, MainActivity.class);
                                            loginIntent.putExtra("GuestUserId", cartValue);
                                            System.out.println("OnFailure CUSTOM LOGIN Success");
                                            startActivity(loginIntent);
                                            finish();
                                        } else {
                                            Snackbar snackbar = Snackbar.make(findViewById(R.id.login_layout), "Invalid Login Details", Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                            System.out.println("OnResponse CUSTOM LOGIN PW MISMATCH" + accessTokenDTO.getCheck());
                                        }
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"No AccessToken Received from Backend!",Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<AccessTokenDTO> call, Throwable t) {
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.login_layout),
                                            t.getMessage(), Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                    System.out.println("OnFailure CUSTOM LOGIN" + t.getMessage());
                                }
                            });
                        }
                    }
                });
            } else {
                Intent SignIntent = new Intent(Login.this, MainActivity.class);
                startActivity(SignIntent);
                finish();
            }
        } else {
            Intent LoggedIn = new Intent(Login.this, MainActivity.class);
            startActivity(LoggedIn);
        }

        //GOOGLE LOGIN 1
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("959388190902-n2h383o5ej00boqakorh0qn4iodcnd95.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.sign_in_button) {
                    signIn();
                }
            }
        });

        //FACEBOOK LOGIN 1
        facebookloginButton = findViewById(R.id.facebook_login_button);
        facebookloginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogIn();
            }
        });

        //SKIP LOGIN
        Button skipSignIn = findViewById(R.id.skip);
        skipSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipSignInIntent = new Intent(Login.this, MainActivity.class);
                int max = Integer.MAX_VALUE;
                int min = 0;
                Random random = new Random();
                int randomNumber = random.nextInt(max - min) + min;
                String guestUserId = String.valueOf(randomNumber);
                System.out.println(guestUserId + "LOGIN GUEST USERID");
                sp = getSharedPreferences("LoginData", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("UserId", guestUserId).apply();
                editor.putString("LoginCheck", "false").apply();
                editor.putString("User", "Guest").apply();
                editor.commit();
                startActivity(skipSignInIntent);
            }
        });
    }

    //FACEBOOK LOGIN PART 2
    private void LogIn() {
        facebookLoginCheck = true;
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(Login.this, Arrays.asList("email"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken accessToken = loginResult.getAccessToken();
                final String[] fb_email = new String[1];
                Bundle bundle = new Bundle();
                bundle.putString("fields", "id,email,name");
                GraphRequest graphRequest = new GraphRequest(loginResult.getAccessToken(), "me", bundle, null, new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        String email = "";
                        try {
                            System.out.println("EMAIL FB" + response.getJSONObject().getString("email"));
                            email = response.getJSONObject().getString("email");
                            System.out.println("NAME FB" + response.getJSONObject().getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String idTokenFB = accessToken.getToken();
                        System.out.println("FACEBOOK ID TOKEN"+idTokenFB);
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "first_name,last_name,email,id");
                        GetProductsService getProductsService = RetrofitClientInstance.getRetrofitInstance().create(GetProductsService.class);
                        Call<AccessTokenDTO> call = getProductsService.sendFacebookLogin(idTokenFB);
                        final String finalEmail = email;
                        call.enqueue(new Callback<AccessTokenDTO>() {
                            @Override
                            public void onResponse(Call<AccessTokenDTO> call, Response<AccessTokenDTO> response) {
                                if(response.body()!=null) {
                                    String userId = response.body().getUserId();
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("UserId", userId).apply();
                                    editor.putString("Email", finalEmail).apply();
                                    editor.putString("LoginCheck", "true").apply();
                                    editor.commit();
                                    System.out.println("OnResponse FB LOGIN Success");
                                    Intent facebookSignInIntent = new Intent(Login.this, MainActivity.class);
                                    startActivity(facebookSignInIntent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(),"AccessToken FB Not Received",Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<AccessTokenDTO> call, Throwable t) {
                                System.out.println("OnFailure FB LOGIN" + t.getMessage());
                            }
                        });
                    }
                });
                graphRequest.executeAsync();
            }
            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(),"CANCELLED FB LOGIN",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(),"Error on Facebook Login",Toast.LENGTH_SHORT);
            }
        });
    }

    //GOOGLE LOGIN PART 2
    private void signIn() {
        facebookLoginCheck = false;
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //GOOGLE LOGIN PART 3 & FACEBOOK PART 3
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (facebookLoginCheck) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        }
    }

    //GOOGLE LOGIN PART 4
    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            final GoogleSignInAccount account = task.getResult(ApiException.class);
            System.out.println(account.getEmail());
            System.out.println(account.getDisplayName());
            sp = getSharedPreferences("LoginData", MODE_PRIVATE);
            String idToken = account.getIdToken();
            System.out.println("GOOGLE ID TOKEN"+idToken);
            GetProductsService getProductsService = RetrofitClientInstance.getRetrofitInstance().create(GetProductsService.class);
            Call<AccessTokenDTO> call = getProductsService.sendGoogleLogin(idToken);
            call.enqueue(new Callback<AccessTokenDTO>() {
                @Override
                public void onResponse(Call<AccessTokenDTO> call, Response<AccessTokenDTO> response) {
                    String userId = response.body().getUserId();
                    System.out.println("USER ID" + userId);
                    final SharedPreferences.Editor editor = sp.edit();
                    editor.putString("User", account.getDisplayName()).apply();
                    editor.putString("Email", account.getEmail()).apply();
                    editor.putString("UserId", userId).apply();
                    editor.putString("LoginCheck", "true").apply();
                    editor.commit();
                    System.out.println("OnResponse Google LOGIN Success");
                    Intent GoogleSignIntent = new Intent(Login.this, MainActivity.class);
                    startActivity(GoogleSignIntent);
                    finish();
                }

                @Override
                public void onFailure(Call<AccessTokenDTO> call, Throwable t) {
                    System.out.println("OnFailure Google LOGIN" + t.getMessage());
                }
            });

        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            System.out.println("OnFailure Google LOGIN" + "signInResult:failed code=" + e.getStatusCode());
        }
    }
}

