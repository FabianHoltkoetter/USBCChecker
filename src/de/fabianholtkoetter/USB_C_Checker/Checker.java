package de.fabianholtkoetter.USB_C_Checker;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Checker extends Activity {

    public static final String MODEL_5X = "nexus 5x";
    public static final String BASE_PATH_5X = "/sys/bus/i2c/drivers/fusb301/";
    public static final String FILENAME_5X = "/fclientcur";
    public static final String MODEL_6P = "nexus 6p";
    public static final String PATH_6P = "/sys/class/typec/typec_device/current_detect";

    private String path;
    private TextView errorLabel;
    private TextView resultlabel;
    private TextView explanationLabel;
    private Button btn;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        errorLabel = (TextView) findViewById(R.id.erorrLabel);
        resultlabel = (TextView) findViewById(R.id.resultLabel);
        explanationLabel = (TextView) findViewById(R.id.explanationLabel);
        btn = (Button) findViewById(R.id.startButton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readFile();
            }
        });

        this.setTitle(this.getTitle() + " (" + Build.MODEL + ")");

        if (Build.MODEL.toLowerCase().equals(MODEL_5X)) {
            File file = new File(BASE_PATH_5X);
            String[] directories = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });

            if (directories.length == 1)
                path = BASE_PATH_5X + directories[0] + FILENAME_5X;
            else if (directories.length > 1) {
                for (String dir : directories) {
                    File f = new File(BASE_PATH_5X + dir + FILENAME_5X);
                    if (path == null && f.exists() && !f.isDirectory()) {
                        path = f.getPath();
                    }
                }
            }

            if (path == null) {
                throw new AssertionError("Could not locate file");
            }
            errorLabel.setVisibility(View.GONE);
            explanationLabel.setText(R.string.explanation_5x);
        } else if (Build.MODEL.toLowerCase().equals(MODEL_6P)) {
            path = PATH_6P;
            errorLabel.setVisibility(View.GONE);
            explanationLabel.setText(R.string.explanation_6p);
        } else {
            errorLabel.setText(R.string.texticon_error);
            resultlabel.setText(Build.MODEL + " is currently not supported.");
            btn.setVisibility(View.GONE);
            errorLabel.setVisibility(View.VISIBLE);
        }
    }

    public boolean readFile() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            resultlabel.setText(bufferedReader.readLine());
            errorLabel.setVisibility(View.GONE);
            return true;
        } catch (IOException e) {
            errorLabel.setText(R.string.texticon_bad);
            errorLabel.setVisibility(View.VISIBLE);
            resultlabel.setText(e.getMessage());
            Log.e("readFile", e.toString());
        }
        return false;
    }
}
