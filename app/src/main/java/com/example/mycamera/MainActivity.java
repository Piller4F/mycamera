package com.example.mycamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private SurfaceView mSurfaceView;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return;
        }
        openCamera();
    }

    private void openCamera() {
        try {
            String cameraId = mCameraManager.getCameraIdList()[0]; // 获取第一个摄像头设备
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            Range<Integer>[] fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

            for (Range<Integer> range : fpsRanges) {
                Log.d("sgq", "fps range: " + range.toString());
            }

            Range<Integer> targetFpsRange = chooseTargetFpsRange(fpsRanges);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    createCaptureSession(targetFpsRange);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    mCameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Range<Integer> chooseTargetFpsRange(Range<Integer>[] fpsRanges) {
        // 这里可以根据你的需求选择合适的帧率范围
        // 这里简单地选择最高帧率范围
        Range<Integer> targetFpsRange = fpsRanges[0];
        for (Range<Integer> range : fpsRanges) {
            if (range.getUpper() > targetFpsRange.getUpper()) {
                targetFpsRange = range;
            }
        }
        return targetFpsRange;
    }

    private void createCaptureSession(Range<Integer> targetFpsRange) {
        try {
            Surface surface = mSurfaceView.getHolder().getSurface();
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession = session;
                    try {
                        CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        requestBuilder.addTarget(surface);
                        requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                        requestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range.create(0, 30));
                        Log.d("sgq", "targetFpsRange = " + targetFpsRange);
                        mCaptureSession.setRepeatingRequest(requestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e("MainActivity", "Failed to configure camera capture session.");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // 空实现
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                // Camera permission denied
            }
        }
    }
}

















//public class MainActivity extends AppCompatActivity {
//
//
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//    private CameraDevice mCameraDevice;
//    private Range<Integer> mFpsRange;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // 检查摄像头权限
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            // 如果没有摄像头权限，则请求权限
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
//        } else {
//            // 如果有摄像头权限，则打开摄像头
//            openCamera();
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private void openCamera() {
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            String cameraId = cameraManager.getCameraIdList()[0]; // 获取第一个摄像头设备
//            Log.d("sgq", "openCamera: cameraId= " + cameraId);
//            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            Size[] sizes = map.getOutputSizes(android.graphics.ImageFormat.YUV_420_888);
//            for (Size size : sizes) {
//                Log.d("CameraSize", "Supported Size: " + size.getWidth() + "x" + size.getHeight());
//            }
//            mFpsRange = chooseBestFpsRange(characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES));
//            Log.d("CameraFps", "Best FPS range: " + mFpsRange.toString());
//
////            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
////            //采用自定义的函数获取Size
////            mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
////            //此处的Format是项目中用到的，就沿用了
////            minFrameDuration=map.getOutputMinFrameDuration(ImageFormat.YUV_420_888,mPreviewSize);
////            System.out.println("minFrameRate:"+minFrameDuration);
//
//
//            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
//                @Override
//                public void onOpened(@NonNull CameraDevice camera) {
//                    mCameraDevice = camera;
//                    // 在此处可以执行你的相机操作
//                    // 例如创建预览会话等
//
//                }
//
//                @Override
//                public void onDisconnected(@NonNull CameraDevice camera) {
//                    camera.close();
//                    mCameraDevice = null;
//                }
//
//                @Override
//                public void onError(@NonNull CameraDevice camera, int error) {
//                    camera.close();
//                    mCameraDevice = null;
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // 选择最适合的帧率范围
//    private Range<Integer> chooseBestFpsRange(Range<Integer>[] fpsRanges) {
//        Range<Integer> bestRange = fpsRanges[0];
//        for (Range<Integer> range : fpsRanges) {
//            if (range.getUpper() > bestRange.getUpper()) {
//                bestRange = range;
//            }
//        }
//        return bestRange;
//    }
//
//    private void createCaptureSession() {
//        try {
//            SurfaceTexture surfaceTexture = new SurfaceTexture(0);
//            surfaceTexture.setDefaultBufferSize(1920, 1080); // 设置预览画面的尺寸
//            Surface previewSurface = new Surface(surfaceTexture);
//
//            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(CameraCaptureSession session) {
//                    mCaptureSession = session;
//
//                    // 创建CaptureRequest，并设置固定帧率范围和关闭自动曝光
//                    CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//                    requestBuilder.addTarget(surface);
//                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
//                    requestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range.create(30, 30)); // 设置固定帧率为 30 帧每秒
//
//                    try {
//                        mCaptureSession.setRepeatingRequest(requestBuilder.build(), null, null);
//                    } catch (CameraAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onConfigureFailed(CameraCaptureSession session) {
//                    // 配置会话失败
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // 用户授予了摄像头权限
//                openCamera();
//            } else {
//                // 用户拒绝了摄像头权限
//                Toast.makeText(this, "需要摄像头权限才能使用相机功能", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mCameraDevice != null) {
//            mCameraDevice.close();
//            mCameraDevice = null;
//        }
//    }
//}












//
//import android.os.Bundle;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//}