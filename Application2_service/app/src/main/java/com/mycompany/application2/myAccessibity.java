package com.mycompany.application2;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class myAccessibity extends AccessibilityService {
    public static myAccessibity myAc;
    //public int i=0;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent p1) {
       /*if(i<5){
           Toast.makeText(getApplicationContext(), "tttyyyy", Toast.LENGTH_SHORT).show();
           i++;
       }*/
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        //Toast.makeText(getApplicationContext(), "ggggggg", Toast.LENGTH_SHORT).show();
        myAc=this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(getApplicationContext(), "ddfghjjjj", 10000).show();
    }
    
    public  void mygesture(int t,int x1,int y1,int x2,int y2){
        final  Path path = new Path();

        path.moveTo(x1,y1);

        path.lineTo(x2,y2);

        final GestureDescription.StrokeDescription strokeDescription 
            = new GestureDescription.StrokeDescription(path, 0, t);
        dispatchGesture(
            new GestureDescription.Builder().addStroke(strokeDescription).build(), 
            new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);

                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);

                }
            }, null);
    
     }
    
}
