// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "GameFramework/Character.h"
#include "AbilitySystemInterface.h"

#include "GxCharacter.generated.h"

class USpringArmComponent;
class UCameraComponent;
class UGxHeroComponent;
class UGxCombatComponent;
class UInputComponent;
class UGxInputConfig;
class AGxPlayerController;
class AGxPlayerState;
class UGxAnimInstance;
class AGxWeapon;
class UGxAbilitySystemComponent;
class UGxGameplayAbility;
class UGameplayEffect;

/**
 * AGxCharacter
 * 
 * 게임에서 기본으로 사용되는 캐릭터 클래스
 */
UCLASS(Meta = (ShortTooltip = "The base character pawn class used by this project."))
class AGxCharacter : public ACharacter, public IAbilitySystemInterface
{
	GENERATED_BODY()

	UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = "Gx|Character", Meta = (AllowPrivateAccess = "true"))
	TObjectPtr<USpringArmComponent> CameraBoom;

	UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = "Gx|Character", Meta = (AllowPrivateAccess = "true"))
	TObjectPtr<UCameraComponent> FollowCamera;

	UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = "Gx|Character", Meta = (AllowPrivateAccess = "true"))
	TObjectPtr<UGxHeroComponent> HeroComponent;

	UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = "Gx|Character", Meta = (AllowPrivateAccess = "true"))
	TObjectPtr<UGxCombatComponent> CombatComponent;

public:
	UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = "Gx|Input", Meta = (AllowPrivateAccess = "true"))
	TObjectPtr<const UGxInputConfig> InputConfig;

public:
	AGxCharacter(const FObjectInitializer& ObjectInitializer = FObjectInitializer::Get());

	UFUNCTION(BlueprintCallable, Category = "Gx|Character")
	AGxPlayerController* GetGxPlayerController() const;

	UFUNCTION(BlueprintCallable, Category = "Gx|Character")
	AGxPlayerState* GetGxPlayerState() const;

	//~IAbilitySystemInterface
	virtual UAbilitySystemComponent* GetAbilitySystemComponent() const override;
	//~End of IAbilitySystemInterface
	UFUNCTION(BlueprintCallable, Category = "Gx|Character")
	UGxAbilitySystemComponent* GetGxAbilitySystemComponent() const;

	UFUNCTION(BlueprintCallable, Category = "Gx|Character")
	UGxAnimInstance* GetGxAnimInstance() const;

public:
	//~ACharacter interface
	virtual bool CanCrouch() const override;
	//~End of ACharacter interface

protected:
	//~ACharacter interface
	virtual void SetupPlayerInputComponent(UInputComponent* PlayerInputComponent) override;
	virtual void PossessedBy(AController* NewController) override;
	virtual void BeginPlay() override;
	virtual void EndPlay(const EEndPlayReason::Type EndPlayReason) override;
	virtual bool CanJumpInternal_Implementation() const override;
	//~End of ACharacter interface
		
	UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = "Gx|Ability")
	TArray<TSubclassOf<UGxGameplayAbility>> DefaultInputAbilities;

	UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = "Gx|Ability")
	TArray<TSubclassOf<UGxGameplayAbility>> DefaultAbilities;
	
	UPROPERTY(EditDefaultsOnly, BlueprintReadOnly, Category = "Gx|Effect")
	TArray<TSubclassOf<UGameplayEffect>> DefaultEffects;

	UPROPERTY(EditDefaultsOnly, Category = "Gx|Combat")
	TArray<TSubclassOf<AGxWeapon>> DefaultWeaponClasses;

public:
	FORCEINLINE USpringArmComponent* GetCameraBoom() const { return CameraBoom; }
	FORCEINLINE UCameraComponent* GetFollowCamera() const { return FollowCamera; }
	FORCEINLINE UGxHeroComponent* GetGxHeroComponent() const { return HeroComponent; }
	FORCEINLINE UGxCombatComponent* GetGxCombatComponent() const { return CombatComponent; }
};
