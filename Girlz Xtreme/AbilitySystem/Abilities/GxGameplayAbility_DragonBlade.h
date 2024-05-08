// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "AbilitySystem/Abilities/GxGameplayAbility.h"

#include "GxGameplayAbility_DragonBlade.generated.h"

class AGxCharacter;
class AGxWeapon;
class UAnimMontage;
class UInputMappingContext;
class UInputAction;

/**
 * UGxGameplayAbility_DragonBlade
 *
 * 공격A 용검 궁극기
 */
UCLASS(Abstract, Meta = (ShortTooltip = "Gameplay ability used for AttakA hero dragon blade ultimate."))
class UGxGameplayAbility_DragonBlade : public UGxGameplayAbility
{
	GENERATED_BODY()

public:
	UGxGameplayAbility_DragonBlade(const FObjectInitializer& ObjectInitializer = FObjectInitializer::Get());

protected:
	//~UGxGameplayAbility interface
	virtual void PreActivate(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, FOnGameplayAbilityEnded::FDelegate* OnGameplayAbilityEndedDelegate, const FGameplayEventData* TriggerEventData = nullptr) override;
	virtual void ActivateAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, const FGameplayEventData* TriggerEventData) override;
	virtual void EndAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, bool bReplicateEndAbility, bool bWasCancelled) override;
	//~End of UGxGameplayAbility interface

	UFUNCTION()
	void OnMontageCancelledCallback();

protected:
	UPROPERTY(EditDefaultsOnly, Category = "Gx|DragonBlade")
	TSubclassOf<AGxWeapon> EquipWeaponClass;

	UPROPERTY(EditDefaultsOnly, Category = "Gx|DragonBlade")
	TObjectPtr<UAnimMontage> AnimMontage;

	UPROPERTY(EditDefaultsOnly, Category = "Gx|DragonBlade")
	float Duration;

	UPROPERTY(EditDefaultsOnly, Category = "Gx|DragonBlade")
	TSubclassOf<UGxGameplayAbility> SlashAbility;

	UPROPERTY(EditDefaultsOnly, Category = "Gx|DragonBlade")
	TObjectPtr<const UInputMappingContext> DragonBladeMappingContext;
	
	UPROPERTY(EditDefaultsOnly, Category = "Gx|DragonBlade")
	uint8 MappingPriority;

	UPROPERTY(EditDefaultsOnly, Category = "Gx|DragonBlade")
	TObjectPtr<const UInputAction> SlashAction;

	UPROPERTY(Transient, VisibleAnywhere, BlueprintReadOnly, Category = "Gx|DragonBlade")
	TObjectPtr<AGxWeapon> CurrentWeapon;

protected:
	void TriggerSlashAbility();

	uint32 SlashActionEventBindingHandle;

	UFUNCTION()
	void OnWaitFinishedCallback();
};
