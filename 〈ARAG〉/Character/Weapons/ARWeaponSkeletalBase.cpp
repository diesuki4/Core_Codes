#include "Character/Weapons/ARWeaponSkeletalBase.h"

AARWeaponSkeletalBase::AARWeaponSkeletalBase()
{
    SkeletalMeshComponent = CreateDefaultSubobject<USkeletalMeshComponent>(TEXT("SkeletalMesh"));
    SkeletalMeshComponent->SetCollisionProfileName(TEXT("NoCollision"));

    RootComponent = SkeletalMeshComponent;
}

void AARWeaponSkeletalBase::Equip(AARCharacter* NewCharacter)
{
    Super::Equip(NewCharacter);

    USkeletalMeshComponent* CharacterMesh = Character->GetMesh();
    ARCHECK(CharacterMesh != nullptr);
    // 캐릭터 메시에 무기 장착
    if (CharacterMesh->DoesSocketExist(WeaponSocket))
    {
        FAttachmentTransformRules AttachmentTransformRules = FAttachmentTransformRules::SnapToTargetIncludingScale;

        AttachToComponent(CharacterMesh, AttachmentTransformRules, WeaponSocket);
    }
}

void AARWeaponSkeletalBase::Unequip()
{
    USkeletalMeshComponent* CharacterMesh = Character->GetMesh();
    ARCHECK(CharacterMesh != nullptr);
    // 캐릭터 메시에서 무기 해제
    if (CharacterMesh->DoesSocketExist(WeaponSocket))
    {
        DetachFromActor(FDetachmentTransformRules::KeepWorldTransform);
    }

    Super::Unequip();
}
