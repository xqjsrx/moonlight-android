/**
 * Created by Karim Mreisi.
 */

package com.limelight.binding.input.virtual_controller.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import com.limelight.BuildConfig;
import com.limelight.LimeLog;
import com.limelight.preferences.PreferenceConfiguration;
import com.limelight.utils.FileUriUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;

public class KeyBoardControllerConfigurationLoader {
    public static final String OSC_PREFERENCE = "keyboard_axi_list";
    public static final String OSC_PREFERENCE_VALUE = "OSC_Keyboard";

    // The default controls are specified using a grid of 128*72 cells at 16:9
    private static int screenScale(int units, int height) {
        return (int) (((float) height / (float) 72) * (float) units);
    }

    private static int screenScaleSwicth(int result, int height) {
        return result * 72 / height;
    }

    private static KeyboardDigitalPadButton createDiaitalPadButton(String elementId, int keyCodeLeft, int keyCodeRight, int keyCodeUp, int keyCodeDown, final KeyBoardController controller, final Context context) {
        KeyboardDigitalPadButton button = new KeyboardDigitalPadButton(controller, context, elementId);
        button.addDigitalPadListener(new KeyboardDigitalPadButton.DigitalPadListener() {
            @Override
            public void onDirectionChange(int direction) {
                if ((direction & KeyboardDigitalPadButton.DIGITAL_PAD_DIRECTION_LEFT) != 0) {
                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCodeLeft);
                    event.setSource(3);
                    controller.sendKeyEvent(event);
                } else {
                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, keyCodeLeft);
                    event.setSource(3);
                    controller.sendKeyEvent(event);
                }
                if ((direction & KeyboardDigitalPadButton.DIGITAL_PAD_DIRECTION_RIGHT) != 0) {
                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCodeRight);
                    event.setSource(3);
                    controller.sendKeyEvent(event);
                } else {
                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, keyCodeRight);
                    event.setSource(3);
                    controller.sendKeyEvent(event);
                }
                if ((direction & KeyboardDigitalPadButton.DIGITAL_PAD_DIRECTION_UP) != 0) {
                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCodeUp);
                    event.setSource(3);
                    controller.sendKeyEvent(event);
                } else {
                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, keyCodeUp);
                    event.setSource(3);
                    controller.sendKeyEvent(event);
                }
                if ((direction & KeyboardDigitalPadButton.DIGITAL_PAD_DIRECTION_DOWN) != 0) {
                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCodeDown);
                    event.setSource(3);
                    controller.sendKeyEvent(event);
                } else {
                    KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, keyCodeDown);
                    event.setSource(3);
                    controller.sendKeyEvent(event);
                }
            }
        });
        return button;
    }


    private static KeyBoardAnalogStickButton createKeyBoardAnalogStickButton(final KeyBoardController controller, String elementId, final Context context, int[] keylist) {

        KeyBoardAnalogStickButton analogStick = new KeyBoardAnalogStickButton(controller, elementId, context, keylist);
        analogStick.setListener(new KeyBoardAnalogStickButton.KeyBoardAnalogStickListener() {
            @Override
            public void onkeyEvent(int code, boolean isPress) {
                KeyEvent keyEvent = new KeyEvent(isPress ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP, code);
                keyEvent.setSource(2);
                controller.sendKeyEvent(keyEvent);
            }
        });

        return analogStick;

    }

    private static KeyBoardAnalogStickButtonFree createKeyBoardAnalogStickButton2(final KeyBoardController controller, String elementId, final Context context, int[] keylist) {

        KeyBoardAnalogStickButtonFree analogStick = new KeyBoardAnalogStickButtonFree(controller, elementId, context, keylist);
        analogStick.setListener(new KeyBoardAnalogStickButtonFree.KeyBoardAnalogStickListener() {
            @Override
            public void onkeyEvent(int code, boolean isPress) {
                KeyEvent keyEvent = new KeyEvent(isPress ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP, code);
                keyEvent.setSource(2);
                controller.sendKeyEvent(keyEvent);
            }
        });

        return analogStick;

    }


    private static KeyBoardDigitalButton createDigitalButton(
            final String elementId,
            final int keyShort,
            final int type,
            final int layer,
            final String text,
            final int icon,
            final KeyBoardController controller,
            final Context context) {
        KeyBoardDigitalButton button = new KeyBoardDigitalButton(controller, elementId, layer, context);
        button.setText(text);
        button.setIcon(icon);

        if(elementId.startsWith("m_s_")||elementId.startsWith("key_s_")){
            button.setEnableSwitchDown(true);
        }

        button.addDigitalButtonListener(new KeyBoardDigitalButton.DigitalButtonListener() {
            @Override
            public void onClick() {
                KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyShort);
                keyEvent.setSource(type);
                controller.sendKeyEvent(keyEvent);
            }

            @Override
            public void onLongClick() {
            }

            @Override
            public void onRelease() {
                KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyShort);
                keyEvent.setSource(type);
                controller.sendKeyEvent(keyEvent);

            }
        });

        return button;
    }


    private static KeyBoardTouchPadButton createDigitalTouchButton(
            final String elementId,
            final int keyShort,
            final int type,
            final int layer,
            final String text,
            final int icon,
            final KeyBoardController controller,
            final Context context) {
        KeyBoardTouchPadButton button = new KeyBoardTouchPadButton(controller, elementId, layer, context);
        button.setText(text);
        button.setIcon(icon);
        button.addDigitalButtonListener(new KeyBoardTouchPadButton.DigitalButtonListener() {
            @Override
            public void onClick() {
                int code=keyShort==9?3:1;
                KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, code);
                keyEvent.setSource(type);
                controller.sendKeyEvent(keyEvent);
            }

            @Override
            public void onLongClick() {
            }

            @Override
            public void onMove(int x, int y) {
                controller.sendMouseMove(x,y);
            }

            @Override
            public void onRelease() {
                int code=keyShort==9?3:1;
                KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, code);
                keyEvent.setSource(type);
                controller.sendKeyEvent(keyEvent);

            }
        });

        return button;
    }

    public static void createDefaultLayout(final KeyBoardController controller, final Context context) {

        DisplayMetrics screen = context.getResources().getDisplayMetrics();

        PreferenceConfiguration config = PreferenceConfiguration.readPreferences(context);

        int height = screen.heightPixels;

        int rightDisplacement = screen.widthPixels - screen.heightPixels * 16 / 9;

        int BUTTON_SIZE = 10;

        int w = screenScale(BUTTON_SIZE, height);

        int maxW = screen.widthPixels / 18;

        if (w > maxW) {
            BUTTON_SIZE = screenScaleSwicth(maxW, height);
            w = screenScale(BUTTON_SIZE, height);
        }

        String result = "";

        //启用自定义配置文件
        if(config.enableCustomKeyboardFile){
            String displayName = "axi_keyboard.json";
            File dataBaseFile=new File(context.getFilesDir().getAbsolutePath(), displayName);
            String authority= BuildConfig.APPLICATION_ID+".fileprovider";
            Uri uri= FileProvider.getUriForFile(context,authority,dataBaseFile);
            result=FileUriUtils.openUriForRead(context,uri);
        }
        if(TextUtils.isEmpty(result)){
            try {
                InputStream is = context.getAssets().open("config/keyboard.json");
                int lenght = is.available();
                byte[] buffer = new byte[lenght];
                is.read(buffer);
                result = new String(buffer, "utf8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (TextUtils.isEmpty(result)) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject jsonObject1 = jsonObject.getJSONObject("data");

            JSONArray keystrokeList = jsonObject1.optJSONArray("keystroke");
            JSONArray dpadList = jsonObject1.optJSONArray("dpad");
            JSONArray rockerList = jsonObject1.optJSONArray("rocker");
            JSONArray mouseList = jsonObject1.optJSONArray("mouse");

            //十字键
            if(dpadList!=null&&dpadList.length()>0){
                for (int i = 0; i < dpadList.length(); i++) {
                    JSONObject obj = dpadList.getJSONObject(i);
                    String code = obj.optString("elementId");
                    int keyCodeLeft = obj.optInt("leftCode");
                    int keyCodeRight = obj.optInt("rightCode");
                    int keyCodeUp = obj.optInt("upCode");
                    int keyCodeDown = obj.optInt("downCode");
                    controller.addElement(createDiaitalPadButton(code, keyCodeLeft, keyCodeRight, keyCodeUp, keyCodeDown, controller, context),
                            screenScale(92, height) + rightDisplacement,
                            screenScale(41, height),
                            (int) (w * 2.5), (int) (w * 2.5)
                    );
                }
            }

            //摇杆
            if(rockerList!=null&&rockerList.length()>0){
                for (int i = 0; i < rockerList.length(); i++) {
                    JSONObject obj = rockerList.getJSONObject(i);
                    String code = obj.optString("elementId");
                    int keyCodeLeft = obj.optInt("leftCode");
                    int keyCodeRight = obj.optInt("rightCode");
                    int keyCodeUp = obj.optInt("upCode");
                    int keyCodeDown = obj.optInt("downCode");
                    int keyCodeMiddle = obj.optInt("middleCode");
                    int[] keys = new int[]{keyCodeUp, keyCodeDown, keyCodeLeft, keyCodeRight, keyCodeMiddle};

                    if(config.enableNewAnalogStick){
                        controller.addElement(createKeyBoardAnalogStickButton2(controller, code, context, keys),
                                screenScale(4, height),
                                screenScale(41, height),
                                (int) (w * 2.5), (int) (w * 2.5)
                        );
                    }else{
                        controller.addElement(createKeyBoardAnalogStickButton(controller, code, context, keys),
                                screenScale(4, height),
                                screenScale(41, height),
                                (int) (w * 2.5), (int) (w * 2.5)
                        );
                    }
                }
            }

            //鼠标按键
            if(mouseList!=null&&mouseList.length()>0){
                for (int i = 0; i < mouseList.length(); i++) {
                    JSONObject obj = mouseList.getJSONObject(i);
                    obj.put("type", 1);
                    keystrokeList.put(obj);
                }
            }

            double buttonSum = 14.0;

            //普通按键
            if(keystrokeList!=null&&keystrokeList.length()>0){
                for (int i = 0; i < keystrokeList.length(); i++) {
                    JSONObject obj = keystrokeList.getJSONObject(i);

                    String name = obj.optString("name");

                    int type = obj.optInt("type");

                    int code = obj.optInt("code");

                    int switchButton=obj.optInt("switchButton");

                    String elementId = type == 0 ? "key_" + code : "m_" + code;

                    if(switchButton==1){
                        elementId=type == 0 ? "key_s_" + code : "m_s_" + code;
                    }

                    int lastIndex = (int) (i / buttonSum);

                    int x = screenScale(1 + (int) (i % buttonSum) * BUTTON_SIZE, height);

                    int y = screenScale(BUTTON_SIZE + lastIndex * BUTTON_SIZE, height);

                    if(TextUtils.equals("m_9",elementId)||TextUtils.equals("m_10",elementId)||TextUtils.equals("m_11",elementId)){
                        controller.addElement(createDigitalTouchButton(elementId, code, type, 1, name, -1, controller, context),
                                x, y,
                                w, w
                        );
                    }else{
                        controller.addElement(createDigitalButton(elementId, code, type, 1, name, -1, controller, context),
                                x, y,
                                w, w
                        );
                    }
                    LimeLog.info("x:" + x + ",y:" + y + ",W&H:" + w + "," + screenScale(BUTTON_SIZE, height));
                }
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        controller.setOpacity(config.oscOpacity);
    }

    public static void saveProfile(final KeyBoardController controller,
                                   final Context context) {
        String name = PreferenceManager.getDefaultSharedPreferences(context).getString(OSC_PREFERENCE, OSC_PREFERENCE_VALUE);

        SharedPreferences.Editor prefEditor = context.getSharedPreferences(name, Activity.MODE_PRIVATE).edit();

        for (keyBoardVirtualControllerElement element : controller.getElements()) {
            String prefKey = "" + element.elementId;
            try {
                prefEditor.putString(prefKey, element.getConfiguration().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefEditor.apply();
    }

    public static void loadFromPreferences(final KeyBoardController controller, final Context context) {
        String name = PreferenceManager.getDefaultSharedPreferences(context).getString(OSC_PREFERENCE, OSC_PREFERENCE_VALUE);

        SharedPreferences pref = context.getSharedPreferences(name, Activity.MODE_PRIVATE);

        for (keyBoardVirtualControllerElement element : controller.getElements()) {
            String prefKey = "" + element.elementId;

            String jsonConfig = pref.getString(prefKey, null);
            if (jsonConfig != null) {
                try {
                    element.loadConfiguration(new JSONObject(jsonConfig));
                } catch (JSONException e) {
                    e.printStackTrace();

                    // Remove the corrupt element from the preferences
                    pref.edit().remove(prefKey).apply();
                }
            }
        }
    }
}
