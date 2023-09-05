using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

public enum RequestType
{
    GET,
    POST
}

// HTTP 요청에 필요한 멤버들을 선언한 클래스다.
public class HttpRequester
{
    public string url;              // URL
    public RequestType requestType; // GET, POST
    public WWWForm formData;        // POST 방식에서 입력 값
    
    public Action<DownloadHandler> onComplete = null;   // 완료시 동작
}
