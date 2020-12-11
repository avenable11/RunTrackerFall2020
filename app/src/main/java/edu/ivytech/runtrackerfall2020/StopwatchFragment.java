package edu.ivytech.runtrackerfall2020;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import edu.ivytech.runtrackerfall2020.MapsActivity;
import edu.ivytech.runtrackerfall2020.R;
import edu.ivytech.runtrackerfall2020.database.RunsDB;


public class StopwatchFragment extends Fragment {
    private TextView mHoursTextView;
    private TextView mMinsTextView;
    private TextView mSecsTextView;
    private TextView mTenthsTextView;

    private Button mResetButton;
    private Button mStartStopButton;
    private Button mMapButton;

    private Timer mTimer;

    private long mStartTimeMillis;
    private long mElapsedTimeMillis;
    private boolean mStopwatchOn;
    private NumberFormat mNumber;
    private SharedPreferences mPrefs;

    private Intent mServiceIntent;
    private RunsDB mDB;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        mHoursTextView = v.findViewById(R.id.textViewHoursValue);
        mMinsTextView = v.findViewById(R.id.textViewMinsValue);
        mSecsTextView = v.findViewById(R.id.textViewSecsValue);
        mTenthsTextView = v.findViewById(R.id.textViewTenthsValue);
        mResetButton = v.findViewById(R.id.buttonReset);
        mStartStopButton = v.findViewById(R.id.buttonStartStop);
        mMapButton = v.findViewById(R.id.buttonViewMap);

        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mStopwatchOn) {
                    stop();
                } else {
                    start();
                }
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        mPrefs = getActivity().getSharedPreferences("Prefs", Context.MODE_PRIVATE);

        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent runMap = new Intent(getActivity().getApplicationContext(), MapsActivity.class);
                startActivity(runMap);
            }
        });

        mDB = RunsDB.get(getActivity().getApplicationContext());
        mServiceIntent = new Intent(getActivity().getApplicationContext(), RunsService.class);
        return v;

    }

    private void start() {
        if(mTimer != null) {
            mTimer.cancel();
        }

        if(!mStopwatchOn) {
            mStartTimeMillis = System.currentTimeMillis() - mElapsedTimeMillis;
        }
        mStopwatchOn = true;
        mStartStopButton.setText(R.string.stop);
        getActivity().startService(mServiceIntent);

        TimerTask task =  new TimerTask() {
            @Override
            public void run() {
                mElapsedTimeMillis = System.currentTimeMillis() - mStartTimeMillis;
                updateViews(mElapsedTimeMillis);
            }
        };
        mTimer = new Timer(true);
        mTimer.scheduleAtFixedRate(task, 0, 100);
    }

    private void stop() {
        mStopwatchOn = false;
        if (mTimer != null)
            mTimer.cancel();
        mStartStopButton.setText(R.string.start);
        getActivity().stopService(mServiceIntent);
        updateViews(mElapsedTimeMillis);
    }

    private void updateViews(final long elapsedMillis) {
        int elapsedTenths = (int) ((elapsedMillis/100) % 10);
        int elapsedSecs = (int) ((elapsedMillis/1000)%60);
        int elapsedMins = (int) ((elapsedMillis/1000*60)%60);
        int elapsedHours = (int) (elapsedMillis/(60*60*1000));

        if(elapsedHours > 0) {
            updateView(mHoursTextView, elapsedHours, 1);
        }
        updateView(mMinsTextView, elapsedMins, 2);
        updateView(mSecsTextView, elapsedSecs, 2);
        updateView(mTenthsTextView, elapsedTenths, 1);
    }

    private void updateView(final TextView textView, final long elapsedTime, final int minIntDigits) {
        mNumber = NumberFormat.getInstance();
        textView.post(new Runnable() {
            @Override
            public void run() {
                mNumber.setMinimumIntegerDigits(minIntDigits);
                textView.setText(mNumber.format(elapsedTime));
            }
        });
    }

    private void reset() {
        stop();
        mElapsedTimeMillis = 0;
        updateViews(mElapsedTimeMillis);
        mDB.deleteLocations();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean("stopwatchOn", mStopwatchOn);
        edit.putLong("startTimeMillis", mStartTimeMillis);
        edit.putLong("elapsedTimeMillis", mElapsedTimeMillis);
        edit.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        mStopwatchOn = mPrefs.getBoolean("stopwatchOn", false);
        mStartTimeMillis = mPrefs.getLong("startTimeMillis", System.currentTimeMillis());
        mElapsedTimeMillis = mPrefs.getLong("elapsedTimeMillis", 0);

        if(mStopwatchOn) {
            start();
        } else {
            updateViews(mElapsedTimeMillis);
        }

    }
}
