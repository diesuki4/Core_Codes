// Fill out your copyright notice in the Description page of Project Settings.

#include "GxGameplayAbility_SwiftStrike.h"

#include "GxLogChannels.h"
#include "Interaction/Tasks/GxAbilityTask_PlayMontageAndWait.h"
#include "MotionWarpingComponent.h"
#include "Player/GxPlayerController.h"
#include "Character/GxCharacter.h"
#include "Character/GxCombatComponent.h"
#include "Components/CapsuleComponent.h"
#include "Components/SphereComponent.h"
#include "GameFramework/CharacterMovementComponent.h"
#include "Kismet/KismetMathLibrary.h"
#include "Camera/CameraComponent.h"

#include UE_INLINE_GENERATED_CPP_BY_NAME(GxGameplayAbility_SwiftStrike)

//////////////////////////////////////////////////////////////////////////
// UGxGameplayAbility_SwiftStrike

UGxGameplayAbility_SwiftStrike::UGxGameplayAbility_SwiftStrike(const FObjectInitializer& ObjectInitializer)
	: Super(ObjectInitializer)
	, SwiftTargetLocationName(TEXT("SwiftTargetLocation"))
	, SwiftDistance(1000.0f)
{

}

void UGxGameplayAbility_SwiftStrike::PreActivate(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, FOnGameplayAbilityEnded::FDelegate* OnGameplayAbilityEndedDelegate, const FGameplayEventData* TriggerEventData)
{
	Super::PreActivate(Handle, ActorInfo, ActivationInfo, OnGameplayAbilityEndedDelegate, TriggerEventData);

	HitActors.Reset();
	PerpareMotionWarping();
}

void UGxGameplayAbility_SwiftStrike::ActivateAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, const FGameplayEventData* TriggerEventData)
{
	Super::ActivateAbility(Handle, ActorInfo, ActivationInfo, TriggerEventData);

	UGxAbilityTask_PlayMontageAndWait* GxPlayMontageAndWaitTask = UGxAbilityTask_PlayMontageAndWait::CreateGxPlayMontageAndWaitProxy(this, NAME_None, AnimMontage);
	gxcheck(GxPlayMontageAndWaitTask);

	GxPlayMontageAndWaitTask->OnCompleted.AddDynamic(this, &ThisClass::OnMontageFinishedCallback);
	GxPlayMontageAndWaitTask->OnInterrupted.AddDynamic(this, &ThisClass::OnMontageCancelledCallback);
	GxPlayMontageAndWaitTask->OnCancelled.AddDynamic(this, &ThisClass::OnMontageCancelledCallback);
	GxPlayMontageAndWaitTask->ReadyForActivation();
}

void UGxGameplayAbility_SwiftStrike::EndAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, bool bReplicateEndAbility, bool bWasCancelled)
{
	FinishMotionWarping();
	HitActors.Reset();

	Super::EndAbility(Handle, ActorInfo, ActivationInfo, bReplicateEndAbility, bWasCancelled);
}

void UGxGameplayAbility_SwiftStrike::OnMontageFinishedCallback()
{
	EndAbility(CurrentSpecHandle, CurrentActorInfo, CurrentActivationInfo, /*bReplicateEndAbility*/true, /*bWasCancelled*/false);
}

void UGxGameplayAbility_SwiftStrike::OnMontageCancelledCallback()
{
	EndAbility(CurrentSpecHandle, CurrentActorInfo, CurrentActivationInfo, /*bReplicateEndAbility*/true, /*bWasCancelled*/true);
}

void UGxGameplayAbility_SwiftStrike::PerpareMotionWarping()
{
	AGxPlayerController* GxPC = GetGxPlayerControllerFromActorInfo();
	gxcheck(GxPC);
	AGxCharacter* GxCharacter = GetGxCharacterFromActorInfo();
	gxcheck(GxCharacter);
	UCapsuleComponent* CapsuleComponent = GxCharacter->GetCapsuleComponent();
	gxcheck(CapsuleComponent);
	UCharacterMovementComponent* CharacterMovement = GxCharacter->GetCharacterMovement();
	gxcheck(CharacterMovement);

	GxPC->SetIgnoreLookInput(true);
	CapsuleComponent->SetCollisionProfileName(TEXT("IgnorePlayer"));

	GxCharacter->bUseControllerRotationPitch = true;
	// Flying 모드가 아니면 Motion Warping 불가
	CharacterMovement->SetMovementMode(MOVE_Flying);

	UGxCombatComponent* GxCombatComponent = GxCharacter->GetGxCombatComponent();
	gxcheck(GxCombatComponent);

	CurrentWeapon = GxCombatComponent->EnableWeapon(EquipWeaponClass);

	USphereComponent* SphereComponent = GxCharacter->GetComponentByClass<USphereComponent>();
	gxcheck(SphereComponent);

	SphereComponent->SetActive(true);
	SphereComponent->OnComponentBeginOverlap.AddDynamic(this, &ThisClass::OnOverlapBegin);
	SphereComponent->OnComponentEndOverlap.AddDynamic(this, &ThisClass::OnOverlapEnd);

	UMotionWarpingComponent* MotionWarpingComponent = GxCharacter->GetComponentByClass<UMotionWarpingComponent>();
	gxcheck(MotionWarpingComponent);

	UCameraComponent* CameraComponent = GxCharacter->GetFollowCamera();
	gxcheck(CameraComponent);

	const FVector CameraLocation = CameraComponent->GetComponentLocation();
	const FVector CameraForwardVector = UKismetMathLibrary::GetForwardVector(CameraComponent->GetComponentRotation());
	const FVector TargetLocation = CameraLocation + (CameraForwardVector * SwiftDistance);

	MotionWarpingComponent->AddOrUpdateWarpTargetFromLocation(SwiftTargetLocationName, TargetLocation);
}

void UGxGameplayAbility_SwiftStrike::FinishMotionWarping()
{
	AGxPlayerController* GxPC = GetGxPlayerControllerFromActorInfo();
	gxcheck(GxPC);
	AGxCharacter* GxCharacter = GetGxCharacterFromActorInfo();
	gxcheck(GxCharacter);
	UCapsuleComponent* CapsuleComponent = GxCharacter->GetCapsuleComponent();
	gxcheck(CapsuleComponent);
	UCharacterMovementComponent* CharacterMovement = GxCharacter->GetCharacterMovement();
	gxcheck(CharacterMovement);

	GxPC->SetIgnoreLookInput(false);
	CapsuleComponent->SetCollisionProfileName(TEXT("Pawn"));

	GxCharacter->bUseControllerRotationPitch = false;
	FRotator Rotation = GxCharacter->GetActorRotation();
	GxCharacter->SetActorRotation(FRotator(0.0f, Rotation.Yaw, Rotation.Roll));

	UGxCombatComponent* GxCombatComponent = GxCharacter->GetGxCombatComponent();
	gxcheck(GxCombatComponent);

	GxCombatComponent->DisableWeapon(CurrentWeapon);
	CurrentWeapon = nullptr;

	CharacterMovement->SetMovementMode(MOVE_Falling);
	CharacterMovement->Velocity = FVector::ZeroVector;

	USphereComponent* SphereComponent = GxCharacter->GetComponentByClass<USphereComponent>();
	gxcheck(SphereComponent);

	SphereComponent->OnComponentBeginOverlap.RemoveDynamic(this, &ThisClass::OnOverlapBegin);
	SphereComponent->OnComponentEndOverlap.RemoveDynamic(this, &ThisClass::OnOverlapEnd);
	SphereComponent->SetActive(false);

	UMotionWarpingComponent* MotionWarpingComponent = GxCharacter->GetComponentByClass<UMotionWarpingComponent>();
	gxcheck(MotionWarpingComponent);

	MotionWarpingComponent->RemoveWarpTarget(SwiftTargetLocationName);
}

void UGxGameplayAbility_SwiftStrike::OnOverlapBegin(UPrimitiveComponent* OverlappedComp, AActor* OtherActor, UPrimitiveComponent* OtherComp, int32 OtherBodyIndex, bool bFromSweep, const FHitResult& SweepResult)
{
	if (HitActors.Find(OtherActor) == nullptr)
	{
		HitActors.Add(OtherActor);
		GX_NET_LOG_SCREEN(FColor::Green, TEXT("%s Hit"), *OtherActor->GetName());
	}
}

void UGxGameplayAbility_SwiftStrike::OnOverlapEnd(UPrimitiveComponent* OverlappedComp, AActor* OtherActor, UPrimitiveComponent* OtherComp, int32 OtherBodyIndex)
{

}
