package io.joshking.swamphacks2018;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

public class Camera2BasicFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private AutoFitTextureView textureView;
    private CameraUtilsBare cameraUtils;
    private WebSocketConnection connection;
    private Vibrator vibrator;


    public static Camera2BasicFragment newInstance() {
        return new Camera2BasicFragment();
    }

    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        textureView = view.findViewById(R.id.texture);
        textureView.setOnClickListener(v -> vibrate());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();

        vibrator = activity.getSystemService(Vibrator.class);
        File file = new File(activity.getExternalFilesDir(null), "pic.jpg");
        CameraManager cameraManager = activity.getSystemService(CameraManager.class);
        Display display = activity.getWindowManager().getDefaultDisplay();

        cameraUtils = new CameraUtilsBare(cameraManager, file, () -> display, () -> textureView, () -> {
            connection.sendFile(cameraUtils.getFile(), response -> {
                cameraUtils.takePicture();

                try {
                    int probability = Integer.parseInt(response);
                    handleProbabilityOfObject(probability);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        connection = new WebSocketConnection("ws://10.192.134.211:8080", () -> {
            if (checkPermissions()) {
                cameraUtils.init();
            } else {
                requestCameraPermission();
            }
        });
        connection.connectWebSocket();
    }

    private void handleProbabilityOfObject(@IntRange(from = 0, to = 100) int probability) {
        if (probability > 50)
            vibrate();
    }

    private void vibrate() {
        long[] vibratePattern = {0, 100, 0, 100, 50, 300, 0, 200};

        if (vibrator.hasAmplitudeControl()) {
            int[] amplitudes = {0, 175, 0, 255, 0, 255, 0, 50};
            vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, amplitudes, -1));
        } else {
            vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, -1));
        }
    }


    @Override
    public void onPause() {
        cameraUtils.closeCamera();
//        stopBackgroundThread();
        super.onPause();
    }

    private void requestCameraPermission() {
        if (FragmentCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                cameraUtils.init();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
//    private void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread("CameraBackground");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//
//    private void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try {
//            mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }


    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            message = "Yikes! Well this is embarrassing..\n\n" + message;
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> activity.finish())
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> FragmentCompat.requestPermissions(parent,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION))
                    .setNegativeButton(android.R.string.cancel,
                            (dialog, which) -> {
                                Activity activity = parent.getActivity();
                                if (activity != null) {
                                    activity.finish();
                                }
                            })
                    .create();
        }
    }

}
