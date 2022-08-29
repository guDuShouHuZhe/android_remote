package com.mycompany.application2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.mycompany.application2.R;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.text.style.MetricAffectingSpan;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String bt_start ="启动服务";
    private final String bt_exti="退出服务";
    
    private TextView tv,tv1;
	private Button  bt;
    private ExecutorService es,es_receiver;
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
	private ServerSocket server;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageRreader;
    private VirtualDisplay vd;
    private int w,h,dpi;
    private WindowManager manager;
    private Display dy;
    private boolean is_connected;
    private myhand hand ;
    private int t,x1,y1,x2,y2;
    private byte b1[],b2[];
    private int b1_b2_state=1;
    private int rotation;
    private Bitmap b;
    private DisplayMetrics outMerics;
    
    private static class myhand extends Handler{
        private WeakReference<MainActivity> main;
        
        public myhand(MainActivity main){
            super(Looper.myLooper());
            this.main=new WeakReference<MainActivity>(main);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity main=this.main.get();
            if(main!=null){
                if(msg.what==1){
                    main.tv.setText("客户端未连接 ⊙﹏⊙");
                }else if(msg.what==2){
                    main.tv.setText("客户端已连接 ⊙_⊙");
                }else if(msg.what==3){
                    main.tv.setText("服务线程已退出");
                    main.bt.setText(main.bt_start);
                }else if(msg.what==4){myAccessibity.myAc.mygesture(main.t,main.x1,main.y1,main.x2,main.y2);
                
                }else if(msg.what==5){
                    Toast.makeText(main.getApplication(), "测试", Toast.LENGTH_SHORT).show();
                }
            }
        }
        
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hand=new myhand(this);
        tv1=findViewById(R.id.activitymainTextView2);
		tv=findViewById(R.id.activitymainTextView1);
		bt=findViewById(R.id.activitymainButton1);
        manager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        dy=manager.getDefaultDisplay();
        outMerics=new DisplayMetrics();        
        dy.getRealMetrics(outMerics);
        w=outMerics.widthPixels;
        h=outMerics.heightPixels;
        dpi=outMerics.densityDpi;
		bt.setOnClickListener(this);
        requestScreenShot();
        
	}

    private void requestScreenShot() {
        mMediaProjectionManager=(MediaProjectionManager)
            getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if(mMediaProjectionManager!=null){
           startActivityForResult(
            mMediaProjectionManager.createScreenCaptureIntent()
            ,1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&&resultCode==RESULT_OK&&data!=null){
           mMediaProjection=mMediaProjectionManager.getMediaProjection(resultCode,data);
           mImageRreader=ImageReader.newInstance(w,h,PixelFormat.RGBA_8888,2);
           mMediaProjection.createVirtualDisplay("ScreenCapture"
           ,w,h,dpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
           ,mImageRreader.getSurface(),null,null);
           String ip=ipUtl.getIp(getApplicationContext());
           if(ip!=null) tv1.setText(ip+"端口:8888");
           mImageRreader.setOnImageAvailableListener( new ImageReader.OnImageAvailableListener(){

                    @Override
                    public void onImageAvailable(ImageReader p1) {
                   
                        if(is_connected){
                            rotation=dy.getRotation();
                            Image image=p1.acquireLatestImage();
                            if(image==null){return;}
                            int w=image.getWidth();
                            int h=image.getHeight();
                            final Image.Plane[] p=image.getPlanes();
                            final ByteBuffer buffer=p[0].getBuffer();
                            int pixelStride=p[0].getPixelStride();
                            int rowStride=p[0].getRowStride();
                            int rowPadding=rowStride-pixelStride*w;
                            b=Bitmap.createBitmap(w+rowPadding/pixelStride,h,Bitmap.Config.ARGB_8888);
                            b.copyPixelsFromBuffer(buffer);
                            image.close();
                            //b=b.createBitmap(b,0,0,w,h);
                            
                            Matrix m=new Matrix();
                            m.postScale(0.5f,0.5f);
                            if(rotation==0||rotation==2){
                              b=Bitmap.createBitmap(b,0,0,w,h,m,true);
                            }else if(rotation==1||rotation==3){
                            
                              b=Bitmap.createBitmap(b,0,656,w,607,m,true);
                             // b=Bitmap.createBitmap(b,0,656,w,1263,m,true);
                            }
                           // Toast.makeText(getApplication(), ""+w+"|"+h, Toast.LENGTH_SHORT).show();
                            ByteArrayOutputStream bi=new ByteArrayOutputStream();
                            b.compress(Bitmap.CompressFormat.JPEG,50,bi);
                            switch(b1_b2_state){
                                case 2:
                                   b1=bi.toByteArray();
                                break;
                                case 1:
                                   b2=bi.toByteArray();
                                        
                                break;
                            }
                            
                        }else {
                            Image im=p1.acquireLatestImage();
                           if(im!=null)im.close();
                            //Toast.makeText(getApplication(), ""+manager.getDefaultDisplay().getRotation(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    
               
             
           },null);
          // myAccessibity.myAc.mygesture(150,500,1080,500,500);
         }else {
             Toast.makeText(this, "请选择同意", Toast.LENGTH_SHORT).show();
             finish();
         }
    }
    
	@Override
	public void onClick(View p1) {
        
        String s=((Button)p1).getText().toString();
		if(s.equals(bt_start)){
                    tv.setText("等待连接……");
                    es=Executors.newSingleThreadExecutor();        
                    es.execute(new Runnable(){

                            @Override
                            public void run() {
                                try {                           
                                    while(true){    
                                       server=new ServerSocket(8888);
                                       socket=server.accept();    
                                       server.close();
                                       //server=null;
                                       output = new  DataOutputStream(socket.getOutputStream());
                                       input =new DataInputStream(socket.getInputStream());
                                       is_connected=socket.isConnected();
                                       hand.sendEmptyMessage(2);  
                                       es_receiver=Executors.newSingleThreadExecutor();
                                       es_receiver.execute(new myRunnable());
                                       while(true){                                                                                                   
                                         try{
                                             
                                             if(b1_b2_state==1){
                                                 if(b2!=null){
                                                    b1_b2_state=2;
                                                    output.writeInt(b2.length);
                                                    output.writeInt(rotation);
                                                    dy.getRealMetrics(outMerics);
                                                    output.writeInt(outMerics.widthPixels);
                                                    output.writeInt(outMerics.heightPixels);
                                                    output.write(b2);
                                                    output.flush();
                                                    b2=null;
                                                    
                                                 }else{
                                                    output.writeInt(0);
                                                    //continue;
                                                 }
                                             }else if(b1_b2_state==2){
                                                 if(b1!=null){
                                                    b1_b2_state=1;
                                                    output.writeInt(b1.length);
                                                    output.writeInt(rotation);
                                                    dy.getRealMetrics(outMerics);
                                                    output.writeInt(outMerics.widthPixels);
                                                    output.writeInt(outMerics.heightPixels);
                                                    output.write(b1);
                                                    output.flush();
                                                    b1=null;
                                                 }else{
                                                    output.writeInt(0);
                                                 }
                                             }
                                             
                                         }catch(IOException e) {
                                             b1=null;
                                             b2=null;
                                             b1_b2_state=1;
                                             is_connected=false;
                                             hand.sendEmptyMessage(1);
                                             if(output!=null&&!socket.isOutputShutdown()){output.close();}
                                             if(input!=null&&!socket.isInputShutdown()){input.close();}
                                             if(socket!=null&&!socket.isClosed()){socket.close();}                 
                                             
                                           
                                             //es_receiver.shutdownNow();
                                             if(es.isShutdown()){
                                                 hand.sendEmptyMessage(3);
                                                // hand.sendEmptyMessage(5);
                                                 return;}
                                             break;
                                         }
                                      
                                       }
                                    }
                                    //ouput_thread=false;
                                } catch (IOException e) {
                                    
                                    is_connected=false;
                                    hand.sendEmptyMessage(3);
                                    
                                    try {
                                        if(output!=null&&!socket.isOutputShutdown()){output.close();}
                                        if(input!=null&&!socket.isInputShutdown()){input.close();}
                                        if(socket!=null&&!socket.isClosed()){socket.close();}                 
                                        if(server!=null&&!server.isClosed()) {server.close();}
                                        
                                       
                                    } catch (IOException o) {o.printStackTrace();}
                                }
                            }
                            
                       
                   });
                   bt.setText(bt_exti);
                   
                       
                       
                   
          
         }else if(s.equals(bt_exti)){
            
             try {
                 if(output!=null&&!socket.isOutputShutdown()){output.close();}
                 if(input!=null&&!socket.isInputShutdown()){input.close();}
                 if(socket!=null&&!socket.isClosed()){socket.close();}                 
                 if(server!=null&&!server.isClosed()) {server.close();}
             } catch (IOException e) {}
             if(es!=null) es.shutdownNow();
             if(es_receiver!=null)es_receiver.shutdownNow();
             bt.setText(bt_start);
             
         }
			  
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if(is_connected){
        try {
             if(output!=null&&!socket.isOutputShutdown()){output.close();}
             if(input!=null&&!socket.isInputShutdown()){input.close();}
             if(socket!=null&&!socket.isClosed()){socket.close();}                 
             if(server!=null&&!server.isClosed()) {server.close();}
        } catch (IOException e) {}
        if(es!=null) es.shutdownNow();
        if(es_receiver!=null)es_receiver.shutdownNow();
    }
    
    /*private Bitmap getBitmap(){
        Image image=mImageRreader.acquireLatestImage();
        if(image==null){return null;}
        int w=image.getWidth();
        int h=image.getHeight();
        final Image.Plane[] p=image.getPlanes();
        final ByteBuffer buffer=p[0].getBuffer();
        int pixelStride=p[0].getPixelStride();
        int rowStride=p[0].getRowStride();
        int rowPadding=rowStride-pixelStride*w;
        Bitmap b=Bitmap.createBitmap(w+rowPadding/pixelStride,h,Bitmap.Config.ARGB_8888);
        b.copyPixelsFromBuffer(buffer);
        image.close();
        b=b.createBitmap(b,0,0,w,h);
        return b;
    }*/
    
    private class myRunnable implements Runnable{

      @Override
      public void run() {
        try {
           while(true){
            
                //input = new DataInputStream(socket.getInputStream());
                //if(is_connected){
                    t=input.readInt();
                    x1=input.readInt();
                    y1=input.readInt();
                    x2=input.readInt();
                    y2=input.readInt();
                    hand.sendEmptyMessage(4);
                  /*  String path=getExternalFilesDir(null).getPath()+"/jpg/";
                    File fl=new File (path);
                    if(!fl.exists()){
                       fl.mkdirs(); 
                    }
                    FileOutputStream f=new FileOutputStream(path+"jt.jpg");
                    b.compress(Bitmap.CompressFormat.JPEG,50,f);
                  */
            }
         } catch (IOException e) {
                //try {
                //    wait();
                //} catch (InterruptedException i) {
                //    Thread.currentThread().interrupt();
                //}
         }
        
      }
    }

   
}
