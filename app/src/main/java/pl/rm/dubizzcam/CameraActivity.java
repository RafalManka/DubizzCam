package pl.rm.dubizzcam;

import android.app.Activity;
import android.os.Bundle;

import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.SimpleCameraHost;

import pl.rm.dubizzcam.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class CameraActivity extends Activity implements
        CameraHostProvider {

    private DubizzleCameraFragment current = null;


    @Override
    public CameraHost getCameraHost() {
        return (new SimpleCameraHost(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

//        getActionBar().hide();

        current = new DubizzleCameraFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.container, current).commit();


    }





}
