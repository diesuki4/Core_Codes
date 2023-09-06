#pragma once

#include "ARAG.h"
#include "Character/Weapons/ARWeaponBase.h"
#include "ARWeaponSkeletalBase.generated.h"
/*
    Skeletal Mesh를 메시로 갖는(애니메이션이 있는) 무기
    (AR Weapon Base - AR Weapon Skeletal Base)
*/
UCLASS()
class ARAG_API AARWeaponSkeletalBase : public AARWeaponBase
{
    GENERATED_BODY()
    
public:
    AARWeaponSkeletalBase();

public:
    virtual void Equip(AARCharacter* NewCharacter) override;
    virtual void Unequip() override;

    UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = WeaponSkeletal, meta = (AllowPrivateAccess = true))
    USkeletalMeshComponent* SkeletalMeshComponent;
};
