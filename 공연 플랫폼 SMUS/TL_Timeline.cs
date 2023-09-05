using System;
using System.Linq;
using System.Collections;
using System.Collections.Generic;
using Timeline.Types;
using Timeline.Utility;

// 하나의 소품의 키 목록을 관리하기 위한 클래스다.
// 타임라인은 Dictionaty[GUID] = SortedSet 형태로 저장된다.
// 각 소품은 고유의 GUID를 가지며, 타임라인 키는 프레임이 빠른 순으로 저장된다.
namespace Timeline
{
    namespace Timeline
    {
        [Serializable]
        public class TL_Timeline
        {
            public TL_ENUM_Types tlType;    // 소품 종류
            public string itemName;         // 소품 이름

            CDTSortedSet keys;              // 타임라인 키 목록

            public TL_Timeline(TL_ENUM_Types tlType, string itemName)
            {
                this.tlType = tlType;
                this.itemName = itemName;

                keys = new CDTSortedSet();
            }

            public List<TL_Types.Key> GetKeys()
            {
                return keys.ToList();
            }

            public void SetKeys(CDTSortedSet keys)
            {
                DeleteAllKeys();

                this.keys = keys;
            }

            public bool AddKey(TL_Types.Key key)
            {
                keys.Add(key);

                return keys.Contains(key);
            }

            public bool DeleteKey(TL_Types.Key key)
            {
                return keys.Remove(key);
            }

            public bool DeleteKey(int frame)
            {
                foreach (TL_Types.Key key in keys)
                    if (key.frame == frame)
                        return DeleteKey(key);

                return false;
            }

            public bool DeleteAllKeys()
            {
                keys.Clear();

                return keys.Count == 0;                
            }

            public bool UpdateKey(TimelineKey tlKey)
            {
                bool result = 0 < keys.RemoveWhere(x => x.frame == tlKey.frame);

                if (result)
                    AddKey(new TL_Types.Key(tlKey.frame, tlKey.active, tlKey.position, tlKey.rotation));

                return result;
            }
            
            public int IndexOf(int frame)
            {
                List<TL_Types.Key> lstKeys = keys.ToList();

                for (int i = 0; i < lstKeys.Count; ++i)
                    if (lstKeys[i].frame == frame)
                        return i;

                return -1;             
            }

            ~TL_Timeline()
            {
                DeleteAllKeys();
            }
        }
    }
}
