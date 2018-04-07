package com.pip.unitskoda;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.kserno.baseclasses.BaseActivity;
import com.kserno.baseclasses.BasePresenter;
import com.kserno.baseclasses.BaseRecyclerAdapter;
import com.pip.unitskoda.calendar.Attendee;
import com.pip.unitskoda.calendar.CalendarManager;
import com.pip.unitskoda.calendar.ParticipantAdapter;
import com.pip.unitskoda.di.main.DaggerMainComponent;
import com.pip.unitskoda.di.main.MainComponent;
import com.pip.unitskoda.di.main.MainModule;
import com.pip.unitskoda.meeting.MeetingActivity;
import com.pip.unitskoda.recording.Recorder;
import com.pip.unitskoda.user.UserActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import it.macisamuele.calendarprovider.CalendarInfo;
import it.macisamuele.calendarprovider.EventInfo;

/**
 * Created by filipsollar on 6.4.18.
 */

public class MainActivity extends BaseActivity implements MainContract.Screen, BaseRecyclerAdapter.ItemClickListener<Attendee> {

    private MainComponent mComponent;

    public static final String EXTRA_ATTENDEE = "EXTRA_ATTENDEE";

    private Spinner spCalendar;
    private TextView tvEventName;
    private RecyclerView rvParticipants;
    private Button btSelectEvent;

    private EventInfo mEventInfo;

    private ParticipantAdapter mAdapter;

    private List<Attendee> mAttendees = new ArrayList<>();
    private List<String> userModels = new ArrayList<>();

    @Inject
    MainPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        spCalendar = findViewById(R.id.spCalendar);
        tvEventName = findViewById(R.id.tvEventName);
        rvParticipants = findViewById(R.id.rvParticipants);
        btSelectEvent = findViewById(R.id.btSelectEvent);

        mAdapter = new ParticipantAdapter();
        mAdapter.addListener(this);
        rvParticipants.setAdapter(mAdapter);

        // Microphone permissions
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_CALENDAR)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        onPermissionsGranted();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    }
                })
                .check();

        btSelectEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMeeting();
            }
        });

        mPresenter.loadModels();

    }

    private void goToMeeting() {
        Intent intent = new Intent(this, MeetingActivity.class);

        intent.putExtra(MeetingActivity.EXTRA_EVENT_NAME, mEventInfo.getTitle());
        intent.putExtra(MeetingActivity.EXTRA_ATTENDEES, new ArrayList<>(mAttendees));

        startActivity(intent);
    }

    @Override
    protected void createPresenter() {
        getComponent().inject(this);
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    private MainComponent getComponent() {
        if (mComponent == null) {

            mComponent = DaggerMainComponent.builder()
                    .baseComponent(((BaseApplication) getApplication()).getBaseComponent())
                    .mainModule(new MainModule(this, Recorder.INSTANCE))
                    .build();
        }


        return mComponent;
    }

    private void onPermissionsGranted() {
        setupCalendarSelects();

    }

    private void setupCalendarSelects() {
        // Load calendars
        final List<CalendarInfo> calendars = CalendarManager.getCalendars(MainActivity.this);

        ArrayAdapter<String> calendarArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        calendarArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calendarArrayAdapter.addAll(CalendarManager.getCalendarStrings(calendars));

        spCalendar.setAdapter(calendarArrayAdapter);


        // Set event info from first calendar in list
        // TODO change default select to preference
        spCalendar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                updateEvent(calendars.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateEvent(CalendarInfo currentCalendar) {
        List<EventInfo> events = CalendarManager.getCurrentEventsOfCalendar(MainActivity.this, currentCalendar);

        if (events.isEmpty()) {
            // TODO display no pending events
        } else {
            mEventInfo = events.get(0);
            mAttendees = CalendarManager.getAttendeesOfEvent(MainActivity.this, mEventInfo, userModels);

            // Event card
            tvEventName.setText(mEventInfo.getTitle());

            // Participants card
            mAdapter.setData(mAttendees);

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.stop();
    }

    @Override
    public void onItemClicked(Attendee item) {
        Intent intent = new Intent(this, UserActivity.class);

        intent.putExtra(EXTRA_ATTENDEE, item);

        startActivity(intent);

    }

    @Override
    public void showUserModels(List<String> userModels) {
        this.userModels = userModels;
        for (Attendee attendee: mAttendees) {
            //attendee.models(userModels); TODO rozjebat kokota kusika
        }
        mAdapter.setUserModels();
    }
}
