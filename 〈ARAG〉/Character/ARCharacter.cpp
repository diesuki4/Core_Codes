#include "Character/ARCharacter.h"
#include "Camera/CameraComponent.h"
#include "Components/CapsuleComponent.h"
#include "Components/InputComponent.h"
#include "GameFramework/CharacterMovementComponent.h"
#include "GameFramework/Controller.h"
#include "GameFramework/SpringArmComponent.h"
#include "EnhancedInputComponent.h"
#include "EnhancedInputSubsystems.h"

#include "ActorComponents/ARDataComponent.h"
#include "Character/ActorComponents/ARCombatComponent.h"
#include "Character/ARPlayerController.h"
#include "Character/Types/ARInputMappingPriority.h"
#include "Character/Weapons/ARWeaponBase.h"
#include "Character/DataAssets/ARCharacterDataAsset.h"

AARCharacter::AARCharacter()
{
    GetCapsuleComponent()->InitCapsuleSize(42.F, 96.0F);
        
    bUseControllerRotationPitch = false;
    bUseControllerRotationYaw = false;
    bUseControllerRotationRoll = false;

    GetCharacterMovement()->bOrientRotationToMovement = true;
    GetCharacterMovement()->RotationRate = FRotator(0.0F, 500.0F, 0.0F);

    GetCharacterMovement()->JumpZVelocity = 700.F;
    GetCharacterMovement()->AirControl = 0.35F;
    GetCharacterMovement()->MaxWalkSpeed = 500.F;
    GetCharacterMovement()->MinAnalogWalkSpeed = 20.F;
    GetCharacterMovement()->BrakingDecelerationWalking = 2000.F;

    CameraBoom = CreateDefaultSubobject<USpringArmComponent>(TEXT("CameraBoom"));
    CameraBoom->SetupAttachment(RootComponent);
    CameraBoom->TargetArmLength = 400.0F;
    CameraBoom->bUsePawnControlRotation = true;
    CameraBoom->SetRelativeLocation(FVector(30.F, 0.F, 200.F));

    FollowCamera = CreateDefaultSubobject<UCameraComponent>(TEXT("FollowCamera"));
    FollowCamera->SetupAttachment(CameraBoom, USpringArmComponent::SocketName);
    FollowCamera->bUsePawnControlRotation = false;
    FollowCamera->SetRelativeRotation(FRotator(-20.0F, 0.0F, 0.0F));

    CombatComponent = CreateDefaultSubobject<UARCombatComponent>(TEXT("CombatComponent"));

    DataComponent = CreateDefaultSubobject<UARDataComponent>(TEXT("DataComponent"));

    AbilitySystemComponent = CreateDefaultSubobject<UARAbilitySystemComponent>(TEXT("AbilitySystemComponent"));
    AttributeSet = CreateDefaultSubobject<UARCharacterAttributeSet>(TEXT("AttributeSet"));

    State = EARCharacterState::Normal;
}

void AARCharacter::PostInitializeComponents()
{
    Super::PostInitializeComponents();

    ARCHECK(DataComponent != nullptr);
    // 캐릭터 데이터 가져오기
    CharacterData = Cast<UARCharacterDataAsset>(DataComponent->GetActorData());
    ARCHECK(CharacterData != nullptr);

    UARCharacterAnimInstance* CharacterAnimInstance = Cast<UARCharacterAnimInstance>(GetMesh()->GetAnimInstance());
    ARCHECK(CharacterAnimInstance != nullptr);
    // 피격, 죽음 애니메이션 종료시 동작 등록
    CharacterAnimInstance->OnAnimationEnd.AddLambda([this]()
    {
        switch (State)
        {
        case EARCharacterState::Die:    // 죽음
            SetActorEnableCollision(false);
            GetMesh()->SetHiddenInGame(true);
            SetActorHiddenInGame(true);
            Destroy();
            break;
        default:    // 피격
            SetState(EARCharacterState::Normal);
        }
    });
    // 피격 시 상태 변경 등록
    OnDamaged.AddLambda([this](AActor* DamageCauser) { SetState(EARCharacterState::Damaged); });
    // 죽음 시 동작 등록
    OnDie.AddLambda([this]()
    {
        SetCanBeDamaged(false);
        DisableInput(Cast<APlayerController>(GetController()));

        SetState(EARCharacterState::Die);
    });

    // 임시 코드
    HP = CharacterData->MaxHP;
}

void AARCharacter::PossessedBy(AController* NewController)
{
    Super::PossessedBy(NewController);

    ARCHECK(AbilitySystemComponent != nullptr);

    AbilitySystemComponent->InitAbilityActorInfo(this, this);
    // 데이터 에셋으로부터 기본 GAS 능력 부여
    AbilitySystemComponent->GiveAbility(CharacterData->Abilities);
    // 데이터 에셋으로부터 기본 GAS 이펙트 적용
    AbilitySystemComponent->ApplyGameplayEffectToSelf(CharacterData->Effects);
}

void AARCharacter::OnRep_PlayerState()
{
    Super::OnRep_PlayerState();

    AbilitySystemComponent->InitAbilityActorInfo(this, this);
}

void AARCharacter::BeginPlay()
{
    Super::BeginPlay();

    SetState(State);
}

void AARCharacter::SetupPlayerInputComponent(class UInputComponent* PlayerInputComponent)
{
    UEnhancedInputComponent* EnhancedInputComponent = CastChecked<UEnhancedInputComponent>(PlayerInputComponent);
    ARCHECK(EnhancedInputComponent != nullptr);

    EnhancedInputComponent->BindAction(MoveAction, ETriggerEvent::Triggered, this, &AARCharacter::Move);

    EnhancedInputComponent->BindAction(LookAction, ETriggerEvent::Triggered, this, &AARCharacter::Look);

    EnhancedInputComponent->BindAction(JumpAction, ETriggerEvent::Started, this, &ACharacter::Jump);
    EnhancedInputComponent->BindAction(JumpAction, ETriggerEvent::Completed, this, &ACharacter::StopJumping);

    EnhancedInputComponent->BindAction(LfMouseAction, ETriggerEvent::Started, this, &AARCharacter::LfMousePressed);
    EnhancedInputComponent->BindAction(LfMouseAction, ETriggerEvent::Completed, this, &AARCharacter::LfMouseReleased);
    EnhancedInputComponent->BindAction(RtMouseAction, ETriggerEvent::Started, this, &AARCharacter::RtMousePressed);
}

void AARCharacter::Move(const FInputActionValue& Value)
{
    ARCHECK(Controller != nullptr);

    FVector2D MovementVector = Value.Get<FVector2D>();

    const FRotator Rotation = Controller->GetControlRotation();
    const FRotator YawRotation(0, Rotation.Yaw, 0);

    const FVector ForwardDirection = FRotationMatrix(YawRotation).GetUnitAxis(EAxis::X);
    const FVector RightDirection = FRotationMatrix(YawRotation).GetUnitAxis(EAxis::Y);

    AddMovementInput(ForwardDirection, MovementVector.Y);
    AddMovementInput(RightDirection, MovementVector.X);
}

void AARCharacter::Look(const FInputActionValue& Value)
{
    ARCHECK(Controller != nullptr);

    FVector2D LookAxisVector = Value.Get<FVector2D>();

    AddControllerYawInput(LookAxisVector.X);
    AddControllerPitchInput(LookAxisVector.Y);
}

void AARCharacter::LfMousePressed()
{
    ARCHECK(CombatComponent);
    // 현재 무기에 구현된 좌클릭 누름 동작 수행
    CombatComponent->GetWeapon()->LfMousePressed();
}

void AARCharacter::LfMouseReleased()
{
    ARCHECK(CombatComponent);
    // 현재 무기에 구현된 좌클릭 뗌 동작 수행
    CombatComponent->GetWeapon()->LfMouseReleased();
}

void AARCharacter::RtMousePressed()
{
    ARCHECK(CombatComponent);
    // 현재 무기에 구현된 우클릭 누름 동작 수행
    CombatComponent->GetWeapon()->RtMousePressed();
}

void AARCharacter::PawnClientRestart()
{
    Super::PawnClientRestart();
    // 기본 Input Mapping Context 적용
    AARPlayerController* PlayerController = Cast<AARPlayerController>(Controller);
    ARCHECK(PlayerController != nullptr);

    PlayerController->ClearAllMappings();
    PlayerController->AddMappingContext(DefaultMappingContext, EARInputMappingPriority::Movement);
}

float AARCharacter::TakeDamage(float DamageAmount, struct FDamageEvent const& DamageEvent,
                               AController* EventInstigator, AActor* DamageCauser)
{
    float FinalDamage = Super::TakeDamage(DamageAmount, DamageEvent, EventInstigator, DamageCauser);

    ARLOG(Warning, TEXT("%s took Damage %f"), *GetName(), FinalDamage);
    // 임시 코드
    if ((HP -= FinalDamage) <= 0)
    {
        OnDie.Broadcast();
    }
    else
    {
        OnDamaged.Broadcast(DamageCauser);
    }

    return FinalDamage;
}

// 델리게이트를 활용해 옵저버 패턴으로 구현
void AARCharacter::SetState(EARCharacterState NewState)
{
    ARCHECK(State != NewState);

    EARCharacterState OldState = State;
    // 상태 변경 델리게이트 발동
    OnStateChanged.Broadcast(OldState, State = NewState);
}
