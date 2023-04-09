using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class Main : MonoBehaviour
{
    AndroidJavaObject nativeCameraHolder;
    Texture2D texture = null;
    int textureId = 0;

    void Awake()
    {
#if UNITY_ANDROID
        nativeCameraHolder = new AndroidJavaObject("com.unity3d.player.CameraHolder");
        if (nativeCameraHolder == null)
            Debug.Log("zwh Start nativeCameraHolder is null");
#endif
    }

    void Start()
    {
        nativeCameraHolder.Call("openCamera");
        Debug.Log("zwh unity " + Screen.width + "=" + Screen.height);
    }

    public MeshRenderer meshRenderer;
    public Image image;
    void Update()
    {
        if (nativeCameraHolder.Call<bool>("isFrameUpdated"))
        {
            textureId = nativeCameraHolder.Call<int>("updateTexture");
            if (texture == null && textureId != 0)
            {
                Debug.Log("zwh create external texture");
                texture = Texture2D.CreateExternalTexture(nativeCameraHolder.Call<int>("getWidth"), nativeCameraHolder.Call<int>("getHeight"),
                    TextureFormat.RGB565, false, false, (IntPtr)textureId);
                texture.wrapMode = TextureWrapMode.Clamp;
                texture.filterMode = FilterMode.Bilinear;
                meshRenderer.material.mainTexture = texture;

                Rect rect = new Rect(0, 0, texture.width, texture.height);
                Vector2 pivot = Vector2.zero;
                Sprite sprite = Sprite.Create(texture, rect, pivot);
                image.sprite = sprite;
            }
        }
    }
}
