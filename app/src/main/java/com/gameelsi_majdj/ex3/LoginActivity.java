package com.gameelsi_majdj.ex3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteDatabase toDoDb = null;
    private Button logIn;
    private EditText userName, password;
    private String query;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setTitle("Todo Login");
        sp=getSharedPreferences("isConnected", Context.MODE_PRIVATE);
//        user already logged in ? go to ToDos activity
        if(sp.getBoolean("loggedIn",false)){
            startActivity(new Intent(LoginActivity.this, ToDoListActivity.class));
        }
        logIn = findViewById(R.id.logIn);
        logIn.setOnClickListener(this);
        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        // Opens a current database or creates it
        try {
            // Pass the database name, designate that only this app can use it
            // and a DatabaseErrorHandler in the case of database corruption
            toDoDb = openOrCreateDatabase("TodosDB.db", MODE_PRIVATE, null);
            // build an SQL statement to create 'Users' table (if not exists)
            query = "CREATE TABLE IF NOT EXISTS users (username VARCHAR primary key, password VARCHAR);";
            toDoDb.execSQL(query);
            query = "CREATE TABLE IF NOT EXISTS todos (_id integer primary key, username VARCHAR, title VARCHAR, description VARCHAR, datetime INTEGER);";
            toDoDb.execSQL(query);
        }
        catch (Exception e) {
            Log.d("myLog", "Error In Creating Database");
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.logIn:
                validateWithDb();
                break;
        }
    }
    private void validateWithDb(){
        // Get the contact name and email entered
        String user = userName.getText().toString();
        String pass = password.getText().toString();
        String passFromDb=null;

        if(user.trim().length() == 0 ){
            Toast.makeText(this, "Error! username is missing", Toast.LENGTH_SHORT).show();
        }
        else if(pass.trim().length()==0){
            Toast.makeText(this, "Error! password is missing", Toast.LENGTH_SHORT).show();
        }
        // Execute SQL statement to insert new data
        else{
            try {
                query = "INSERT INTO users (username, password) VALUES ('" + user + "', '" + pass + "');";
                toDoDb.execSQL(query);
                sp=getSharedPreferences("isConnected", Context.MODE_PRIVATE);
                editor= sp.edit();
                editor.putBoolean("loggedIn", true);
                editor.putString("username",user);
                editor.commit();
                startActivity(new Intent(LoginActivity.this, ToDoListActivity.class));
            }
            catch (Exception e) {
                try {
                    Cursor cr = toDoDb.rawQuery("SELECT password FROM users WHERE username=='"+user+"'", null);
//                we know for sure that only 1 value should have been returned because the user is unique (a key )
                    if (cr.moveToFirst()) {
                        passFromDb = cr.getString(cr.getColumnIndex("password"));
                    }
//                we're done with the cursor
                    cr.close();
                    if (pass.equals(passFromDb)) {
//                        update the share preference so that we save the user state
                        sp=getSharedPreferences("isConnected", Context.MODE_PRIVATE);
                        editor= sp.edit();
                        editor.putBoolean("loggedIn", true);
                        editor.putString("username",user);
                        editor.commit();
                        startActivity(new Intent(LoginActivity.this, ToDoListActivity.class));
                    } else {
                        Toast.makeText(this, "WRONG PASSWORD,Try Again !", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception s){
                    Log.d("myLog", "error in query");

                }
            }

            }
        }

// about dialog
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        MenuItem aboutMenu = menu.add("About");
        MenuItem exitMenu = menu.add("Exit");
        aboutMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                showAboutDialog();
                return true;
            }
        });

        exitMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                showExitDialog();
                return true;
            }
        });
        return true;
    }

    private void showAboutDialog() {

        String aboutApp = getString(R.string.app_name) +" "+
                "(" + getPackageName() +")"+"\n\n" +
                "By Jameel Silwadi & Majd Jaber, 25/5/21.";

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle("About App");
        dialog.setMessage(aboutApp);
        dialog.setCancelable(false);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showExitDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.ic_exit);
        dialog.setTitle("Exit App");
        dialog.setMessage("Do you really want to exit ToDoApp ?");
        dialog.setCancelable(false);
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // close this activity
            }
        });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        toDoDb.close();
        super.onDestroy();
    }


}