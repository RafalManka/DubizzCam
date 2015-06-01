package pl.rm.dubizzcam;

import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private CameraView cameraView;
    private boolean useBackCamera;
    private LinearLayout mPreviewContainer;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_camera, container, false);
        cameraView = (CameraView) content.findViewById(R.id.camera);
        setCameraView(cameraView);
        return (content);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CircleButton btnTakePic = (CircleButton) view.findViewById(R.id.btnTakePic);
        btnTakePic.setOnClickListener(this);
        CircleButton btnToggleCam = (CircleButton) view.findViewById(R.id.btnToggleCam);
        btnToggleCam.setOnClickListener(this);

        mPreviewContainer = (LinearLayout) view.findViewById(R.id.cameraContainer);

        setHost(new MySimpleCameraHost(getActivity()));
    }


    private void addCameraView() {
        mPreviewContainer.removeAllViews();
        cameraView = new SquareCameraPreview(getActivity());
        cameraView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cameraView.setHost(new MySimpleCameraHost(getActivity()));
        setCameraView(cameraView);
        mPreviewContainer.addView(cameraView);
    }

    private void toggleCam() {
        // do some change to the settings.
        useBackCamera = !useBackCamera;
        if (null != cameraView) {
            cameraView.onPause();
        }
        addCameraView();
        cameraView.onResume();
    }

    private class MySimpleCameraHost extends SimpleCameraHost {

        public MySimpleCameraHost(Context _ctxt) {
            super(_ctxt);
        }

        @Override
        protected boolean useFrontFacingCamera() {
            return !useBackCamera;
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
                break;
            }

        }
    }

}