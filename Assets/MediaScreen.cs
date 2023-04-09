using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class MediaScreen : MonoBehaviour
{
    public Button btn;

    public MeshRenderer meshRenderer;
    public RawImage rawImage;
    private AndroidJavaObject nativeObject;
    private int width = 1600;
    private int height = 900;
    private Texture2D texture2D;

    void Start()
    {
        nativeObject = new AndroidJavaObject("com.unity3d.player.VideoPlugin");
        texture2D = new Texture2D(width, height, TextureFormat.RGB24, false, false);
        nativeObject.Call("start", (int)texture2D.GetNativeTexturePtr(), width, height);
        meshRenderer.material.mainTexture = texture2D;
        rawImage.texture = texture2D;

        btn.onClick.AddListener(() => {
            SceneManager.LoadScene("Camera");
        });
    }

    void Update()
    {
        if (texture2D != null && nativeObject.Call<bool>("isUpdateFrame"))
        {
            Debug.Log("VideoPlugin:Update");
            nativeObject.Call("updateTexture");
            GL.InvalidateState();
        }
    }
}
