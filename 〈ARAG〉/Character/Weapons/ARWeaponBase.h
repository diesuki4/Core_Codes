#pragma once

#include "ARAG.h"
#include "GameFramework/Actor.h"

#include "Character/ARCharacter.h"
#include "Character/ARCharacterAnimInstance.h"
#include "Animation/AnimMontage.h"

#include "ARWeaponBase.generated.h"

// 무기 장착 델리게이트
DECLARE_MULTICAST_DELEGATE(FOnEquipDelegate);
// 무기 해제 델리게이트
DECLARE_MULTICAST_DELEGATE(FOnUnequipDelegate);

/* 무기 Base */
UCLASS()
class ARAG_API AARWeaponBase : public AActor
{
    GENERATED_BODY()

public:
    AARWeaponBase();

protected:
    virtual void BeginPlay() override;

public:
    UFUNCTION(BlueprintCallable)
    virtual void Equip(AARCharacter* NewCharacter);
    UFUNCTION(BlueprintCallable)
    virtual void Unequip();

    virtual void LfMousePressed();      // 좌클릭 누를 시 호출
    virtual void LfMouseReleased();     // 좌클릭 뗄 시 호출
    virtual void RtMousePressed();      // 우클릭 누를 시 호출

    virtual bool CanUse();              // 현재 사용 가능한 상태인지 반환

    float GetWeaponDamage() { return WeaponDamage; }

    FOnEquipDelegate OnEquip;
    FOnUnequipDelegate OnUnequip;

protected:
    // 각 애님 노티파이 시 호출되는 함수
    virtual void AttackStart();         // AttackStart
    virtual void Attack();              // Attack
    virtual void AttackEnd();           // AttackEnd

    virtual void MeleeAttack();         // 활, 총 등의 무기도 근접 공격을 지원 
    // ARCharacter->OnStateChanged 델리게이트에 등록할 함수
    virtual void OnCharacterStateChanged(EARCharacterState OldState, EARCharacterState NewState);

    AARCharacter* Character;
    
    UPROPERTY()
    UARCharacterAnimInstance* CharacterAnimInstance;
    // 무기별 애니메이션 몽타주
    UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = Weapon, Meta = (AllowPrivateAccess = true))
    UAnimMontage* AttackMontage;
    // 무기별 Input Mapping Context
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = Weapon, meta = (AllowPrivateAccess = true))
    class UInputMappingContext* WeaponMappingContext;
    // 무기별 장착 위치
    UPROPERTY(EditDefaultsOnly, Category = Weapon, meta = (AllowPrivateAccess = true))
    FName WeaponSocket;
    // 무기 공격력
    UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = Weapon, meta = (AllowPrivateAccess = true))
    float WeaponDamage;

    // 근접 공격 판정 거리
    UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = Weapon, meta = (AllowPrivateAccess = true))
    float MeleeAttackRange;
    // 근접 공격 판정 반지름
    UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = Weapon, meta = (AllowPrivateAccess = true))
    float MeleeAttackRadius;
    // Aim Offset 사용 여부
    UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = Weapon, meta = (AllowPrivateAccess = true))
    bool bUseAimOffset;
};
