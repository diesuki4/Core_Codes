// Fill out your copyright notice in the Description page of Project Settings.

#include "GxStatics.h"

#include "GxLogChannels.h"

#include UE_INLINE_GENERATED_CPP_BY_NAME(GxStatics)

//////////////////////////////////////////////////////////////////////////
// UGxStatics

const int32 UGxStatics::GetServerPort(UObject* WorldContextObject)
{
	check(WorldContextObject);

	if (IsRunningDedicatedServer())
	{
		UWorld* World = WorldContextObject->GetWorld();
		check(World);

		return World->URL.Port;
	}

	return -1;
}
