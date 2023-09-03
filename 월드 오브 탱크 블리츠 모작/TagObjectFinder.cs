using System;
using System.Linq;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 아군은 Player, 적은 Enemy 태그를 갖는다.
// 모든 AI가 상대를 찾기 위해 FindGameObjectsWithTag()를 호출하면
// 부하가 너무 크기 때문에 최적화를 위해 구현된 풀이다.
public class TagObjectFinder : MonoBehaviour
{
    public static TagObjectFinder Instance;

    // <태그 : 오브젝트 목록>을 딕셔너리로 관리한다.
    Dictionary<string, GameObject[]> targetList;

    string[] tankTags   = {"Player", "Enemy"};
    string[] tankLayers = {"Ally", "Enemy"};

    void Awake()
    {
        if (Instance == null)
            Instance = this;

        targetList = new Dictionary<string, GameObject[]>();
    }

    void Start() { }

    void Update() { }

    // 딕셔너리에서 태그에 해당하는 오브젝트 목록을 검색한다.
    public GameObject[] FindObjectWithTag(string tag)
    {
        // 태그가 있으면
        if (targetList.ContainsKey(tag))
        {
            GameObject[] objsWithTag;

            // 목록을 가져와서
            if (targetList.TryGetValue(tag, out objsWithTag))
                // 반환한다.
                return objsWithTag;
            // 목록이 존재하지 않으면
            else
                // null 반환.
                return null;
        }
        // 태그가 없으면
        else
        {
            // 딕셔너리에 목록을 추가하고
            UpdateTargetList(tag);

            // 반환한다.
            return targetList[tag];
        }
    }

    // 태그(키)에 해당하는 오브젝트 목록(값)을 갱신한다.
    public void UpdateTargetList(string tag)
    {
        targetList[tag] = GameObject.FindGameObjectsWithTag(tag);
    }

    // 상대 팀의 태그를 반환한다.
    public string OpponentTag(string tag)
    {
        return (tag == tankTags[0]) ? (tankTags[1]) : (tag == tankTags[1]) ? (tankTags[0]) : "Untagged";
    }

    // 상대 팀의 레이어를 반환한다.
    public int OpponentLayer(int layer)
    {
        int allyLayer = LayerMask.NameToLayer(tankLayers[0]);
        int enemyLayer = LayerMask.NameToLayer(tankLayers[1]);

        return (layer == allyLayer) ? (enemyLayer) : (layer == enemyLayer) ? (allyLayer) : LayerMask.NameToLayer("Default");
    }
}
