// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "Abilities/GameplayAbility.h"
#include "Input/GxInputTypes.h"

#include "GxGameplayAbility.generated.h"

class AGxCharacter;
class AGxPlayerController;
class UGxAbilitySystemComponent;
struct FGameplayAbilityActorInfo;
struct FGameplayTagContainer;
struct FGameplayEventData;

/**
 * UGxGameplayAbility
 *
 * 게임에서 사용될 기본 Gameplay Ability
 */
UCLASS(Abstract, Meta = (ShortTooltip = "The base gameplay ability class used by this project."))
class UGxGameplayAbility : public UGameplayAbility
{
	GENERATED_BODY()

public:
	UGxGameplayAbility(const FObjectInitializer& ObjectInitializer = FObjectInitializer::Get());

	UFUNCTION(BlueprintCallable, Category = "Gx|Ability")
	UGxAbilitySystemComponent* GetGxAbilitySystemComponentFromActorInfo() const;

	UFUNCTION(BlueprintCallable, Category = "Gx|Ability")
	AGxPlayerController* GetGxPlayerControllerFromActorInfo() const;

	UFUNCTION(BlueprintCallable, Category = "Gx|Ability")
	AGxCharacter* GetGxCharacterFromActorInfo() const;

protected:
	//~UGameplayAbility interface
	virtual bool CanActivateAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayTagContainer* SourceTags, const FGameplayTagContainer* TargetTags, FGameplayTagContainer* OptionalRelevantTags) const override;
	virtual void PreActivate(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, FOnGameplayAbilityEnded::FDelegate* OnGameplayAbilityEndedDelegate, const FGameplayEventData* TriggerEventData = nullptr) override;
	virtual void ActivateAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, const FGameplayEventData* TriggerEventData) override;
	virtual void EndAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, bool bReplicateEndAbility, bool bWasCancelled) override;
	//~End of UGameplayAbility interface

	UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = "Gx|Ability")
	TSubclassOf<AGxCharacter> ActivatableHeroClass;

	// 입력 바인딩에 사용될 입력 ID
	UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = "Gx|Inputs")
	EGxAbilityInputID AbilityInputID;

public:
	UFUNCTION(BlueprintCallable, Category = "Gx|Inputs")
	const EGxAbilityInputID GetGxAbilityInputID() const { return AbilityInputID; }
};
