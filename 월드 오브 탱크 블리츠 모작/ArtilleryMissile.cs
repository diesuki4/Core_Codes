using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// AI 자주포 탱크가 발사하는 미사일이다.
public class ArtilleryMissile : MonoBehaviour
{
    // 쏜 사람
    GameObject shooter;

    // 포물선 궤도의 곡률
    public float curvature;
    public float damageRadius;
    public GameObject explosionVFX;

    Vector3 destination;
    Vector3 originPos;
    Vector3 prevPos;

    float damage;
    float t;

    void Start() { }

    void Update()
    {
        t += Time.deltaTime;

        // 구면 보간을 이용해 미사일을 포물선 궤도로 이동시킨다.
        Vector3 center = (originPos + destination) * 0.5f - new Vector3(0, curvature, 0);
        Vector3 relOriginPos = originPos - center;
        Vector3 relDestination = destination - center;

        transform.position = Vector3.Slerp(relOriginPos, relDestination, t / deadline) + center;
        // 미사일의 방향을 갱신한다.
        transform.forward = (transform.position - prevPos).normalized;

        if (deadline <= t)
            Explosion();
    }

    public void Initialize(GameObject shooter, float damage, Vector3 destination, float deadline = 3.f)
    {
        this.shooter = shooter;
        this.damage = damage;
        this.destination = destination;
        this.deadline = deadline;

        originPos = prevPos = transform.position;
    }

    // 인자로 받은 탱크에 데미지를 입힐 수 있는지 반환
    public bool IsDamageAvailable(GameObject go)
    {
        // 상대 팀의 태그와 다른지 확인한다.
        if (go && GetTankTransform(go.transform).CompareTag(TagObjectFinder.Instance.OpponentTag(shooter.tag)))
            return true;
        else
            return false;
    }

    // 부모 트랜스폼을 재귀적으로 타고 올라가며, 탱크의 트랜스폼을 찾는다.
    // (탱크는 HPManager 컴포넌트를 갖고 있다.)
    Transform GetTankTransform(Transform tr)
	{
        if (tr.GetComponent<HPManager>() || tr == tr.root)
            return tr;

		return GetTankTransform(tr.parent);
	}

    // 미사일 폭발
    // 반경 damageRadius 이내의 적에게 폭발 지점으로부터의 거리에 따라 데미지를 입힌다.
    void Explosion()
    {
        GameObject expVFX = Instantiate(explosionVFX, transform.position, transform.rotation);
        Destroy(expVFX, 3);

        Collider[] hitCols = Physics.OverlapSphere(transform.position, damageRadius, 1 << TagObjectFinder.Instance.OpponentLayer(shooter.layer));

        foreach (Collider col in hitCols)
        {
            if (!col)
                continue;

            if (IsDamageAvailable(col.gameObject))
            {
                if (!shooter || (shooter && col.gameObject != shooter.gameObject))
                {
                    HPManager targetHPManager = col.transform.GetComponent<HPManager>();

                    if (targetHPManager)
                        targetHPManager.ApplyDamage((int)(Mathf.Lerp(damage, 0f, Vector3.Distance(transform.position, col.transform.position) / damageRadius)));
                }
            }
        }

        Destroy(gameObject);
    }

    void OnCollisionEnter(Collision other)
    {
        if (IsDamageAvailable(other.gameObject) && other.gameObject.tag != gameObject.tag)
            Explosion();
    }
}
