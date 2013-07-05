package com.koushikdutta.positron;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by koush on 7/4/13.
 */
public class Positron {
    SQLiteDatabase database;
    Context context;
    String token;
    Drive drive;

    private static final String MIMETYPE_FOLDER = "application/vnd.google-apps.folder";

    File ensureFolder(String folder) throws IOException {
        String[] splits = folder.split("/");

        File parent = null;

        for (String split: splits) {
            String q = String.format("mimeType='%s'", MIMETYPE_FOLDER);
            if (parent != null)
                q = String.format("%s and '%s' in parents", q, parent.getId());

            FileList list = drive.files().list().setQ(q).setFields("items(id, title)").execute();
            boolean found = false;
            for (File file: list.getItems()) {
                if (split.equals(file.getTitle())) {
                    found = true;
                    parent = file;
                    break;
                }
            }
            if (found)
                continue;

            File file = new File();
            file.setTitle(split);
            file.setMimeType(MIMETYPE_FOLDER);
            if (parent != null) {
                ArrayList<ParentReference> parents = new ArrayList<ParentReference>();
                parents.add(new ParentReference().setId(parent.getId()));
                file.setParents(parents);
            }
            parent = drive.files().insert(file).execute();
        }

        return parent;
    }


    public Positron(Context context, SQLiteDatabase database) {
        this.database = database;
        this.context = context;
    }

    public void sync(final String account) {
        final Handler handler = new Handler();

        new Thread() {
            @Override
            public void run() {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, "https://www.googleapis.com/auth/userinfo.profile","https://www.googleapis.com/auth/drive.appdata");
                credential.setSelectedAccountName(account);
                try {
                    token = credential.getToken();
                    drive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                    .setApplicationName("Positron Sample")
                    .build();

                    File f = new File();
                    f.setTitle("testy");
                    f.setParents(Arrays.asList(new ParentReference().setId("appdata")));
                    f.setMimeType("text/plain");
                    ByteArrayInputStream bin = new ByteArrayInputStream("shit".getBytes());
                    InputStreamContent content = new InputStreamContent("text/plain", bin);
                    drive.files().insert(f, content).execute();
                }
                catch (UserRecoverableAuthException e) {
                    Intent authorizationIntent = e.getIntent();
                    if (context instanceof Activity) {
                        Activity activity = (Activity)context;
                        activity.startActivityForResult(authorizationIntent, 5000);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
