using System.IO;
using System.Linq;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Timeline.Types;
using Timeline.Utility;
using Timeline.Timeline;

// 공연 창작툴의 타임라인을 담당하는 클래스다.
// 타임라인에 등록된 소품 목록을 관리하며, 소품과 키 추가,
// 공연 저장과 불러오기 기능을 제공한다.
public class TimelineManager : MonoBehaviour
{
    public static TimelineManager Instance;

    public ConcertInfo concertInfo; // 공연 정보 (DB에서 가져오는 창작자 이름, 곡명 등)
    public ConcertData concertData; // 공연 데이터 (FTP 서버에서 가져오는 썸네일, BGM, CDATA)

    bool bNewConcert;   // 새로운 공연을 생성하는지 여부

    void Awake()
    {
        if (Instance == null)
            Instance = this;
        else
            Destroy(this);

        // 타임라인 정보 <GUID: 타임라인 키 목록>
        timelines = new Dictionary<string, TL_Timeline>();
        // 월드 상의 타임라인 오브젝트 목록
        timelineObjects = new Dictionary<string, TimelineObject>();

        // 이전 레벨에서 공연 수정을 클릭한 경우,
        // 해당 공연 ID가 레지스트리에 저장된다.
        if (PlayerPrefs.HasKey("concert_id"))
        {
            int concert_id = PlayerPrefs.GetInt("concert_id");

            bNewConcert = false;
            // 공연을 수정하는 경우 DB, FTP 서버에서 해당 정보를 불러온다.
            concertData = ConcertManager.GetConcertData(concert_id);
            concertInfo = ConcertManager.GetConcert(concert_id, false);
            // cdata 형식의 문자열을 타임라인으로 변환한다.
            timelines = TL_Utility.FromCDATA(concertData.cdata);

            // 팀원이 작성한 음악 스펙트럼 표시 함수
            FileBrowserTest.Instance.SetMusicWave(concertInfo.music_name, concertData.bgm);
            
            // 불러들인 타임라인 정보를 바탕으로
            // 그리드에 배치하고 타임라인 UI에 키를 찍는다.
            Initialize();
        }
        // 새로운 공연을 생성하는 경우
        else
        {
            int concert_id = ConcertManager.NextConcertId();

            bNewConcert = true;

            concertInfo = new ConcertInfo();
            concertInfo.concert_id = concert_id;
            concertInfo.id = AccountManager.id;

            concertData = new ConcertData();
            concertData.concert_id = concert_id;
        }
    }

    public Dictionary<string, TL_Timeline> timelines;           // 타임라인 정보 <GUID: 타임라인 키 목록>
    
    public Dictionary<string, TimelineObject> timelineObjects;  // 월드 상의 타임라인 오브젝트 목록

    // 고유값이 GUID에 해당하는 타임라인 객체를 반환
    public TimelineObject GetTimelineObject(string guid)
    {
        if (timelineObjects.ContainsKey(guid) == false)
            return null;

        return timelineObjects[guid];
    }

    void Update() { }

    float posY = 125;

    // 서버에서 불러온 타임라인 정보가 있을 경우,
    // 그리드에 배치하고 타임라인 UI에 키를 찍는다.
    void Initialize()
    {
        foreach (KeyValuePair<string, TL_Timeline> pair in timelines)
        {
            string guid = pair.Key;
            TL_Timeline timeline = pair.Value;

            // 프리팹을 객체화시켜 목록에 추가하고
            TimelineObject tlObject = Instantiate(GetPrefab(timeline.tlType, timeline.itemName)).GetComponent<TimelineObject>();
            tlObject.Initialize(timeline.tlType, timeline.itemName, guid, timeline.GetKeys().First());
            timelineObjects.Add(guid, tlObject);
            // 그리드에 배치한다.
            BuildingSystem.Instance.objectList.Add(tlObject.GetComponent<PlaceableObject>());

            // 팀원이 관리하는 소품 목록에도 추가해준다.
            ObjectListName objectListName = AddList.Instance.AddObjectList(guid, timeline);
            // 팀원의 타임라인 UI에 키를 찍어준다.
            foreach (TL_Types.Key key in timeline.GetKeys().Skip(1))
                KeyManager.Instance.AddObjectKey(objectListName, key, posY);

            posY -= 50;
        }

        // 팀원의 타임라인 재생기에 타임라인 정보를 로드시킨다.
        GetComponent<TimelinePlayer>().LoadKeyData();
    }

    // 현재 작업 중인 공연 정보를 DB에 업로드하고,
    // cdata 형식으로 변환된 타임라인, 썸네일, BGM을 FTP 서버에 업로드
    public bool Save()
    {
        bool b1, b2;

        concertData.cdata = TL_Utility.ToCDATA(timelines);

        if (bNewConcert)
        {
            b1 = ConcertManager.NewConcert(concertInfo);
            b2 = ConcertManager.NewConcertData(concertData);
        }
        else
        {
            b1 = ConcertManager.SetConcert(concertInfo);
            b2 = ConcertManager.SetConcertData(concertData);
        }

        return b1 & b2;
    }

    // 소품 종류에 해당하는 경로에서 소품 이름에 해당하는 프리팹을 가져온다.
    public GameObject GetPrefab(TL_ENUM_Types tlType, string itemName)
    {
        return Resources.Load(tlType.ToString() + "/" + itemName) as GameObject;
    }

    // 타임라인에 소품(GUID)이 존재하는지 반환
    public bool isTimelineExist(string guid)
    {
        return timelines.ContainsKey(guid);
    }

    // 새로운 소품(GUID)을 타임라인에 추가
    public bool NewTimeline(string guid, TL_ENUM_Types tlType, string itemName)
    {
        timelines[guid] = new TL_Timeline(tlType, itemName);

        return timelines.ContainsKey(guid);
    }

    // 특정 소품(GUID)의 모든 키를 반환
    public TL_Timeline GetTimeline(string guid)
    {
        if (timelines.ContainsKey(guid))
            return timelines[guid];
        else
            return null;
    }

    // 특정 소품(GUID)에 타임라인 키를 추가
    public bool AddKey(string guid, int frame, bool active, Transform tr)
    {
        TL_Types.Key key;

        if (isTimelineExist(guid) == false)
            return false;
        else if ((key = GenerateKey(timelines[guid].tlType, frame, active, tr)) != null)
            return timelines[guid].AddKey(key);
        else
            return false;
    }

    // 소품 종류에 해당하는 타임라인 키를 생성
    TL_Types.Key GenerateKey(TL_ENUM_Types tlType, int frame, bool active, Transform tr)
    {
        switch (tlType)
        {
            case TL_ENUM_Types.Object :
                return new TL_Types.Object(frame, active, tr.position, tr.rotation);
            case TL_ENUM_Types.Effect :
                return new TL_Types.Effect(frame, active, tr.position, tr.rotation);
            case TL_ENUM_Types.Light :
                return new TL_Types.Light(frame, active, tr.position, tr.rotation);
        }

        return null;
    }

    // 타임라인에서 특정 소품(GUID)의 타임라인 키를 삭제
    public bool DeleteKey(string guid, TL_Types.Key key)
    {        
        if (isTimelineExist(guid) == false)
            return false;

        return timelines[guid].DeleteKey(key);
    }

    // 타임라인에서 특정 소품(GUID)의 프레임에 해당하는 타임라인 키를 삭제
    public bool DeleteKey(string guid, int frame)
    {
        if (isTimelineExist(guid) == false)
            return false;

        return timelines[guid].DeleteKey(frame);
    }

    // 타임라인에서 특정 소품(GUID)의 모든 타임라인 키 삭제
    public bool DeleteAllKeys(string guid)
    {
        if (isTimelineExist(guid) == false)
            return false;

        return timelines[guid].DeleteAllKeys();
    }

    // 타임라인에서 특정 소품(GUID)의 프레임이 몇 번째인지 반환
    public int IndexOf(string guid, int frame)
    {
        return timelines[guid].IndexOf(frame);
    }

    // 타임라인에서 특정 소품의 타임라인 키 정보를 갱신
    public bool UpdateKey(TimelineKey tlKey)
    {
        if (isTimelineExist(tlKey.guid) == false)
            return false;

        return timelines[tlKey.guid].UpdateKey(tlKey);
    }

    public Dictionary<string, TL_Timeline> GetTimelines()
    {
        return timelines;
    }
}
