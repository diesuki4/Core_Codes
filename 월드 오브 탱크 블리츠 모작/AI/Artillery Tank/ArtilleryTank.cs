using System.Linq;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.AI;

// 일정 시간마다 적을 향해 미사일을 발사하는 AI 자주포 탱크다.
public class ArtilleryTank : MonoBehaviour
{
    // 탱크의 포탑 컴포넌트
    public ArtilleryCannon artyCannon;
    // 발사 간격
    public float fireEverySecs;
    // 초기 발사 딜레이
    public float initialFireDelay;
    // 최소 포탄 발사 거리
    public float minFireRange;
    // 푀대 포탄 발사 거리
    public float maxFireRange;
    public float damage = 120;

    IEnumerator Start()
    {
        artyCannon.Initialize(gameObject);

        yield return new WaitForSeconds(initialFireDelay);

        // 일정 간격마다 포탄을 발사하는 코루틴을 실행한다.
        while (true)
            yield return StartCoroutine(IEFire());
    }

    void Update() { }

    IEnumerator IEFire()
    {
        // 지정 가능한 타겟을 찾는다.
        GameObject target = FindTarget(minFireRange, maxFireRange);
        // 찾은 타겟이 있으면
        if (target)
        {
            // 포탑에 타겟을 지정한다. (타겟이 지정되면 포탑이 타겟 방향으로 Y축 회전한다.)
            artyCannon.SetTarget(target);
            // 지정한 각도차 이하가 될 때까지 기다린다.
            yield return StartCoroutine(artyCannon.WaitUntilAngleLessThan(1.f));
            // 미사일 발사
            artyCannon.Fire();
            // 발사 쿨타임 동안 대기한다.
            yield return new WaitForSeconds(fireEverySecs);
        }
        // 찾은 타겟이 없으면
        else
        {
            // 한 프레임을 넘긴다.
            yield return null;
        }
    }

    // minDistance ~ maxDistance 내에 있는 전차를 랜덤하게 타겟으로 지정한다.
    GameObject FindTarget(float minDistance, float maxDistance)
    {
        // 상대 팀의 태그를 찾는다.
        string opponentTag = TagObjectFinder.Instance.OpponentTag(gameObject.tag);
        // 해당 태그를 가진 모든 오브젝트 목록을 가져온다.
        GameObject[] objsWithTag = TagObjectFinder.Instance.FindObjectWithTag(opponentTag);
        // minDistance ~ maxDistance 내에 있는 오브젝트를 필터링한다.
        GameObject[] inRangeObjs = objsWithTag.Where(x => minDistance <= Vector3.Distance(transform.position, x.transform.position)
                                                     && Vector3.Distance(transform.position, x.transform.position) <= maxDistance).ToArray();

        // 자주포를 제외한 일반(HPManager 컴포넌트를 가진 탱크) 탱크만을 필터링
        inRangeObjs = inRangeObjs.Where(x => x.GetComponent<HPManager>()).ToArray();
        
        // 후보가 1대 이상일 때
        if (1 <= inRangeObjs.Length)
            // 랜덤하게 타겟을 지정한다.
            return inRangeObjs[Random.Range(0, inRangeObjs.Length)];
        // 후보를 찾지 못했으면
        else
            // null 반환
            return null;
    }
}
