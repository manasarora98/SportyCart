package com.project.sportycart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.project.sportycart.adapter.HomeAdapter;
import com.project.sportycart.entity.Product;
import com.project.sportycart.retrofit.GetProductsService;
import com.project.sportycart.retrofit.RetrofitClientInstance;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, HomeAdapter.ProductCommunication {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Button cartToolbarButton;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter homeAdapter;
    private SearchView searchView;
    private TextView username;
    private TextView useremail;
    private SharedPreferences sharedPreferences;
    private HomeAdapter.ProductCommunication productCommunication;
    // this is manas branch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         toolbar=findViewById(R.id.main_toolbar);
         setSupportActionBar(toolbar);
         sharedPreferences=getSharedPreferences("LoginData",MODE_PRIVATE);
         String UserId=sharedPreferences.getString("UserId","");

        Intent loginIntent = getIntent();
        String cartValue=loginIntent.getStringExtra("GuestUserId");
        if(cartValue!=""){

            GetProductsService getProductsService=RetrofitClientInstance.getRetrofitInstance().create(GetProductsService.class);
            Call<Boolean>call=getProductsService.updateUserLogin(cartValue,UserId);
            call.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        Toast.makeText(getApplicationContext(),"SuccessRedirect",Toast.LENGTH_SHORT).show();
                        System.out.println("OnResponse updateUserLogin/GuestUserLogin");
                        Intent cartRedirect=new Intent(MainActivity.this,ViewCartActivity.class);
                        startActivity(cartRedirect);
                }
                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    System.out.println("OnFailure updateUserLogin/GuestUserLogin"+t.getMessage());
                }
            });
        }

         drawerLayout=findViewById(R.id.drawer_layout);
         navigationView=findViewById(R.id.nav_view);
         sharedPreferences=getSharedPreferences("LoginData",MODE_PRIVATE);
         String userSP=sharedPreferences.getString("User","");
         String emailSP=sharedPreferences.getString("Email","");
         View headerView = navigationView.getHeaderView(0);
         String userId=sharedPreferences.getString("UserId","");
         System.out.println(userId+"MAIN ACTIVITY GUEST USERID");
         username = (TextView) headerView.findViewById(R.id.userName);
         username.setText(userSP);
         useremail=(TextView)headerView.findViewById(R.id.userEmail);
         useremail.setText(emailSP);


        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.openNavDrawer,
                R.string.closeNavDrawer);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        getSupportActionBar().setTitle("SportyCart");
        toolbar.setSubtitle("Making you sporty!");
        toolbar.setLogo(R.drawable.ic_directions_run_black_24dp);

        cartToolbarButton = (Button)findViewById(R.id.carttoolbarbtn);
        cartToolbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent=new Intent(getApplicationContext(), ViewCartActivity.class);
                startActivity(cartIntent);

            }
        });

        searchView=findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getApplicationContext(),query.toString(),Toast.LENGTH_SHORT).show();
                Intent searchIntent=new Intent(MainActivity.this,SearchResults.class);
                searchIntent.putExtra("searchKey",query);
                startActivity(searchIntent);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        GetProductsService getProductsService = RetrofitClientInstance.getRetrofitInstance().create(GetProductsService.class);
        Call<List<Product>> call= getProductsService.getAllProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                generateDataList(response.body());
                System.out.println("OnResponse getAllProducts");
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(MainActivity.this,t.getMessage(),Toast.LENGTH_LONG).show();
                System.out.println("OnFailure getAllProducts"+t.getMessage());
            }
        });
    }

    private void generateDataList(List<Product> list){
        recyclerView=findViewById(R.id.my_recycler_view);
        if(list!=null) {
            for (Product product : list) {
                ProductCollection.add(product);
            }
        }
        homeAdapter=new HomeAdapter(list,MainActivity.this);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(homeAdapter);
    }

    @Override
    public void onClick(Product product) {
        Intent productIntent=new Intent( MainActivity.this, ProductDetails.class);
        productIntent.putExtra("Image", product.getImageUrl());
        productIntent.putExtra("ProductName",product.getName());
        productIntent.putExtra(("ProductDescription"),product.getDescription());
        productIntent.putExtra(("ColorAttribute"),product.getProductAttributes().getColor());
        productIntent.putExtra(("SizeAttribute"),product.getProductAttributes().getSize());
        productIntent.putExtra(("MaterialAttribute"),product.getProductAttributes().getMaterial());
        productIntent.putExtra(("PID"),product.getProductId());
        startActivity(productIntent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        Toast.makeText(this, "this menu item clicked", Toast.LENGTH_SHORT).show();
        switch(item.getItemId()) {
            case R.id.cricket_nav_menu:
                Intent catIntent1 = new Intent(this, CategoryProducts.class);
                catIntent1.putExtra("categoryId",2);
                this.startActivity(catIntent1);
                break;
            case R.id.fitness_nav_menu:
                Intent catIntent2= new Intent(this,CategoryProducts.class);
                catIntent2.putExtra("categoryId",3);
                this.startActivity(catIntent2);
                break;
            case R.id.badminton_nav_menu:
                Intent catIntent3= new Intent(this,CategoryProducts.class);
                catIntent3.putExtra("categoryId",4);
                this.startActivity(catIntent3);
                break;
            case R.id.tennis_nav_menu:
                Intent catIntent4= new Intent(this,CategoryProducts.class);
                catIntent4.putExtra("categoryId",5);
                this.startActivity(catIntent4);
                break;
            case R.id.merchandise_nav_menu:
                Intent catIntent5= new Intent(this,CategoryProducts.class);
                catIntent5.putExtra("categoryId",1);
                this.startActivity(catIntent5);
                break;
            case R.id.logout:
                sharedPreferences=getSharedPreferences("LoginData",MODE_PRIVATE);
                String logincheckLogout=sharedPreferences.getString("LoginCheck","false");
                if(logincheckLogout.equals("true")) {
                    SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                    Intent logoutIntent = new Intent(MainActivity.this, Login.class);
                    startActivity(logoutIntent);
                }
                else{
                    Toast.makeText(getApplicationContext(),"LoginFirst",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.details_nav_menu:
                sharedPreferences=getSharedPreferences("LoginData",MODE_PRIVATE);
                String logincheck=sharedPreferences.getString("LoginCheck","false");
                if(logincheck.equals("true")) {
                    Intent userDetails = new Intent(MainActivity.this, UserDetails.class);
                    startActivity(userDetails);
                }
                else {
                    Toast.makeText(getApplicationContext(),"LoginFirst",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.order_nav_menu:
                sharedPreferences=getSharedPreferences("LoginData",MODE_PRIVATE);
                String loginCheck=sharedPreferences.getString("LoginCheck","false");
                if(loginCheck.equals("true")) {
                    Intent orderHistoryIntent = new Intent(MainActivity.this, OrderLog.class);
                    SharedPreferences sharedPreferences = getSharedPreferences("LoginData", MODE_PRIVATE);
                    String userId = sharedPreferences.getString("UserId", "");
                    orderHistoryIntent.putExtra("UserId", userId);
                    startActivity(orderHistoryIntent);
                }
                else{
                    Toast.makeText(getApplicationContext(),"LoginFirst",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}