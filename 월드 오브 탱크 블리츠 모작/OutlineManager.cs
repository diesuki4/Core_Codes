using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 플레이어 화면 상 크로스헤어 안에 들어온 탱크에 외곽선을 표시하는 컴포넌트이다.
// 화면 중앙에서 반지름이 radius인 원 내에 deltaRadius/deltaAngle 마다 레이를 쏘아 탱크를 확인한다.

// 샤워기와 같은 원리이다.
// 스피어 트레이스의 경우 거리가 멀어질수록, 크로스헤어보다 매우 작아진다는 문제점이 있다.
public class OutlineManager : MonoBehaviour
{
    // 디버그 모드 활성화 시 레이가 보이게 된다.
    public bool debugMode;

    public float radius;
    public float deltaRadius;
    public float deltaAngle;
    // 게임 상 AI 목록
    public GameObject[] arrAI;

    // 화면 상 크로스헤어 좌표
    Vector3 crossHairPos2D;

    void Start()
    {
        crossHairPos2D = new Vector3(Screen.width / 2, Screen.height / 2 + 108);
    }

    void Update()
    {
        float currentRadius = 0;
        List<Ray> rays = new List<Ray>();

        // 안쪽에서부터 샤워기처럼 레이를 생성한다.
        while (currentRadius <= radius)
        {
            float currentAngle = 0;

            while (currentAngle < 360)
            {
                Vector3 deltaVec = Quaternion.Euler(0, 0, currentAngle) * Vector3.right;
                Vector3 newCameraPos = crossHairPos2D + deltaVec * currentRadius;
                
                Ray ray = Camera.main.ScreenPointToRay(newCameraPos);
                rays.Add(ray);

                if (debugMode)
                    Debug.DrawLine(ray.origin, ray.origin + ray.direction * 500, Color.red, Time.deltaTime);

                currentAngle += deltaAngle;

                if (currentRadius == 0)
                    break;
            }

            currentRadius += deltaRadius;
        }

        List<GameObject> objDetected = new List<GameObject>();

        // 생성한 레이들에서 판정을 진행한 후 외곽선을 활성화한다.
        foreach (Ray ray in rays)
        {
            RaycastHit hitInfo;
            
            if (Physics.Raycast(ray, out hitInfo))
            {
                GameObject hitObj = hitInfo.transform.gameObject;
                Outline outline = null;

                if (objDetected.Contains(hitObj))
                    continue;

                if ((outline = hitObj.GetComponent<Outline>()) != null)
                {
                    outline.enabled = true;
                    objDetected.Add(hitObj);

                    break;
                }
            }
        }

        // 리스트에 담기지 않은(판정되지 않은) 탱크는 외곽선을 꺼준다.
        foreach (GameObject AI in arrAI)
            if (!objDetected.Contains(AI) && AI)
                AI.GetComponent<Outline>().enabled = false;
    }
}
