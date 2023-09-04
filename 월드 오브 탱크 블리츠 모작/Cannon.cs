using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 일반 AI 탱크의 포탑이다.
// 일반 AI 탱크의 포탑에서는 포탄과 총알 2가지를 쏠 수 있다.
public class Cannon : MonoBehaviour
{
    public enum Weapon
    {
        Bullet,
        Projectile
    }

    [HideInInspector]
    public GameObject owner;
    public GameObject projectile;
    public GameObject bullet;
    // 탄퍼짐 정도
    public float spread = 1;
    // 포탄 발사력
    public float projectileShootForce = 8000;

    public GameObject muzzleEffect;
    public float muzzleLifeTime = 2;

    public AudioClip shootSound;

    [HideInInspector]
    AudioSource audioSource;

    void Start()
    {
        owner = GetTankTransform(transform).gameObject;

        audioSource = GetComponent<AudioSource>();
    }

    void Update() { }

    public void Shoot(Weapon weapon = Weapon.Bullet)
    {
        if (muzzleEffect)
        {
            GameObject muzzle = Instantiate(muzzleEffect, transform.position, transform.rotation, transform);
            Destroy(muzzle, muzzleLifeTime);
        }

        // 탄퍼짐 정도를 설정할 수 있다.
        Vector3 spreadVector = new Vector3(Random.Range(-spread, spread), Random.Range(-spread, spread), Random.Range(-spread, spread)) / 100;
        Vector3 direction = transform.forward + spreadVector;

        switch (weapon)
        {
            case Canon.Weapon.Bullet:
            {
                GameObject goBullet = Instantiate(bullet, transform.position, Quaternion.LookRotation(direction));
                Bullet newBullet = goBullet.GetComponent<Bullet>();
                newBullet.Initialize(owner);
                break;
            }
            case Canon.Weapon.Projectile:
            {
                GameObject goProjectile = Instantiate(projectile, transform.position, Quaternion.LookRotation(direction));
                Projectile newProjectile = goProjectile.GetComponent<Projectile>();
                newProjectile.Initialize(owner);
                break;
            }
        }

        audioSource.PlayOneShot(shootSound);
    }

    // 부모 트랜스폼을 재귀적으로 타고 올라가며, 탱크의 트랜스폼을 찾는다.
    // (탱크는 HPManager 컴포넌트를 갖고 있다.)
    Transform GetTankTransform(Transform tr)
    {
        if (tr.GetComponent<HPManager>() || tr == tr.root)
            return tr;

        return GetTankTransform(tr.parent);
    }
}
