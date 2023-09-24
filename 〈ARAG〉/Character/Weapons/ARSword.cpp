#include "Character/Weapons/ARSword.h"

AARSword::AARSword()
{

}

// 검 사용 -> 애니메이션 재생 -> Attack 애님 노티파이 -> Attack() 실행
void AARSword::LfMousePressed()
{
    Super::LfMousePressed();

    ARCHECK(CharacterAnimInstance != nullptr);

    // 검을 사용할 수 있으면
    if (CanUse())
    {
        // 공격 몽타주 재생
        CharacterAnimInstance->PlayAttackMontage();
        CharacterAnimInstance->JumpToMontageSection(EARCharacterMontageSectionName::LfMousePressed_ML);
    }
}

// LfMousePressed() -> 애니메이션 재생 -> Attack 애님 노티파이 -> Attack() 실행
void AARSword::Attack()
{
    Super::Attack();
    // 근접 공격은 조부모인 Weapon Base에서 제공하는 기능이다.
    MeleeAttack();
}
