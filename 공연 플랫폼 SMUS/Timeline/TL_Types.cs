using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// 타임라인에 찍을 키에 필요한 데이터를 정의한 클래스다.
namespace Timeline
{
    namespace Types
    {
        // 타임라인에는 프레임이 빠른 순으로 키가 저장된다.
        public class CDTSortedSet : SortedSet<TL_Types.Key>
        {
            public CDTSortedSet() : base(new KeyComparer()) {}
        }

        class KeyComparer : IComparer<TL_Types.Key>
        {
            public int Compare(TL_Types.Key x, TL_Types.Key y)
            {
                return x.frame.CompareTo(y.frame);
            }
        }

        // 소품 종류
        public enum TL_ENUM_Types
        {
            Object, // 오브젝트
            Effect, // 이펙트
            Light   // 라이트
        }

        public class TL_Types
        {
            // 모든 소품은 키를 갖는다.
            [Serializable]
            public class Key
            {
                public int frame;           // 지장된 프레임
                public bool active;         // 활성화 여부
                public Vector3 position;    // 저장된 위치
                public Quaternion rotation; // 저장된 회전

                public Key(int frame, bool active, Vector3 position, Quaternion rotation)
                {
                    this.frame = frame;
                    this.active = active;
                    this.position = position;
                    this.rotation = rotation;
                }
            }

            // 오브젝트 소품
            [Serializable]
            public class Object : Key
            {
                public Object(int _frame, bool _active, Vector3 _position, Quaternion _rotation) : base(_frame, _active, _position, _rotation) { }
            }

            // 이펙트 소품
            [Serializable]
            public class Effect : Key
            {
                public Effect(int _frame, bool _active, Vector3 _position, Quaternion _rotation) : base(_frame, _active, _position, _rotation) { }
            }

            // 라이트 소품
            [Serializable]
            public class Light : Key
            {
                public Light(int _frame, bool _active, Vector3 _position, Quaternion _rotation) : base(_frame, _active, _position, _rotation) { }
            }
        }
    }
}
