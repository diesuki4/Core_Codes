#pragma once

#include "ARAG.h"
#include "GameFramework/Character.h"
#include "InputActionValue.h"

#include "AbilitySystemInterface.h"
#include "AbilitySystem/ARAbilitySystemComponent.h"
#include "Character/AbilitySystem/ARCharacterAttributeSet.h"
#include "Character/Types/ARCharacterState.h"

#include "ARCharacter.generated.h"

// 캐릭터 상태 변경 델리게이트
DECLARE_MULTICAST_DELEGATE_TwoParams(FOnCharacterStateChangedDelegate, EARCharacterState /*OldState*/, EARCharacterState /*NewState*/);
// 캐릭터 피격 델리게이트
DECLARE_MULTICAST_DELEGATE_OneParam(FOnCharacterDamagedDelegate, AActor* /*DamageCauser*/);
// 캐릭터 죽음 델리게이트
DECLARE_MULTICAST_DELEGATE(FOnCharacterDieDelegate);

class UARCharacterAttributeSet;
class UARCombatComponent;
class UARDataComponent;

/* 메인 캐릭터 */
UCLASS(config=Game)
class AARCharacter : public ACharacter, public IAbilitySystemInterface
{
    GENERATED_BODY()

    UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = Camera, meta = (AllowPrivateAccess = "true"))
    class USpringArmComponent* CameraBoom;

    UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = Camera, meta = (AllowPrivateAccess = "true"))
    class UCameraComponent* FollowCamera;
    
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = Input, meta = (AllowPrivateAccess = "true"))
    class UInputMappingContext* DefaultMappingContext;

    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = Input, meta = (AllowPrivateAccess = "true"))
    class UInputAction* JumpAction;

    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = Input, meta = (AllowPrivateAccess = "true"))
    class UInputAction* MoveAction;

    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = Input, meta = (AllowPrivateAccess = "true"))
    class UInputAction* LookAction;
    // 마우스 좌버튼 입력
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = Input, meta = (AllowPrivateAccess = "true"))
    class UInputAction* LfMouseAction;
    // 마우스 우버튼 입력
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = Input, meta = (AllowPrivateAccess = "true"))
    class UInputAction* RtMouseAction;

public:
    AARCharacter();

    FOnCharacterStateChangedDelegate OnStateChanged;
    FOnCharacterDamagedDelegate OnDamaged;
    FOnCharacterDieDelegate OnDie;

    virtual void PawnClientRestart() override;
    virtual float TakeDamage(float DamageAmount, struct FDamageEvent const& DamageEvent,
                             class AController* EventInstigator, AActor* DamageCauser) override;

    FORCEINLINE class USpringArmComponent* GetCameraBoom() const { return CameraBoom; }
    FORCEINLINE class UCameraComponent* GetFollowCamera() const { return FollowCamera; }

protected:
    virtual void SetupPlayerInputComponent(class UInputComponent* PlayerInputComponent) override;

    virtual void PostInitializeComponents() override;
    virtual void PossessedBy(AController* NewController) override;
    virtual void OnRep_PlayerState() override;
    virtual void BeginPlay() override;

    void Move(const FInputActionValue& Value);
    void Look(const FInputActionValue& Value);
    void LfMousePressed();  // 좌클릭 누를 때 처리
    void LfMouseReleased(); // 좌클릭 뗐을 때 처리
    void RtMousePressed();  // 우클릭 누를 때 처리

    // 캐릭터의 상태
    UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = Character, Meta = (AllowPrivateAccess = true))
    EARCharacterState State;

    // 임시 코드
    UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = Character, Meta = (AllowPrivateAccess = true))
    float HP;

public:
    // 전투 담당 컴포넌트
    UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = Combat)
    UARCombatComponent* CombatComponent;

    UARCombatComponent* GetCombatComponent() { return CombatComponent; }

    EARCharacterState GetState() const { return State; }
    void SetState(EARCharacterState NewState);

    // 임시 코드
    float GetHP() const { return HP; }

/* GAS */
public:
    virtual UAbilitySystemComponent* GetAbilitySystemComponent() const override { return AbilitySystemComponent; }

protected:
    UPROPERTY(EditDefaultsOnly)
    UARAbilitySystemComponent* AbilitySystemComponent;

    UPROPERTY(Transient)
    UARCharacterAttributeSet* AttributeSet;

/* 캐릭터 데이터 */
protected:
    UPROPERTY(VisibleAnywhere)
    UARDataComponent* DataComponent;
    // Data Component에서 가져온 데이터 캐싱
    class UARCharacterDataAsset* CharacterData;

public:
    UARDataComponent* GetDataComponent() { return DataComponent; }
};
