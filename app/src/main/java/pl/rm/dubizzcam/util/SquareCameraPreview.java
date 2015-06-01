package pl.rm.dubizzcam.util;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;

import com.commonsware.cwac.camera.CameraView;

/**
 * Created by rafalmanka on 6/1/15 for DubizzCam.
 */
public class SquareCameraPreview extends CameraView {


    public SquareCameraPreview(Context context) {
        super(context);
    }

    public SquareCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        } else {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        }

    }

}
