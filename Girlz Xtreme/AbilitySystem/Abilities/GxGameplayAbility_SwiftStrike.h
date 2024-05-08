// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "AbilitySystem/Abilities/GxGameplayAbility.h"

#include "GxGameplayAbility_SwiftStrike.generated.h"

class AActor;
class UPrimitiveComponent;
class AGxCharacter;
class AGxWeapon;
class UAnimMontage;
struct FHitResult;

/**
 * UGxGameplayAbility_SwiftStrike
 *
 * 공격A 질풍참 스킬
 */
UCLASS(Abstract, Meta = (ShortTooltip = "Gameplay ability used for AttakA hero swift strike skill."))
class UGxGameplayAbility_SwiftStrike : public UGxGameplayAbility
{
	GENERATED_BODY()

public:
	UGxGameplayAbility_SwiftStrike(const FObjectInitializer& ObjectInitializer = FObjectInitializer::Get());

protected:
	//~UGxGameplayAbility interface
	virtual void PreActivate(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, FOnGameplayAbilityEnded::FDelegate* OnGameplayAbilityEndedDelegate, const FGameplayEventData* TriggerEventData = nullptr) override;
	virtual void ActivateAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, const FGameplayEventData* TriggerEventData) override;
	virtual void EndAbility(const FGameplayAbilitySpecHandle Handle, const FGameplayAbilityActorInfo* ActorInfo, const FGameplayAbilityActivationInfo ActivationInfo, bool bReplicateEndAbility, bool bWasCancelled) override;
	//~End of UGxGameplayAbility interface

	UFUNCTION()
	void OnMontageFinishedCallback();
	
	UFUNCTION()
	void OnMontageCancelledCallback();
	
	virtual void PerpareMotionWarping();
	virtual void FinishMotionWarping();

protected:
	UPROPERTY(EditDefaultsOnly, Category = "Gx|SwiftStrike")
	TSubclassOf<AGxWeapon> EquipWeaponClass;

	UPROPERTY(EditDefaultsOnly, Category = "Gx|SwiftStrike")
	TObjectPtr<UAnimMontage> AnimMontage;

	UPROPERTY(EditDefaultsOnly, Category = "Gx|SwiftStrike")
	FName SwiftTargetLocationName;

	UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = "Gx|SwiftStrike")
	float SwiftDistance;

	UPROPERTY(Transient, VisibleAnywhere, BlueprintReadOnly, Category = "Gx|SwiftStrike")
	TObjectPtr<AGxWeapon> CurrentWeapon;

protected:
	UFUNCTION()
	void OnOverlapBegin(UPrimitiveComponent* OverlappedComp, AActor* OtherActor, UPrimitiveComponent* OtherComp, int32 OtherBodyIndex, bool bFromSweep, const FHitResult& SweepResult);

	UFUNCTION()
	void OnOverlapEnd(UPrimitiveComponent* OverlappedComp, AActor* OtherActor, UPrimitiveComponent* OtherComp, int32 OtherBodyIndex);

	UPROPERTY(Transient, VisibleAnywhere, BlueprintReadOnly, Category = "Gx|SwiftStrike")
	TSet<TObjectPtr<AActor>> HitActors;
};
