package android.luo.stone.test1;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private MenuItem mItemSwitchCamera = null;
    private boolean mIsFrontCamera = false;
    Mat mGray,mRgb;
    CascadeClassifier haarCascade;
    File mCascadeFile;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG,"opencv loaded successful！");
        }else {
            Log.d(TAG,"opencv not loaded ！");
        }

    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:{

                    try{
                        Log.i(TAG,"Cascade");
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface );
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "cascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        haarCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (haarCascade.empty()) {
                            Log.i(TAG, "级联分类器加载失败");
                            haarCascade = null;
                        }
                    }
                    catch(Exception e)
                    {
                        Log.i(TAG,"未找到级联分类器");
                    }
                    mOpenCvCameraView.enableView();

                }break;
                default:{
                    super.onManagerConnected(status);
                }break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Called onCreat");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemSwitchCamera = menu.add("Toggle Front/Back camera");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = "";

        if (item == mItemSwitchCamera) {
            mOpenCvCameraView.setVisibility(SurfaceView.GONE);
            mIsFrontCamera = !mIsFrontCamera;

            if (mIsFrontCamera) {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_view);
                mOpenCvCameraView.setCameraIndex(1);
                toastMesage = "Front Camera";
            } else {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_view);
                mOpenCvCameraView.setCameraIndex(-1);
                toastMesage = "Back Camera";
            }

            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
            mOpenCvCameraView.enableView();
            Toast toast = Toast.makeText(this, toastMesage, Toast.LENGTH_LONG);
            toast.show();
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.onResume();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgb = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgb.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgb = inputFrame.rgba();//保存摄像的输出的彩色图像
        mGray = inputFrame.gray();//保存输出的灰度图像
        if (mIsFrontCamera) {
            Core.flip(mRgb,mRgb,1);//如果是前置摄像头则翻转图像
        }
        //检测人脸的方法
        MatOfRect faces = new MatOfRect();
        if (haarCascade != null) {
            haarCascade.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(200,200), new Size());
        }
        //画框
        Rect[] facesArray = faces.toArray();
        for(int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mRgb, facesArray[i].tl(), facesArray[i].br(), new Scalar(0,255,0), 3);
        }

       // Core.rectangle(temp, new Point(temp.cols()/2 , temp.rows() / 2 ), new Point(temp.cols() / 2 + 200, temp.rows() / 2 + 200), new Scalar(255,255,255),1);
        return mRgb;
    }
}
