package org.rhona.piclibrary.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by DoctorKevin on 2017/5/8.
 */

public class AppTools {
    public final static String telRegex = "[1][34578]\\d{9}";
    public final static String emailRegex = "^[a-z0-9]+([._\\\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$";
    public static final String URLPATH = "((http|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";


    /**
     * @param context
     * @param value
     * @return
     */
    public static int dp2px(Context context, int value) {
        return (int) (context.getResources().getDisplayMetrics().density * value + 0.5f);
    }

    /**
     * @param context
     * @param value
     * @return
     */
    public static int px2dp(Context context, int value) {
        return (int) (value * 1f / context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int getWindowsWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getWindowsHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getPhoneDensity(Context context) {
        return (int) (context.getResources().getDisplayMetrics().density + 0.5f);
    }


    /**
     * 获取顶部状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }


    public static String getVersion(Activity activity) {
        PackageManager manager = activity.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
            String versionName = info.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length() - 1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断当前网络是否可用
     */
    public static boolean isNetOk(Activity act) {
        try {
            ConnectivityManager manager = (ConnectivityManager) act
                    .getApplicationContext().getSystemService(
                            Context.CONNECTIVITY_SERVICE);
            if (manager == null) {
                return false;
            }
            NetworkInfo networkinfo = manager.getActiveNetworkInfo();

            if (networkinfo == null || !networkinfo.isAvailable()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 匹配是否是手机号码
     *
     * @param number
     * @return
     */
    public static boolean isMobilePhone(String number) {
        return number.matches(telRegex);
    }

    /**
     * 匹配是否是邮箱地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        return Pattern.matches(emailRegex, email);
    }

    public static String getPath(Uri data, Activity activity) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(data, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;
            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(activity, "图片路径不合法", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return "";
            }
            return picturePath;
        }
        return null;
    }

    /**
     * 判断是否内存卡可用
     *
     * @return
     */
    public static boolean hasSdcard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 打开相册
     *
     * @param context
     * @param request
     */
    public static void openGallery(Activity context, int request) {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        context.startActivityForResult(intent, request);
    }

    /**
     *
     * @param activity
     * @param code
     * @param targetFile  图片绝对路径
     * @throws Exception
     */
    public static void openCamera(Activity activity, int code, File targetFile) throws Exception {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        // 判断存储卡是否可以用，可用进行存储
        if (AppTools.hasSdcard()) {
            if (targetFile != null) {
                File file = new File(targetFile.getParent());
                if (!file.exists()) {
                    file.mkdirs();
                }
            }
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".fileprovider",
                        targetFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(targetFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            activity.startActivityForResult(intent, code);
        }
    }

    /**
     * 判断EditText 是否为空或者是否满足字数的要求
     *
     * @param editText
     * @param count    字数限制要求
     * @return
     */
    public static boolean isEmptyEt(EditText editText, int count) {
        if (count == 0) {
            return TextUtils.isEmpty(editText.getText().toString().trim());
        } else {
            if (TextUtils.isEmpty(editText.getText().toString().trim()))
                return true;
            else {
                return editText.getText().toString().trim().length() < count;
            }
        }
    }
}
