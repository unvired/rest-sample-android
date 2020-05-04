package com.unvired.restsample.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.unvired.database.DBException;
import com.unvired.database.IDataStructure;
import com.unvired.model.InfoMessage;
import com.unvired.restsample.R;
import com.unvired.restsample.be.WEATHER_HEADER;
import com.unvired.restsample.util.Constants;
import com.unvired.restsample.util.PAHelper;
import com.unvired.restsample.util.Utils;
import com.unvired.sync.out.ISyncAppCallback;
import com.unvired.sync.response.ISyncResponse;
import com.unvired.sync.response.SyncBEResponse;
import com.unvired.ui.Home;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class HomeActivity extends AppCompatActivity {

    private TextInputEditText city;
    private TextView desc;
    private TextView temp;
    private TextView humid;

    private WEATHER_HEADER weather_header;

    private String responseCode;
    private String responseText;

    private CardView resultLayout;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resultLayout = (CardView) findViewById(R.id.resultLayout);
        resultLayout.setVisibility(View.GONE);

        city = (TextInputEditText) findViewById(R.id.city);
        desc = (TextView) findViewById(R.id.desc);
        temp = (TextView) findViewById(R.id.temp);
        humid = (TextView) findViewById(R.id.humid);

        AppCompatButton getWeather = (AppCompatButton) findViewById(R.id.getWeather);
        getWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (city.getText() == null || city.getText().toString().trim().isEmpty()) {
                   // ((TextInputLayout) city.getParentForAccessibility()).setError("Please provide city");
                    Toast toast = Toast.makeText(getApplicationContext(),"Enter city name",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    city.requestFocus();
                    return;
                }

                try {
                    WEATHER_HEADER header = new WEATHER_HEADER();
                    header.setCITY(city.getText().toString().trim());

                    getWeather(header);

                } catch (DBException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.home_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.settings:
                startActivity(new Intent(this, Home.class));
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void getWeather(final WEATHER_HEADER header) {

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please wait..");
        }

        progressDialog.show();

        final ISyncAppCallback callback = new ISyncAppCallback() {
            @Override
            public void onResponse(ISyncResponse iSyncResponse) {

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                SyncBEResponse syncBEResponse;
                responseText = null;

                if (iSyncResponse == null) {
                    responseCode = Constants.RESPONSE_CODE_ERROR;
                    responseText = getResources().getString(R.string.invalidResponse);
                } else {

                    switch (iSyncResponse.getResponseStatus()) {
                        case SUCCESS:

                            if (iSyncResponse instanceof SyncBEResponse) {

                                syncBEResponse = (SyncBEResponse) iSyncResponse;

                                responseCode = Constants.RESPONSE_CODE_SUCCESSFUL;
                                Vector<InfoMessage> infoMessages = syncBEResponse.getInfoMessages();

                                if (infoMessages != null && infoMessages.size() > 0) {
                                    StringBuilder infoMsgs = new StringBuilder();

                                    for (int i = 0; i < infoMessages.size(); i++) {
                                        responseCode = infoMessages.get(i).getCategory().equals(InfoMessage.CATEGORY_SUCCESS) ? Constants.RESPONSE_CODE_SUCCESSFUL : Constants.RESPONSE_CODE_ERROR;

                                        if (infoMessages.get(i).getMessage() != null && !infoMessages.get(i).getMessage().equals("")) {
                                            infoMsgs.append(infoMessages.get(i).getMessage() + "\n");
                                        }
                                    }

                                    responseText = infoMsgs.toString();

                                    if (responseText.trim().isEmpty())
                                        responseText = getResources().getString(R.string.personDownloadSuccess);
                                }

                                if (responseCode.equals(Constants.RESPONSE_CODE_SUCCESSFUL)) {
                                    Hashtable<String, Hashtable<IDataStructure, Vector<IDataStructure>>> dataBEs = syncBEResponse.getDataBEs();
                                    Hashtable<IDataStructure, Vector<IDataStructure>> tempCollectionOfHeaderAndItems = null;

                                    if (!dataBEs.isEmpty()) {

                                        Enumeration<String> beKeys = dataBEs.keys();

                                        if (beKeys.hasMoreElements()) {
                                            String customerBEName = beKeys.nextElement();
                                            tempCollectionOfHeaderAndItems = dataBEs.get(customerBEName);

                                            Enumeration<IDataStructure> contactHeaderKeys = tempCollectionOfHeaderAndItems.keys();

                                            while (contactHeaderKeys.hasMoreElements()) {
                                                weather_header = (WEATHER_HEADER) contactHeaderKeys.nextElement();
                                            }
                                        }
                                    }
                                }
                            }
                            break;

                        case FAILURE:
                            responseCode = Constants.RESPONSE_CODE_ERROR;

                            if (iSyncResponse instanceof SyncBEResponse) {
                                syncBEResponse = (SyncBEResponse) iSyncResponse;
                                responseText = syncBEResponse.getErrorMessage();

                                if (syncBEResponse.getErrorMessage().contains(getResources().getString(R.string.invalidResponse))) {
                                    responseText = getResources().getString(R.string.invalidResponse);
                                } else {
                                    responseText = syncBEResponse.getErrorMessage();
                                }

                                Vector<InfoMessage> infoMessages = syncBEResponse.getInfoMessages();

                                if (infoMessages != null && infoMessages.size() > 0) {
                                    StringBuilder infoMsgs = new StringBuilder();

                                    for (int i = 0; i < infoMessages.size(); i++) {
                                        if (infoMessages.get(i).getMessage() != null && !infoMessages.get(i).getMessage().equals("")) {
                                            infoMsgs.append(infoMessages.get(i).getMessage() + "\n");
                                        }
                                    }
                                    responseText = infoMsgs.toString();
                                }

                                if (responseText.trim().isEmpty())
                                    responseText = getResources().getString(R.string.invalidResponse);

                            } else {
                                responseText = getResources().getString(R.string.invalidResponse);
                            }
                            break;
                    }

                    if (responseCode != null && responseCode.equalsIgnoreCase(Constants.RESPONSE_CODE_ERROR)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showInfo(responseText);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadInfo();
                            }
                        });
                    }
                }
            }
        };

        /*
        * Always execute Process Agent(PA) in thread
        */
        new Thread(new Runnable() {
            @Override
            public void run() {
                PAHelper.getWeather(header, callback);
            }
        }).start();
    }

    private void showInfo(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();

        alertDialog.show();
    }

    private void loadInfo() {

        if (weather_header == null) {
            resultLayout.setVisibility(View.GONE);
            return;
        }

        resultLayout.setVisibility(View.VISIBLE);

        desc.setText(weather_header.getWEATHER_DESC());
        temp.setText(Utils.getTemperature(weather_header));
        humid.setText(weather_header.getHUMIDITY());
    }

}
