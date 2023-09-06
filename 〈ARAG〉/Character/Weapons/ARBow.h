#pragma once

#include "ARAG.h"
#include "Character/Weapons/ARWeaponSkeletalBase.h"
#include "ARBow.generated.h"
/*
    활 무기 (AR Weapon Base - AR Weapon Skeletal Base - AR Bow)
    - 좌클릭 누름: 활 조준
    - 좌클릭 뗌:   활 쏘기
    - 우클릭 누름: 화살 다시 집어넣기
*/
UCLASS()
class ARAG_API AARBow : public AARWeaponSkeletalBase
{
    GENERATED_BODY()
    
public:
    AARBow();

public:
    virtual void Equip(AARCharacter* NewCharacter) override;
    virtual void Unequip() override;

    // 활 조준 -> 애니메이션 재생 -> AttackStart 애님 노이파이 -> AttackStart() 실행
    virtual void LfMousePressed() override;      
    // 활 쏘기 -> 애니메이션 재생 -> Attack 애님 노티파이 -> Attack() 실행
    virtual void LfMouseReleased() override;
    // 화살 다시 집어넣기 -> 애니메이션 재생 -> AttackEnd 애님 노이파이 -> AttackEnd() 실행
    virtual void RtMousePressed() override;

    virtual bool CanUse() override;      // 현재 사용 가능한 상태인지 반환

protected:
    // 각 애님 노티파이 시 호출되는 함수
    virtual void AttackStart() override;  // AttackStart
    virtual void Attack() override;       // Attack
    // ARCharacter->OnStateChanged 델리게이트에 등록
    // 공격 중에 피격당할 시 처리하기 위함
    virtual void OnCharacterStateChanged(EARCharacterState OldState, EARCharacterState NewState) override;

    // 화살집 종류
    UPROPERTY(EditDefaultsOnly, Category = Bow, Meta = (AllowPrivateAccess = true))
    TSubclassOf<class UARQuiverComponent> QuiverComponentClass;
    // 캐릭터 메시에 화살집 컴포넌트를 부착할 소켓
    UPROPERTY(EditDefaultsOnly, Category = Bow, meta = (AllowPrivateAccess = true))
    FName QuiverSocket;
    // 활 공격 시 캐릭터 메시 손 부분에 화살을 붙일 소켓
    UPROPERTY(EditDefaultsOnly, Category = Bow, meta = (AllowPrivateAccess = true))
    FName ArrowSocket;
    // 일반 FOV
    UPROPERTY(EditDefaultsOnly, Category = Bow, meta = (AllowPrivateAccess = true))
    float IdleFOV;
    // 조준 시 FOV
    UPROPERTY(EditDefaultsOnly, Category = Bow, meta = (AllowPrivateAccess = true))
    float AimingFOV;

    // 캐릭터에 붙인 화살집 저장
    UARQuiverComponent* QuiverComponent;
    // 현재 캐릭터 손에 있는 화살
    class AARBowArrow* CurrentArrow;
};
