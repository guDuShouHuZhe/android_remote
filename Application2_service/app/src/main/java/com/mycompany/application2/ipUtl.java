package com.mycompany.application2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ipUtl {
    public static String getIp(Context c) {
        if (Build.VERSION.SDK_INT < 23) {
            return getIPAddress(c);
        } else return getIP(c);
    }
    public static String getIP(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        String ip="";
        if (networkCapabilities == null) {
            //System.out.println("请打开网络");
            return "网络未打开";
        } //else 
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            ip="当前使用移动网络"+"\n"+"\n";
        }else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            ip="当前使用wifi网络"+"\n"+"\n";
        }
        //System.out.println("当前使用移动网络");
        try {
            
            //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {

                        if (inetAddress instanceof Inet4Address ) {
                            ip = ip + "ivp4:"+inetAddress.getHostAddress() + "\n";

                        }else if(inetAddress instanceof Inet6Address){
                            ip = ip + "ivp6:"+inetAddress.getHostAddress() + "\n";
                        }
                    }
                }
            }
            return ip;
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
        //}
        /* else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
         //System.out.println("当前使用WIFI网络");
         WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
         WifiInfo wifiInfo = wifiManager.getConnectionInfo();
         String ipAddress = intIP2StringIP(wifiInfo.getIpAddress() );//得到IPV4地址
         return ipAddress;
         }*/

    }//sdk23开始getActiveNetworkInfo()已经废弃
    public static String getIPAddress(Context context) {
        //Log.d("app","测试");
        ConnectivityManager cManager=(ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }


            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {

                //有线网络
                try {
                    // 获取本地设备的所有网络接口
                    Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                        .getNetworkInterfaces();
                    while (enumerationNi.hasMoreElements()) {
                        NetworkInterface networkInterface = enumerationNi.nextElement();
                        String interfaceName = networkInterface.getDisplayName();
                        Log.i("tag", "网络名字" + interfaceName);

                        // 如果是有线网卡
                        if (interfaceName.equals("eth0")) {
                            Enumeration<InetAddress> enumIpAddr = networkInterface
                                .getInetAddresses();

                            while (enumIpAddr.hasMoreElements()) {
                                // 返回枚举集合中的下一个IP地址信息
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                // 不是回环地址，并且是ipv4的地址
                                if (!inetAddress.isLoopbackAddress()
                                    && inetAddress instanceof Inet4Address) {
                                    Log.i("tag", inetAddress.getHostAddress() + "   ");

                                    return inetAddress.getHostAddress();
                                }
                            }
                        }
                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                }

            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }





    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
            ((ip >> 8) & 0xFF) + "." +
            ((ip >> 16) & 0xFF) + "." +
            (ip >> 24 & 0xFF);
    }


}
