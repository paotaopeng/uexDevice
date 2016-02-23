package org.zywx.wbpalmstar.plugin.uexdevice;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.plugin.uexdevice.vo.FunctionDataVO;
import org.zywx.wbpalmstar.plugin.uexdevice.vo.ResultIsEnableVO;
import org.zywx.wbpalmstar.plugin.uexdevice.vo.ResultSettingVO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;

public class EUExDevice extends EUExBase {

	public static final String tag = "uexDevice_";
	public static final String CALLBACK_NAME_DEVICE_GET_INFO = "uexDevice.cbGetInfo";
    public static final String CALLBACK_NAME_DEVICE_SCREEN_CAPTURE = "uexDevice.cbScreenCapture";
    public static final String CALLBACK_NAME_DEVICE_GET_VOLUME = "uexDevice.cbGetVolume";
    public static final String CALLBACK_NAME_DEVICE_GET_BRIGHTNESS = "uexDevice.cbGetScreenBrightness";

    public static final String onFunction_orientationChange = "uexDevice.onOrientationChange";

	public static final int F_DEVICE_INFO_ID_ORIENTATION_PORTRAIT = 1; // 竖屏
	public static final int F_DEVICE_INFO_ID_ORIENTATION_LANDSCAPE = 2;// 横屏
	public static final int F_JV_CONNECT_UNREACHABLE = -1;
	public static final int F_JV_CONNECT_WIFI = 0;
	public static final int F_JV_CONNECT_3G = 1;
	public static final int F_JV_CONNECT_GPRS = 2;
	public static final int F_JV_CONNECT_4G = 3;
	public static final int F_JV_CONNECT_UNKNOWN = 4;

	private Vibrator m_v;

	private ResoureFinder finder;

	public EUExDevice(Context context, EBrowserView inParent) {
		super(context, inParent);
		finder = ResoureFinder.getInstance(context);
	}

	/**
	 * params[0]--->震动毫秒数
	 * 
	 * @param params
	 */
	public void vibrate(String[] params) {
		if (params.length > 0) {
			try {
				if (null == m_v) {
					m_v = (Vibrator) mContext
							.getSystemService(Service.VIBRATOR_SERVICE);
				}
				m_v.vibrate(Integer.parseInt(params[0]));
			} catch (SecurityException e) {
				Toast.makeText(mContext,
						finder.getString("no_permisson_declare"),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void cancelVibrate(String[] params) {
		if (null != m_v) {
			try {
				m_v.cancel();
			} catch (SecurityException e) {
				;
			}
		}
	}

	@Override
	public boolean clean() {
		cancelVibrate(null);
		m_v = null;
		return true;
	}

	private static final int F_C_WINDOWSIZE = 18;
	private static final int F_C_SIM_SERIALNUMBER = 19;
	private static final int F_C_SOFT_TOKEN = 20;

	private static final String F_JK_WINDOWSIZE = "resolutionRatio";
	private static final String F_JK_SIM_SERIALNUMBER = "simSerialNumber";
	private static final String  F_JK_SOFT_TOKEN = "softToken";
	/**
	 * params[0]-->InfoID
	 * 
	 * @param params
	 */
	public void getInfo(String[] params) {
		if (params.length > 0) {
			JSONObject jsonObj = new JSONObject();
			String outKey = null;
			String outStr = null;
			int id = Integer.valueOf(params[0]);
			switch (id) {
			case EUExCallback.F_C_CPU:
				outKey = EUExCallback.F_JK_CPU;
				outStr = getCPUFrequency();
				break;
			case EUExCallback.F_C_OS:
				outKey = EUExCallback.F_JK_OS;
				outStr = "Android " + Build.VERSION.RELEASE;
				break;
			case EUExCallback.F_C_MANUFACTURER:
				outKey = EUExCallback.F_JK_MANUFACTURER;
				outStr = Build.MANUFACTURER;
				break;
			case EUExCallback.F_C_KEYBOARD:
				outKey = EUExCallback.F_JK_KEYBOARD;
				outStr = getKeyBoardType();
				break;
			case EUExCallback.F_C_BLUETOOTH:
				outKey = EUExCallback.F_JK_BLUETOOTH;
				outStr = getBlueToothSupport();
				break;
			case EUExCallback.F_C_WIFI:
				outKey = EUExCallback.F_JK_WIFI;
				outStr = getWIFISupport();
				break;
			case EUExCallback.F_C_CAMERA:
				outKey = EUExCallback.F_JK_CAMERA;
				outStr = getCameraSupport();
				break;
			case EUExCallback.F_C_GPS:
				outKey = EUExCallback.F_JK_GPS;
				outStr = getGPSSupport();
				break;
			case EUExCallback.F_C_GPRS:
				outKey = EUExCallback.F_JK_GPRS;
				outStr = getMobileDataNetworkSupport();
				break;
			case EUExCallback.F_C_TOUCH:
				outKey = EUExCallback.F_JK_TOUCH;
				outStr = getTouchScreenType();
				break;
			case EUExCallback.F_C_IMEI:
				outKey = EUExCallback.F_JK_IMEI;
				outStr = getDeviceIMEI();
				break;
			case EUExCallback.F_C_DEVICE_TOKEN:
				outKey = EUExCallback.F_JK_DEVICE_TOKEN;
				outStr = getDeviceToken();
				break;
			case EUExCallback.F_C_CONNECT_STATUS:
				outKey = EUExCallback.F_JK_CONNECTION_STATUS;
				outStr = String.valueOf(getNetworkStatus());
				break;
			case EUExCallback.F_C_REST_DISK_SIZE:
				outKey = EUExCallback.F_JK_REST_DISK_SIZE;
				outStr = getRestDiskSize();
				break;
			case EUExCallback.F_C_MOBILE_OPERATOR_NAME:
				outKey = EUExCallback.F_JK_MOBILE_OPERATOR_NAME;
				outStr = getMobileOperatorName();
				break;
			case EUExCallback.F_C_MAC_ADDRESS:
				outKey = EUExCallback.F_JK_MAC_ADDRESS;
				outStr = getMacAddress();
				break;
			case EUExCallback.F_C_MODEL:
				outKey = EUExCallback.F_JK_MODEL;
				outStr = Build.MODEL;
				break;
			case F_C_WINDOWSIZE:
				outKey = F_JK_WINDOWSIZE;
				DisplayMetrics dm = new DisplayMetrics();
				((Activity) mContext).getWindowManager().getDefaultDisplay()
						.getMetrics(dm);
				outStr = dm.widthPixels + "*" + dm.heightPixels;
				break;
			case F_C_SIM_SERIALNUMBER:
				outKey = F_JK_SIM_SERIALNUMBER;
				outStr =getSimSerialNumber();
				break;
			case F_C_SOFT_TOKEN:
				outKey = F_JK_SOFT_TOKEN;
				outStr = null;
				break;
			default:
				break;
			}
			try {
				jsonObj.put(outKey, outStr);
				jsCallback(CALLBACK_NAME_DEVICE_GET_INFO, 0,
						EUExCallback.F_C_JSON, jsonObj.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 获得设备CPU的频率
	 * 
	 * @return
	 */
	private String getCPUFrequency() {
		String result = "";
		LineNumberReader isr = null;
		try {
			Process pp = Runtime
					.getRuntime()
					.exec("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
			isr = new LineNumberReader(new InputStreamReader(
					pp.getInputStream()));
			String line = isr.readLine();
			if (line != null && line.length() > 0) {
				try {
					result = Integer.parseInt(line.trim()) / 1000 + "MHZ";
				} catch (Exception e) {
					BDebug.log("EUExDeviceInfo---getCPUFrequency()---NumberFormatException ");
				}
			} else {
				result = "0";
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	// 读取IMEI需要在manifest中加入<uses-permission
	// android:name="android.permission.READ_PHONE_STATE"/>权限
	/**
	 * 获得设备的IMEI号
	 */
	private String getDeviceIMEI() {
		String imei = "unknown";
		try {
			TelephonyManager telephonyManager = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager != null
					&& telephonyManager.getDeviceId() != null) {
				imei = telephonyManager.getDeviceId();
			}
		} catch (SecurityException e) {
			Toast.makeText(mContext, finder.getString("no_permisson_declare"),
					Toast.LENGTH_SHORT).show();
		}
		return imei;
	}
/**
 * 获得设备的序列号
 * @return
 */
	private String getSimSerialNumber(){
		String serialNumber = "unknown";
		try {
			TelephonyManager telephonyManager = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager != null
					&& telephonyManager.getSimSerialNumber() != null) {
				serialNumber = telephonyManager.getSimSerialNumber();
			}
		} catch (SecurityException e) {
			Toast.makeText(mContext, finder.getString("no_permisson_declare"),
					Toast.LENGTH_SHORT).show();
		}
		return serialNumber;
	}
	private String getRestDiskSize() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return "0";
		}
		String sdPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		BDebug.d(tag, "getRestDiskSize() sdPath:" + sdPath);
		StatFs fs = new StatFs(sdPath);
		final long unit = fs.getBlockSize();
		final long avaliable = fs.getAvailableBlocks();
		final long size = unit * avaliable;
		return String.valueOf(size);
	}

	/**
	 * 检测是否支持触屏 (0---> 不支持触屏) (1--->支持触屏)
	 * 
	 * @return
	 */
	private String getTouchScreenType() {
		String type = "0";// 未定义
		switch (mContext.getResources().getConfiguration().touchscreen) {
		case Configuration.TOUCHSCREEN_FINGER:// 电容屏
			type = "1";
			break;
		case Configuration.TOUCHSCREEN_STYLUS:// 电阻屏
			type = "1";
			break;
		case Configuration.TOUCHSCREEN_NOTOUCH:// 非触摸屏
			type = "0";
			break;
		}
		return type;
	}

	public void onConfigurationChanged(int inMode) {
		String js = SCRIPT_HEADER + "if(" + onFunction_orientationChange + "){"
				+ onFunction_orientationChange + "(" + inMode + ");}";
		mBrwView.loadUrl(js);
	}

	/**
	 * 检测是否支持蓝牙 (0---> 不支持蓝牙) (1--->支持蓝牙)
	 * 
	 * @return
	 */
	private String getBlueToothSupport() {
		String supported = "0";
		// android从2.0(API level=5)开始提供蓝牙API，getDefaultAdapter返回适配器不为空则证明支持蓝牙
		try {
			if (Build.VERSION.SDK_INT >= 5) {
				final Class<?> btClass = Class
						.forName("android.bluetooth.BluetoothAdapter");
				final Method method = btClass.getMethod("getDefaultAdapter",
						new Class[] {});
				if (method.invoke(btClass, new Object[] {}) != null) {
					supported = "1";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return supported;
	}

	/**
	 * 检测是否支持WIFI
	 * 
	 * @return
	 */
	private String getWIFISupport() {
		String supported = "0";// 默认不支持
		if ((WifiManager) mContext.getSystemService(Context.WIFI_SERVICE) != null) {// 支持WIFI
			supported = "1";
		}
		return supported;
	}

	/**
	 * 检测是否支持摄像头 (0---> 不支持摄像头) (1--->支持摄像头)
	 * 
	 * @return
	 */
	private String getCameraSupport() {
		String support = "0";// 默认不支持摄像头
		try {
			Camera camera = Camera.open();
			if (camera != null) {// 支持摄像头
				support = "1";
				camera.release();
			}
		} catch (Exception e) {
			Toast.makeText(mContext, finder.getString("no_permisson_declare"),
					Toast.LENGTH_SHORT).show();
		}

		return support;
	}

	/**
	 * 检测是否支持GPS定位 (0---> 不支持GPS定位) (1--->支持GPS定位)
	 * 
	 * @return
	 */
	private String getGPSSupport() {
		String support = "0";// 默认不支持GPS
		try {
			LocationManager locationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
			if (locationManager != null) {// 支持GPS
				support = "1";
			}
		} catch (SecurityException e) {
			Toast.makeText(mContext, finder.getString("no_permisson_declare"),
					Toast.LENGTH_SHORT).show();
		}

		return support;
	}

	/**
	 * 检测移动数据网络是否打开 (0---> 移动数据网络未打开) (1--->移动数据网络已打开)
	 * 
	 * @return
	 */
	private String getMobileDataNetworkSupport() {
		String support = "0";// 默认未打开数据网络
		try {
			ConnectivityManager cm = (ConnectivityManager) mContext
					.getApplicationContext().getSystemService(
                            Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				NetworkInfo info = cm.getActiveNetworkInfo();
				if (info != null
						&& info.getType() == ConnectivityManager.TYPE_MOBILE) {
					support = "1";
				}
			}
		} catch (SecurityException e) {
			Toast.makeText(mContext, finder.getString("no_permisson_declare"),
					Toast.LENGTH_SHORT).show();
		}
		return support;
	}

	private int getNetworkStatus() {
		int status = F_JV_CONNECT_UNREACHABLE;
		try {
			ConnectivityManager cm = (ConnectivityManager) mContext
					.getApplicationContext().getSystemService(
                            Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				NetworkInfo info = cm.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					switch (info.getType()) {
					case ConnectivityManager.TYPE_MOBILE:
						TelephonyManager telephonyManager = (TelephonyManager) mContext
								.getSystemService(Context.TELEPHONY_SERVICE);
						switch (telephonyManager.getNetworkType()) {
						case TelephonyManager.NETWORK_TYPE_1xRTT:
						case TelephonyManager.NETWORK_TYPE_CDMA:
						case TelephonyManager.NETWORK_TYPE_EDGE:
						case TelephonyManager.NETWORK_TYPE_GPRS:
							status = F_JV_CONNECT_GPRS;
							break;
						case TelephonyManager.NETWORK_TYPE_EVDO_0:
						case TelephonyManager.NETWORK_TYPE_EVDO_A:
						case TelephonyManager.NETWORK_TYPE_HSDPA:
						case TelephonyManager.NETWORK_TYPE_HSPA:
						case TelephonyManager.NETWORK_TYPE_HSUPA:
						case TelephonyManager.NETWORK_TYPE_UMTS:
							status = F_JV_CONNECT_3G;
							break;
						case TelephonyManager.NETWORK_TYPE_LTE:
						case TelephonyManager.NETWORK_TYPE_EHRPD:
						case TelephonyManager.NETWORK_TYPE_EVDO_B:
						case TelephonyManager.NETWORK_TYPE_HSPAP:
						case TelephonyManager.NETWORK_TYPE_IDEN:
						case TelephonyManager.NETWORK_TYPE_UNKNOWN:
							status = F_JV_CONNECT_4G;
							break;
						default:
							status = F_JV_CONNECT_UNKNOWN;
							break;
						}
						break;
					case ConnectivityManager.TYPE_WIFI:
						status = F_JV_CONNECT_WIFI;
						break;
					}
				}
			}
		} catch (SecurityException e) {
			Toast.makeText(mContext, finder.getString("no_permisson_declare"),
					Toast.LENGTH_SHORT).show();
		}
		return status;
	}

	/**
	 * 获取电信运营商的名称
	 */
	private String getMobileOperatorName() {
		String name = "unKnown";
		TelephonyManager telephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
			// IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber
			// Identification
			// Number）是区别移动用户的标志，
			// 储存在SIM卡中，可用于区别移动用户的有效信息。
			// IMSI由MCC、MNC组成，
			// 其中MCC为移动国家号码，由3位数字组成唯一地识别移动客户所属的国家，我国为460；
			// MNC为网络id，由2位数字组成, 用于识别移动客户所归属的移动网络，中国移动为00和02，中国联通为01,中国电信为03
			String imsi = telephonyManager.getNetworkOperator();
			if (imsi.equals("46000") || imsi.equals("46002")) {
				name = "中国移动";
			} else if (imsi.equals("46001")) {
				name = "中国联通";
			} else if (imsi.equals("46003")) {
				name = "中国电信";
			} else {
				// 其他电信运营商直接显示其名称，一般为英文形式
				name = telephonyManager.getSimOperatorName();
			}
		}
		return name;
	}

	/**
	 * 获取本地mac地址 <uses-permission
	 * android:name="android.permission.ACCESS_WIFI_STATE"/>权限
	 * 
	 * @return
	 */
	private String getMacAddress() {
		String macAddress = "unKnown";
		WifiManager wifiMgr = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiMgr != null) {
			WifiInfo info = wifiMgr.getConnectionInfo();
			if (null != info) {
				macAddress = info.getMacAddress();
			}
		}
		return macAddress;
	}

	private String getDeviceToken() {
		SharedPreferences preferences = mContext.getSharedPreferences("app",
				Context.MODE_WORLD_READABLE);
		return preferences.getString("softToken", null);
	}

	private String getKeyBoardType() {
		// public static final int KEYBOARD_UNDEFINED = 0;
		// public static final int KEYBOARD_NOKEYS = 1;
		// public static final int KEYBOARD_QWERTY = 2;
		// public static final int KEYBOARD_12KEY = 3;
		String type = null;
		switch (mContext.getResources().getConfiguration().keyboard) {
		case Configuration.KEYBOARD_12KEY:
			type = "1";// 普通键盘,0-9,*,#
			break;
		case Configuration.KEYBOARD_QWERTY:
			type = "1";// QWERTY标准全键盘
			break;
		case Configuration.KEYBOARD_NOKEYS:
			type = "0";// 不支持键盘
			break;
		case Configuration.KEYBOARD_UNDEFINED:
			type = "0";// 未定义
			break;
		}
		return type;
	}

	public void screenCapture(final String[] params) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleScreenCapture(params);
            }
        });
    }

    public void handleScreenCapture(String [] params) {
        if (params == null || params.length < 1) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        double quality = 1.0;
        try {
            quality = Double.parseDouble(params[0]);
        } catch (NumberFormatException e) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }


        WindowManager windowManager = ((Activity)mContext).getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        //获取屏幕
        View decorview = ((Activity)mContext).getWindow().getDecorView();
        decorview.setDrawingCacheEnabled(true);
        Bitmap bitmap = decorview.getDrawingCache();
        File dir = new File(Environment.getExternalStorageDirectory(),
                "image_temp");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filepath = dir.getAbsolutePath() + File.separator + new Date().getTime() + ".jpg" ;
        File file = new File(filepath);

        FileOutputStream fos = null;
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
            if (null != fos) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, (int) (quality * 100), fos);
                fos.flush();
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("savePath", file.getAbsolutePath());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String js = SCRIPT_HEADER + "if(" + CALLBACK_NAME_DEVICE_SCREEN_CAPTURE + "){"
                    + CALLBACK_NAME_DEVICE_SCREEN_CAPTURE + "('" + jsonObject.toString() + "');}";
            onCallback(js);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //听筒和外音的切换
    public void setAudioCategory(final String [] params) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleSetAudioCategory(params);
            }
        });
    }
    public void handleSetAudioCategory(String [] params) {
        if (params == null || params.length < 1) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        //0 扩音器  1 听筒
        int status = 0;
        try {
            status = Integer.parseInt(params[0]);
            if (status != 1) {
                status = 0;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        AudioManager am = (AudioManager) ((Activity)mContext).getSystemService(Context.AUDIO_SERVICE);
        if (status == 0) {
			am.setMode(AudioManager.MODE_NORMAL);
		} else {
            am.setMode(AudioManager.MODE_IN_CALL);

        }
    }

    public void setVolume(final String [] params) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleSetVolume(params);
            }
        });
    }
    public void handleSetVolume(String [] params) {
        if (params == null || params.length < 1) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        double value = 0.0;
        try {
            value = Double.parseDouble(params[0]);
        } catch (NumberFormatException e) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (value < 0 || value > 1) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        AudioManager am = (AudioManager) ((Activity)mContext).getSystemService(Context.AUDIO_SERVICE);
        int maxValume = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        am.setStreamVolume(AudioManager.STREAM_SYSTEM, (int) (maxValume * value), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    //获取音量大小,返回值的区间在0--1
    public void getVolume(final String [] params) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleGetVolume();
            }
        });
    }
    public void handleGetVolume() {
        AudioManager am = (AudioManager) ((Activity)mContext).getSystemService(Context.AUDIO_SERVICE);
        int currentVolumn = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int maxValume = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);

        double value = currentVolumn * 1.0 / maxValume;
        //格式化
        BigDecimal bigDecimal = new BigDecimal(value);
        double result = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("volume", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String js = SCRIPT_HEADER + "if(" + CALLBACK_NAME_DEVICE_GET_VOLUME + "){"
                + CALLBACK_NAME_DEVICE_GET_VOLUME + "('" + jsonObject.toString() + "');}";
        onCallback(js);
    }

    //屏幕常亮控制 0 取消常量，  1 代表常量
    public void setScreenAlwaysBright(final String [] params) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleSetScreenAlwaysBright(params);
            }
        });
    }

    public void handleSetScreenAlwaysBright(String [] params) {
        if (params == null || params.length < 1) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        int status = 0;
        try {
            status = Integer.parseInt(params[0]);
            if (status != 1) {
                status = 0;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Activity.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        wakeLock.setReferenceCounted(false);
        if (status == 1) {
            wakeLock.acquire();
        } else {
            wakeLock.release();
        }
    }

    //屏幕亮度控制
    public void setScreenBrightness(final String [] params) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleSetScreenBrightness(params);
            }
        });
    }

    public void handleSetScreenBrightness(String [] params) {
        if (params == null || params.length < 1) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        float brightness = 0;
        try {
            brightness = Float.parseFloat(params[0]);
        } catch (NumberFormatException e) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (brightness < 0 || brightness > 1) {
            Toast.makeText(mContext, finder.getString("plugin_uexDevice_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }

        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.screenBrightness = brightness;
        ((Activity) mContext).getWindow().setAttributes(lp);
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) (brightness * 255));
    }

    public void getScreenBrightness(final String [] params) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleGetScreenBrightness();
            }
        });
    }

    public void handleGetScreenBrightness () {
        int screenBrightness = 255;
        try {
            screenBrightness = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        double value = screenBrightness * 1.0 / 255;
        //格式化
        BigDecimal bigDecimal = new BigDecimal(value);
        double result = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("brightness", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String js = SCRIPT_HEADER + "if(" + CALLBACK_NAME_DEVICE_GET_BRIGHTNESS + "){"
                + CALLBACK_NAME_DEVICE_GET_BRIGHTNESS + "('" + jsonObject.toString() + "');}";
        onCallback(js);



    }
    public void openWiFiInterface(final String [] params) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleOpenWiFiInterface();
            }
        });
    }

    public void handleOpenWiFiInterface() {
        Intent wifiSettingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivity(wifiSettingsIntent);
    }

    public void isFunctionEnable(final String [] params) {
        ResultIsEnableVO resultVO = new ResultIsEnableVO();
        if (params == null || params.length < 1){
            errorCallback(0,0,"error params");
            return;
        }
        FunctionDataVO dataVO = DataHelper.gson.fromJson(params[0], FunctionDataVO.class);
        if (dataVO == null || TextUtils.isEmpty(dataVO.getSetting())){

        }else{
            String setting = dataVO.getSetting().toUpperCase();
            resultVO.setSetting(dataVO.getSetting());
            boolean result;
            if (setting.equals(JsConst.SETTING_GPS)){
                result = DeviceUtils.isGPSEnable(mContext);
            }else if(setting.equals(JsConst.SETTING_BLUETOOTH)){
                result = DeviceUtils.isBluetoothEnable();
            }else if (setting.equals(JsConst.SETTING_NETWORK)){
                int connectionStatus = getNetworkStatus();
                if (connectionStatus == F_JV_CONNECT_UNREACHABLE
                        || connectionStatus == F_JV_CONNECT_UNKNOWN){
                    result = false;
                }else {
                    result = true;
                }
            }else{
                errorCallback(0,0,"error params");
                return;
            }
            resultVO.setIsEnable(result);
        }
        callBackPluginJs(JsConst.CALLBACK_IS_FUNCTION_ENABLE, DataHelper.gson.toJson(resultVO));
    }

    public void openSetting(final String [] params) {
        ResultSettingVO resultVO = new ResultSettingVO();
        if (params == null || params.length < 1){
            errorCallback(0, 0, "error params");
            return;
        }
        FunctionDataVO dataVO = DataHelper.gson.fromJson(params[0], FunctionDataVO.class);
        String setting = null;
        if (!TextUtils.isEmpty(dataVO.getSetting())){
            setting = dataVO.getSetting().toUpperCase();
            resultVO.setSetting(dataVO.getSetting());
        }
        int errorCode = openSetting(setting);
        resultVO.setErrorCode(errorCode);
        callBackPluginJs(JsConst.CALLBACK_OPEN_SETTING, DataHelper.gson.toJson(resultVO));
    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

    private int openSetting(String setting){
        Intent intent = new Intent();
        if (TextUtils.isEmpty(setting)){
            intent.setAction(Settings.ACTION_SETTINGS);
        }else if (setting.equals(JsConst.SETTING_GPS)){
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        }else if (setting.equals(JsConst.SETTING_BLUETOOTH)){
            intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        }else if(setting.equals(JsConst.SETTING_NETWORK)){
            intent.setAction(Settings.ACTION_DATA_ROAMING_SETTINGS);
        }else{
            intent.setAction(Settings.ACTION_SETTINGS);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try
        {
            mContext.startActivity(intent);
        } catch(ActivityNotFoundException ex){
            ex.printStackTrace();
            return JsConst.ERROR_CODE_FAIL;
        }catch (Exception e){
            e.printStackTrace();
        }
        return JsConst.ERROR_CODE_SUCCESS;
    }
}
