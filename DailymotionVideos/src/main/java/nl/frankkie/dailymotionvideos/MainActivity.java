package nl.frankkie.dailymotionvideos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        getUsername();
    }

    protected void getUsername(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Give the username of a Dailymotion uploader");
        final EditText editText = new EditText(this);
        editText.setHint("e.g. MysteriousBrony");
        builder.setView(editText);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HttpGet request = new HttpGet("https://api.dailymotion.com/user/" + editText.getText().toString());
                Communication.MyCommunicationResponseListener listener = new Communication.MyCommunicationResponseListener() {
                    @Override
                    public void onResponse(JSONObject json) {
                        try {
                            String screenname = json.getString("screenname");
                            String id = json.getString("id");
                            if (screenname.equals("Deleted user")){
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                                builder1.setTitle("Error");
                                builder1.setMessage("This user does not exists or is deleted");
                                builder1.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        getUsername();
                                    }
                                });
                                builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });
                                builder1.create().show();
                            } else {
                                getPlaylists(id);
                            }
                        } catch (Exception e){
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                            builder1.setTitle("Error");
                            builder1.setMessage("Try again later");
                            builder1.setPositiveButton("OK..", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                            builder1.create().show();
                        }
                    }
                };
                Communication.MyCommunicationAsyncTask task = new Communication.MyCommunicationAsyncTask(MainActivity.this, listener, request);
                task.execute("");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.create().show();
    }

    protected void getPlaylists(String userId) {
        //Get Playlists!
        HttpGet request = new HttpGet("https://api.dailymotion.com/user/" + userId + "/playlists");
        Communication.MyCommunicationResponseListener listener = new Communication.MyCommunicationResponseListener() {
            @Override
            public void onResponse(JSONObject json) {
                fillUI(json);
            }
        };
        Communication.MyCommunicationAsyncTask task = new Communication.MyCommunicationAsyncTask(this, listener, request);
        task.execute("");
    }

    protected void initUI() {
        setTitle(R.string.playlists);
        setContentView(R.layout.activity_main);
        container = (LinearLayout) findViewById(R.id.playlist_container);
        container.removeAllViews();
    }

    protected void fillUI(JSONObject json) {
        if (json == null) {
            Util.showAlertDialog(this, "Communication Error", "Cannot load playlists... try again later.");
            return;
        }
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        try {
            JSONArray list = json.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                final JSONObject jsonObject = list.getJSONObject(i);
                Button itemLayout = new Button(this);
                itemLayout.setText(jsonObject.getString("name"));
                //LinearLayout itemLayout = (LinearLayout) layoutInflater.inflate(R.layout.playlist_item, container, false);
                //TextView itemTv = (TextView) itemLayout.findViewById(R.id.playlist_item_text);
                //itemTv.setText(jsonObject.getString("name"));
                final String playlistId = jsonObject.getString("id");
                itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickedPlaylist(playlistId);
                    }
                });
                container.addView(itemLayout);
                boolean useSeparator = true;
                if (useSeparator) { //not at the bottom
                    LinearLayout separator = (LinearLayout) layoutInflater.inflate(R.layout.item_separator, container, false);
                    container.addView(separator);
                }
            }
            if (list.length() == 0){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("Error");
                builder1.setMessage("This user does not have playlists");
                builder1.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getUsername();
                    }
                });
                builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder1.create().show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Util.showAlertDialog(this, "Communication Error", "Cannot load playlists... try again later.");
            return;
        }
    }

    public void clickedPlaylist(String id) {
        Intent intent = new Intent();
        intent.setClass(this, PlaylistActivity.class);
        intent.putExtra("playlistId", id);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /*
   {
    "page": 1,
    "limit": 10,
    "explicit": false,
    "total": 3,
    "has_more": false,
    "list": [
        {
            "id": "x2emfj",
            "name": "MLP - Season 3",
            "owner": "x19emeo"
        },
        {
            "id": "x2emfe",
            "name": "MLP - Season 2",
            "owner": "x19emeo"
        },
        {
            "id": "x2emfc",
            "name": "MLP - Season 1",
            "owner": "x19emeo"
        }
    ]
}
     */

}
