using System.IO;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;

// 로그인 UI 매니저다.
// 고양이 이미지 업로드, 로그인 버튼 등의 콜백 함수가 구현되어 있다.
public class LoginUIManager : MonoBehaviour
{
    public static LoginUIManager Instance;

    void Awake()
    {
        if (Instance == null)
            Instance = this;
        else
            Destroy(gameObject);
    }

    public const string SERVER_ADDR = "http://192.168.1.59:8888";

    void Start() { }
    void Update() { }

    // 고양이 이미지 업로드 버튼
    public void OnClickBtnUploadImage()
    {
        string url = SERVER_ADDR + "/catimage";
        HttpRequester requester = new HttpRequester();

        requester.url = url;
        requester.requestType = RequestType.POST;

        // 찍은 사진을 불러오고 위치 정보를 가져온다.
        CatImageRaw data = new CatImageRaw();
        data.imageRaw = File.ReadAllBytes(Application.dataPath + "/" + "cat.png");
        data.takenLocation = GPSManager.Instance.GetLocation();

        // Body에 담아 POST 방식으로 서버에 보낸다.
        WWWForm form = new WWWForm();
        form.AddBinaryData("image", data.imageRaw, SystemInfo.deviceUniqueIdentifier + ".png", "image/png");
        form.AddField("takenLocation", data.takenLocation);

        requester.formData = form;
        requester.onComplete = OnCompleteBtnUploadImage;

        HttpManager.Instance.SendRequest(requester);
    }

    void OnCompleteBtnUploadImage(DownloadHandler handler)
    {
        Debug.Log("[OnCompleteBtnUploadImage] 이미지 업로드 " + (handler.text == "false" ? "실패" : "완료"));
    }

    // 로그인 버튼
    public void OnClickLogin()
    {
        string url = SERVER_ADDR + "/login";
        HttpRequester requester = new HttpRequester();

        // 기기 고유 식별 번호가 유저 ID를 대체
        string param = "?";
        param += "deviceId=" + SystemInfo.deviceUniqueIdentifier;

        requester.url = url + param;
        requester.requestType = RequestType.GET;
        requester.onComplete = OnCompleteLogin;

        HttpManager.Instance.SendRequest(requester);
    }

    void OnCompleteLogin(DownloadHandler handler)
    {
        string result = handler.text;

        if (result == "false")
            Debug.Log("[TODO] 신규 유저이므로 고양이 사진 찍고 서버에 저장하기");
        else
            // 기존 사용자는 고양이 세이브 데이터를 JSON 형식으로 받는다.
            LoadMyCatDataToUI(JsonUtility.FromJson<MyCat>(result));
    }
}
