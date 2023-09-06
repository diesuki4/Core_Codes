#include "Monster/ARMonsterController.h"

#include "Monster/ARMonsterBase.h"
#include "Monster/Types/ARMonsterState.h"
#include "Monster/DataAssets/ARMonsterDataAsset.h"
#include "ActorComponents/ARDataComponent.h"
#include "Character/ARCharacter.h"
#include "BehaviorTree/BehaviorTree.h"
#include "BehaviorTree/BlackboardData.h"
#include "BehaviorTree/BlackboardComponent.h"
#include "Perception/AIPerceptionComponent.h"
#include "Perception/AISenseConfig_Sight.h"

const FName AARMonsterController::OriginLocationKey(TEXT("OriginLocation"));
const FName AARMonsterController::PatrolLocationKey(TEXT("PatrolLocation"));
const FName AARMonsterController::TargetActorKey(TEXT("TargetActor"));
const FName AARMonsterController::MonsterStateKey(TEXT("MonsterState"));

AARMonsterController::AARMonsterController()
{
    // 타겟 감지시 처리 함수 바인딩
    UAIPerceptionComponent* AIPerception = CreateOptionalDefaultSubobject<UAIPerceptionComponent>(TEXT("AIPerception"));
    AIPerception->OnTargetPerceptionUpdated.AddDynamic(this, &ThisClass::OnTargetDetected);

    SetPerceptionComponent(*AIPerception);
    // AI Perception 시야 설정 (세부 속성 값은 데이터 에셋을 통해 설정)
    UAISenseConfig_Sight* SightConfig = CreateOptionalDefaultSubobject<UAISenseConfig_Sight>(TEXT("SightConfig"));
    SightConfig->DetectionByAffiliation.bDetectEnemies = true;
    SightConfig->DetectionByAffiliation.bDetectNeutrals = true;
    SightConfig->DetectionByAffiliation.bDetectFriendlies = true;

    AIPerception->SetDominantSense(*SightConfig->GetSenseImplementation());
    AIPerception->ConfigureSense(*SightConfig);

    nDetectedTargets = 0;
}

void AARMonsterController::OnPossess(APawn* InPawn)
{
    Super::OnPossess(InPawn);

    UBlackboardComponent* BlackboardComp = Blackboard.Get();

    if (UseBlackboard(BBAsset, BlackboardComp))
    {
        // 초기 위치 저장
        Blackboard = BlackboardComp;
        Blackboard->SetValueAsVector(OriginLocationKey, InPawn->GetActorLocation());

        AARMonsterBase* Monster = Cast<AARMonsterBase>(InPawn);
        // 데이터 에셋에서 설정 값들을 가져온다.
        UARDataComponent* DataComponent = Monster->GetDataComponent();
        ARCHECK(DataComponent != nullptr);

        UARMonsterDataAsset* MonsterData = Cast<UARMonsterDataAsset>(DataComponent->GetActorData());

        float DetectDistance = MonsterData->DetectDistance;
        float MaxDetectDistance = MonsterData->MaxDetectDistance;
        float DetectHalfAngle = MonsterData->DetectHalfAngle;
        // 데이터 에셋에서 가져온 속성들로 시야 설정 갱신
        SetSightConfig(DetectDistance, MaxDetectDistance, DetectHalfAngle);

        if (RunBehaviorTree(BTAsset) == false)
        {
            ARLOG(Error, TEXT("AIController couldn't run behavior tree!"));
        }
    }
}

void AARMonsterController::OnUnPossess()
{
    Super::OnUnPossess();

    UBehaviorTreeComponent* BehaviorTreeComponent = Cast<UBehaviorTreeComponent>(BrainComponent);
    ARCHECK(BehaviorTreeComponent != nullptr);

    BehaviorTreeComponent->StopTree(EBTStopMode::Safe);
}

// 전달된 값으로 AI Perception 컴포넌트의 시야 설정을 갱신한다.
void AARMonsterController::SetSightConfig(float InSightRadius, float InLoseSightRadius, float InPeripheralVisionAngleDegrees)
{
    FAISenseID AISenseID = UAISense::GetSenseID(UAISense_Sight::StaticClass());
    ARCHECK(AISenseID.IsValid());

    UAIPerceptionComponent* AIPerception = GetPerceptionComponent();
    ARCHECK(AIPerception != nullptr);

    UAISenseConfig* AISenseConfig = AIPerception->GetSenseConfig(AISenseID);
    ARCHECK(AISenseConfig != nullptr);

    UAISenseConfig_Sight* SightConfig = Cast<UAISenseConfig_Sight>(AISenseConfig);
    ARCHECK(SightConfig != nullptr);

    SightConfig->SightRadius = InSightRadius;
    SightConfig->LoseSightRadius = InLoseSightRadius;
    SightConfig->PeripheralVisionAngleDegrees = InPeripheralVisionAngleDegrees;

    AIPerception->RequestStimuliListenerUpdate();
}

// AI Perception을 통해 타겟 감지시 처리 함수다.
void AARMonsterController::OnTargetDetected(AActor* Actor, const FAIStimulus Stimulus)
{
    // 플레이어가 아니면 감지하지 않는다.
    AARCharacter* NewTarget = Cast<AARCharacter>(Actor);
    ARCHECK(NewTarget != nullptr);
    // 플레이어 감지 시
    if (Stimulus.WasSuccessfullySensed())
    {
        // 타겟으로 설정
        ++nDetectedTargets;
        Blackboard->SetValueAsObject(TargetActorKey, NewTarget);
    }
    else
    {
        --nDetectedTargets;
    }

    AARMonsterBase* Monster = Cast<AARMonsterBase>(GetPawn());
    ARCHECK(Monster != nullptr);

    // 감지된 플레이어가 있으면
    if (0 < nDetectedTargets)
    {
        // 타겟을 추격
        Monster->SetState(EARMonsterState::Chase);
    }
    // 없으면
    else
    {
        // 초기 위치로 돌아가는 중이 아니면
        if (Monster->GetState() != EARMonsterState::Return)
        {
            // Idle로 설정
            Monster->SetState(EARMonsterState::Idle);
        }
        // 타겟 없음으로 설정
        Blackboard->SetValueAsObject(TargetActorKey, nullptr);
    }
}
