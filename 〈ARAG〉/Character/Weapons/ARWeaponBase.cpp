#include "Character/Weapons/ARWeaponBase.h"

#include "Character/ARPlayerController.h"
#include "Character/Types/ARInputMappingPriority.h"
#include "Character/ActorComponents/ARCombatComponent.h"
#include "Types/ARCollisionChannel.h"

AARWeaponBase::AARWeaponBase()
{
    PrimaryActorTick.bCanEverTick = false;

    MeleeAttackRange = 200.F;
    MeleeAttackRadius = 30.F;
    bUseAimOffset = false;
}

void AARWeaponBase::BeginPlay()
{
    Super::BeginPlay();
    
}

void AARWeaponBase::Equip(AARCharacter* NewCharacter)
{
    ARCHECK(NewCharacter != nullptr);
    // 소유자 설정
    SetOwner(Character = NewCharacter);
    // 캐릭터 상태 변경 델리게이트에 함수 등록
    Character->OnStateChanged.AddUObject(this, &ThisClass::OnCharacterStateChanged);

    CharacterAnimInstance = Cast<UARCharacterAnimInstance>(Character->GetMesh()->GetAnimInstance());
    ARCHECK(CharacterAnimInstance != nullptr);
    // 애니메이션 몽타주 설정, 애님 노티파이에 호출될 함수 등록
    CharacterAnimInstance->SetAttackMontage(AttackMontage);
    CharacterAnimInstance->OnAttackStart.AddUObject(this, &ThisClass::AttackStart);
    CharacterAnimInstance->OnAttack.AddUObject(this, &ThisClass::Attack);
    CharacterAnimInstance->OnAttackEnd.AddUObject(this, &ThisClass::AttackEnd);
    // 무기에 해당하는 Input Mapping Context 등록
    AARPlayerController* PlayerContoller = Cast<AARPlayerController>(Character->GetController());
    ARCHECK(PlayerContoller != nullptr);
    ARCHECK(WeaponMappingContext != nullptr);

    PlayerContoller->AddMappingContext(WeaponMappingContext, EARInputMappingPriority::Weapon);
    // 무기 장착 델리게이트 발동
    OnEquip.Broadcast();
}

void AARWeaponBase::Unequip()
{
    ARCHECK(Character != nullptr);
    // Input Mapping Context 제거
    AARPlayerController* PlayerContoller = Cast<AARPlayerController>(Character->GetController());
    ARCHECK(PlayerContoller != nullptr);
    ARCHECK(WeaponMappingContext != nullptr);
    ARCHECK(CharacterAnimInstance != nullptr);

    PlayerContoller->RemoveMappingContext(WeaponMappingContext);
    // 애니메이션 몽타주, 애님 노티파이 관련 정리
    CharacterAnimInstance->OnAttackStart.RemoveAll(this);
    CharacterAnimInstance->OnAttack.RemoveAll(this);
    CharacterAnimInstance->OnAttackEnd.RemoveAll(this);
    CharacterAnimInstance->SetAttackMontage(nullptr);
    CharacterAnimInstance = nullptr;
    // 캐릭터 상태 변경 델리게이트에서 함수 제거
    Character->OnStateChanged.RemoveAll(this);

    // 소유자 해제
    SetOwner(Character = nullptr);
    // 무기 해제 델리게이트 발동
    OnUnequip.Broadcast();

    OnUnequip.Clear();
    OnEquip.Clear();
}

void AARWeaponBase::LfMousePressed()  { }
void AARWeaponBase::LfMouseReleased() { }
void AARWeaponBase::RtMousePressed()  { }

// 무기를 사용 가능한 상태인지 반환
bool AARWeaponBase::CanUse()
{
    UARCombatComponent* CombatComponent = Character->GetCombatComponent();
    ARCHECK(CombatComponent != nullptr, false);

    return CombatComponent->CanAttack();
}
// AttackStart 애님 노티파이에 호출
void AARWeaponBase::AttackStart()
{
    ARCHECK(Character != nullptr);

    UARCombatComponent* CombatComponent = Character->GetCombatComponent();
    ARCHECK(CombatComponent != nullptr);
    // 공격 중으로 설정
    Character->SetState(EARCharacterState::Attack);

    ARCHECK(CharacterAnimInstance != nullptr);
    // 공격 상태가 되면 무기에 따라 Aim Offset 적용
    if (bUseAimOffset)
    {
        CharacterAnimInstance->SetUsingAimOffset(true);
    }
}

// Attack 애님 노티파이에 호출
void AARWeaponBase::Attack()
{

}

// AttackEnd 애님 노티파이에 호출
void AARWeaponBase::AttackEnd()
{
    ARCHECK(Character != nullptr);

    UARCombatComponent* CombatComponent = Character->GetCombatComponent();
    ARCHECK(CombatComponent != nullptr);
    // 공격 중이 아님으로 설정
    Character->SetState(EARCharacterState::Normal);

    ARCHECK(CharacterAnimInstance != nullptr);
    // 일반 상태가 되면 무기에 따라 Aim Offset 해제
    if (bUseAimOffset)
    {
        CharacterAnimInstance->SetUsingAimOffset(false);
    }
}

// 모든 무기는 근접 공격을 지원한다.
void AARWeaponBase::MeleeAttack()
{
    // 공격 판정 진행
    FHitResult HitResult;
    FCollisionQueryParams Params(NAME_None, false, Character);

    FVector ActorLocation = Character->GetActorLocation();
    FVector ActorForwardVector = Character->GetActorForwardVector();

    bool bResult = GetWorld()->SweepSingleByChannel(
        HitResult,
        ActorLocation, ActorLocation + ActorForwardVector * MeleeAttackRange,
        FQuat::Identity,
        AR_ECC_Attack,
        FCollisionShape::MakeSphere(MeleeAttackRadius),
        Params
    );

#if ENABLE_DRAW_DEBUG
    FVector TraceVec = ActorForwardVector * MeleeAttackRange;
    FVector Center = ActorLocation + TraceVec * 0.5F;
    float HalfHeight = MeleeAttackRange * 0.5F + MeleeAttackRadius;
    FQuat CapsuleRot = FRotationMatrix::MakeFromZ(TraceVec).ToQuat();
    FColor DrawColor = (bResult) ? FColor::Green : FColor::Red;
    float DebugLifeTime = 5.0F;

    DrawDebugCapsule(GetWorld(), Center, HalfHeight, MeleeAttackRadius, CapsuleRot, DrawColor, false, DebugLifeTime);
#endif
    // 판정 성공했으면
    if (bResult)
    {
        // 상대 액터 데미지 처리
        AActor* Victim = HitResult.GetActor();
        ARCHECK(Victim != nullptr);

        FDamageEvent DamageEvent;
        Victim->TakeDamage(WeaponDamage, DamageEvent, Character->GetController(), Character);
    }
}

void AARWeaponBase::OnCharacterStateChanged(EARCharacterState OldState, EARCharacterState NewState)
{
    // 공격 도중 맞거나 죽었으면
    if ((OldState == EARCharacterState::Attack) && (NewState == EARCharacterState::Damaged || NewState == EARCharacterState::Die))
    {
        // 몽타주 중지
        CharacterAnimInstance->StopAllMontages(0.1F);
    }
}
