package com.limelight.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocaleManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.os.Build;
import android.os.LocaleList;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import com.limelight.R;
import com.limelight.nvstream.http.ComputerDetails;
import com.limelight.preferences.PreferenceConfiguration;

import java.util.Locale;

public class UiHelper {

    private static final int TV_VERTICAL_PADDING_DP = 15;
    private static final int TV_HORIZONTAL_PADDING_DP = 15;

    // Removed setGameModeStatus() and its calls from this class

    public static void notifyStreamConnecting(Context context) { /* No-op */ }

    public static void notifyStreamConnected(Context context) { /* No-op */ }

    public static void notifyStreamEnteringPiP(Context context) { /* No-op */ }
    
    public static void notifyStreamExitingPiP(Context context) { /* No-op */ }

    public static void notifyStreamEnded(Context context) { /* No-op */ }
    
    public static void setLocale(Activity activity)
    {
        String locale = PreferenceConfiguration.readPreferences(activity).language;
        if (!locale.equals(PreferenceConfiguration.DEFAULT_LANGUAGE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // On Android 13, migrate this non-default language setting into the OS native API
                LocaleManager localeManager = activity.getSystemService(LocaleManager.class);
                localeManager.setApplicationLocales(LocaleList.forLanguageTags(locale));
                PreferenceConfiguration.completeLanguagePreferenceMigration(activity);
            }
            else {
                Configuration config = new Configuration(activity.getResources().getConfiguration());

                // Some locales include both language and country which must be separated
                // before calling the Locale constructor.
                if (locale.contains("-"))
                {
                    config.locale = new Locale(locale.substring(0, locale.indexOf('-')),
                            locale.substring(locale.indexOf('-') + 1));
                }
                else
                {
                    config.locale = new Locale(locale);
                }

                activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
            }
        }
    }

    public static void applyStatusBarPadding(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.setOnApplyWindowInsetsListener((v, windowInsets) -> {
                v.setPadding(v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        windowInsets.getTappableElementInsets().bottom);
                return windowInsets;
            });
            view.requestApplyInsets();
        }
    }

    public static void notifyNewRootView(final Activity activity)
    {
        View rootView = activity.findViewById(android.R.id.content);
        UiModeManager modeMgr = (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Allow this non-streaming activity to layout under notches.
            //
            // We should NOT do this for the Game activity unless
            // the user specifically opts in, because it can obscure
            // parts of the streaming surface.
            activity.getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if (modeMgr.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            // Increase view padding on TVs
            float scale = activity.getResources().getDisplayMetrics().density;
            int verticalPaddingPixels = (int) (TV_VERTICAL_PADDING_DP*scale + 0.5f);
            int horizontalPaddingPixels = (int) (TV_HORIZONTAL_PADDING_DP*scale + 0.5f);

            rootView.setPadding(horizontalPaddingPixels, verticalPaddingPixels,
                    horizontalPaddingPixels, verticalPaddingPixels);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Draw under the status bar on Android Q devices

            // Using getDecorView() here breaks the translucent status/navigation bar when gestures are disabled
            activity.findViewById(android.R.id.content).setOnApplyWindowInsetsListener((view, windowInsets) -> {
                Insets tappableInsets = windowInsets.getTappableElementInsets();
                view.setPadding(tappableInsets.left,
                        tappableInsets.top,
                        tappableInsets.right,
                        0);

                if (tappableInsets.bottom != 0) {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                } else {
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }

                return windowInsets;                
            });

            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    public static void showDecoderCrashDialog(Activity activity) {
        final SharedPreferences prefs = activity.getSharedPreferences("DecoderTombstone", 0);
        final int crashCount = prefs.getInt("CrashCount", 0);
        int lastNotifiedCrashCount = prefs.getInt("LastNotifiedCrashCount", 0);

        // Remember the last crash count we notified at, so we don't
        // display the crash dialog every time the app is started until
        // they stream again
        if (crashCount != 0 && crashCount != lastNotifiedCrashCount) {
            if (crashCount % 3 == 0) {
                // At 3 consecutive crashes, we'll forcefully reset their settings
                PreferenceConfiguration.resetStreamingSettings(activity);
                Dialog.displayDialog(activity,
                        activity.getResources().getString(R.string.title_decoding_reset),
                        activity.getResources().getString(R.string.message_decoding_reset),
                        () -> prefs.edit().putInt("LastNotifiedCrashCount", crashCount).apply());
            }
            else {
                Dialog.displayDialog(activity,
                        activity.getResources().getString(R.string.title_decoding_error),
                        activity.getResources().getString(R.string.message_decoding_error),
                        () -> prefs.edit().putInt("LastNotifiedCrashCount", crashCount).apply());
            }
        }
    }

    public static void displayQuitConfirmationDialog(Activity parent, final Runnable onYes, final Runnable onNo) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE && onYes != null) onYes.run();
            else if (which == DialogInterface.BUTTON_NEGATIVE && onNo != null) onNo.run();
        };

        new AlertDialog.Builder(parent)
                .setMessage(parent.getResources().getString(R.string.applist_quit_confirmation))
                .setPositiveButton(parent.getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(parent.getResources().getString(R.string.no), dialogClickListener)
                .show();
    }

    public static void displayDeletePcConfirmationDialog(Activity parent, ComputerDetails computer, final Runnable onYes, final Runnable onNo) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE && onYes != null) onYes.run();
            else if (which == DialogInterface.BUTTON_NEGATIVE && onNo != null) onNo.run();
        };

        new AlertDialog.Builder(parent)
                .setMessage(parent.getResources().getString(R.string.delete_pc_msg))
                .setTitle(computer.name)
                .setPositiveButton(parent.getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(parent.getResources().getString(R.string.no), dialogClickListener)
                .show();
    }

    public static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static void setStatusBarLightMode(@NonNull final Window window,
                                             final boolean isLightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            View decorView = window.getDecorView();
            int vis = decorView.getSystemUiVisibility();
            if (isLightMode) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(vis);
        }
    }
}
