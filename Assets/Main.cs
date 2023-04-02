using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Main : MonoBehaviour
{
    public Texture2D texture;
    int textureId;

    public bool isLock;

    #region nativeFunction
    AndroidJavaObject nativeCameraHolder;
#if UNITY_ANDROID
    private void _openCamera()
    {
        //Debug.Log("_openCamera");
        nativeCameraHolder.Call("openCamera");
    }
    private void _closeCamera()
    {
        //Debug.Log("_closeCamera");
        nativeCameraHolder.Call("closeCamera");
    }
    private bool _isFrameUpdated()
    {
        //Debug.Log("_isFrameUpdated");
        return nativeCameraHolder.Call<bool>("isFrameUpdated");
    }
    private bool _isTextureReaded()
    {
        //Debug.Log("_isTextureReaded");
        return nativeCameraHolder.Call<bool>("isTextureReaded");
    }
    private int _updateTexture()
    {
        //Debug.Log("_updateTexture");
        return nativeCameraHolder.Call<int>("updateTexture");
    }
    private bool _copyTexture()
    {
        //Debug.Log("_copyTexture");
        return nativeCameraHolder.Call<bool>("copyTexture");
    }
    private int _getWidth()
    {
        //Debug.Log("_getWidth");
        return nativeCameraHolder.Call<int>("getWidth");
    }
    private int _getHeight()
    {
        //Debug.Log("_getHeight");
        return nativeCameraHolder.Call<int>("getHeight");
    }
#endif
    #endregion

    void Awake()
    {
#if UNITY_ANDROID
        nativeCameraHolder = new AndroidJavaObject("com.unity3d.player.CameraHolder");
        if (nativeCameraHolder == null)
            Debug.Log("Start nativeCameraHolder is null");
#endif
    }

    void Start()
    {
        isLock = true;
        _openCamera();
    }

    void Stop()
    {
        _closeCamera();
    }

    void Update()
    {
        Debug.Log("isLock = " + isLock);
        if (_isFrameUpdated())
        {
            textureId = _updateTexture();
            if (texture == null && textureId != 0)
            {
                // entry only once
                Debug.Log("create external texture");
                texture = Texture2D.CreateExternalTexture(_getWidth(), _getHeight(),
                    TextureFormat.RGB565, false, false, (IntPtr)textureId);
                texture.wrapMode = TextureWrapMode.Clamp;
                texture.filterMode = FilterMode.Bilinear;
            }
            else if (textureId != 0)
            {
                if (isLock)
                {
                    isLock = false;
                    texture.UpdateExternalTexture((IntPtr)textureId);
                    GetComponent<MeshRenderer>().material.mainTexture = texture;
                    _copyTexture();//set isLock=true after copy
                }
                else
                    Debug.Log("Waiting");
            }
        }
    }

    void setIsLock(string boolStr)
    {
        Debug.Log(boolStr);
        if (boolStr == "0")
        {
            isLock = false;
            Debug.Log(isLock);
        }
        else
        {
            isLock = true;
            Debug.Log(isLock);
        }
    }
}
