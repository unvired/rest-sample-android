package com.unvired.sample.rest.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.unvired.core.FrameworkVersion;
import com.unvired.database.DBException;
import com.unvired.exception.ApplicationException;
import com.unvired.logger.Logger;
import com.unvired.login.AuthenticationService;
import com.unvired.login.LoginListener;
import com.unvired.login.LoginParameters;
import com.unvired.login.LoginParameters.LOGIN_TYPE;
import com.unvired.login.UnviredAccount;
import com.unvired.model.ApplicationVersion;
import com.unvired.sample.rest.R;
import com.unvired.sample.rest.util.Constants;
import com.unvired.sample.rest.util.PermissionHelper;
import com.unvired.sample.rest.util.UnviredAppPreference;
import com.unvired.sample.rest.util.Utils;
import com.unvired.utils.FrameworkHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StartUpActivity extends AppCompatActivity implements LoginListener {

    private static final String CLASS_NAME = StartUpActivity.class.getName();
    private static final String URL = "https://sandbox.unvired.io/UMP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);
    }

    @Override
    public void onResume() {
        super.onResume();
        initialize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionHelper.GENERAL_PERMISSION) {
            boolean permitted = true;
            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {
                    permitted = false;
                }
            }

            if (permitted) {
                initializeFramework(this);
            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initialize() {

        List<String> permissionList = new ArrayList<>();

        if (!PermissionHelper.hasPhonePermission(this)) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!PermissionHelper.hasStoragePermission(this)) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionList.size() > 0) {
            PermissionHelper.requestPermissions(this, permissionList);
        } else {
            initializeFramework(StartUpActivity.this);
        }
    }

    public void initializeFramework(Context context) {

        UnviredAppPreference ShoutPreference = new UnviredAppPreference(context, "UNVIRED_APP_PREF_KEY", Constants.UNVIRED_APP_PREFERENCE_KEY, true);
//        Fabric.with(context, new Crashlytics());
        if (ShoutPreference != null) {
            String applicationVersion = ShoutPreference.getString("APPLICATION_VERSION");
            String frameworkVersion = ShoutPreference.getString("FRAMEWORK_VERSION");
            String userId = ShoutPreference.getString("USER_ID");
            String feUserId = ShoutPreference.getString("FEUSERID");
            String serverURL = ShoutPreference.getString("SERVER_URL");
            String screenName = ShoutPreference.getString("SCREEN_NAME");

           /* Crashlytics.setString("APPLICATION_VERSION:", applicationVersion);
            Crashlytics.setString("FRAMEWORK_VERSION:", frameworkVersion);
            Crashlytics.setString("FE_USER_ID:", feUserId);
            Crashlytics.setString("SERVER_URL:", serverURL);
            Crashlytics.setUserIdentifier(userId);
            if (!Strings.isNullOrEmpty(screenName))
                Crashlytics.setString("SCREEN_NAME", screenName);*/
        }

        String metaDataXml = null;

        try {
            InputStream inputStream = this.getResources().openRawResource(R.raw.metadata);
            metaDataXml = FrameworkHelper.getString(inputStream);
        } catch (ApplicationException e) {
            Logger.log(Logger.LEVEL_ERROR, CLASS_NAME, "initializeFramework", "ApplicationException: " + e.getMessage());
        }

        LoginParameters.setUrl(URL);
        LoginParameters.setAppTitle(getResources().getText(R.string.app_name).toString());
        LoginParameters.setMetaDataXml(metaDataXml);
        LoginParameters.setLoginListener(this);
        LoginParameters.setDemoModeRequired(false);
//        LoginParameters.setDemoData(demoDataInputStream);
//        LoginParameters.setDemoModeListener(roundOrdersDemomodeListener);
        LoginParameters.setContext(context);
        LoginParameters.showCompanyField(true);
        LoginParameters.setAppName(Constants.APPLICATION_NAME);
        LoginParameters.setLoginTypes(new LoginParameters.LOGIN_TYPE[]{LOGIN_TYPE.UNVIRED_ID});

        ApplicationVersion.setBUILD_NUMBER("1");

        try {
            AuthenticationService.login(this.getApplicationContext());
        } catch (ApplicationException e) {
            Logger.log(Logger.LEVEL_ERROR, this.CLASS_NAME, "initializeFramework", e.getMessage());
        } catch (DBException e) {
            Logger.log(Logger.LEVEL_ERROR, this.CLASS_NAME, "initializeFramework", e.getMessage());
        } catch (Exception e) {
            Logger.log(Logger.LEVEL_ERROR, this.CLASS_NAME, "initializeFramework", "Exception caught: " + e.getMessage());
        }

        this.finish();
    }

    private void checkGooglePlayServices() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.playServiceError));
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")));
                finish();

            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        switch (result) {
            case ConnectionResult.SUCCESS:
                //***************GCM Start************************
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, com.unvired.gcm.RegistrationIntentService.class);
                startService(intent);

                //***************GCM End************************

                saveCustomKeys();
                navigateToHomeActivity();

                break;

            case ConnectionResult.SERVICE_MISSING:
                builder.setPositiveButton(getResources().getString(R.string.install), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")));
                        finish();

                    }
                });
                builder.setMessage(getResources().getString(R.string.installPlayService));
                builder.show();
                break;

            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                builder.setPositiveButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")));
                        finish();

                    }
                });
                builder.setMessage(getResources().getString(R.string.updatePlayService));
                builder.show();
                break;

            default:
                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")));
                        finish();

                    }
                });

                builder.setMessage(getResources().getString(R.string.errorPlayService));
                builder.show();
                break;
        }
    }

    private void saveCustomKeys() {
        UnviredAppPreference unviredAppPreference = new UnviredAppPreference(this, "UNVIRED_APP_PREF_KEY", Constants.UNVIRED_APP_PREFERENCE_KEY, true);
        unviredAppPreference.put("APPLICATION_VERSION", FrameworkVersion.getApplicationVersion());
        unviredAppPreference.put("FRAMEWORK_VERSION", FrameworkVersion.getFrameworkVersion());
        unviredAppPreference.put("USER_ID", Utils.getUserId());
        unviredAppPreference.put("FEUSERID", Utils.getFEUserId());
        unviredAppPreference.put("SERVER_URL", Utils.getServerURL());
    }

    private void navigateToHomeActivity() {
        startActivity(new Intent(this, HomeActivity.class));

    }

    @Override
    public void loginSuccessful() {
        checkGooglePlayServices();

    }

    @Override
    public void loginCancelled() {

    }

    @Override
    public void loginFailure(String s) {

    }

    @Override
    public void authenticateAndActivationSuccessful() {
        checkGooglePlayServices();
    }

    @Override
    public void authenticateAndActivationFailure(String s) {

    }

    @Override
    public void invokeAppLoginActivity(boolean b) {

    }

    @Override
    public void noSDCardFound(String s) {

    }

    @Override
    public void invokeMultiAccountActivity(List<UnviredAccount> list) {

    }

}