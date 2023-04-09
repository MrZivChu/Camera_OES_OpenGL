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

    private SurfaceTexture mSurfaceTexture;
    private GLTextureOES mTextureOES;
    private GLTexture2D mUnityTexture;
    private FBO mFBO;

    private boolean mFrameUpdated = false;
    private Camera mCamera;
    private int cameraWidth;
    private int cameraHeight;

    public void openCamera() {
        Log.d(TAG, "openCamera");
        mCamera = Camera.open(1);
        cameraWidth = mCamera.getParameters().getPreviewSize().width;
        cameraHeight = mCamera.getParameters().getPreviewSize().height;

        mUnityTexture = new GLTexture2D(UnityPlayer.currentActivity, cameraWidth, cameraHeight);
        mFBO = new FBO(mUnityTexture);
        mTextureOES = new GLTextureOES(UnityPlayer.currentActivity, cameraWidth, cameraHeight);
        // SurfaceTexture只能用GL_TEXTURE_EXTERNAL_OES,具体看SurfaceTexture说明
        mSurfaceTexture = new SurfaceTexture(mTextureOES.getTextureID());
        mSurfaceTexture.setDefaultBufferSize(cameraWidth, cameraHeight);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
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

            mFBO.FBOBegin();
            mTextureOES.draw();
            mFBO.FBOEnd();

            // 这一句可以用unity中的GL.InvalidateState()替代,也是同样的效果
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

    public boolean isFrameUpdated() {
        return mFrameUpdated;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mFrameUpdated = true;
    }
}
