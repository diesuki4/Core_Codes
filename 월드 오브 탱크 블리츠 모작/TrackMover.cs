using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 탱크의 무한궤도 효과
public class TrackMover : MonoBehaviour
{
    // 방향
    public enum Direction
    {
        // 정방향
        Normal = 1,
        // 역방향
        Inverse = -1
    }

    [Header("정방향/역방향")]
    public Direction directon = Direction.Normal;
	public Transform wheels;

    [Header("속력")]
	public float speed = 1;

	Material mat;
	Vector2 UVDirection;

	void Start()
	{
		mat = GetComponent<Renderer>().material;
        UVDirection = new Vector2((float)directon, 0);
	}

	void Update() { }

    // UV 오프셋을 조정해 무한궤도를 돌린다.
	public void MoveTrack(Vector2 vector)
	{
		if (!mat)
			return;

		Vector2 moveVector = Vector2.zero;

		if (UVDirection.x != 0)
			moveVector.x = vector.x * UVDirection.x;
			
		if (UVDirection.y != 0)
			moveVector.y = vector.x * UVDirection.y;
			
		mat.mainTextureOffset += moveVector * speed * Time.deltaTime;

		// 실제 바퀴들도 회전시켜준다.
		if (wheels)
			foreach (Transform child in wheels)
				child.Rotate(Vector3.left * moveVector.x * speed * 5.f);
	}
}
