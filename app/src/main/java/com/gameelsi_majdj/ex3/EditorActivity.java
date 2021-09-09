package com.gameelsi_majdj.ex3;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {
    private Button dateBtn, timeBtn, addBtn;
    private TextView titleView;
    private EditText titleEdt, descEdt, dateEdt, timeEdt;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private SQLiteDatabase toDoDb = null;
    private String username, query, todoTitle, todoDescription, todoDateTime;
    private int todoId;
    // this booleans i use to indicate if it add or update a todoo
    private boolean actionType;
    long dateInMS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        this.setTitle("Todo Editor");

        // if an item from the listview was clicked those values wouldnt be empty
        todoId = getIntent().getIntExtra("todoId", 0);
        todoTitle = getIntent().getStringExtra("todoTitle");
        todoDescription = getIntent().getStringExtra("todoDescription");
        todoDateTime = getIntent().getStringExtra("todoDateTime");
        // definitions
        dateBtn = findViewById(R.id.datePicker);
        timeBtn = findViewById(R.id.timePicker);
        addBtn = findViewById(R.id.addBtn);
        titleView = findViewById(R.id.mainTitle);
        titleEdt = findViewById(R.id.editTitle);
        descEdt = findViewById(R.id.editDescription);
        dateEdt = findViewById(R.id.editDate);
        timeEdt = findViewById(R.id.editTime);
        sp = getSharedPreferences("isConnected", Context.MODE_PRIVATE);
        editor = sp.edit();
        addBtn.setOnClickListener(this);
        timeBtn.setOnClickListener(this);
        dateBtn.setOnClickListener(this);
        //get current user
        username = sp.getString("username", "");
        toDoDb = openOrCreateDatabase("TodosDB.db", MODE_PRIVATE, null);
        // if one of the todos fields were null then we know we're adding new todoo
        if (todoTitle == null) {
            titleView.setText("ADD new Todo");
            //we're adding not updating
            actionType = true;
        }
        // we're updating a todoo
        else {
            // split time and date to render them
            String[] dateAndTime= todoDateTime.split(" ");
            titleView.setText("UPDATE Todo (id=" + todoId + ")");
            addBtn.setText("UPDATE");
            titleEdt.setText(todoTitle);
            descEdt.setText(todoDescription);
            timeEdt.setText(dateAndTime[1]);
            dateEdt.setText(dateAndTime[0]);
            // we're updating not adding
            actionType = false;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addBtn:
                if (actionType)
                    addToDb();
                else
                    updateDb();
                break;
            case R.id.timePicker:
                pickTime();
                break;
            case R.id.datePicker:
                pickDate();
                break;
        }
    }

    private void pickTime() {
        // create new instance
        final Calendar myCalender = Calendar.getInstance();
        int hh = myCalender.get(Calendar.HOUR_OF_DAY);
        int mm = myCalender.get(Calendar.MINUTE);
//         create time picker dialog
        TimePickerDialog timeDialog= new TimePickerDialog(EditorActivity.this, android.R.style.Theme_DeviceDefault_Dialog, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //build and insert leading 0 if values are less than 10 (1 digit)
                String timeFormat="";
                if(hourOfDay<10)
                    timeFormat+="0"+hourOfDay;
                else
                    timeFormat+=hourOfDay;
                timeFormat+=":";
                if(minute<10)
                    timeFormat+="0"+minute;
                else
                    timeFormat+=minute;
                timeEdt.setText(timeFormat);
            }
        },hh,mm,true);
        timeDialog.show();
    }

    private void pickDate() {
        // Create new instance
        final Calendar myCalender=Calendar.getInstance();
        int cday=myCalender.get(Calendar.DAY_OF_MONTH);
        int cmonth=myCalender.get(Calendar.MONTH);
        int cyear=myCalender.get(Calendar.YEAR);
        // Create date picker dialog
        DatePickerDialog calenderDialog=new DatePickerDialog(EditorActivity.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // month starts from 0 so we add 1 (ref. android documents )
                month++;
                String dateFormat="";
                // build the format and insert leading 0 if month or day were less than 10
                if(dayOfMonth<10)
                    dateFormat+="0"+dayOfMonth;
                else
                    dateFormat+=dayOfMonth;
                dateFormat+="/";
                if(month<10)
                    dateFormat+="0"+month;
                else
                    dateFormat+=month;
                dateFormat+="/"+year;
                dateEdt.setText(dateFormat);
            }
            // Set current date as default
        },cyear,cmonth,cday);
        calenderDialog.show();
    }

    private void updateDb() {
        query = checkEmptyAndReturnQuery();
        // fields aren't empty
        if (query != null) {
            try {
                toDoDb.execSQL(query);
                setAlarm(dateInMS,todoTitle,todoId);
                Toast.makeText(EditorActivity.this, "Todo was UPDATED ", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("myLog", "error in query");
            }
        }
    }

    private void addToDb() {
        query = checkEmptyAndReturnQuery();
        if (query != null) {
            try {
                toDoDb.execSQL(query);
                // get the id of the last id generated in db(id of the todoo we just added )
                Cursor c=toDoDb.rawQuery(" SELECT last_insert_rowid()",null);
                if(c.moveToFirst()) todoId=c.getInt(0);
                c.close();
                // Clear fields for next ToDo
                clearFields();
                setAlarm(dateInMS,todoTitle,todoId);
                Toast.makeText(this, "Todo was ADDED", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("myLog", "error in query");
            }
        }
    }

    @Override
    protected void onDestroy() {
        // we're done using it
        toDoDb.close();
        super.onDestroy();
    }

    private void clearFields() {
        titleEdt.getText().clear();
        descEdt.getText().clear();
        dateEdt.getText().clear();
        timeEdt.getText().clear();
    }

    private String checkEmptyAndReturnQuery() {
        String title = titleEdt.getText().toString();
        String description = descEdt.getText().toString();
        String date = dateEdt.getText().toString();
        String time = timeEdt.getText().toString();
        // check if fields are empty
        if (title.trim().length() == 0) {
            Toast.makeText(this, "Please make sure to fill the title field !", Toast.LENGTH_SHORT).show();
            return null;
        } else if (description.trim().length() == 0) {
            Toast.makeText(this, "Please make sure to fill the description !", Toast.LENGTH_SHORT).show();
            return null;
        } else if (time.trim().length() == 0) {
            Toast.makeText(this, "Please make sure to fill the time field !", Toast.LENGTH_SHORT).show();
            return null;
        } else if (date.trim().length() == 0) {
            Toast.makeText(this, "Please make sure to fill the date !", Toast.LENGTH_SHORT).show();
            return null;
         // check if the date inserted is valid - similar to the defined regular expression dd/mm/yyyy
        }else if(!date.matches("\\d{2}/\\d{2}/\\d{4}")){
            Toast.makeText(this, "Please insert a valid date( dd/mm/yyyy )", Toast.LENGTH_SHORT).show();
            return null;
        // check if the time inserted is valid - similar to the defined regular expression hh:mm
        }else if(!time.matches("\\d{2}:\\d{2}")){
            Toast.makeText(this, "Please insert a valid time( hh:mm )", Toast.LENGTH_SHORT).show();
            return null;
        }else {
            // unify date and time to a single string, convert to date then convert to long
            Calendar myCalendar = Calendar.getInstance();
            // accepted format
            SimpleDateFormat convert=new SimpleDateFormat("dd/MM/yyyy HH:mm");
            // if wrong date was inserted display an error later
            convert.setLenient(false);
            String unified = date+" "+time;
            Date fullDate=null;
            try {
                fullDate=convert.parse(unified);
                myCalendar.setTime(fullDate);
                dateInMS=myCalendar.getTimeInMillis();
            }
            catch (Exception e){
                Toast.makeText((this), "Please insert a valid date&time", Toast.LENGTH_SHORT).show();
                return null;
            }

            if (actionType) {
                todoTitle=title;
                return "INSERT INTO todos(_id,username,title,description,datetime) VALUES(null, '" + username + "', '" + title + "', '" + description + "', '" + dateInMS + "');";
            }
            else {
                return "UPDATE todos SET title= '" + title + "', description= '" + description + "', datetime= '" + dateInMS + "' " + "WHERE _id='" + todoId + "'";
            }

        }
    }
    private void setAlarm(long trigger,String title,int alarmId){
        // get current time
        long currDate =Calendar.getInstance().getTimeInMillis();
        // check if trigger time is in present , if it is then set alarm else don't
        if(trigger>currDate) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            // Call BroadcastReceiver
            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            alarmIntent.putExtra("user", username);
            alarmIntent.putExtra("title", title);
            // Create pending intent
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, alarmId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // compatibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, alarmPendingIntent);
        }
    }




}