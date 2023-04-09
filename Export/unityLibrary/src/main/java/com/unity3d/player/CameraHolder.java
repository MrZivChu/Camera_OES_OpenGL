package com.unity3d.player;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class CameraHolder implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "zwh";//CameraHolder.class.getSimpleName();

    private SurfaceTexture mSurfaceTexture; //camera preview
    private GLTextureOES mTextureOES;       //GL_TEXTURE_EXTERNAL_OES
    private GLTexture2D mUnityTexture;      //GL_TEXTURE_2D 用于在Unity里显示的贴图
    private FBO mFBO;

    private boolean mFrameUpdated;
    private Camera mCamera;
    private int cameraWidth;
    private int cameraHeight;

    public void openCamera() {
        Log.d(TAG, "openCamera");
        mFrameUpdated = false;
        mCamera = Camera.open(1);
        cameraWidth = mCamera.getParameters().getPreviewSize().width;
        cameraHeight = mCamera.getParameters().getPreviewSize().height;

        // 利用OpenGL生成OES纹理并绑定到mSurfaceTexture
        // 再把camera的预览数据设置显示到mSurfaceTexture，OpenGL就能拿到摄像头数据。
        mTextureOES = new GLTextureOES(UnityPlayer.currentActivity, cameraWidth, cameraHeight);
        mSurfaceTexture = new SurfaceTexture(mTextureOES.getTextureID());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    public boolean isFrameUpdated() {
        return mFrameUpdated;
    }

    public int getWidth() {
        return mCamera.getParameters().getPreviewSize().width;
    }

    public int getHeight() {
        return mCamera.getParameters().getPreviewSize().height;
    }

    public int updateTexture() {
        Log.d(TAG, "updateTexture");
        synchronized (this) {
            mFrameUpdated = false;
            mSurfaceTexture.updateTexImage();

            // 根据宽高创建Unity使用的GL_TEXTURE_2D纹理
            if (mUnityTexture == null) {
                mUnityTexture = new GLTexture2D(UnityPlayer.currentActivity, cameraWidth, cameraHeight);
                mFBO = new FBO(mUnityTexture);
            }
            float[] mMVPMatrix = new float[16];
            Matrix.setIdentityM(mMVPMatrix, 0);
            mFBO.FBOBegin();
            GLES20.glViewport(0, 0, cameraWidth, cameraHeight);
            mTextureOES.draw(mMVPMatrix);
            mFBO.FBOEnd();

            Point screenSize = new Point();
            if (Build.VERSION.SDK_INT >= 17) {
                UnityPlayer.currentActivity.getWindowManager().getDefaultDisplay().getRealSize(screenSize);
            } else {
                UnityPlayer.currentActivity.getWindowManager().getDefaultDisplay().getSize(screenSize);
            }
            GLES20.glViewport(0, 0, screenSize.x, screenSize.y);
            return mUnityTexture.getTextureID();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mFrameUpdated = true;
    }
}
