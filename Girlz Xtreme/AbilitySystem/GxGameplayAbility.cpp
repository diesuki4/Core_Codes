// Fill out your copyright notice in the Description page of Project Settings.

#include "GxGameplayAbility.h"

#include "GxLogChannels.h"
#include "Player/GxPlayerController.h"
#include "Character/GxCharacter.h"
#include "AbilitySystem/GxAbilitySystemComponent.h"

#include UE_INLINE_GENERATED_CPP_BY_NAME(GxGameplayAbility)

//////////////////////////////////////////////////////////////////////////
// UGxGameplayAbility

UGxGameplayAbility::UGxGameplayAbility(const FObjectInitializer& ObjectInitializer)
	: Super(ObjectInitializer)
{
	InstancingPolicy = EGameplayAbilityInstancingPolicy::InstancedPerActor;
	NetExecutionPolicy = EGameplayAbilityNetExecutionPolicy::LocalPredicted;
	NetSecurityPolicy = EGameplayAbilityNetSecurityPolicy::ClientOrServer;

	bServerRespectsRemoteAbilityCancellation = false;
}

UGxAbilitySystemComponent* UGxGameplayAbility::GetGxAbilitySystemComponentFromActorInfo() const
{
	return (CurrentActorInfo ? Cast<UGxAbilitySystemComponent>(CurrentActorInfo->AbilitySystemComponent.Get()) : nullptr);
}

AGxPlayerController* UGxGameplayAbility::GetGxPlayerControllerFromActorInfo() const
{
	return (CurrentActorInfo ? Cast<AGxPlayerController>(CurrentActorInfo->PlayerController.Get()) : nullptr);
}

AGxCharacter* UGxGameplayAbility::GetGxCharacterFromActorInfo() const
{
	return (CurrentActorInfo ? Cast<AGxCharacter>(CurrentActorInfo->AvatarActor.Get()) : nullptr);
}

bool UGxGameplayAbility::CanActivateAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayTagContainer* SourceTags, const FGameplayTagContainer* TargetTags, FGameplayTagContainer* OptionalRelevantTags) const
{
	if (!Super::CanActivateAbility(Handle, ActorInfo, SourceTags, TargetTags, OptionalRelevantTags))
	{
		return false;
	}

	AGxCharacter* GxCharacter = GetGxCharacterFromActorInfo();
	gxcheck(GxCharacter, false);

	if (!GxCharacter->IsA(ActivatableHeroClass))
	{
		return false;
	}

	return true;
}

void UGxGameplayAbility::PreActivate(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, FOnGameplayAbilityEnded::FDelegate* OnGameplayAbilityEndedDelegate, const FGameplayEventData* TriggerEventData)
{
	Super::PreActivate(Handle, ActorInfo, ActivationInfo, OnGameplayAbilityEndedDelegate, TriggerEventData);

}

void UGxGameplayAbility::ActivateAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, const FGameplayEventData* TriggerEventData)
{
	Super::ActivateAbility(Handle, ActorInfo, ActivationInfo, TriggerEventData);

}

void UGxGameplayAbility::EndAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, bool bReplicateEndAbility, bool bWasCancelled)
{
	Super::EndAbility(Handle, ActorInfo, ActivationInfo, bReplicateEndAbility, bWasCancelled);

}
