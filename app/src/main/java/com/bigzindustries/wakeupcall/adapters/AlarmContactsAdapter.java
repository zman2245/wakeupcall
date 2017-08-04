package com.bigzindustries.wakeupcall.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bigzindustries.wakeupcall.R;

public class AlarmContactsAdapter extends CursorAdapter {

    private AlarmContactsDbDelegate delegate;

    public AlarmContactsAdapter(Context context, Cursor cursor, AlarmContactsDbDelegate delegate) {
        super(context, cursor, 0);

        this.delegate = delegate;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.alarm_contact, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView name = view.findViewById(R.id.name);
        final TextView number = view.findViewById(R.id.number);
        Button remove = view.findViewById(R.id.remove_button);

        // Populate fields with extracted cursor properties
        name.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        number.setText(cursor.getString(cursor.getColumnIndexOrThrow("number")));
        remove.setOnClickListener((removeButton) ->
                delegate.removeAlarmContact(number.getText().toString()));
    }
}
