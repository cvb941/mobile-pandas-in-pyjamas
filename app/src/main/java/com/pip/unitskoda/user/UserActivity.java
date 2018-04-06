package com.pip.unitskoda.user;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kserno.baseclasses.BaseActivity;
import com.kserno.baseclasses.BasePresenter;
import com.pip.unitskoda.BaseApplication;
import com.pip.unitskoda.R;
import com.pip.unitskoda.di.user.DaggerUserComponent;
import com.pip.unitskoda.di.user.UserComponent;
import com.pip.unitskoda.di.user.UserModule;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by filipsollar on 6.4.18.
 */

public class UserActivity extends BaseActivity implements UserContract.Screen {

    private UserComponent mComponent;
    private State mState = State.NOT_RECORDING;
    @Inject UserPresenter mPresenter;

    @BindView(R.id.ibStart)
    ImageButton ibStart;



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
        return R.layout.activity_user;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ibStart = findViewById(R.id.ibStart);
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
        ibStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartClicked();
            }
        });


    }

    private UserComponent getComponent() {
        if (mComponent == null) {
            mComponent = DaggerUserComponent.builder()
                    .baseComponent(((BaseApplication) getApplication()).getBaseComponent())
                    .userModule(new UserModule(this))
                    .build();
        }
        return mComponent;
    }

    @Override
    public void connectionError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .show();
    }

    @Override
    public void success() {
        new AlertDialog.Builder(this)
                .setTitle("Success")
                .show();
    }

    @OnClick(R.id.ibStart)
    public void onStartClicked() {
        if (mState == State.NOT_RECORDING) {
            mPresenter.startRecording("test3@test.com", "record");
            mState = State.RECORDING;
        } else {
            mPresenter.stopRecording();
            mState = State.NOT_RECORDING;
        }
    }

    enum State {
        NOT_RECORDING, RECORDING
    }



}
