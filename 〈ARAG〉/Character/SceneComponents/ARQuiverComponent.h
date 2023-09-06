#pragma once

#include "ARAG.h"
#include "Components/StaticMeshComponent.h"
#include "ARQuiverComponent.generated.h"

DECLARE_MULTICAST_DELEGATE(FOnArrowsIsZeroDelegate);    // 화살 개수가 0이 될 때 발동
DECLARE_MULTICAST_DELEGATE(FOnArrowsChangedDelegate);   // 화살 개수가 변할 때 발동

class AARBowArrow;

/* 활(AR Bow) 무기 장착 시 캐릭터에 추가되는 화살집 컴포넌트 */
UCLASS()    
class ARAG_API UARQuiverComponent : public UStaticMeshComponent
{
    GENERATED_BODY()
    
public:
    UARQuiverComponent();

protected:
    virtual void BeginPlay() override;
    virtual void EndPlay (const EEndPlayReason::Type EndPlayReason) override;
    
public:
    FOnArrowsIsZeroDelegate OnArrowsIsZero;
    FOnArrowsChangedDelegate OnArrowsChanged;

    int32 GetArrows() const;
    bool CanPickArrow() const;  // 화살 개수가 0인지 확인해 뽑을 수 있는지 반환
    AARBowArrow* PickArrow();   // 화살집에서 화살 1개 뽑기
    bool PutArrow(AARBowArrow* NewArrow = nullptr);   // 화살집에 화살 1개 넣기

private:
    bool SetArrows(int32 NewArrows);
    void AttachArrow(AARBowArrow* NewArrow);     // 화살집에 화살 부착

    // 화살 종류
    UPROPERTY(EditDefaultsOnly, Category = Quiver, Meta = (AllowPrivateAccess = true))
    TSubclassOf<class AARBowArrow> ArrowClass;
    // 최대 화살 개수
    UPROPERTY(EditDefaultsOnly, BlueprintReadWrite, Category = Quiver, Meta = (AllowPrivateAccess = true))
    int32 MaxArrows;
    // 현재 화살 개수
    UPROPERTY(VisibleAnywhere, BlueprintReadOnly, Category = Quiver, Meta = (AllowPrivateAccess = true))
    int32 nArrows;
    // 화살집 메시에서의 화살 소켓
    UPROPERTY(EditDefaultsOnly, Category = Quiver, meta = (AllowPrivateAccess = true))
    FName ArrowBaseSocket;
    // 현재 화살 액터 배열
    TArray<AARBowArrow*> ArrowActors;
};
