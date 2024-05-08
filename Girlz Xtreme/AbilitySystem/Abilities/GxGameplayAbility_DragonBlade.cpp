// Fill out your copyright notice in the Description page of Project Settings.

#include "GxGameplayAbility_DragonBlade.h"

#include "GxLogChannels.h"
#include "Interaction/Tasks/GxAbilityTask_PlayMontageAndWait.h"
#include "Character/GxCharacter.h"
#include "Character/GxCombatComponent.h"
#include "Player/GxPlayerController.h"
#include "EnhancedInputSubsystems.h"
#include "Input/GxInputComponent.h"
#include "AbilitySystem/GxAbilitySystemComponent.h"
#include "Abilities/Tasks/AbilityTask_WaitDelay.h"

#include UE_INLINE_GENERATED_CPP_BY_NAME(GxGameplayAbility_DragonBlade)

//////////////////////////////////////////////////////////////////////////
// UGxGameplayAbility_DragonBlade

UGxGameplayAbility_DragonBlade::UGxGameplayAbility_DragonBlade(const FObjectInitializer& ObjectInitializer)
	: Super(ObjectInitializer)
	, Duration(7.0f)
{
	
}

void UGxGameplayAbility_DragonBlade::PreActivate(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, FOnGameplayAbilityEnded::FDelegate* OnGameplayAbilityEndedDelegate, const FGameplayEventData* TriggerEventData)
{
	Super::PreActivate(Handle, ActorInfo, ActivationInfo, OnGameplayAbilityEndedDelegate, TriggerEventData);

	AGxCharacter* GxCharacter = GetGxCharacterFromActorInfo();
	gxcheck(GxCharacter);

	UGxCombatComponent* GxCombatComponent = GxCharacter->GetGxCombatComponent();
	gxcheck(GxCombatComponent);

	CurrentWeapon = GxCombatComponent->EnableWeapon(EquipWeaponClass);

	AGxPlayerController* GxPC = GetGxPlayerControllerFromActorInfo();
	gxcheck(GxPC);
	ULocalPlayer* LP = GxPC->GetLocalPlayer();
	gxcheck(LP);
	UEnhancedInputLocalPlayerSubsystem* EILPSubsystem = LP->GetSubsystem<UEnhancedInputLocalPlayerSubsystem>();
	gxcheck(EILPSubsystem);
	gxcheck(DragonBladeMappingContext);

	EILPSubsystem->AddMappingContext(DragonBladeMappingContext, MappingPriority);

	UGxInputComponent* GxIC = Cast<UGxInputComponent>(GxCharacter->InputComponent);
	gxcheck(GxIC);

	FEnhancedInputActionEventBinding& SlashActionEventBinding = GxIC->BindAction(SlashAction, ETriggerEvent::Started, this, &ThisClass::TriggerSlashAbility);
	SlashActionEventBindingHandle = SlashActionEventBinding.GetHandle();
}

void UGxGameplayAbility_DragonBlade::ActivateAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, const FGameplayEventData* TriggerEventData)
{
	Super::ActivateAbility(Handle, ActorInfo, ActivationInfo, TriggerEventData);

	UGxAbilityTask_PlayMontageAndWait* GxPlayMontageAndWaitTask = UGxAbilityTask_PlayMontageAndWait::CreateGxPlayMontageAndWaitProxy(this, NAME_None, AnimMontage);
	gxcheck(GxPlayMontageAndWaitTask);

	GxPlayMontageAndWaitTask->OnCancelled.AddDynamic(this, &ThisClass::OnMontageCancelledCallback);
	GxPlayMontageAndWaitTask->ReadyForActivation();

	UAbilityTask_WaitDelay* WaitDelayTask = UAbilityTask_WaitDelay::WaitDelay(this, Duration);
	WaitDelayTask->OnFinish.AddDynamic(this, &ThisClass::OnWaitFinishedCallback);
	WaitDelayTask->ReadyForActivation();
}

void UGxGameplayAbility_DragonBlade::EndAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, bool bReplicateEndAbility, bool bWasCancelled)
{
	AGxCharacter* GxCharacter = GetGxCharacterFromActorInfo();
	gxcheck(GxCharacter);

	UGxCombatComponent* GxCombatComponent = GxCharacter->GetGxCombatComponent();
	gxcheck(GxCombatComponent);

	GxCombatComponent->DisableWeapon(CurrentWeapon);
	CurrentWeapon = nullptr;

	AGxPlayerController* GxPC = GetGxPlayerControllerFromActorInfo();
	gxcheck(GxPC);
	ULocalPlayer* LP = GxPC->GetLocalPlayer();
	gxcheck(LP);
	UEnhancedInputLocalPlayerSubsystem* EILPSubsystem = LP->GetSubsystem<UEnhancedInputLocalPlayerSubsystem>();
	gxcheck(EILPSubsystem);
	gxcheck(DragonBladeMappingContext);

	UGxInputComponent* GxIC = Cast<UGxInputComponent>(GxCharacter->InputComponent);
	gxcheck(GxIC);

	GxIC->RemoveBindingByHandle(SlashActionEventBindingHandle);

	EILPSubsystem->RemoveMappingContext(DragonBladeMappingContext);

	Super::EndAbility(Handle, ActorInfo, ActivationInfo, bReplicateEndAbility, bWasCancelled);
}

void UGxGameplayAbility_DragonBlade::OnMontageCancelledCallback()
{
	EndAbility(CurrentSpecHandle, CurrentActorInfo, CurrentActivationInfo, /*bReplicateEndAbility*/true, /*bWasCancelled*/true);
}

void UGxGameplayAbility_DragonBlade::OnWaitFinishedCallback()
{
	EndAbility(CurrentSpecHandle, CurrentActorInfo, CurrentActivationInfo, /*bReplicateEndAbility*/true, /*bWasCancelled*/false);
}

void UGxGameplayAbility_DragonBlade::TriggerSlashAbility()
{
	UGxAbilitySystemComponent* GxASC = GetGxAbilitySystemComponentFromActorInfo();
	gxcheck(GxASC);

	GxASC->TryActivateAbilityByClass(SlashAbility);
}
