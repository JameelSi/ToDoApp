package com.gameelsi_majdj.ex3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ToDoListActivity extends AppCompatActivity  implements ListView.OnItemClickListener {
    private SearchView searchField;
    private ToDoViewAdapter adapter;
    private ArrayList<ToDoView> data;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private SQLiteDatabase toDoDb = null;
    private String username;
    private ArrayList<ToDoView> dataTemp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);
        sp=getSharedPreferences("isConnected", Context.MODE_PRIVATE);
        //  would never be null since we cant reach this activity before adding to SP which user is logged in
        username=sp.getString("username","");
        //set title on the action bar
        this.setTitle("Todo List ("+sp.getString("username","")+")");
        searchField=findViewById(R.id.searchView);

        ListView list=findViewById(R.id.listView);
        FloatingActionButton addToDo = findViewById(R.id.addToDoBtn);
        toDoDb = openOrCreateDatabase("TodosDB.db", MODE_PRIVATE, null);
        data = new ArrayList<ToDoView>();
        fillArrayFromDB();
        adapter = new ToDoViewAdapter(this, data);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        //on long click delete item
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg, View arg1, int pos, long id) {
                showDeleteDialog(pos,data.get(pos).getId());
                return true;
            }
        });
        // fill a temp data, this will be used for filtering when searching
        dataTemp=new ArrayList<ToDoView>();
        dataTemp.addAll(data);
        //on click go to editor page
        addToDo.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) { startActivity(new Intent(ToDoListActivity.this, EditorActivity.class)); }});
        // a function that sets a listener to the search field and on every character inserted it returns all todoos that contains it
        reactiveSearch();
    }

    private void reactiveSearch(){
        searchField.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String str) {
                // call filter function that returns todoos  containing str , call filter only if theres at least 1 item
                filter(str);
                return false;
            }
        });
    }

    private void fillArrayFromDB(){
        try {
            //get all todos for the loggedin user
            Cursor cr = toDoDb.rawQuery("SELECT * FROM todos WHERE username=='"+username+"'", null);
            int titleColumn = cr.getColumnIndex("title");
            int descriptionColumn = cr.getColumnIndex("description");
            int datetimeColumn = cr.getColumnIndex("datetime");
            int idInDB=cr.getColumnIndex("_id");
            // Move to the first row of results & Verify that we have results

            if (cr.moveToFirst()) {
                do {
                    // Get the results and store them in a String
                    String title = cr.getString(titleColumn);
                    String desc = cr.getString(descriptionColumn);
                    long datetime = cr.getLong(datetimeColumn);
                    int rowId=cr.getInt(idInDB);
                    // get the datetime and convert it to date then to string
                    SimpleDateFormat convert=new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    Calendar myCalendar = Calendar.getInstance();
                    myCalendar.setTimeInMillis(datetime);
                    Date currDate=myCalendar.getTime();
                    String currDateTime =convert.format(currDate);
                    // push to array
                    data.add(new ToDoView(title,desc,currDateTime,rowId));
                    // Keep getting results as long as they exist
                } while (cr.moveToNext());
            }
           //we're done with the cursor
            cr.close();
        }
        catch (Exception s){
            Log.d("myLog", "error in query");
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        // MenuItem icon on AppBar
        MenuItem logoutMenu = menu.add("LogOut");
        logoutMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        logoutMenu.setIcon(R.drawable.ic_exit);
        logoutMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showExitDialog();
                return true;
            }
        });
        return true;
    }

    private void showExitDialog() {
        // build the exit dialog and customize it
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.ic_exit);
        dialog.setTitle("Exit App");
        dialog.setMessage("Do you really want to LOGOUT ?");
        dialog.setCancelable(false);
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // update the sharedpreference so that the next time user opens the add it would take him to the login page
                editor= sp.edit();
                editor.putBoolean("loggedIn", false);
                editor.putString("username","");
                editor.commit();
                startActivity(new Intent(ToDoListActivity.this, LoginActivity.class));
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

    private void showDeleteDialog(int pos,int id) {
        final Intent intent = new Intent(this, AlarmReceiver.class);

        // build a dialog and customize it
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.ic_delete);
        dialog.setTitle("Delete ToDo");
        dialog.setMessage("Do you really want delete this ToDo ?");
        dialog.setCancelable(false);
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    int deleteId=data.get(pos).getId();
                    // if user hit yes delete the todoo from db and from
                    String query = "DELETE FROM todos WHERE _id=='"+id+"'";
                    toDoDb.execSQL(query);
                    data.remove(pos);
                    adapter.notifyDataSetChanged();
                    // delete pending alarm for this todo
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.cancel(PendingIntent.getBroadcast(ToDoListActivity.this,deleteId, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                    Toast.makeText(ToDoListActivity.this, "Todo was DELETED ", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    Log.d("myLog", "error in query");
                }
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
    // filter and render todos with only those that contain the query
    public void filter(String query) {
        query = query.toLowerCase();
        // clear current array and fill it with suitable data
        data.clear();
        // query is empty
        if (query.length() == 0)
            data.addAll(dataTemp);
        else
            for (ToDoView todo : dataTemp)
                if (todo.getTitle().toLowerCase().contains(query) || todo.getDescription().toLowerCase().contains(query))
                    data.add(todo);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // on item clicked go to editor to update values
        ToDoView todo = data.get(position);
        // pass parameters to editor activity to be able to update
        Intent intent = new Intent(ToDoListActivity.this, EditorActivity.class);
        intent.putExtra("todoId", todo.getId());
        intent.putExtra("todoTitle", todo.getTitle());
        intent.putExtra("todoDateTime", todo.getDateTime());
        intent.putExtra("todoDescription", todo.getDescription());

        startActivity(intent);
    }

    //pull the data from db and push to the array when tis activity resumes so that when the user adds new ToDos and goes back to this activity it becomes visible
    @Override
    protected void onResume() {
        // clear data array then push the new values
        data.clear();
        // pull the data from the database , create new ToDoo object and push to array data
        fillArrayFromDB();
        // fill a temp data, this will be used for filtering when searching
        dataTemp=new ArrayList<ToDoView>();
        dataTemp.addAll(data);
        // notify the adapter that a ToDoo has been added
        adapter.notifyDataSetChanged();
        super.onResume();
    }


}