// Fill out your copyright notice in the Description page of Project Settings.

#include "GxWeapon.h"

#include "GxLogChannels.h"
#include "Character/GxCharacter.h"
#include "Components/SkeletalMeshComponent.h"

#include UE_INLINE_GENERATED_CPP_BY_NAME(GxWeapon)

//////////////////////////////////////////////////////////////////////////
// AGxWeapon

AGxWeapon::AGxWeapon(const FObjectInitializer& ObjectInitializer)
	: Super(ObjectInitializer)
	, AttachSocket(TEXT("weapon_socket"))
{
	SkeletalMeshComponent = ObjectInitializer.CreateDefaultSubobject<USkeletalMeshComponent>(this, TEXT("SkeletalMeshComponent"));
	SkeletalMeshComponent->SetCollisionProfileName(TEXT("NoCollision"));
	RootComponent = SkeletalMeshComponent;

	// Tick is disabled by default.
	PrimaryActorTick.bCanEverTick = false;
}

void AGxWeapon::Equip(AGxCharacter* NewOwnerCharacter)
{
	gxcheck(NewOwnerCharacter);

	USkeletalMeshComponent* CharacterMesh = NewOwnerCharacter->GetMesh();
	gxcheck(CharacterMesh);

	if (CharacterMesh->DoesSocketExist(AttachSocket))
	{
		FAttachmentTransformRules AttachmentTransformRules = FAttachmentTransformRules::SnapToTargetIncludingScale;

		AttachToComponent(CharacterMesh, AttachmentTransformRules, AttachSocket);

		SetOwner(NewOwnerCharacter);
		OnEquipped.Broadcast(NewOwnerCharacter);
	}
}

void AGxWeapon::Unequip()
{
	AGxCharacter* OwnerCharacter = Cast<AGxCharacter>(GetOwner());
	gxcheck(OwnerCharacter);

	DetachFromActor(FDetachmentTransformRules::KeepWorldTransform);

	OnUnequipped.Broadcast(OwnerCharacter);
	SetOwner(nullptr);
}
