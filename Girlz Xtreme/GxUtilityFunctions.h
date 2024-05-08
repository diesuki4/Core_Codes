// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "Types/AttributeStorage.h"

/**
 * GxUtilityFunctions
 *
 * 유용한 전역 함수 모음
 */

// enum class 열거자의 값을 기반 타입으로 반환
template <typename E>
constexpr auto ToUtype(E enumerator) noexcept
{
	return StaticCast<UE::Interchange::Private::TUnderlyingType<E>::type>(enumerator);
}
