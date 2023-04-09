package com.unity3d.player;

import android.opengl.GLES20;

public class FBO {
    private int mFBOID;

    public FBO(GLTexture2D texture2D) {
        int[] temps = new int[1];
        GLES20.glGenFramebuffers(1, temps, 0);
        mFBOID = temps[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOID);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture2D.getTextureID(), 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void FBOBegin() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOID);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        Utils.checkGlError("glBindBuffer GL_ARRAY_BUFFER 0");
    }

    public void FBOEnd() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
}

