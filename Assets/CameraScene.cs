using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class CameraScene : MonoBehaviour
{
    public Button btn;

    AndroidJavaObject nativeCameraHolder;
    Texture2D texture2D = null;
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

        btn.onClick.AddListener(() => {
            SceneManager.LoadScene("Media");
        });
    }

    public MeshRenderer meshRenderer;
    public Image image;
    void Update()
    {
        if (nativeCameraHolder.Call<bool>("isFrameUpdated"))
        {
            textureId = nativeCameraHolder.Call<int>("updateTexture");
            if (texture2D == null && textureId != 0)
            {
                Debug.Log("zwh create external texture");
                texture2D = Texture2D.CreateExternalTexture(nativeCameraHolder.Call<int>("getWidth"), nativeCameraHolder.Call<int>("getHeight"),
                    TextureFormat.RGB565, false, false, (IntPtr)textureId);
                texture2D.wrapMode = TextureWrapMode.Clamp;
                texture2D.filterMode = FilterMode.Bilinear;
                meshRenderer.material.mainTexture = texture2D;

                Rect rect = new Rect(0, 0, texture2D.width, texture2D.height);
                Vector2 pivot = Vector2.zero;
                Sprite sprite = Sprite.Create(texture2D, rect, pivot);
                image.sprite = sprite;
            }
            GL.InvalidateState();
        }
    }
}
