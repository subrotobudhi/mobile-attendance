package com.percobaan.presensionlinesatu;


import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class DetektorEmulator {
    Context context;
    public DetektorEmulator(Context context){
        this.context=context;
    }

    private static boolean hasEth0Interface() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().equals("eth0"))
                    return true;
            }
        } catch (SocketException ex) {
        }
        return false;
    }

    private static boolean hasQemuCpuInfo() {
        try {
            BufferedReader cpuInfoReader = new BufferedReader(new FileReader("/proc/cpuinfo"));
            String line;
            while ((line = cpuInfoReader.readLine()) != null) {
                if (line.contains("Goldfish")||line.contains("Virtual")||line.contains("virtual")||line.contains("0000000000000000"))
                    return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static boolean hasQemuFile() {
        return new File("/sys/qemu_trace").exists()
                || new File("/dev/socket/genyd").exists()
                || new File("/dev/socket/baseband_genyd").exists()
                || new File("fstab.andy").exists()
                || new File("ueventd.andy.rc").exists()
                || new File("fstab.nox").exists()
                || new File("ueventd.nox.rc").exists();
//                || new File("/init.nox.rc").exists();
//                || new File("/init.goldfish.rc").exists();
//                || new File("/sys/qemu_trace").exists()
//                || new File("/system/bin/qemud").exists();

    }


    private static String getProp(Context ctx, String propName) {
        try {
            ClassLoader cl = ctx.getClassLoader();
            Class<?> klazz = cl.loadClass("android.os.properties");
            Method getProp = klazz.getMethod("get", String.class);
            Object[] params = {propName};
            return (String) getProp.invoke(klazz, params);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private boolean hasQemuBuildProps() {
        return "goldfish".equals(getProp(context, "ro.hardware"))
                || "ranchu".equals(getProp(context, "ro.hardware"))
                || "generic".equals(getProp(context, "ro.product.device"))
                || "1".equals(getProp(context, "ro.kernel.qemu"))
                || "0".equals(getProp(context, "ro.secure"));
    }

    private boolean isDebuggerConnected() {
        return Debug.isDebuggerConnected();
    }

    private boolean hasEmulatorBuildProp() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.toLowerCase().contains("andy")
                || Build.MODEL.toLowerCase().contains("andy")
                || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")|| Build.MANUFACTURER.toLowerCase().contains("geny")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.PRODUCT.contains("google_sdk") || Build.PRODUCT.contains("sdk")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.BOARD.contains("unknown")
                || Build.ID.contains("FRF91")
                || Build.MANUFACTURER.contains("unknown")
                || Build.SERIAL == null
//                || Build.getSerial() == null
                || Build.TAGS.contains("test-keys")
                || Build.USER.contains("android-build")
                || Build.BOARD.toLowerCase().contains("nox")
                || Build.BOOTLOADER.toLowerCase().contains("coolpad5890") //bootloader nox v6
                || Build.BOOTLOADER.toLowerCase().contains("nox")
                || Build.HARDWARE.toLowerCase().contains("nox")
                || Build.PRODUCT.toLowerCase().contains("nox")
                || Build.MODEL.toLowerCase().contains("nox")
                || Build.MODEL.toLowerCase().contains("droid4x")
                || Build.HARDWARE.toLowerCase().contains("droid4x")
                ;
    }

    private boolean hasEmulatorTelephonyProperty() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        return "Android".equals(tm.getNetworkOperatorName())
                || "51001".equals(tm.getNetworkOperatorName())
                || "T-Mobile".equals(tm.getNetworkOperatorName())
                || "NTT DOCOMO".equals(tm.getNetworkOperatorName())
                || "Android".equals(tm.getSimOperator())
                || "Corporation Tbk".equals(tm.getSimOperator())
                || "T-Mobile".equals(tm.getSimOperator())
                || "NTT DOCOMO".equals(tm.getSimOperator())
//                || "000000000000000".equals(tm.getDeviceId()) || tm.getDeviceId().matches("^0+$")
//                || tm.getLine1Number().startsWith("155552155")
//                || tm.getSubscriberId().endsWith("0000000000")
//                || "15552175049".equals(tm.getVoiceMailNumber())
                ;
    }


    boolean isEmulator() {
        return hasEth0Interface()
                || hasQemuCpuInfo()
                || hasQemuFile()
                || hasQemuBuildProps()
                || isDebuggerConnected()
                || hasEmulatorBuildProp()
                || hasEmulatorTelephonyProperty();
    }


}
