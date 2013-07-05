package com.koushikdutta.positron.sample;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.koushikdutta.positron.Positron;

public class Sample extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SQLiteDatabase db = openOrCreateDatabase("foo", MODE_PRIVATE, null);
        Positron p = new Positron(this, db);

        p.sync("koush@koushikdutta.com");
    }
}
