#include "Character/Weapons/ARBow.h"

#include "Character/ARPlayerController.h"
#include "Character/SceneComponents/ARQuiverComponent.h"
#include "Character/Weapons/ARBowArrow.h"
#include "HUD/ARHUDWidget.h"
#include "GameFramework/ProjectileMovementComponent.h"
#include "Components/BoxComponent.h"
#include "Camera/CameraComponent.h"

AARBow::AARBow()
{
    IdleFOV = 90.F;
    AimingFOV = 60.F;
}

void AARBow::Equip(AARCharacter* NewCharacter)
{
    Super::Equip(NewCharacter);

    USkeletalMeshComponent* CharacterMesh = NewCharacter->GetMesh();
    ARCHECK(CharacterMesh != nullptr);
    // 캐릭터 메시에 화살집 컴포넌트 부착
    if (CharacterMesh->DoesSocketExist(QuiverSocket))
    {
        UActorComponent* ActorComponent = NewCharacter->AddComponentByClass(QuiverComponentClass, true, FTransform(), false);
        ARCHECK(ActorComponent != nullptr);

        QuiverComponent = Cast<UARQuiverComponent>(ActorComponent);
        QuiverComponent->AttachToComponent(CharacterMesh, FAttachmentTransformRules::SnapToTargetNotIncludingScale, QuiverSocket);
    }

    UCameraComponent* Camera = Character->GetFollowCamera();
    ARCHECK(Camera != nullptr);
    // 기존 캐릭터 FOV 값 백업
    IdleFOV = Camera->FieldOfView;
}

void AARBow::Unequip()
{
    USkeletalMeshComponent* CharacterMesh = Character->GetMesh();
    ARCHECK(CharacterMesh != nullptr);
    // 캐릭터 메시에서 화살집 컴포넌트 해제
    if (CharacterMesh->DoesSocketExist(QuiverSocket))
    {
        ARCHECK(QuiverComponent != nullptr);

        QuiverComponent->DetachFromComponent(FDetachmentTransformRules::KeepWorldTransform);
        QuiverComponent->DestroyComponent();
    }

    Super::Unequip();
}

// 활 조준 -> 애니메이션 재생 -> AttackStart 애님 노이파이 -> AttackStart() 실행
void AARBow::LfMousePressed()
{
    Super::LfMousePressed();

    ARCHECK(CharacterAnimInstance != nullptr);

    // 활을 사용할 수 있으면
    if (CanUse())
    {
        // 활 공격 몽타주 재생
        CharacterAnimInstance->PlayAttackMontage();
        CharacterAnimInstance->JumpToMontageSection(EARCharacterMontageSectionName::LfMousePressed);
    }
    // 화살이 없으면
    else if (Character->GetState() == EARCharacterState::Normal)
    {
        // 근접 공격 몽타주 재생
        CharacterAnimInstance->PlayAttackMontage();
        CharacterAnimInstance->JumpToMontageSection(EARCharacterMontageSectionName::LfMousePressed_ML);
    }
}

// 활 쏘기 -> 애니메이션 재생 -> Attack 애님 노티파이 -> Attack() 실행
void AARBow::LfMouseReleased()
{
    Super::LfMouseReleased();

    ARCHECK(CharacterAnimInstance != nullptr);
    ARCHECK(CurrentArrow != nullptr);
    
    // 화살을 다시 집어넣는 중에 쏘는 것을 방지하기 위함
    if (CharacterAnimInstance->GetCurrentSection() == EARCharacterMontageSectionName::LfMousePressed)
    {
        CharacterAnimInstance->PlayAttackMontage();
        CharacterAnimInstance->JumpToMontageSection(EARCharacterMontageSectionName::LfMouseReleased);
    }
}

// 화살 다시 집어넣기 -> 애니메이션 재생 -> AttackEnd 애님 노이파이 -> AttackEnd() 실행
void AARBow::RtMousePressed()
{
    Super::RtMousePressed();

    ARCHECK(CharacterAnimInstance != nullptr);
    ARCHECK(CurrentArrow != nullptr);

    // 화살을 다시 집어넣는 중에 또 집어넣는 것을 방지하기 위함
    if (CharacterAnimInstance->GetCurrentSection() != EARCharacterMontageSectionName::RtMousePressed)
    {
        CharacterAnimInstance->PlayAttackMontage();
        CharacterAnimInstance->JumpToMontageSection(EARCharacterMontageSectionName::RtMousePressed);
    }
}

bool AARBow::CanUse()
{
    ARCHECK(Super::CanUse(), false);
    ARCHECK(QuiverComponent != nullptr, false);
    // 캐릭터 손에 화살이 없다 && 화살집에 남은 화살이 있다
    return (CurrentArrow == nullptr) && QuiverComponent->CanPickArrow();
}

// LfMousePressed() -> 애니메이션 재생 -> AttackStart 애님 노이파이 -> AttackStart() 실행
void AARBow::AttackStart()
{
    Super::AttackStart();

    ARCHECK(QuiverComponent != nullptr);
    // 화살집에서 화살을 가져왔으면
    if ((CurrentArrow = QuiverComponent->PickArrow()) != nullptr)
    {
        // 캐릭터 메시 손 부분에 붙인다.
        USkeletalMeshComponent* CharacterMesh = Character->GetMesh();
        ARCHECK(CharacterMesh != nullptr);

        CurrentArrow->AttachToComponent(CharacterMesh, FAttachmentTransformRules::SnapToTargetNotIncludingScale, ArrowSocket);

        UCameraComponent* Camera = Character->GetFollowCamera();
        ARCHECK(Camera != nullptr);
        // 조준 FOV 설정
        Camera->FieldOfView = AimingFOV;

        AARPlayerController* PlayerController = Cast<AARPlayerController>(Character->GetController());
        ARCHECK(PlayerController != nullptr);
        // 조준선 표시
        PlayerController->GetHUDWidget()->SetUIVisibility(EARCharacterUI::Crosshair, ESlateVisibility::HitTestInvisible);
    }
}

// LfMouseReleased() -> 애니메이션 재생 -> Attack 애님 노티파이 -> Attack() 실행
void AARBow::Attack()
{
    Super::Attack();

    // 현재 캐릭터 손에 화살이 있으면
    if (CurrentArrow != nullptr)
    {
        UProjectileMovementComponent* ProjectileMovement = CurrentArrow->GetProjectileMovement();
        ARCHECK(ProjectileMovement != nullptr);
        ARCHECK(Character != nullptr);
        // 손에서 화살을 떼고 발사시킨다.
        CurrentArrow->DetachFromActor(FDetachmentTransformRules::KeepWorldTransform);
        CurrentArrow->Box->SetCollisionProfileName("Arrow");
        CurrentArrow->SetActorRotation(Character->GetBaseAimRotation());
        ProjectileMovement->Velocity = Character->GetBaseAimRotation().Vector() * ProjectileMovement->InitialSpeed;
        ProjectileMovement->Activate();

        CurrentArrow->SetLifeSpan(CurrentArrow->GetArrowLifeSpan());
        CurrentArrow = nullptr;
    }
    // 없으면 근접 공격 수행
    else
    {
        MeleeAttack();
    }
}

void AARBow::OnCharacterStateChanged(EARCharacterState OldState, EARCharacterState NewState)
{
    Super::OnCharacterStateChanged(OldState, NewState);

    // 공격이 끝났거나 피격 당했는데
    if ((NewState == EARCharacterState::Normal) || (NewState == EARCharacterState::Damaged))
    {
        ARCHECK(QuiverComponent != nullptr);
        // 손에 화살이 있으면
        if (CurrentArrow != nullptr)
        {
            // 다시 화살집에 넣는다.
            CurrentArrow->DetachFromActor(FDetachmentTransformRules::KeepWorldTransform);
            QuiverComponent->PutArrow(CurrentArrow);

            CurrentArrow = nullptr;
        }

        UCameraComponent* Camera = Character->GetFollowCamera();
        ARCHECK(Camera != nullptr);
        // FOV를 복원한다.
        Camera->FieldOfView = IdleFOV;

        AARPlayerController* PlayerController = Cast<AARPlayerController>(Character->GetController());
        ARCHECK(PlayerController != nullptr);
        // 조준선을 숨긴다.
        PlayerController->GetHUDWidget()->SetUIVisibility(EARCharacterUI::Crosshair, ESlateVisibility::Collapsed);
    }
}
