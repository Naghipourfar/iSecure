package ir.naghi.isecure;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button deviceListActivity = (Button) findViewById(R.id.devicesListButton);
        deviceListActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("ir.naghi.ledhandler.DevicesListActivity"));
            }
        });
        Button LEDControllerButton = (Button) findViewById(R.id.LEDControllerButton);
        LEDControllerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("ir.naghi.ledhandler.LEDControllerActivity"));
            }
        });
    }

}
