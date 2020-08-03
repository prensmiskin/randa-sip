package com.qarocotwo.randa_grid;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;

public class MainActivity extends AppCompatActivity {
    Button b1;
    TextView sipadd;
    public String domain = "192.168.1.42";
    public String username = "7001";
    public String password = "123";
    public String sipAddress = "7001@192.168.1.42";
    public SipManager sipManager = null;
    public SipManager mSipManager = null;
    public SipProfile mSipProfile = null;
    public SipAudioCall call = null;


    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // sipAddress = (String) getIntent().getExtras().get("sipAddress");
        b1 = (Button) findViewById(R.id.button);
        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sipadd = (TextView) findViewById(R.id.textone);

        sipadd.setText(sipAddress);
        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (call != null) {
                    call.close();
                }
                finish();
            }
        });
        initializeManager();
    }

    @Override
    public void onStart() {
        super.onStart();
        // When we get back from the preference setting Activity, assume
        // settings have changed, and re-login with new auth info.
        initializeManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.close();
        }

        closeLocalProfile();

        // if (callReceiver != null) {
        // this.unregisterReceiver(callReceiver);
        // }
    }

    public void initializeManager() {
        if (mSipManager == null) {
            mSipManager = SipManager.newInstance(this);
        }

        initializeLocalProfile();
    }

    public void initializeLocalProfile() {
        if (mSipManager == null) {
            return;
        }

        if (mSipProfile != null) {
            closeLocalProfile();
        }
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        String username = prefs.getString("namePref", "");
        String domain = prefs.getString("domainPref", "");
        String password = prefs.getString("passPref", "");

        if (username.length() == 0 || domain.length() == 0
                || password.length() == 0) {
            // showDialog(UPDATE_SETTINGS_DIALOG);
            return;
        }

        try {
            SipProfile.Builder builder = new SipProfile.Builder(username,
                    domain);
            builder.setPassword(password);
            builder.setDisplayName(username);
            builder.setAuthUserName(username);
            mSipProfile = builder.build();

            Intent i = new Intent();
            i.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i,
                    Intent.FILL_IN_DATA);
            mSipManager.open(mSipProfile, pi, null);
            //
            //
            // // This listener must be added AFTER manager.open is called,
            // // Otherwise the methods aren't guaranteed to fire.

            mSipManager.setRegistrationListener(mSipProfile.getUriString(),
                    new SipRegistrationListener() {
                        public void onRegistering(String localProfileUri) {
                            // updateStatus("Registering with SIP Server...");
                            Log.d("onRegistering",
                                    "Registering with SIP Server...");
                        }

                        public void onRegistrationDone(String localProfileUri,
                                                       long expiryTime) {
                            // updateStatus("Ready");
                            Log.d("onRegistrationDone",
                                    "RegistrationDone..Ready");

                        }

                        public void onRegistrationFailed(
                                String localProfileUri, int errorCode,
                                String errorMessage) {
                            // updateStatus("Registration failed.  Please check settings.");
                            Log.d("onRegistrationFailed", "RegistrationFailed");

                        }
                    });
        } catch (ParseException pe) {
            // updateStatus("Connection Error.");
        } catch (SipException se) {
            // updateStatus("Connection error.");
        }

        initiateCall();
    }

    @SuppressLint("LongLogTag")
    public void closeLocalProfile() {
        if (mSipManager == null) {
            return;
        }
        try {
            if (mSipProfile != null) {
                mSipManager.close(mSipProfile.getUriString());
            }
        } catch (Exception ee) {
            Log.d("WalkieTalkieActivity/onDestroy",
                    "Failed to close local profile.", ee);
        }
    }

    @SuppressLint("LongLogTag")
    public void initiateCall() {

        // updateStatus(sipAddress);
        Log.d("nzm", "initiatecall");

        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners. Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is
                // established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(true);
                    call.toggleMute();
                    Log.d("on call established", "on call established");
                    // updateStatus(call);
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    // updateStatus("Ready.");
                    // Intent i = new
                    // Intent(getBaseContext(),DialActivity.class);
                    // startActivity(i);
                    finish();
                }
            };

            call = mSipManager.makeAudioCall(mSipProfile.getUriString(), sipAddress,
                    listener, 3000);
            Log.d("call", "" + call.getState());
        } catch (Exception e) {
            Log.i("WalkieTalkieActivity/InitiateCall",
                    "Error when trying to close manager.", e);
            if (mSipProfile != null) {
                try {
                    mSipManager.close(mSipProfile.getUriString());
                } catch (Exception ee) {
                    Log.i("WalkieTalkieActivity/InitiateCall",
                            "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }

}