package com.gameelsi_majdj.ex3;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.widget.TextView;

public class ToDoViewAdapter extends ArrayAdapter<ToDoView> {
    public ToDoViewAdapter(Activity context, ArrayList<ToDoView> todoData) {
        super(context, 0, todoData);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        if(convertView == null) {
            convertView = View.inflate(getContext(), R.layout.list_item, null);
        }
        // Get the {@link todoview} object located at this position in the list
        ToDoView currentView = getItem(position);
        // Find the TextView in the list_item.xml
        TextView title = convertView.findViewById(R.id.titleView);
        title.setText(currentView.getTitle());

        TextView description = convertView.findViewById(R.id.descriptionView);
        description.setText(currentView.getDescription());

        TextView date = convertView.findViewById(R.id.dateTimeView);
        date.setText(currentView.getDateTime());

        // Return the whole list item layout (containing 4 TextViews) so that it can be shown in the ListView
        return convertView;
    }




}


