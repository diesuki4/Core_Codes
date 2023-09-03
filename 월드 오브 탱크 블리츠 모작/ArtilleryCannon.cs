using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// AI 자주포 탱크의 포탑이다.
public class ArtilleryCannon : MonoBehaviour
{
    public GameObject missile;      // 미사일
    public Transform muzzle;        // 총구의 트랜스폼
    public float missileDeadline;   // 소멸 수명
    public float rotSensitivity;    // 포탑의 회전 속도

    GameObject shooter;
    GameObject target;
    float damage;

    void Start()  { }

    void Update()
    {
        TurretFollowTarget();
    }

    // 포탑이 지정된 타겟을 향해 Y축 회전한다.
    void TurretFollowTarget()
    {
        if (target == null)
            return;

        Vector3 targetDir = Vector3.ProjectOnPlane(target.transform.position - transform.position, Vector3.up).normalized;
        Quaternion destinationRot = Quaternion.LookRotation(targetDir);

        transform.rotation = Quaternion.Lerp(transform.rotation, destinationRot, Time.deltaTime * rotSensitivity);
    }

    public void Initialize(GameObject shooter)
    {
        this.shooter = shooter;
        this.damage = shooter.GetComponent<ArtilleryTank>().damage;
    }

    // 미사일 발사 함수
    public void Fire()
    {
        ArtilleryMissile artyMissile = Instantiate(missile, muzzle.position, muzzle.rotation).GetComponent<ArtilleryMissile>();
        
        artyMissile.Initialize(shooter, damage, target.transform.position, missileDeadline);
    }

    public void SetTarget(GameObject target)
    {
        this.target = target;
    }

    public IEnumerator WaitUntilAngleLessThan(float angleUntil = 1.f)
    {
        // 각도 차이가 angleUntil보다 큰 동안 대기한다.
        while (angleUntil < Quaternion.Angle(Quaternion.LookRotation(Vector3.ProjectOnPlane(transform.forward, Vector3.up)),
                    Quaternion.LookRotation(Vector3.ProjectOnPlane(target.transform.position - transform.position, Vector3.up).normalized)))
            yield return null;
    }
}
