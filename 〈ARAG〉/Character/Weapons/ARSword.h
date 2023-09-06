#pragma once

#include "ARAG.h"
#include "Character/Weapons/ARWeaponSkeletalBase.h"
#include "ARSword.generated.h"
/*
    검 무기 (AR Weapon Base - AR Weapon Skeletal Base - AR Sword)
*/
UCLASS()
class ARAG_API AARSword : public AARWeaponSkeletalBase
{
    GENERATED_BODY()

public:
    AARSword();

public:
    // 검 사용 -> 애니메이션 재생 -> Attack 애님 노티파이 -> Attack() 실행
    virtual void LfMousePressed() override;

protected:
    virtual void Attack() override;  // Attack 애님 노티파이 시 호출되는 함수
};
