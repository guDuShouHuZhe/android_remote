package com.mycompany.application2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
  

public class MainActivity extends AppCompatActivity {
    private final int VERSION_CODES=1024;
    private TextureView texture;
    private Button bt;
    private EditText et;
    private String s="";
    //private int port;
    private Socket clien_socket;
    private ExecutorService es;
    private Bitmap bitmap;
    private static myhand  activity_hand;
    private  DataInputStream clien_input;
    private DataOutputStream clien_output;
    private int screen_w, screen_h;
    private RelativeLayout  rl;
    private WindowManager manager;
    private int  server_w,server_h,server_orientation;
    private int start_x1,start_y1;
    private double start_len;
    private float start_centerX,start_centerY;
    private boolean  is_connected=false;
    private boolean is_scale=false;
    private Matrix m;

    //private  boolean input_thread=false;

    private static class myhand extends Handler {
        /* WeakReference 弱引用防止内存泄露*/
        private WeakReference<MainActivity> main;

        public myhand(MainActivity main) {
            super(Looper.myLooper());
            this.main = new WeakReference<MainActivity>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity main=this.main.get();
            if (main != null) {

                if (msg.what == 1) {

                    main.drawBitmap(main.bitmap);

                } else if (msg.what == 3) {
                   if(main.texture.getParent()!=null)main. rl.removeView(main. texture);
                   main. bt.setVisibility(0);
                   main. et.setVisibility(0);
                   main. et.requestFocus();
                   Toast.makeText(main.getApplication(), msg.getData().getString("app"), 3000).show();

                } else if (msg.what == 2) {

                    Toast.makeText(main.getApplication(), msg.getData().getString("app"), 4000).show();

                } else if (msg.what == 4) {
                    //Toast.makeText(fws.getApplication(), msg.getData().getString("app"),4000).show();



                } else if (msg.what == 5) {

                }

            }
        }

    }
    public static void pHand(String app, String text, int what) {
        Message msg=activity_hand.obtainMessage();
        //Message msg=new Message();
        Bundle bd=new Bundle();
        bd.putString(app, text);
        //bd.putIntArray(app_int, p);
        msg.setData(bd);
        msg.what = what;
        activity_hand.sendMessage(msg);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {  
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); 
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //NotchScreenManager.getInstance().setDisplayInNotch(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics  outMetrics = new DisplayMetrics();
        manager = ((WindowManager)getSystemService(Context.WINDOW_SERVICE));
        manager.getDefaultDisplay().getRealMetrics(outMetrics);       
        /*getDefaultDisplay()在api30中已弃用*/        
        screen_w = outMetrics.widthPixels;
        screen_h = outMetrics.heightPixels;
        m=new Matrix();
        activity_hand = new myhand(this);
        rl = findViewById(R.id.activitymainRelativeLayout1);
        texture = new TextureView(this);

        texture.setLayoutParams(new RelativeLayout.LayoutParams(
                                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                               
        //texture= findViewById(R.id.activity_mainTextureView);
        et = findViewById(R.id.activity_mainEditText);
        bt = findViewById(R.id.activity1_bt);
        
           
        texture.setSurfaceTextureListener(new android.view.TextureView.SurfaceTextureListener(){
                //onSurfaceTextureAvailable()：在SurfaceTexture准备使用时调用。
                //onSurfaceTextureDestroyed()：当指定SurfaceTexture即将被销毁时调用。如果返回true，则调用此方法后，
                //表面纹理中不会发生渲染。如果返回false，则客户端需要调用release()。大多数应用程序应该返回true。
                // onSurfaceTextureSizeChanged()：当SurfaceTexture缓冲区大小更改时调用。
                // onSurfaceTextureUpdated()：当指定SurfaceTexture的更新时调用updateTexImage()。
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture p1, int p2, int p3) {

                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture p1, int p2, int p3) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture p1) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture p1) {
                }
            });
        et.addTextChangedListener(new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
                }

                @Override
                public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
                }

                @Override
                public void afterTextChanged(Editable p1) {
                    s=p1.toString();
                    
                }


            });

        bt.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View p1) {
                    if(texture.getParent()==null)rl.addView(texture);
                    bt.setVisibility(8);
                    et.setVisibility(8);
                     /*String s  =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                     .getAbsolutePath() + "/app.jpg";
                     final Bitmap b=BitmapFactory.decodeFile(s);
                     //Toast.makeText(getApplication(), s, 10000).show();
                     if (b != null) {
                   
                     new Handler().postDelayed(new Runnable(){

                     @Override
                     public void run() {
                         drawBitmap(b);
                     }
                     }, 50); 

                     }*/
                   
                    //if (!input_thread) {
                    
                    es = Executors.newSingleThreadExecutor();   
                    es.execute(
                            new Runnable(){

                                @Override
                                public void run() {
                                    try {  
                                        String uri=s.substring(0,s.indexOf(";"));
                                        String str = s.substring(s.indexOf(";")+1);
                                        int port=Integer.parseInt(str);
                                        //input_thread = true;
                                        clien_socket = new Socket(uri, port);
                                        clien_output = new DataOutputStream(
                                            clien_socket.getOutputStream());
                                        clien_input = new DataInputStream(
                                            clien_socket.getInputStream());                                     

                                        if (clien_socket.isConnected()) {
                                            is_connected=true;
                                            pHand("app", "连接成功",  2);
                                        } else {
                                            is_connected=false;
                                            pHand("app", "连接失败",  2);
                                        }
                                        //clien_socket.setOOBInline(false);//指将外带的数据是否插入到正常的数据流
                                          while (true){    
                                            int len = clien_input.readInt();
                                            if(len==0)continue;
                                            server_orientation=clien_input.readInt(); 
                                            server_w=clien_input.readInt();
                                            server_h=clien_input.readInt();
                                            // clien_input.skip(4);
                                            byte[] bytes = new byte[len];                   
                                            clien_input.readFully(bytes, 0, len);
                                            ByteArrayInputStream bit_byte=new ByteArrayInputStream(bytes);
                                            bitmap = BitmapFactory.decodeStream(bit_byte);
                                            if(bitmap!=null){
                                                 activity_hand.sendEmptyMessage(1);                                                                                  
                                            }
                                          }
                          
                                        
                                    } catch (IOException e) {                                        
                                        pHand("app", "网络异常", 3);                                                                        
                                        try {
                                            
                                            if(clien_input!=null)   clien_input.close();
                                            if(clien_output !=null)clien_output.close();
                                            if (clien_socket!=null )clien_socket.close();
                                         
                                        } catch (IOException o) {}
                                        is_connected=false;
                                        //try {
                                            //
                                            //clien_socket.close();
                                        //} catch (IOException o) {}
                                    } //catch (InterruptedException e){}

                                }



                            });
                    //}

                }
            });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 适配android11读写权限
            if (Environment.isExternalStorageManager()) {
                //已获取android读写权限
            } else {//清单文件还要添加android.permission.MANAGE_EXTERNAL_STORAGE  
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, VERSION_CODES);
            }

        }
        rl.setOnTouchListener(new View.OnTouchListener(){

                @Override
                public boolean onTouch  (View p1, MotionEvent p2) {
                  
                  if(is_connected&&texture.getParent()!=null){
                    switch(/*p2.getAction()&MotionEvent.ACTION_MASK*/p2.getActionMasked()){
                        case  MotionEvent.ACTION_DOWN:
                         
                            start_x1=(int)p2.getRawX();
                            start_y1=(int)p2.getRawY();
                            return true;
                            
                        case MotionEvent.ACTION_MOVE:
                            if(p2.getPointerCount()==2){
                                float dx=p2.getRawX(1)-p2.getRawX(0); 
                                float dy=p2.getRawY(1)-p2.getRawY(0); 
                                double d_len= Math.sqrt(dx*dx+dy*dy);   
                               //if(Math.abs(d_len-tmp_len)>10){
                                texture.setPivotX(start_centerX);
                                texture.setPivotY(start_centerY);                           
                                texture.setScaleX((float)(d_len/start_len));
                                texture.setScaleY((float)(d_len/start_len));
                                  
                                  //tmp_len=d_len;                             
                               //}
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if(!is_scale){
                               long t=p2.getEventTime()- p2.getDownTime();
                               final int end_x1=(int)p2.getRawX();
                               final int end_y1=(int)p2.getRawY();
                               final  int duration;
                               if(t>600&&start_x1==end_x1&&start_y1==start_y1){
                                  duration=1000;
                               }else duration=100;   
                               if(is_connected){
                                 new Thread(){
                                   @Override
                                   public void run(){
                                     try {
                                        if(server_orientation==0){
                                            clien_output.writeInt(duration);
                                            clien_output.writeInt((start_x1*server_w)/screen_w);                         
                                            clien_output.writeInt((start_y1*server_h)/screen_h);
                                            clien_output.writeInt((end_x1*server_w)/screen_w);
                                            clien_output.writeInt((end_y1*server_h)/screen_h);
                                        }else if(server_orientation==1){
                                            clien_output.writeInt(duration);
                                            clien_output.writeInt((start_y1*server_w)/screen_h);  
                                            clien_output.writeInt(server_h- (start_x1*server_h)/screen_w);                         
                                            clien_output.writeInt((end_y1*server_w)/screen_h);
                                            clien_output.writeInt( server_h- (end_x1*server_h)/screen_w);
                                      
                                        }else if(server_orientation==3){
                                            clien_output.writeInt(duration);
                                            clien_output.writeInt(server_w-(start_y1*server_w)/screen_h );                         
                                            clien_output.writeInt((start_x1*server_h)/screen_w );
                                            clien_output.writeInt(server_w-(end_y1*server_w)/screen_h );
                                            clien_output.writeInt((end_x1*server_h)/screen_w);
                                        }
                                     } catch (IOException e) {
                                        pHand("app","命令发送失败",2);
                                     }
                                   }
                                 }.start();
                               }
                            }else {
                                is_scale=false;
                                texture.setPivotX(start_centerX);
                                texture.setPivotY(start_centerY);
                                texture.setScaleX(1);
                                texture.setScaleY(1);
                                
                            }
                            break;
                            case MotionEvent.ACTION_POINTER_DOWN:
                                if(p2.getPointerCount()==2){
                                    float dxx=p2.getRawX(1)-p2.getRawX(0); 
                                    float dyy=p2.getRawY(1)-p2.getRawY(0); 
                                    start_len= Math.sqrt(dxx*dxx+dyy*dyy);                                 
                                    start_centerX=(p2.getRawX(1)+p2.getRawX(0))/2;
                                    start_centerY=(p2.getRawY(1)+p2.getRawY(0))/2;
                                    is_scale=true;
                                   
                                }
                               
                                break;
                        
                    }
                  }
                  return false;
                }

            
        });
        // Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);       
        // Cursor cursor = getContentResolver().query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, null, null, null);
        //  s = cursor.getString(cursor.getColumnIndex(MediaStore.Downloads.));

    }

    @Override
    public void onBackPressed() {
        if (bt.getVisibility()==0) {
            super.onBackPressed();
        } else { 
            try {
                if(clien_input!=null)  clien_input.close();
                if(clien_output!=null)clien_output.close();
                if(clien_socket!=null)clien_socket.close();
                
            } catch (IOException e) {}
            is_connected=false;
            if(es!=null)es.shutdownNow();
            if(texture.getParent()!=null)rl.removeView(texture);
            bt.setVisibility(0);
            et.setVisibility(0);
            et.requestFocus();
        }
    }

    /*@Override
     public boolean OnKeyListener(){

     }*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //if(is_connected){
             if(clien_input!=null)   clien_input.close();
             if(clien_output!=null)clien_output.close();
             //   is_connected=false;
            //}
            if(clien_socket!=null)clien_socket.close();
        } catch (IOException e) {}
        if(es!=null)es.shutdownNow();
        
    }

    private  void drawBitmap(Bitmap bitmap) {

        //Bitmap bitma=BitmapFactory.decodeResource( (new ByteArrayInputStream(b));
        Canvas canvas = texture.lockCanvas(new Rect(0, 0, screen_w, screen_h));//锁定画布
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);// 清空画布
        //Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        switch(server_orientation){
           
           case 1:
           m.setRotate(90);
               // Toast.makeText(getApplication(), ""+bitmap.getWidth(), Toast.LENGTH_SHORT).show();    
           bitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);
            // Toast.makeText(getApplication(), ""+bitmap.getWidth(), Toast.LENGTH_SHORT).show();
           break;
           case 2:
           m.setRotate(180);
           bitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);     
           break;
           case 3:
           m.setRotate(270);
           //bitmap.getGenerationId()
           bitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);
           break;
        }
        //Rect res = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        //Toast.makeText(getApplication(), ""+bitmap.getHeight(), Toast.LENGTH_SHORT).show();
        Rect dst = new Rect(0, 0, screen_w, screen_h);
        canvas.drawBitmap(bitmap, null, dst, new Paint());//将bitmap画到画布上
        texture. unlockCanvasAndPost(canvas);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意申请权限
            } else {
                // 用户拒绝申请权限
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VERSION_CODES && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                //已获取android读写权限

            } else {
                //存储权限获取失败
                Toast.makeText(this, "未获取权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void newFile(String name) {
        String filePath = Environment.getExternalStoragePublicDirectory("").toString() + name/*"/text1.txt"*/;
        File  file = new File(filePath);//创建文件路径
        try {if (!file.exists())
                file.createNewFile();//仅仅创建文件，mkdir创建目录
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}
