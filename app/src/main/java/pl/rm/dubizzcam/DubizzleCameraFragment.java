package pl.rm.dubizzcam;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import at.markushi.ui.CircleButton;
import pl.rm.dubizzcam.util.SquareCameraPreview;

/**
 * Created by rafalmanka on 6/1/15 for DubizzCam.
 */
public class DubizzleCameraFragment extends CameraFragment implements View.OnClickListener {

    private CameraView mCameraView;
    private boolean useFrontCamera;
    private LinearLayout mPreviewContainer;
    private CircleButton btnToggleFlash;
    private CircleButton btnToggleCam;
    private String mFlashMode;
    private MySimpleCameraHost mCameraHost;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CircleButton btnTakePic = (CircleButton) view.findViewById(R.id.btnTakePic);
        btnTakePic.setOnClickListener(this);
        btnToggleCam = (CircleButton) view.findViewById(R.id.btnToggleCam);
        btnToggleCam.setOnClickListener(this);
        btnToggleFlash = (CircleButton) view.findViewById(R.id.btnToggleFlash);
        btnToggleFlash.setOnClickListener(this);

        addCameraView(view);
    }


    private void addCameraView(View v) {

        FrameLayout frame = (FrameLayout) v.findViewById(R.id.cameraContainer);
        frame.removeAllViews();
        mCameraView = new SquareCameraPreview(getActivity());

        mCameraView.setLayoutParams(new FrameLayout.LayoutParams(300, ViewGroup.LayoutParams.MATCH_PARENT));
        mCameraView.setHost(mCameraHost = new MySimpleCameraHost(getActivity()));
        setCameraView(mCameraView);
        frame.addView(mCameraView);


//        mPreviewContainer = (LinearLayout) view.findViewById(R.id.cameraContainer);
//        mCameraView = (CameraView) view.findViewById(R.id.camera);
//
//        CameraHost mySimpleCameraHost = new MySimpleCameraHost(getActivity());
//        setHost(mySimpleCameraHost);
//        setCameraView(mCameraView);
//
//        mPreviewContainer.removeAllViews();
//        mCameraView = new SquareCameraPreview(getActivity());
//        mCameraView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        mPreviewContainer.addView(mCameraView);
//
//        cameraHost = new MySimpleCameraHost(getActivity());
//        setHost(mySimpleCameraHost);
//        setCameraView(mCameraView);

    }

    private void toggleCam() {
        // do some change to the settings.
        useFrontCamera = !useFrontCamera;
        if (mCameraView != null) {
            mCameraView.onPause();
        }
        addCameraView(getView());
        mCameraView.onResume();
    }

    private class MySimpleCameraHost extends SimpleCameraHost {

        public MySimpleCameraHost(Context _ctxt) {
            super(_ctxt);
        }

        @Override
        protected boolean useFrontFacingCamera() {
            return useFrontCamera;
        }

        @Override
        public void onCameraFail(CameraHost.FailureReason reason) {
            super.onCameraFail(reason);
            Toast.makeText(getActivity(),
                    getString(R.string.alert_camera_fail),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public Camera.Size getPreviewSize(int displayOrientation, int width, int height, Camera.Parameters parameters) {
            return super.getPreviewSize(displayOrientation, width, width, parameters);
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            Camera.Size size = super.getPictureSize(xact, parameters);
            size.height = size.width;
            // setPictureSize(parameters.getPictureSize().width, parameters.getPictureSize().width);
            return size;
        }

//        takeP
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTakePic: {
                takePicture();
                break;
            }
            case R.id.btnToggleCam: {
                toggleCam();
                btnToggleCam.setImageResource(useFrontCamera ? R.drawable.ic_camera_rear_black_24dp : R.drawable.ic_camera_front_black_24dp);
                break;
            }
            case R.id.btnToggleFlash: {

                /*FLASH_MODE_AUTO	Flash will be fired automatically when required.
                String	FLASH_MODE_OFF	Flash will not be fired.
                        String	FLASH_MODE_ON	Flash will always be fired during snapshot.
                String	FLASH_MODE_RED_EYE	Flash will be fired in red-eye reduction mode.
                String	FLASH_MODE_TORCH*/
                String flashMode = getFlashMode();
                if (flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
                    btnToggleFlash.setImageResource(R.drawable.ic_flash_on_black_24dp);
                    mFlashMode = Camera.Parameters.FLASH_MODE_ON;
                } else if (flashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
                    btnToggleFlash.setImageResource(R.drawable.ic_flash_off_black_24dp);
                    mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
                } else {
                    btnToggleFlash.setImageResource(R.drawable.ic_flash_auto_black_24dp);
                    mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
                }

                break;
            }

        }
    }

}