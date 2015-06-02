package pl.rm.dubizzcam;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraUtils;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import at.markushi.ui.CircleButton;

/**
 * Created by rafalmanka on 6/2/15 for DubizzCam.
 */
public class SingleCameraActivity extends Activity implements CameraHostProvider, View.OnClickListener {

    private static final String TAG = SingleCameraActivity.class.getSimpleName();

    private CameraFragment mCameraFragment;
    private DubizzCameraHost mCameraHost;
    private ProgressDialog mProgressDialog;

    // Options
    private FlashOption mFlashOption;
    private DeviceCamera mDeviceCamera;

    // UI
    private CircleButton btnButtonCapture;
    private ImageView ivFlashImage;
    private ImageView ivToggleCamera;

    // helpers
    private boolean mCameraFail;


    public void setCurrentDeviceCamera(DeviceCamera currentCamera) {
        this.mDeviceCamera = currentCamera;
    }

    public ProgressDialog getProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle(getResources().getString(
                    R.string.processing_image));
            mProgressDialog.setMessage(getResources().getString(
                    R.string.please_wait));
        }
        return mProgressDialog;
    }


    enum DeviceCamera implements Serializable {

        FRONT(R.drawable.ic_camera_front), BACK(R.drawable.ic_camera_rear);

        private final int icon;

        DeviceCamera(@DrawableRes int icon) {
            this.icon = icon;
        }

        public static DeviceCamera getDefault() {
            return BACK;
        }

        public DeviceCamera getNext() {
            switch (this) {
                case FRONT:
                    return BACK;
                case BACK:
                    return FRONT;
                default:
                    return getDefault();
            }
        }

        public void updateFlashIconVisibility(ImageView flashImage) {
            switch (this) {
                case FRONT: {
                    flashImage.setVisibility(View.INVISIBLE);
                    break;
                }
                case BACK: {
                    flashImage.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }

        public void setupIcon(ImageView toggleCamera) {
            toggleCamera.setImageResource(icon);
        }
    }

    enum FlashOption implements Serializable {
        AUTO(Camera.Parameters.FLASH_MODE_AUTO, R.drawable.ic_flash_auto),
        NO(Camera.Parameters.FLASH_MODE_OFF, R.drawable.ic_flash_off),
        YES(Camera.Parameters.FLASH_MODE_ON, R.drawable.ic_flash_on);

        private final String flashMode;
        private final int imageResource;

        FlashOption(String flashMode, @DrawableRes int imageResource) {
            this.flashMode = flashMode;
            this.imageResource = imageResource;
        }

        public FlashOption getNextFlashOption() {
            switch (this) {
                case AUTO:
                    return NO;
                case YES:
                    return AUTO;
                case NO:
                    return YES;
                default:
                    return getDefault();
            }

        }

        private FlashOption getDefault() {
            return AUTO;
        }

        public void setupIcon(ImageView flashIcon) {
            flashIcon.setImageResource(imageResource);
        }

        public String getFlashMode() {
            return flashMode;
        }
    }

    public DeviceCamera getCurrentDeviceCamera() {
        if (mDeviceCamera == null) {
            mDeviceCamera = DeviceCamera.getDefault();
        }
        return mDeviceCamera;
    }

    @Override
    public CameraHost getCameraHost() {
        if (mCameraHost == null) {
            mCameraHost = new DubizzCameraHost(this);
        }
        return mCameraHost;
    }


    public FlashOption getFlashOption() {
        if (mFlashOption == null) {
            mFlashOption = FlashOption.AUTO;
        }
        return mFlashOption;
    }

    private boolean isSDMounted() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(DeviceCamera.class.getSimpleName(), getCurrentDeviceCamera());
        outState.putSerializable(FlashOption.class.getSimpleName(), getFlashOption());
        super.onSaveInstanceState(outState);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mDeviceCamera = (DeviceCamera) savedInstanceState.getSerializable(DeviceCamera.class.getSimpleName());
            mFlashOption = (FlashOption) savedInstanceState.getSerializable(FlashOption.class.getSimpleName());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_camera);
        // state
        restoreState(savedInstanceState);
        // UI
        restartCamera();
        initToggleCameraImage();
        initCaptureButton();
        initFlashImage();
    }


    private void initFlashImage() {
        ivFlashImage = (ImageView) findViewById(R.id.flashIcon);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            ivFlashImage.setVisibility(View.INVISIBLE);
        } else {
            getFlashOption().setupIcon(ivFlashImage);
            ivFlashImage.setOnClickListener(this);
        }
    }

    private void initCaptureButton() {
        btnButtonCapture = (CircleButton) findViewById(R.id.btnCapture);
        btnButtonCapture.setOnClickListener(this);
    }

    private void initToggleCameraImage() {
        ivToggleCamera = (ImageView) findViewById(R.id.toggleCamera);
        if (Camera.getNumberOfCameras() <= 1) {
            ivToggleCamera.setVisibility(View.INVISIBLE);
        } else {
            getCurrentDeviceCamera().setupIcon(ivToggleCamera);
            ivToggleCamera.setOnClickListener(this);
        }
    }

//    private void initCameraPreview() {
////        if (firstResume) {
//        restartCamera();
////            firstResume = false;
////        }
//    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flashIcon: {
                onFlashImageClicked();
                break;
            }
            case R.id.btnCapture: {
                onCaptureButtonClicked();
                break;
            }
            case R.id.toggleCamera: {
                onToggleCameraImageClicked();
                break;
            }
        }
    }

    private void onToggleCameraImageClicked() {
        Log.v(TAG, "toggleCamera");
        if (mCameraFail) {
            Log.e(TAG, "camera does not work");
            return;
        }

        setCurrentDeviceCamera(getCurrentDeviceCamera().getNext());
        getCurrentDeviceCamera().updateFlashIconVisibility(ivFlashImage);
        getCurrentDeviceCamera().setupIcon(ivToggleCamera);

        Log.v(TAG, "currentCameraId: " + getCurrentDeviceCamera().toString());
        restartCamera();
        Log.v(TAG, "camera switched");

    }


    private void onCaptureButtonClicked() {
        Log.v(TAG, "onCaptureButtonClicked");
        if (mCameraFail) {
            showToast(R.string.alert_camera_not_availeable);
            return;
        }
        try {
            if (mCameraHost != null && mCameraHost.isFocusReady) {
                btnButtonCapture.setEnabled(false);
                mCameraFragment.autoFocus();
            } else {
                showToast(R.string.preview_is_not_ready_yet);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }
    }

    private void onFlashImageClicked() {
        if (mCameraFail) {
            showToast(getString(R.string.camera_not_availeable));
            return;
        }

        toggleFlashOption();
        refreshFlashIcon();
        restartCamera();

    }

    private void refreshFlashIcon() {
        getFlashOption().setupIcon(ivFlashImage);
    }

    private void toggleFlashOption() {
        mFlashOption = getFlashOption().getNextFlashOption();
    }

    private void restartCamera() {
        if (mCameraFail) {
            return;
        }
        this.mCameraHost = new DubizzCameraHost(this);
        mCameraFragment = new CameraFragment();
        mCameraFragment.setHost(this.mCameraHost);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.camera_fragment_container, mCameraFragment);
        transaction.commit();

    }


    private class DubizzCameraHost extends SimpleCameraHost {

        public boolean isFocusReady = false;

        public DubizzCameraHost(Context _ctxt) {
            super(_ctxt);
        }

        @Override
        public void autoFocusAvailable() {
            isFocusReady = true;
        }

        @Override
        public void autoFocusUnavailable() {
            isFocusReady = true;
        }

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            mCameraFragment.takePicture(false, true);
        }

        @Override
        public Camera.ShutterCallback getShutterCallback() {
            return new Camera.ShutterCallback() {

                @Override
                public void onShutter() {
                    getProgressDialog().show();
                }
            };
        }

        @Override
        public boolean useSingleShotMode() {
            return true;
        }

        public RecordingHint getRecordingHint() {
            return RecordingHint.STILL_ONLY;
        }

        protected boolean useFrontFacingCamera() {
            return getCurrentDeviceCamera() == DeviceCamera.FRONT;
        }

        @Override
        public void onCameraFail(FailureReason reason) {

            String text;
            if (reason == FailureReason.UNKNOWN) {
                text = getString(R.string.error_camera_unknown);
            } else if (reason == FailureReason.NO_CAMERAS_REPORTED) {
                text = getString(R.string.error_no_camera_found);
            } else {
                text = getString(R.string.an_error_occured);
            }

            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            mCameraFail = true;
        }

        @Override
        public boolean mirrorFFC() {
            return true;
        }

        @Override
        public Camera.Parameters adjustPictureParameters(PictureTransaction xact,
                                                         Camera.Parameters parameters) {
            String mode = getFlashOption().getFlashMode();
            String better_mode = CameraUtils.findBestFlashModeMatch(parameters,
                    mode);
            if (better_mode != null) {
                parameters.setFlashMode(better_mode);
            }
            return parameters;
        }

        private Bitmap getBitmapCropped(String path) throws Exception {
            Log.v(TAG, "cropping image " + path);
            int rotationAngle = 0;
            ExifInterface exif = new ExifInterface(path);
            String orientString = exif
                    .getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer
                    .parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                rotationAngle = 90;
            }

            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                rotationAngle = 180;
            }

            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                rotationAngle = 270;
            }

            Log.v(TAG, "image should be rotated by " + rotationAngle + " degrees");


            BitmapFactory.Options optionsJustBounds = new BitmapFactory.Options();
            optionsJustBounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, optionsJustBounds);

            int bitmapWidth = optionsJustBounds.outWidth;
            int bitmapHeight = optionsJustBounds.outHeight;

            int x = 0, y = 0;

            int smallerSize;
            Log.v(TAG, "calculate proportions");
            if (bitmapWidth > bitmapHeight) {
                smallerSize = bitmapHeight;
            } else {
                smallerSize = bitmapWidth;
            }
            Log.v(TAG, "smaller size = " + smallerSize);

            // New api allows 596, for old api use 149
            float maxSize = 596;
            float scaleFactor = ((float) smallerSize) / maxSize;

            if (scaleFactor > 1) {
                scaleFactor = (float) Math.round(scaleFactor);
            } else {
                scaleFactor = 1;
            }
            Log.v(TAG, "scale factor = " + scaleFactor);
            Log.v(TAG, "get bitmap in approximately nice size");
            BitmapFactory.Options optionsScaled = new BitmapFactory.Options();
            optionsScaled.inSampleSize = (int) scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeFile(path, optionsScaled);
            Bitmap rotatedBitmap;
            if (rotationAngle == 90 || rotationAngle == 270) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotationAngle, (float) bitmap.getWidth() / 2,
                        (float) bitmap.getHeight() / 2);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } else {
                rotatedBitmap = bitmap;
            }

            bitmap = rotatedBitmap;
            if (bitmap.getWidth() > bitmap.getHeight()) {
                smallerSize = bitmap.getHeight();
                x = (bitmap.getWidth() - smallerSize) / 2;
            } else {
                smallerSize = bitmap.getWidth();
                y = (bitmap.getHeight() - smallerSize) / 2;
            }
            Log.v(TAG, "smaller size = " + smallerSize + ", x = " + x + ", y = " + y);

            /*if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                y = y + 70;
            } else {
                x = x + 70;
            }*/
            Bitmap croppedBmp = Bitmap.createBitmap(bitmap, x, y, smallerSize,
                    smallerSize);

            if (croppedBmp.getWidth() != croppedBmp.getHeight()) {
                Log.e(TAG,
                        "width and height are not equal! width="
                                + croppedBmp.getWidth() + " height="
                                + croppedBmp.getHeight());
            }

            return croppedBmp;
        }

        private File getTempImageFile() throws IOException {
            if (isSDMounted()) {
                return File.createTempFile("crop", ".png",
                        getExternalCacheDir());
            } else {
                return null;
            }
        }

        @Override
        public void saveImage(PictureTransaction xact, byte[] data) {

            File pictureFile;
            try {
                pictureFile = getTempImageFile();
                if (pictureFile == null) {
                    Log.e(TAG, "picture file is null");
                    return;
                }
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.flush();
                fos.close();
                Bitmap croppedBitmap = getBitmapCropped(pictureFile.getAbsolutePath());
                saveToDcim(croppedBitmap);


            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(getString(R.string.problem_cropping_image));
                    }
                });
            } finally {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        restartCamera();

                        if (getProgressDialog().isShowing()) {
                            getProgressDialog().cancel();
                        }
                        btnButtonCapture.setEnabled(true);

                    }
                });

            }
        }

        private void saveToDcim(Bitmap croppedBitmap) throws IOException {

            File filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            // Create a new folder in SD Card
            File dir = new File(filepath.getAbsolutePath()
                    + "/DubizzCam/");
            if (dir.mkdirs()) {
                Log.v(TAG, "directory created");
            } else {
                Log.v(TAG, "directory already exists");
            }

            File file = new File(dir, getImageName());
            Log.v(TAG, "saving file to " + file.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(file);
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }

        private String getImageName() {
            // Create a name for the saved image
            Date date = new Date();
            String name = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(date);

            return "IMG_" + name + ".jpeg";

        }

    }


    private void showToast(@StringRes int string) {
        showToast(getString(string));
    }

    private void showToast(String string) {
        Toast.makeText(getApplicationContext(),
                string, Toast.LENGTH_LONG).show();
    }


}