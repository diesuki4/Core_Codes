using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 탱크가 발사하는 포탄이다.
// (탱크는 총알과 포탄 2가지를 쏠 수 있다.)
// 유연한 판정을 위해 충돌 검사와 레이 검사를 동시에 진행한다.
public class Projectile : MonoBehaviour
{
    [Header("소멸 수명")]
    public int lifetime;
    [Header("초기 속도")]
    public float speed = 80;
    [Header("최대 속도")]
    public float speedMax = 80;
    [Header("초당 증가하는 속도")]
    public float speedMult = 1;
    [Header("발사체 타격 효과")]
    public GameObject explosionVFX;
    
    public float explosionRadius = 20;
    public float explosionForce = 1000;
    public float damageRadius = 20;

    GameObject shooter;  // 쏜 사람
    int targetLayer;     // 상대 팀의 레이어
    int damage;

    Rigidbody rigidBody;
    // 현재 위치 기준 back 방향 1unit 위치
    Vector3 prevpos;

    void Start()
    {
        rigidBody = GetComponent<Rigidbody>();

        prevpos = transform.position - transform.forward;

        // lifetime 뒤에 소멸
        Destroy(gameObject, lifetime);
    }

    void Update()
    {
        float magnitude = Vector3.Distance(transform.position, prevpos) + 1;

        // 충돌 검사와 더불어, 이전 위치에서도 레이를 쏘아 판정한다.
        RaycastHit[] hits = Physics.RaycastAll(transform.position + (-transform.forward * magnitude), transform.forward, magnitude);

        foreach (RaycastHit hit in hits)
        {
            // 감지된 게 발사체 자신이 아니고 쏜 탱크가 아니면
            if (hit.transform.root != transform.root && (!shooter || GetTankTransform(hit.transform) != GetTankTransform(shooter.transform))) {
                // 이펙트 표시, 폭발 데미지 처리
                ProcessExplosion(hit.point);
                break;
            }
        }

        prevpos = transform.position;
    }

    void FixedUpdate()
    {
        // 증가하는 speed 에 따라 velocity 를 적용한다.
        rigidBody.velocity = transform.forward * speed;

        // 최대 속도로 Clamp
        speed = Mathf.Clamp(speed + speedMult * Time.fixedDeltaTime, 0, speedMax);
    }

    public void Initialize(GameObject shooter)
    {
        this.shooter = shooter;
        damage = shooter.GetComponent<TankAI>().damage;
        targetLayer = TagObjectFinder.Instance.OpponentLayer(shooter.layer);

        Rigidbody shooterRB = shooter.GetComponent<Rigidbody>();

        // 앞 방향으로 발사력을 가한다.
        rigidBody.velocity = shooterRB.velocity;
        rigidBody.AddForce(transform.forward * projectileShootForce, ForceMode.Acceleration);

        SetIgnoreSelf();
    }

    // 이펙트 표시, 폭발 데미지 처리 부분
    public void ProcessExplosion(Vector3 position)
    {
        GameObject effect = Instantiate(explosionVFX, position, transform.rotation);
        Destroy(effect, 3);

        // 현재 위치에서 explosionRadius 내에 있는 모든 Collider
        foreach (Collider hit in Physics.OverlapSphere(position, explosionRadius))
        {
            Rigidbody hitRB = null;

            if (!hit)
                continue;
            else
                hitRB = hit.GetComponent<Rigidbody>();

            // 폭발력을 가한다.
            if (hitRB)
                hitRB.AddExplosionForce(explosionForce, position, explosionRadius, 3.0f);
        }

        // 현재 위치에서 damageRadius 내에 있는 모든 Collider
        for (Collider hit in Physics.OverlapSphere(position, damageRadius, 1 << targetLayer))
        {
            if (!hit)
                continue;

            // 피아 식별, 내가 포함됐는지 검사한다.
            if (IsDamageAvailable(hit.gameObject))
            {
                if (!shooter || (shooter && hit.gameObject != shooter.gameObject))
                {
                    // 상대 탱크에게 데미지를 적용한다.
                    HPManager targetHPManager = hit.transform.GetComponent<HPManager>();

                    if (targetHPManager)
                        targetHPManager.ApplyDamage(shooter, damage);
                }
            }
        }

        // 소멸
        Destroy(gameObject);
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

    // 발사체를 쏜 탱크 자신과는 충돌하지 않도록 처리
    void SetIgnoreSelf()
    {
        Collider myCol = GetComponent<Collider>();
        Collider[] childCols = GetTankTransform(shooter.transform).GetComponentsInChildren<Collider>();

        foreach (Collider childCol in childCols)
            Physics.IgnoreCollision(myCol, childCol);
    }

    void OnCollisionEnter(Collision collision)
    {
        // 피아 식별, 발사체끼리 충돌을 검사한다.
        if (IsDamageAvailable(collision.gameObject) && collision.gameObject.tag != gameObject.tag)
            // 이펙트 표시, 폭발 데미지 처리
            ProcessExplosion(transform.position);
    }
}
