package com.kye.smart.simplevideorecord;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kye.smart.simplevideorecord.ulits.ViewUtils;
import com.kye.smart.simplevideorecord.widget.RecordVideoButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //UI
    private RecordVideoButton mRecordControl;
    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private TextView mRecordTime;

    //DATA
    private boolean isPause;     //暂停标识
    private boolean isRecording = false; // 标记，判断当前是否正在录制
    private long mRecordCurrentTime = 0;  //录制时间间隔

    // 存储文件
    private File mVecordFile;
    private Camera mCamera;
    private MediaRecorder mediaRecorder;
    //最低时长录制时间
    private final static float MIN_TIME = 5 * 1000;
    //最长时间录制时间
    private final static float MAX_TIME = 15 * 1000;
    private SurfaceHolder.Callback mCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            initCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            if (mSurfaceHolder.getSurface() == null) {
                return;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            stopCamera();
        }
    };


    private MediaRecorder.OnErrorListener onErrorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mediaRecorder, int what, int extra) {
            try {
                if (mediaRecorder != null) {
                    mediaRecorder.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private ImageView mIvDelete;
    private ImageView mIvFinish;
    private String mVideoPath;
    private static final int RECORDING_DURATION = 10;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORDING_DURATION:
                    if (isRecording) {


                    }
                    break;
                default:
                    break;
            }

        }
    };
    private ProgressBar mProgressRight;
    private ProgressBar mProgressLeft;
    private SimpleDateFormat mSdf;
    private float mRecordTimeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        init();
        initEvent();
    }

    private void bindViews() {
        surfaceView = (SurfaceView) findViewById(R.id.record_surfaceView);
        mRecordControl = (RecordVideoButton) findViewById(R.id.record_control);
        mRecordTime = (TextView) findViewById(R.id.record_time);
        mIvDelete = (ImageView) findViewById(R.id.iv_delete);
        mIvFinish = (ImageView) findViewById(R.id.iv_finish);
        mProgressRight = (ProgressBar) findViewById(R.id.progress_right);
        mProgressLeft = (ProgressBar) findViewById(R.id.progress_left);
        mProgressRight.setMax((int) MAX_TIME);
        mProgressLeft.setMax((int) MAX_TIME);
        mProgressLeft.setRotation(180);
    }

    private void init() {
        //配置SurfaceHodler
        mSurfaceHolder = surfaceView.getHolder();
        // 设置Surface不需要维护自己的缓冲区
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置分辨率
        mSurfaceHolder.setFixedSize(320, 240);
        // 设置该组件不会让屏幕自动关闭
        mSurfaceHolder.setKeepScreenOn(true);
        //相机创建回调接口
        mSurfaceHolder.addCallback(mCallBack);
        initCamera();
    }


    protected void initEvent() {
        mIvFinish.setOnClickListener(this);
        mIvDelete.setOnClickListener(this);
        mRecordControl.setLongClickListener(new RecordVideoButton.OnGestureListener() {
            @Override
            public void setOnLongClickListener() {
                if (ViewUtils.isFastDoubleClick()) {
                    return;
                }
                if (isRecording) {
                    return;
                }
                //开始录制视频
                if (mVideoPath != null && !mVideoPath.equals("")) {
                    deleteVideoFile();
                }
                needShowLayout(false);
                startRecord();
            }

            @Override
            public void onPause(float animatorTime) {

            }

            @Override
            public void completed(float animatorTime) {

                mRecordTimeCount = 15 * 1000 - animatorTime;
                //停止视频录制
                stopRecord();
                if (mRecordTimeCount < MIN_TIME) {
                    deleteVideoFile();
                    Toast.makeText(MainActivity.this, "视频录制时长不得少于5秒,请重新录制", Toast.LENGTH_SHORT).show();
                    needShowLayout(false);
                } else {
                    needShowLayout(true);
                }
            }

            @Override
            public void remainingTime(float time) {
                int progress = (int) (MAX_TIME - time);
                if (mSdf == null) {
                    mSdf = new SimpleDateFormat("mm:ss", Locale.CHINA);
                }
                String format = mSdf.format(MAX_TIME - time);
                mRecordTime.setText(format);
                mProgressRight.setProgress(progress);
                mProgressLeft.setProgress(progress);
            }

            @Override
            public void exceptionMessage(long downTime) {

            }
        });
    }

    /**
     * 是否显示删除和确定按钮
     *
     * @param b
     */
    private void needShowLayout(boolean b) {
        mIvDelete.setVisibility(b ? View.VISIBLE : View.GONE);
        mIvFinish.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    /**
     * 删除录制视频文件
     */
    private void deleteVideoFile() {
        if (mVideoPath == null || mVideoPath.equals("")) {
            return;
        }
        File targetFile = new File(mVideoPath);
        if (targetFile.exists()) {
            targetFile.delete();
        }
    }

    /**
     * 开始录制视频
     */
    public void startRecord() {
        boolean creakOk = createRecordDir();
        if (!creakOk) {
            return;
        }
        mCamera.unlock();
        setConfigRecord();
        try {
            //开始录制
            mediaRecorder.prepare();
            mediaRecorder.start();
            mHandler.sendEmptyMessageDelayed(RECORDING_DURATION, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRecording = true;
    }

    /**
     * 创建视频文件保存路径
     */
    private boolean createRecordDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "请查看您的SD卡是否存在！", Toast.LENGTH_SHORT).show();
            return false;
        }

        File sampleDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "KyeRepairs");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        String recordName = "Repairs_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
        mVecordFile = new File(sampleDir, recordName);
        return true;
    }


    /**
     * 配置MediaRecorder()
     */
    private void setConfigRecord() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setOnErrorListener(onErrorListener);

        //使用SurfaceView预览
        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        //1.设置采集声音
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        //设置采集图像
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //2.设置视频，音频的输出格式 mp4
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        //3.设置音频的编码格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //设置图像的编码格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //设置立体声
//        mediaRecorder.setAudioChannels(2);
        //设置最大录像时间 单位：毫秒
        mediaRecorder.setMaxDuration((int) MAX_TIME);
        //设置最大录制的大小 单位，字节
//        mediaRecorder.setMaxFileSize(1024 * 1024);
        //音频一秒钟包含多少数据位
        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mediaRecorder.setAudioEncodingBitRate(44100);
//        if (mProfile.videoBitRate > 2 * 1024 * 1024) {
//            mediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
//        } else {
        mediaRecorder.setVideoEncodingBitRate(4 * 1024 * 1024);
//        }
//        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);

        //设置选择角度，顺时针方向，因为默认是逆向90度的，这样图像就是正常显示了,这里设置的是观看保存后的视频的角度
        mediaRecorder.setOrientationHint(90);
        // 设置视频录制的分辨率,必须放在设置编码和格式的后面，否则报错
        mediaRecorder.setVideoSize(320, 240);
        mVideoPath = mVecordFile.getAbsolutePath();
        mediaRecorder.setOutputFile(mVideoPath);
    }

    private void saveVideoAndClose() {
        Intent intent = new Intent();
        intent.putExtra("video_path", mVideoPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 停止录制视频
     */
    public void stopRecord() {
        if (isRecording && mediaRecorder != null) {

            try {
                // 设置后不会崩
                mediaRecorder.setOnErrorListener(null);
                mediaRecorder.setOnInfoListener(null);
                mediaRecorder.setPreviewDisplay(null);
                //停止录制
                mediaRecorder.stop();
            } catch (IllegalStateException e) {
                // TODO: handle exception
                Log.i("Exception", Log.getStackTraceString(e));
            } catch (RuntimeException e) {
                // TODO: handle exception
                Log.i("Exception", Log.getStackTraceString(e));
            } catch (Exception e) {
                // TODO: handle exception
                Log.i("Exception", Log.getStackTraceString(e));
            }

            mediaRecorder.reset();
            //释放资源
            mediaRecorder.release();
            mediaRecorder = null;

            //设置开始按钮可点击，停止按钮不可点击
            mRecordControl.setEnabled(true);
            isRecording = false;
        }
    }

    /**
     * 初始化摄像头
     */
    private void initCamera() {
        if (mCamera != null) {
            stopCamera();
        }
        //默认启动后置摄像头
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mCamera == null) {
            Toast.makeText(this, "请检查相机权限!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            //配置CameraParams
            setCameraParams();
            //启动相机预览
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("llw", "Error starting camera preview: " + e.getMessage());
        }

    }

    /**
     * 设置摄像头为竖屏
     */
    private void setCameraParams() {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            //设置相机的横竖屏幕
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                params.set("orientation", "portrait");
                mCamera.setDisplayOrientation(90);
            } else {
                params.set("orientation", "landscape");
                mCamera.setDisplayOrientation(0);
            }
            //设置聚焦模式
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            //缩短Recording启动时间
            params.setRecordingHint(true);
            //是否支持影像稳定能力，支持则开启
            if (params.isVideoStabilizationSupported()) {
                params.setVideoStabilization(true);
            }
            mCamera.setParameters(params);
        }
    }

    /**
     * 释放摄像头资源
     */
    private void stopCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_delete:
                deleteVideoFile();
                resetProgress();
                needShowLayout(false);
                break;

            case R.id.iv_finish:
                saveVideoAndClose();
                break;
            default:
                break;

        }
    }

    /**
     * 重置进度条
     */
    private void resetProgress() {
        mRecordControl.resetProgress();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecord();
        resetProgress();
        if (mRecordTimeCount < MIN_TIME) {
            deleteVideoFile();
            needShowLayout(false);
        } else {
            needShowLayout(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecord();
    }
}
