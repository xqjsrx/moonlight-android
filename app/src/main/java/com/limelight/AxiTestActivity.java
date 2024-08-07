package com.limelight;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.limelight.utils.DeviceUtils;
import com.limelight.utils.UiHelper;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Description
 * Date: 2024-05-06
 * Time: 16:11
 */
public class AxiTestActivity extends Activity implements View.OnClickListener {

    private TextView tx_gamepad_info;

    private Vibrator vibrator;

    private Button bt_vibrator;
    private List<InputDevice> ids = new ArrayList<>();

    private Vibrator vibratorOnline;

    private Button bt_vibrator_value;

    private int simulatedAmplitude=220;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_axitest);

        UiHelper.setStatusBarLightMode(getWindow(),true);

        tx_gamepad_info = findViewById(R.id.tx_game_pad_info);
        TextView tx_content=findViewById(R.id.tx_content);
        bt_vibrator=findViewById(R.id.bt_vibrator);

        bt_vibrator_value=findViewById(R.id.bt_vibrator_value);

        vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String kernelVersion =System.getProperty("os.version");
        StringBuffer sb=new StringBuffer();
        sb.append("安卓版本："+ DeviceUtils.getSDKVersionName());
        sb.append("\tapi版本："+Build.VERSION.SDK_INT);
        sb.append("\n内核版本："+kernelVersion);
        sb.append("\n品牌型号："+DeviceUtils.getManufacturer()+"\t-\t"+DeviceUtils.getModel());
        tx_content.setText(sb.toString());

        boolean hasVibrator=((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).hasVibrator();
        String content=hasVibrator?"有震动马达":"无震动马达";
        bt_vibrator.setText("测试设备震动（"+content+"）");

        showSimlateAmp();
    }

    private void showSimlateAmp(){
        bt_vibrator_value.setText("振幅强度（"+simulatedAmplitude+"）");
    }


    private void cancleRumble(){
        if(vibratorOnline!=null){
            vibratorOnline.cancel();
        }
        if(vibrator!=null){
            vibrator.cancel();
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.bt_vibrator_cancle){
            cancleRumble();
            return;
        }
        //机身震动
        if (v.getId() == R.id.bt_vibrator) {
            String[] titles=new String[]{"简单震一秒","持续HD震动"};
            new AlertDialog.Builder(this).setItems(titles, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    switch (which){
                        case 0:
                            vibrator.vibrate(1000);
                            break;
                        case 1:
                            rumble(vibrator);
                            break;
                    }
                }
            }).setTitle("请选择").create().show();
            return;
        }

        //手柄震动
        if (v.getId() == R.id.bt_vibrator_gamepad) {
            if(ids.size()==0){
                Toast.makeText(AxiTestActivity.this, "目前没有检测到手柄，请连接手柄，点击刷新按钮，再尝试！", Toast.LENGTH_LONG).show();
                return;
            }
            String[] strings = new String[ids.size()];
            for (int i = 0; i < ids.size(); i++) {
                strings[i] = ids.get(i).getName();
            }
            new AlertDialog.Builder(this).setItems(strings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (ids.get(which).getVibrator().hasVibrator()) {
                        String[] titles=new String[]{"简单震一秒","持续HD震动"};
                        new AlertDialog.Builder(AxiTestActivity.this).setItems(titles, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which2) {
                                dialog.dismiss();
                                switch (which2){
                                    case 0:
                                        ids.get(which).getVibrator().vibrate(1000);
                                        break;
                                    case 1:
                                        cancleRumble();
                                        vibratorOnline=ids.get(which).getVibrator();
                                        rumble(vibratorOnline);
                                        break;
                                }
                            }
                        }).setTitle("请选择").create().show();
                    } else {
                        Toast.makeText(AxiTestActivity.this, "手柄没有识别到震动传感器！", Toast.LENGTH_SHORT).show();
                    }

                }
            }).setTitle("请选择").create().show();

            return;
        }
        //刷新手柄信息
        if (v.getId() == R.id.bt_update_gamepad) {
            updateGamePad();
            return;
        }

        if(v.getId()==R.id.bt_vibrator_value){
            SeekBar mSeekBar=new SeekBar(this);
            mSeekBar.setMax(255);
            mSeekBar.setProgress(simulatedAmplitude);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    simulatedAmplitude=progress;
                    showSimlateAmp();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            AlertDialog.Builder editDialog = new AlertDialog.Builder(this);
            editDialog.setTitle("设置振幅0-255【HD震动生效】");
            //设置dialog布局
            editDialog.setView(mSeekBar);
            editDialog.create().show();
            return;
        }

//        if(v.getId()==R.id.bt_vibrator_setting){
//            Intent intent=new Intent();
//            intent.setClassName("com.android.settings","com.android.settings.SubSettings");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            return;
//        }
    }


    private void rumble(Vibrator vibrator){
        long pwmPeriod = 20;
        long onTime = (long)((simulatedAmplitude / 255.0) * pwmPeriod);
        long offTime = pwmPeriod - onTime;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            VibrationAttributes vibrationAttributes = new VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_HARDWARE_FEEDBACK)
                    .build();
            vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, onTime, offTime}, 0), vibrationAttributes);
        }
        else {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            vibrator.vibrate(new long[]{0, onTime, offTime}, 0, audioAttributes);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(vibratorOnline!=null){
            vibratorOnline.cancel();
        }
    }

    private void updateGamePad() {
        ids.clear();
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                if (getMotionRangeForJoystickAxis(dev, MotionEvent.AXIS_X) != null &&
                        getMotionRangeForJoystickAxis(dev, MotionEvent.AXIS_Y) != null) {
                    // This is a gamepad
                    ids.add(dev);
                    //android 12
                    sb.append("名称："+dev.getName());
                    sb.append("\n");
                    sb.append("传感器：");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        String sensor="";
                        if (dev.getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                            sensor="+加速度传感器";
                        }
                        if(dev.getSensorManager().getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
                            sensor="+陀螺仪";
                        }
                        if(sensor.length()==0){
                            sb.append("无（没有相关驱动或者手柄不支持）");
                        }else{
                            sb.append(sensor);
                        }
                        sb.append("\n");
                    }else{
                        sb.append("低于android12没有对应API");
                        sb.append("\n");
                    }
                    sb.append("VID_PID："+dev.getVendorId()+"_"+dev.getProductId()
                            +"\t    ["+String.format("%04x", dev.getVendorId())+"_"+String.format("%04x", dev.getProductId())+"]");
                    sb.append("\n");
                    sb.append("震动："+(dev.getVibrator().hasVibrator()?"支持":"不支持"));
                    sb.append("\n");
                    sb.append("详细信息：\n");
                    sb.append(dev.toString());
                    sb.append("\n");
                }

            }
        }

        tx_gamepad_info.setText("手柄数量：" + ids.size() + "\n" + sb.toString());
    }


    private static InputDevice.MotionRange getMotionRangeForJoystickAxis(InputDevice dev, int axis) {
        InputDevice.MotionRange range;

        // First get the axis for SOURCE_JOYSTICK
        range = dev.getMotionRange(axis, InputDevice.SOURCE_JOYSTICK);
        if (range == null) {
            // Now try the axis for SOURCE_GAMEPAD
            range = dev.getMotionRange(axis, InputDevice.SOURCE_GAMEPAD);
        }

        return range;
    }

}
