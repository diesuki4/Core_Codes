#pragma once

#include "ARAG.h"
#include "Animation/AnimInstance.h"

#include "Character/Types/ARCharacterMontageSectionName.h"
#include "Character/Types/ARCharacterState.h"

#include "ARCharacterAnimInstance.generated.h"

// 각 애님 노티파이 시 발동하는 델리게이트
DECLARE_MULTICAST_DELEGATE(FOnAttackStartDelegate);     // AttackStart
DECLARE_MULTICAST_DELEGATE(FOnAttackDelegate);          // Attack
DECLARE_MULTICAST_DELEGATE(FOnAttackEndDelegate);       // AttackEnd
DECLARE_MULTICAST_DELEGATE(FOnAnimationEndDelegate);    // AnimationEnd (피격, 죽음 애니메이션)

class UAnimMontage;

/* 메인 캐릭터 애님 인스턴스 */
UCLASS()
class ARAG_API UARCharacterAnimInstance : public UAnimInstance
{
    GENERATED_BODY()
    
public:
    UARCharacterAnimInstance();

    virtual void NativeInitializeAnimation() override;

    void SetAttackMontage(UAnimMontage* NewAttackMontage);
    void PlayAttackMontage();
    EARCharacterMontageSectionName GetCurrentSection();                           // 현재 섹션을 열거형으로 가져오기
    void JumpToMontageSection(EARCharacterMontageSectionName NewSection);
    FName GetAttackMontageSectionName(EARCharacterMontageSectionName Section);    // 열거형을 통해 섹션 이름 가져오기

    FOnAttackStartDelegate OnAttackStart;
    FOnAttackDelegate OnAttack;
    FOnAttackEndDelegate OnAttackEnd;
    FOnAnimationEndDelegate OnAnimationEnd;
    // 현재 섹션
    EARCharacterMontageSectionName CurrentSection;

private:
    // ARCharacter->OnStateChanged 델리게이트를 통해 받아오는 상태
    UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = Charactrer, Meta = (AllowPrivateAccess = true))
    EARCharacterState State;
    // 무기 장착 시 설정되는 무기별 애니메이션 몽타주
    UAnimMontage* AttackMontage;
    /* ML = Melee 근접 공격 */
    // 좌클릭을 누를 때 재생되는 섹션 이름
    UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = Attack, Meta = (AllowPrivateAccess = true))
    FName LfMousePressedSectionName = FName(TEXT("LfMousePressed"));
    UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = Attack, Meta = (AllowPrivateAccess = true))
    FName LfMousePressedMeleeSectionName = FName(TEXT("LfMousePressed_ML"));
    // 좌클릭을 뗄 때 재생되는 섹션 이름
    UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = Attack, Meta = (AllowPrivateAccess = true))
    FName LfMouseReleasedSectionName = FName(TEXT("LfMouseReleased"));
    UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = Attack, Meta = (AllowPrivateAccess = true))
    FName LfMouseReleasedMeleeSectionName = FName(TEXT("LfMouseReleased_ML"));
    // 우클릭을 누를 때 재생되는 섹션 이름
    UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = Attack, Meta = (AllowPrivateAccess = true))
    FName RtMousePressedSectionName = FName(TEXT("RtMousePressed"));
    UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = Attack, Meta = (AllowPrivateAccess = true))
    FName RtMousePressedMeleeSectionName = FName(TEXT("RtMousePressed_ML"));

    // 장착한 무기 별 Aim Offset 사용 여부
    UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = Character, Meta = (AllowPrivateAccess = true))
    bool bUseAimOffset;

    UFUNCTION()
    void AnimNotify_AttackStart()   { OnAttackStart.Broadcast(); }

    UFUNCTION()
    void AnimNotify_Attack()        { OnAttack.Broadcast(); }

    UFUNCTION()
    void AnimNotify_AttackEnd()     { OnAttackEnd.Broadcast(); }

    UFUNCTION()
    void AnimNotify_AnimationEnd()  { OnAnimationEnd.Broadcast(); }

public:
    void SetUsingAimOffset(bool bNewUseAimOffset) { bUseAimOffset = bNewUseAimOffset; }
};
