// Fill out your copyright notice in the Description page of Project Settings.

#include "GxPlayGameMode.h"

#include "GxLogChannels.h"

#include UE_INLINE_GENERATED_CPP_BY_NAME(GxPlayGameMode)

//////////////////////////////////////////////////////////////////////////
// AGxPlayGameMode

AGxPlayGameMode::AGxPlayGameMode(const FObjectInitializer& ObjectInitializer)
	: Super(ObjectInitializer)
{
	bDelayedStart = true;
}

void AGxPlayGameMode::InitGame(const FString& MapName, const FString& Options, FString& ErrorMessage)
{
	Super::InitGame(MapName, Options, ErrorMessage);
	
	if (IsRunningDedicatedServer())
	{
		GRPC_UpdateAddressToMatchmaker();
	}
}

void AGxPlayGameMode::PostLogin(APlayerController* NewPlayer)
{
	Super::PostLogin(NewPlayer);

	bool bMatchFull = (MaxPlayers <= NumPlayers);

	if (bMatchFull)
	{
		if (IsRunningDedicatedServer())
		{
			GRPC_RemoveAddressFromMatchmaker();
		}

		StartMatch();
	}
	else
	{
		if (IsRunningDedicatedServer())
		{
			GRPC_UpdateAddressToMatchmaker();
		}
	}
}

void AGxPlayGameMode::Logout(AController* Exiting)
{	
	Super::Logout(Exiting);

	bool bNobody = (NumPlayers <= 0);

	if (HasMatchStarted() && bNobody)
	{
		RestartGame();
	}
	else
	{
		if (IsRunningDedicatedServer())
		{
			GRPC_UpdateAddressToMatchmaker();
		}
	}
}

void AGxPlayGameMode::StartMatch()
{
	Super::StartMatch();

	GX_NET_LOG(Warning, TEXT("Match started."));
}
