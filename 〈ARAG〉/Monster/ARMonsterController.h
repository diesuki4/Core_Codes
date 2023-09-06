#pragma once

#include "ARAG.h"
#include "AIController.h"

#include "Perception/AIPerceptionTypes.h"

#include "ARMonsterController.generated.h"

/* 몬스터 AI 컨트롤러 */
UCLASS()
class ARAG_API AARMonsterController : public AAIController
{
    GENERATED_BODY()

public:
    AARMonsterController();

    virtual void OnPossess(APawn* InPawn) override;
    virtual void OnUnPossess() override;

    // 블랙보드 키 이름
    static const FName OriginLocationKey;   // 초기 위치
    static const FName PatrolLocationKey;   // 목적지 위치
    static const FName TargetActorKey;      // 현재 추적 중인 타겟
    static const FName MonsterStateKey;     // 현재 몬스터 상태

private:
    // 데이터 에셋에서 가져온 속성들로 AI Perception Component 시야 설정을 갱신하기 위한 함수
    void SetSightConfig(float InSightRadius, float InLoseSightRadius, float InPeripheralVisionAngleDegrees);

    // 플레이어 감지 시 처리
    UFUNCTION()
    void OnTargetDetected(AActor* Actor, const FAIStimulus Stimulus);
    UPROPERTY(EditDefaultsOnly, Category = AI)
    
    class UBehaviorTree* BTAsset;

    UPROPERTY(EditDefaultsOnly, Category = AI)
    class UBlackboardData* BBAsset;
    // 현재 감지된 플레이어 수 (멀티 플레이어 고려)
    int32 nDetectedTargets;
};
