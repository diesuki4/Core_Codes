#include "Character/ARCharacterAnimInstance.h"

#include "Animation/AnimMontage.h"
#include "Character/ARCharacter.h"
#include "Kismet/KismetMathLibrary.h"

UARCharacterAnimInstance::UARCharacterAnimInstance()
{
    bUseAimOffset = false;
    State = EARCharacterState::Normal;
}

void UARCharacterAnimInstance::NativeInitializeAnimation()
{
    Super::NativeInitializeAnimation();

    AARCharacter* Character = Cast<AARCharacter>(GetOwningActor());
    ARCHECK(Character != nullptr);
    // 상태를 받아오기 위해 람다 함수 등록
    Character->OnStateChanged.AddLambda([this](EARCharacterState OldState, EARCharacterState NewState) { State = NewState; });
}

void UARCharacterAnimInstance::SetAttackMontage(UAnimMontage* NewAttackMontage)
{
    AttackMontage = NewAttackMontage;
}

void UARCharacterAnimInstance::PlayAttackMontage()
{
    if (!Montage_IsPlaying(AttackMontage))
    {
        Montage_Play(AttackMontage);
    }
}

// 현재 섹션을 열거형으로 가져오기
EARCharacterMontageSectionName UARCharacterAnimInstance::GetCurrentSection()
{
    return CurrentSection;
}

void UARCharacterAnimInstance::JumpToMontageSection(EARCharacterMontageSectionName NewSection)
{
    CurrentSection = NewSection;

    Montage_JumpToSection(GetAttackMontageSectionName(NewSection), AttackMontage);
}

// 열거형을 통해 섹션 이름 가져오기
FName UARCharacterAnimInstance::GetAttackMontageSectionName(EARCharacterMontageSectionName NewSection)
{
    switch (NewSection)
    {
        case EARCharacterMontageSectionName::LfMousePressed:     return LfMousePressedSectionName;
        case EARCharacterMontageSectionName::LfMousePressed_ML:  return LfMousePressedMeleeSectionName;

        case EARCharacterMontageSectionName::LfMouseReleased:    return LfMouseReleasedSectionName;
        case EARCharacterMontageSectionName::LfMouseReleased_ML: return LfMouseReleasedMeleeSectionName;

        case EARCharacterMontageSectionName::RtMousePressed:     return RtMousePressedSectionName;
        case EARCharacterMontageSectionName::RtMousePressed_ML:  return RtMousePressedMeleeSectionName;

        default:                                                 return TEXT("");
    }
}
