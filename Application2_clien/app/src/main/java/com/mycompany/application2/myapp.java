package com.mycompany.application2;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
//import androidx.viewpager2.widget.ViewPager2;

public class myapp extends Application {

    //public int w,h,dpi;//定义全局变量，屏幕宽高和dpi
    

    //public boolean tv_select=false;
    //public static Context ct;
    //public static FloatWindowService fs;
    @Override
    public void onCreate() {
        super.onCreate();
        //ct=getApplicationContext ();

        Thread.setDefaultUncaughtExceptionHandler(new my_exception());

        //Toast.makeText(getApplicationContext(),"", Toast.LENGTH_SHORT).show();
    }
    public class my_exception implements Thread.UncaughtExceptionHandler{
        @Override
        public void uncaughtException(Thread p1, Throwable p2)  {
            new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        //toast的弹出需要依赖线程的消息循环
                        //而程序crash时主线程的loop已不够用
                        //因此无法正常弹出需要手动添加消息循环
                        Toast.makeText(getApplicationContext(), 
                                       "程序出错，即将退出", 3000).show();
                        Looper.loop();
                    }}).start();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {}
            saveCrashInfoToFile(p2);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);//退出虚拟机


        }

        /**
         * 保存错误信息到文件中
         * @param ex
         */
        private void saveCrashInfoToFile(Throwable ex) {
            //创建i/o流,Writer是抽象类，也是所有输入输出流基类
            Writer writer = new StringWriter();
            //创建输出流对象
            PrintWriter printWriter = new PrintWriter(writer);
            //打印堆栈追踪信息在输出流
            ex.printStackTrace(printWriter);
            //获取原因，无返回null
            Throwable exCause = ex.getCause();
            //循环获取和打印
            while (exCause != null) {
                exCause.printStackTrace(printWriter);
                exCause =exCause.getCause();
            }
            //关闭输出流
            printWriter.close();
            //获取系统时间
            long timeMillis = System.currentTimeMillis();
            //错误日志文件名称
            String fileName = "crash-" + timeMillis + ".log";
            //判断sd卡可正常使用
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //文件存储位置
                String path = getExternalFilesDir(null).getPath() + "/crash_logInfo/";
                File fl = new File(path);
                //创建文件夹
                if(!fl.exists()) {
                    fl.mkdirs();


                }
                try {
                    //创建文件输入输出流，写文件，关闭
                    FileOutputStream fileOutputStream = new FileOutputStream(path + fileName);
                    fileOutputStream.write(writer.toString().getBytes());
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
