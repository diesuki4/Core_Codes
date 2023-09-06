#include "Monster/ARMonsterBase.h"

#include "Monster/ARMonsterController.h"
#include "Monster/ARMonsterAnimInstance.h"
#include "Monster/DataAssets/ARMonsterDataAsset.h"
#include "Character/ARCharacter.h"
#include "ActorComponents/ARDataComponent.h"
#include "Components/CapsuleComponent.h"
#include "BehaviorTree/BlackboardComponent.h"

AARMonsterBase::AARMonsterBase()
{
    PrimaryActorTick.bCanEverTick = true;

    GetCapsuleComponent()->SetCollisionProfileName("Enemy");

    DataComponent = CreateDefaultSubobject<UARDataComponent>(TEXT("DataComponent"));

    AbilitySystemComponent = CreateDefaultSubobject<UARAbilitySystemComponent>(TEXT("AbilitySystemComponent"));
    AttributeSet = CreateDefaultSubobject<UARMonsterAttributeSet>(TEXT("AttributeSet"));

    AutoPossessAI = EAutoPossessAI::PlacedInWorldOrSpawned;

    State = EARMonsterState::Idle;
}

void AARMonsterBase::PostInitializeComponents()
{
    Super::PostInitializeComponents();

    ARCHECK(DataComponent != nullptr);
    // 몬스터 데이터 가져오기
    MonsterData = Cast<UARMonsterDataAsset>(DataComponent->GetActorData());
    ARCHECK(MonsterData != nullptr);
    ARCHECK(AbilitySystemComponent != nullptr);

    // 데이터 에셋으로부터 기본 GAS 능력 부여
    AbilitySystemComponent->GiveAbility(MonsterData->Abilities);
    // 데이터 에셋으로부터 기본 GAS 이펙트 적용
    AbilitySystemComponent->ApplyGameplayEffectToSelf(MonsterData->Effects);

    UARMonsterAnimInstance* MonsterAnimInstance = Cast<UARMonsterAnimInstance>(GetMesh()->GetAnimInstance());
    ARCHECK(MonsterAnimInstance != nullptr);
    // Attack 애님 노티파이에 수행될 공격 로직 등록
    MonsterAnimInstance->OnAttack.AddUObject(this, &ThisClass::OnAttackTarget);
    // 피격 시 추적을 위한 가해자 설정 및 상태 변경 등록
    OnDamaged.AddLambda([this](AActor* DamageCauser) {
        AARMonsterController* MonsterController = Cast<AARMonsterController>(GetController());

        MonsterController->GetBlackboardComponent()->SetValueAsObject(AARMonsterController::TargetActorKey, DamageCauser);
        SetState(EARMonsterState::Damaged);
    }); 
    // 죽을 시 상태 변경 등록 (죽음 로직은 Behavior Tree를 통해 처리)
    OnDie.AddLambda([this]() { SetState(EARMonsterState::Die); });

    // 임시 코드
    HP = MonsterData->MaxHP;
}

void AARMonsterBase::PossessedBy(AController* NewController)
{
    Super::PossessedBy(NewController);

    ARCHECK(AbilitySystemComponent != nullptr);

    AbilitySystemComponent->InitAbilityActorInfo(this, this);
}

void AARMonsterBase::OnRep_PlayerState()
{
    Super::OnRep_PlayerState();

    AbilitySystemComponent->InitAbilityActorInfo(this, this);
}

void AARMonsterBase::BeginPlay()
{
    Super::BeginPlay();

    SetState(State);
}

void AARMonsterBase::Tick(float DeltaTime)
{
    Super::Tick(DeltaTime);

}

void AARMonsterBase::SetupPlayerInputComponent(UInputComponent* PlayerInputComponent)
{
    Super::SetupPlayerInputComponent(PlayerInputComponent);

}

float AARMonsterBase::TakeDamage(float DamageAmount, struct FDamageEvent const& DamageEvent,
                                 AController* EventInstigator, AActor* DamageCauser)
{
    float FinalDamage = Super::TakeDamage(DamageAmount, DamageEvent, EventInstigator, DamageCauser);

    ARLOG(Warning, TEXT("%s took Damage %f"), *GetName(), FinalDamage);
    // 임시 코드
    if ((HP -= FinalDamage) <= 0)
    {
        SetCanBeDamaged(false);
        OnDie.Broadcast();
    }
    else
    {
        OnDamaged.Broadcast(DamageCauser);
    }

    return FinalDamage;
}
// 공격 몽타주 재생 래퍼 함수
void AARMonsterBase::Attack()
{
    UARMonsterAnimInstance* MonsterAnimInstance = Cast<UARMonsterAnimInstance>(GetMesh()->GetAnimInstance());
    ARCHECK(MonsterAnimInstance != nullptr);

    MonsterAnimInstance->PlayAttackMontage();
    MonsterAnimInstance->JumpToMontageSection(EARMonsterMontageSectionName::AttackSection);
}
// Attack 애님 노티파이 시, 실제 공격 로직 수행 함수
void AARMonsterBase::OnAttackTarget()
{
    AARMonsterController* MonsterController = Cast<AARMonsterController>(GetController());
    ARCHECK(MonsterController != nullptr);

    UObject* CurrentTarget = MonsterController->GetBlackboardComponent()->GetValueAsObject(AARMonsterController::TargetActorKey);
    ARCHECK(CurrentTarget != nullptr);

    AARCharacter* TargetActor = Cast<AARCharacter>(CurrentTarget);
    ARCHECK(TargetActor != nullptr);
    ARCHECK(MonsterData != nullptr);
    // 현재 타겟에게 데미지 처리
    FDamageEvent DamageEvent;
    TargetActor->TakeDamage(MonsterData->AttackDamage, DamageEvent, MonsterController, this);
}

// 델리게이트를 활용해 옵저버 패턴으로 구현
void AARMonsterBase::SetState(EARMonsterState NewState)
{
    ARCHECK(State != NewState);

    EARMonsterState OldState = State;
    State = NewState;

    AARMonsterController* MonsterController = Cast<AARMonsterController>(GetController());
    ARCHECK(MonsterController != nullptr);

    MonsterController->GetBlackboardComponent()->SetValueAsEnum(AARMonsterController::MonsterStateKey, uint8(State));
    // 상태 변경 델리게이트 발동
    OnStateChanged.Broadcast(OldState, State);
}
