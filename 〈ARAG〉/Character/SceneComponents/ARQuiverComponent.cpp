#include "Character/SceneComponents/ARQuiverComponent.h"

#include "Character/Weapons/ARBowArrow.h"
#include "Engine/StaticMeshSocket.h"

UARQuiverComponent::UARQuiverComponent()
{
    PrimaryComponentTick.bCanEverTick = false;

    SetCollisionProfileName(TEXT("NoCollision"));

    nArrows = 0;
    MaxArrows = 10;

    ArrowBaseSocket = TEXT("ArrowBase");
}

void UARQuiverComponent::BeginPlay()
{
    Super::BeginPlay();

    // MaxArrows 수만큼 화살을 생성해 화살집에 넣는다.
    for (int32 i = 0; i < MaxArrows; ++i)
    {
        AARBowArrow* NewArrow = GetWorld()->SpawnActor<AARBowArrow>(ArrowClass);
        NewArrow->SetOwner(GetOwner());

        AttachArrow(NewArrow);
        ArrowActors.Add(NewArrow);
    }

    SetArrows(MaxArrows);
}

void UARQuiverComponent::EndPlay(const EEndPlayReason::Type EndPlayReason)
{
    // 화살 목록의 화살 소멸
    for (int32 i = 0; i < ArrowActors.Num(); ++i)
    {
        if (ArrowActors[i] != nullptr)
        {
            GetWorld()->DestroyActor(ArrowActors[i]);
            ArrowActors[i] = nullptr;
        }
    }

    ArrowActors.Empty();

    Super::EndPlay(EndPlayReason);
}

bool UARQuiverComponent::SetArrows(int32 NewArrows)
{
    NewArrows = FMath::Clamp<int32>(NewArrows, 0, MaxArrows);

    if (NewArrows == nArrows)
        return false;

    nArrows = NewArrows;
    OnArrowsChanged.Broadcast();     // 화살 개수 변경 델리게이트 발동

    if (nArrows <= 0)
    {
        nArrows = 0;
        OnArrowsIsZero.Broadcast();  // 화살 개수 0개 델리게이트 발동
    }

    return true;
}

int32 UARQuiverComponent::GetArrows() const
{
    return nArrows;
}

// 화살을 뽑을 수 있는지 반환
bool UARQuiverComponent::CanPickArrow() const
{
    return (0 < nArrows);
}

// 화살집에서 화살 1개 뽑기
AARBowArrow* UARQuiverComponent::PickArrow()
{
    if (SetArrows(nArrows - 1))
    {
        // 화살 목록의 마지막 화살을 화살집에서 꺼내 반환한다.
        ArrowActors.Last()->DetachFromActor(FDetachmentTransformRules::KeepWorldTransform);

        return ArrowActors.Pop();
    }

    return nullptr;
}

// 화살집에 화살 1개 넣기
bool UARQuiverComponent::PutArrow(AARBowArrow* NewArrow)
{
    if (SetArrows(nArrows + 1))
    {
        // 전달된 화살이 없으면
        if (NewArrow == nullptr)
        {
            // 새로 생성해서 넣는다.
            NewArrow = GetWorld()->SpawnActor<AARBowArrow>(ArrowClass);
            NewArrow->SetOwner(GetOwner());
        }

        AttachArrow(NewArrow);
        ArrowActors.Add(NewArrow);

        return true;
    }

    return false;
}

// 화살집에 화살 부착
void UARQuiverComponent::AttachArrow(AARBowArrow* NewArrow)
{
    ARCHECK(NewArrow != nullptr);
    ARCHECK(DoesSocketExist(ArrowBaseSocket));

    FVector RandomPos = FVector(FMath::RandRange(-5.F, 5.F), FMath::RandRange(-4.F, 4.F), FMath::RandRange(-3.F, 3.F));
    FRotator RandomRot = FRotator(FMath::RandRange(-5.F, 5.F), 0.F, FMath::RandRange(-5.F, 5.F));

    NewArrow->SetActorRelativeLocation(RandomPos);
    NewArrow->SetActorRelativeRotation(RandomRot);

    NewArrow->AttachToComponent(this, FAttachmentTransformRules::KeepRelativeTransform, ArrowBaseSocket);
}
