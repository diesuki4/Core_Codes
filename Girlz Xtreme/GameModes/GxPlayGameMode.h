// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "GameModes/GxGameMode.h"

#include "GxPlayGameMode.generated.h"

/**
 * AGxPlayGameMode
 *
 * 데디케이티드 서버에서 메인 플레이에 사용되는 게임 모드
 */
UCLASS(Meta = (ShortTooltip = "The main play game mode class used by this project."))
class AGxPlayGameMode : public AGxGameMode
{
	GENERATED_BODY()

public:
	AGxPlayGameMode(const FObjectInitializer& ObjectInitializer = FObjectInitializer::Get());

	//~AGameMode interface
	virtual void InitGame(const FString& MapName, const FString& Options, FString& ErrorMessage) override;
	virtual void Logout(AController* Exiting) override;
	virtual void StartMatch() override;
	//~End of AGameMode interface

	//~AGxGameMode interface
	virtual void PostLogin(APlayerController* NewPlayer) override;
	//~End of AGxGameMode interface

protected:
	UFUNCTION(BlueprintImplementableEvent, Category = "Gx|GameMode|Play")
	void GRPC_UpdateAddressToMatchmaker();

	UFUNCTION(BlueprintImplementableEvent, Category = "Gx|GameMode|Play")
	void GRPC_RemoveAddressFromMatchmaker();
};
