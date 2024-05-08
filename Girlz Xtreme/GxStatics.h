// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "Kismet/BlueprintFunctionLibrary.h"

#include "GxStatics.generated.h"

/**
 * UGxStatics
 *
 * 전역 BP 함수들
 */
UCLASS(Meta = (ShortTooltip = "Global blueprint functions."))
class UGxStatics : public UBlueprintFunctionLibrary
{
	GENERATED_BODY()
	
public:	
	UFUNCTION(BlueprintPure, meta = (WorldContext = "WorldContextObject"))
	static const int32 GetServerPort(UObject* WorldContextObject);
};
