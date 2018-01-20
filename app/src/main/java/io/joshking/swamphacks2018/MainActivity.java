package io.joshking.swamphacks2018;


import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

//import android.hardware.camera2.;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 1000;
    private CameraManager cameraManager;
    private String rearCameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FrameLayout mContentView = findViewById(R.id.root_view);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (checkPermissions()) {
            cameraManager = getSystemService(CameraManager.class);
            safeCameraOpen();
        }
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST_CODE);
        return false;

    }

    private boolean safeCameraOpen() {
        boolean qOpened = false;

        try {
//            releaseCameraAndPreview();
//            cameraManager.openCamera(rearCameraId, this, this);

        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

//    private void releaseCameraAndPreview() {
//        mPreview.setCamera(null);
//        if (mCamera != null) {
//            mCamera.release();
//            mCamera = null;
//        }
//    }


    /**
     * Sets up member variables related to camera.
     *
//     * @param width  The width of available size for camera preview
//     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(/*int width, int height*/) {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
//                Size largest = Collections.max(
//                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
//                        new CompareSizesByArea());
//                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
//                        ImageFormat.JPEG, /*maxImages*/2);
//                mImageReader.setOnImageAvailableListener(
//                        mOnImageAvailableListener, mBackgroundHandler);
//
//                // Find out if we need to swap dimension to get the preview size relative to sensor
//                // coordinate.
//                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//                //noinspection ConstantConditions
//                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//                boolean swappedDimensions = false;
//                switch (displayRotation) {
//                    case Surface.ROTATION_0:
//                    case Surface.ROTATION_180:
//                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
//                            swappedDimensions = true;
//                        }
//                        break;
//                    case Surface.ROTATION_90:
//                    case Surface.ROTATION_270:
//                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
//                            swappedDimensions = true;
//                        }
//                        break;
//                    default:
//                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
//                }
//
//                Point displaySize = new Point();
//                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
//                int rotatedPreviewWidth = width;
//                int rotatedPreviewHeight = height;
//                int maxPreviewWidth = displaySize.x;
//                int maxPreviewHeight = displaySize.y;
//
//                if (swappedDimensions) {
//                    rotatedPreviewWidth = height;
//                    rotatedPreviewHeight = width;
//                    maxPreviewWidth = displaySize.y;
//                    maxPreviewHeight = displaySize.x;
//                }
//
//                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
//                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
//                }
//
//                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
//                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
//                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
//                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
//                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
//                        maxPreviewHeight, largest);
//
//                // We fit the aspect ratio of TextureView to the size of preview we picked.
//                int orientation = getResources().getConfiguration().orientation;
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    mTextureView.setAspectRatio(
//                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
//                } else {
//                    mTextureView.setAspectRatio(
//                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
//                }

                this.rearCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
//            ErrorDialog.newInstance(getString(R.string.camera_error))
//                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

}
