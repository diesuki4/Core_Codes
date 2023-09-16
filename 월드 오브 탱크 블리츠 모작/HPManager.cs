using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 모든 탱크에 붙어있는 체력 컴포넌트이다.
// 점수 계산을 위해 자신을 마지막으로 때린 적을 저장하며, 데미지 처리와 죽음을 수행한다.
public class HPManager : MonoBehaviour
{
    [Header("죽었을 때 변경되는 파괴된 탱크 모델")]
    public GameObject destroyedModel;
    [Header("최대 체력")]
    public float maxHp = 200;
    [HideInInspector]
    public float hp;
    // 마지막으로 자신을 맞춘 탱크
    [HideInInspector]
    public GameObject lastHit;

    void Start()
    {
        hp = maxHp;

        lastHit = null;
    }

    void Update() { }

    // 데미지 적용
    public void ApplyDamage(GameObject shooter, float damage)
    {
        if (hp <= 0)
            return;

        // 마지막으로 자신을 맞춘 탱크를 저장하고
        lastHit = shooter;
        // 데미지 적용 (오버로드 함수)
        ApplyDamage(damage);
    }

    // 데미지 적용 (오버로드 함수)
    public void ApplyDamage(float damage)
    {
        if (hp <= 0)
            return;

        // HP를 감소시킨다.
        hp -= damage;
        
        // 플레이어 탱크는 MainBodySetting 컴포넌트를 갖는다.
        // 플레이어 탱크면 피격 메시지 출력
        if (GetComponent<MainBodySetting>())
            UIAnim.Instance.ShowLog(UIAnim.PanelType.Right);

        // 체력이 없으면 죽는다.
        if (hp <= 0)
            Dead();
    }

    // 죽음 처리
    void Dead()
    {
        // 자신을 죽인 탱크의 점수를 증가시킨다.
        if (lastHit)
            ++lastHit.GetComponent<Tank>().killScore;

        // 파괴된 탱크 모델을 현재 위치에 생성한다.
        Instantiate(destroyedModel, transform.position, transform.rotation);

        // 태그 오브젝트 풀 갱신을 요청해, 자신을 제외하도록 한다.
        string o_tag = gameObject.tag;

        gameObject.tag = "Untagged";
        TagObjectFinder.Instance.UpdateTargetList(o_tag);

        hp = 0;
        gameObject.SetActive(false);
    }
}
