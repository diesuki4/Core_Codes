using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

// HTTP 요청을 처리하는 클래스다.
// 코루틴을 통해 GET/POST 요청을 보내고, 완료시 델리게이트에 등록된 작업을 수행한다.
public class HttpManager : MonoBehaviour
{
    public static HttpManager Instance;

    void Awake()
    {
        if (Instance == null)
        {
            Instance = this;
            DontDestroyOnLoad(gameObject);
        }
        else
        {
            Destroy(gameObject);
        }
    }

    public const string SERVER_ADDR = "http://192.168.1.59:8888";
    
    void Start() { }
    void Update() { }

    // 코루틴을 통해 HTTP 요청을 보낸다.
    public void SendRequest(HttpRequester requester)
    {
        StartCoroutine(IESendRequest(requester));
    }

    IEnumerator IESendRequest(HttpRequester requester)
    {
        UnityWebRequest webRequest = null;

        switch (requester.requestType)
        {
            // 입력 값을 URL에 포함시켜 GET 방식으로 보낸다.
            case RequestType.GET :
                webRequest = UnityWebRequest.Get(requester.url);
                break;
            // 입력 값을 Body에 담아 POST 방식으로 보낸다.
            case RequestType.POST :
                webRequest = UnityWebRequest.Post(requester.url, requester.formData);
                break;
        }

        yield return webRequest.SendWebRequest();

        // 응답이 오면 등록된 델리게이트를 발동한다.
        if (webRequest.result == UnityWebRequest.Result.Success &&
            requester.onComplete != null)
            requester.onComplete(webRequest.downloadHandler);
        else
            Debug.Log("[FAIL] : " + webRequest.result + " (" + webRequest.error + ")");
        
        yield return null;
    }
}
