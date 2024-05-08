// Fill out your copyright notice in the Description page of Project Settings.

#include "GxCharacter.h"

#include "GxLogChannels.h"
#include "Camera/CameraComponent.h"
#include "Components/CapsuleComponent.h"
#include "GameFramework/CharacterMovementComponent.h"
#include "GameFramework/SpringArmComponent.h"
#include "Components/SkeletalMeshComponent.h"
#include "Player/GxPlayerController.h"
#include "Player/GxPlayerState.h"
#include "Character/GxHeroComponent.h"
#include "Character/GxCombatComponent.h"
#include "AbilitySystem/GxAbilitySystemComponent.h"
#include "Animation/GxAnimInstance.h"
#include "Weapons/GxWeapon.h"

#include UE_INLINE_GENERATED_CPP_BY_NAME(GxCharacter)

//////////////////////////////////////////////////////////////////////////
// AGxCharacter

AGxCharacter::AGxCharacter(const FObjectInitializer& ObjectInitializer)
	: Super(ObjectInitializer)
{
	GetCapsuleComponent()->InitCapsuleSize(42.f, 96.0f);

	bUseControllerRotationPitch = false;
	bUseControllerRotationYaw = true;
	bUseControllerRotationRoll = false;

	GetCharacterMovement()->bOrientRotationToMovement = false;
	GetCharacterMovement()->RotationRate = FRotator(0.0f, 500.0f, 0.0f);

	GetCharacterMovement()->JumpZVelocity = 400.f;
	GetCharacterMovement()->AirControl = 0.35f;
	GetCharacterMovement()->MaxWalkSpeed = 500.f;
	GetCharacterMovement()->MinAnalogWalkSpeed = 20.f;
	GetCharacterMovement()->BrakingDecelerationWalking = 2000.f;

	CameraBoom = ObjectInitializer.CreateDefaultSubobject<USpringArmComponent>(this, TEXT("CameraBoom"));
	CameraBoom->SetupAttachment(RootComponent);
	CameraBoom->TargetArmLength = 400.0f;	
	CameraBoom->bUsePawnControlRotation = true;

	FollowCamera = ObjectInitializer.CreateDefaultSubobject<UCameraComponent>(this, TEXT("FollowCamera"));
	FollowCamera->SetupAttachment(CameraBoom, USpringArmComponent::SocketName);
	FollowCamera->bUsePawnControlRotation = false;

	HeroComponent = ObjectInitializer.CreateDefaultSubobject<UGxHeroComponent>(this, TEXT("HeroComponent"));

	CombatComponent = ObjectInitializer.CreateDefaultSubobject<UGxCombatComponent>(this, TEXT("CombatComponent"));
}

void AGxCharacter::PossessedBy(AController* NewController)
{
	Super::PossessedBy(NewController);

	AGxPlayerState* GxPS = GetGxPlayerState();
	UGxAbilitySystemComponent* GxASC = GetGxAbilitySystemComponent();
	gxcheck(GxPS);
	gxcheck(GxASC);

	GxASC->InitAbilityActorInfo(GxPS, this);
	GxASC->GiveAbilities(DefaultInputAbilities);
	GxASC->GiveAbilities(DefaultAbilities);
	GxASC->ApplyEffects(DefaultEffects);
}

void AGxCharacter::BeginPlay()
{
	Super::BeginPlay();

	CombatComponent->EquipWeapons(DefaultWeaponClasses);
	CombatComponent->EnableWeapon(DefaultWeaponClasses[0]);
}

void AGxCharacter::EndPlay(const EEndPlayReason::Type EndPlayReason)
{
	TArray<AActor*> AttachedActors;

	GetAttachedActors(AttachedActors, false);

	for (AActor* AttachedActor : AttachedActors)
	{
		AttachedActor->Destroy();
	}

	Super::EndPlay(EndPlayReason);
}

AGxPlayerController* AGxCharacter::GetGxPlayerController() const
{
	return CastChecked<AGxPlayerController>(Controller, ECastCheckedType::NullAllowed);
}

AGxPlayerState* AGxCharacter::GetGxPlayerState() const
{
	return CastChecked<AGxPlayerState>(GetPlayerState(), ECastCheckedType::NullAllowed);
}

UAbilitySystemComponent* AGxCharacter::GetAbilitySystemComponent() const
{
	AGxPlayerState* GxPS = GetGxPlayerState();

	return (GxPS ? GxPS->GetAbilitySystemComponent() : nullptr);
}

UGxAbilitySystemComponent* AGxCharacter::GetGxAbilitySystemComponent() const
{
	return Cast<UGxAbilitySystemComponent>(GetAbilitySystemComponent());
}

UGxAnimInstance* AGxCharacter::GetGxAnimInstance() const
{
	USkeletalMeshComponent* SkelMeshComponent = GetMesh();

	return (SkelMeshComponent ? Cast<UGxAnimInstance>(SkelMeshComponent->GetAnimInstance()) : nullptr);
}

//////////////////////////////////////////////////////////////////////////
// Input

void AGxCharacter::SetupPlayerInputComponent(UInputComponent* PlayerInputComponent)
{
	Super::SetupPlayerInputComponent(PlayerInputComponent);

	HeroComponent->InitializePlayerInput(PlayerInputComponent);
}

bool AGxCharacter::CanJumpInternal_Implementation() const
{
	if (Super::CanJumpInternal_Implementation())
	{
		return true;
	}

	UCharacterMovementComponent* MovementComponent = GetCharacterMovement();
	gxcheck(MovementComponent, false);

	return !MovementComponent->IsFalling();
}

bool AGxCharacter::CanCrouch() const
{
	if (!Super::CanCrouch())
	{
		return false;
	}

	UCharacterMovementComponent* MovementComponent = GetCharacterMovement();
	gxcheck(MovementComponent, false);

	return !MovementComponent->IsFalling();
}
