package com.unity3d.player;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoPlugin implements OnFrameAvailableListener {

    private SurfaceTexture mSurfaceTexture;
    private FilterFBOTexture mFilterFBOTexture;
    private MediaPlayer mMediaPlayer;
    private boolean mIsUpdateFrame;

    public void start(int unityTextureId, int width, int height) {
        FBOUtils.log("start");

        int videoTextureId = FBOUtils.createOESTextureID();
        mSurfaceTexture = new SurfaceTexture(videoTextureId);
        mSurfaceTexture.setDefaultBufferSize(width, height);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mFilterFBOTexture = new FilterFBOTexture(width, height, unityTextureId, videoTextureId);
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
        try {
            final File file = new File("/sdcard/1.mp4");
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setDataSource(Uri.fromFile(file).toString());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                FBOUtils.log("MediaPlayer onPrepared");
                mMediaPlayer.start();
            }
        });
    }

    public void updateTexture() {
        mIsUpdateFrame = false;
        mSurfaceTexture.updateTexImage();
        mFilterFBOTexture.draw();
    }

    public boolean isUpdateFrame() {
        return mIsUpdateFrame;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mIsUpdateFrame = true;
    }

    public void saveTextureToImageWithType(int texture_id, int width, int height, boolean isUnity2DTexture) {
        int[] old_fbo = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_FRAMEBUFFER_BINDING, old_fbo, 0);
        int[] tmp_fbo = new int[1];
        GLES30.glGenFramebuffers(1, tmp_fbo, 0);
        // 根据是否是unity texture来区分attatch到FBO的texture type：
        // unity 		  -> GLES30.GL_TEXTURE_2D
        // SurfaceTexture -> GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        if (isUnity2DTexture) {
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D,
                    texture_id, 0);
        } else {
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    texture_id, 0);
        }
        ByteBuffer outRgbaBuf = ByteBuffer.allocate(width * height * 4);
        //实际看下来，这个函数只可读取到FBO的数据
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, outRgbaBuf);
        //由于我在调试中会直接打断点查看bitmap的样子，所以这边直接recycle了，实际使用中可以save file
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(outRgbaBuf);
        bmp.recycle();

        //把原来的fbo绑定到framebuffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, old_fbo[0]);
        GLES30.glDeleteFramebuffers(1, tmp_fbo, 0);
    }

}
