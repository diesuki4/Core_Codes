using System.Linq;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 자기장에 붙어 있는 데미지 처리 컴포넌트이다.
// 중심에서 2000unit 지점에서 중심 방향으로 레이를 발사해, 현재 자기장의 반지름을 계산한다.
public class MGFieldDamageManager : MonoBehaviour
{
    public static MGFieldDamageManager Instance;

    void Awake()
    {
        if (!Instance)
            Instance = this;
    }

    public LayerMask fieldLayerMask;    // 자기장의 레이어
    public GameObject[] tanks;          // 탱크 목록
    public float damagePerSec = 10;
    public float damageInterval = 1;
    [HideInInspector]
    public float fieldDistance; // 현재 자기장의 반지름

    Ray ray;
    float rayDistance = 2000;
    float t;

    void Start()
    {
        // 중심에서 2000unit 지점에서 중심 방향으로 레이를 설정한다.
        ray = new Ray(transform.position, transform.forward);
        ray.origin = ray.GetPoint(rayDistance);
        ray.direction = -ray.direction;
    }

    void Update()
    {
        t += Time.deltaTime;

        if (damageInterval <= t)
        {
            RaycastHit hitInfo;

            if (Physics.Raycast(ray, out hitInfo, rayDistance, fieldLayerMask))
            {
                // 현재 자기장의 반지름을 갱신
                fieldDistance = Vector3.Distance(transform.position, hitInfo.point);

                // 자기장 중심으로부터 fieldDistance 거리를 벗어난 탱크
                GameObject[] outRangeTanks = tanks.Where(x => x && fieldDistance <= Vector3.Distance(transform.position, x.transform.position)).ToArray();

                foreach (GameObject tank in outRangeTanks)
                {
                    // 데미지를 입힌다.
                    HPManager targetHPManager = tank.GetComponent<HPManager>();

                    if (targetHPManager)
                        targetHPManager.ApplyDamage(damagePerSec);
                }
            }

            t = 0;
        }
    }
}
