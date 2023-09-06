#pragma once

#include "ARAG.h"
#include "Animation/AnimInstance.h"

#include "Monster/Types/ARMonsterMontageSectionName.h"
#include "Monster/Types/ARMonsterState.h"

#include "ARMonsterAnimInstance.generated.h"

// 각 애님 노티파이 시 발동하는 델리게이트
DECLARE_MULTICAST_DELEGATE(FOnAttackDelegate);          // Attack
DECLARE_MULTICAST_DELEGATE(FOnAnimationEndDelegate);    // AnimationEnd

/* 몬스터 애님 인스턴스 */
UCLASS()
class ARAG_API UARMonsterAnimInstance : public UAnimInstance
{
    GENERATED_BODY()
    
public:
    UARMonsterAnimInstance();

    virtual void NativeInitializeAnimation() override;
    virtual void NativeUpdateAnimation(float DeltaSeconds) override;

    void PlayAttackMontage();
    EARMonsterMontageSectionName GetCurrentSection();                           // 현재 섹션을 열거형으로 가져오기
    void JumpToMontageSection(EARMonsterMontageSectionName NewSection);
    FName GetAttackMontageSectionName(EARMonsterMontageSectionName Section);    // 열거형을 통해 섹션 이름 가져오기

    FOnAttackDelegate OnAttack;
    FOnAnimationEndDelegate OnAnimationEnd;
    // 현재 섹션
    EARMonsterMontageSectionName CurrentSection;

private:
    // 소유자 몬스터 캐싱
    class AARMonsterBase* Monster;
    // ARMonsterBase->OnStateChanged 델리게이트를 통해 받아오는 상태
    UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = Monster, Meta = (AllowPrivateAccess = true))
    EARMonsterState MonsterState;

    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = Monster, Meta = (AllowPrivateAccess = true))
    float CurrentSpeed;
    // 공격 섹션 이름
    UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = Monster, Meta = (AllowPrivateAccess = true))
    FName AttackSectionName = FName(TEXT("AttackSection"));

    UFUNCTION()
    void AnimNotify_Attack()        { OnAttack.Broadcast(); }

    UFUNCTION()
    void AnimNotify_AnimationEnd()  { OnAnimationEnd.Broadcast(); }

public:
    // 데이터 에셋에서 기본 이동 Blendspace 가져오기
    UFUNCTION(BlueprintCallable, meta = (BlueprintThreadSafe))
    class UBlendSpace* GetMovementBlendspace() const;
    // 데이터 에셋에서 공격 몽타주 가져오기
    UFUNCTION(BlueprintCallable, meta = (BlueprintThreadSafe))
    class UAnimMontage* GetAttackMontage() const;
    // 데이터 에셋에서 상태에 해당하는 애니메이션 가져오기 (피격, 죽음)
    UFUNCTION(BlueprintCallable, meta = (BlueprintThreadSafe))
    class UAnimSequenceBase* GetAnimByState(EARMonsterState State) const;
};
