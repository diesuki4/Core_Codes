using System.Linq;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Tilemaps;
using UI.Utility;

// 공연 창작툴의 중심이 되는 싱글톤 오브젝트이며, 그리드에 붙은 컴포넌트다.
// 그리드 상의 소품 목록 관리, 셀 관리, 겹침 판정 등의 기능을 수행한다.
public class BuildingSystem : MonoBehaviour
{
    public static BuildingSystem Instance;

    public BuildingSystem()
    {
        if (Instance == null)
            Instance = this;
        else
            Destroy(gameObject);

        // 그리드 상의 소품 목록을 관리한다.
        objectList = new List<PlaceableObject>();
    }

    void Awake()
    {
        gridLayout = grid = GetComponent<Grid>();
    }

    public Canvas cnvsPalette;  // 팔레트 캔버스
    public Canvas cnvsTimeline; // 타임라인 캔버스

    public Tilemap mainTilemap;
    public enum Tile
    {
        Red,            // 겹쳐서 배치 불가하면 빨간 타일
        Green,          // 배치 가능한 곳은 초록 타일
        Transparent     // 기본 타일
    }
    public TileBase[] tiles;

    public enum Placeable
    {
        Possible    = 1 << 0,   // 배치 가능
        Overlap     = 1 << 1,   // 타 소품과 겹침
        OOB         = 1 << 2    // 그리드를 벗어남
    }

    [HideInInspector] public PlaceableObject objectToPlace;     // 현재 선택된 소품
    [HideInInspector] public List<PlaceableObject> objectList;  // 그리드 상 소품 목록

    GridLayout gridLayout;
    Grid grid;

    void Start() { }

    void Update()
    {
        if (objectToPlace == null)
            return;

        // 선택된 소품 회전
        if (Input.GetKeyDown(KeyCode.R))
            objectToPlace.RotateHorizontal(15);
        else if (Input.GetKeyDown(KeyCode.F))
            objectToPlace.RotateHorizontal(-15);
        else if (Input.GetKeyDown(KeyCode.T))
            objectToPlace.RotateVertical(15);
        else if (Input.GetKeyDown(KeyCode.G))
            objectToPlace.RotateVertical(-15);

        // 선택된 소품 수평 이동
        if (Input.GetMouseButton(1) == false)
            if (Input.GetKeyDown(KeyCode.W))
                objectToPlace.Move(PlaceableObject.MoveDirection.Up);
            else if (Input.GetKeyDown(KeyCode.A))
                objectToPlace.Move(PlaceableObject.MoveDirection.Left);
            else if (Input.GetKeyDown(KeyCode.S))
                objectToPlace.Move(PlaceableObject.MoveDirection.Down);
            else if (Input.GetKeyDown(KeyCode.D))
                objectToPlace.Move(PlaceableObject.MoveDirection.Right);

        // 선택된 소품 수직 이동
        if (UI_Utility.GraphicRaycast(cnvsTimeline, Input.mousePosition) == false &&
            UI_Utility.GraphicRaycast(cnvsPalette, Input.mousePosition) == false)
            objectToPlace.VerticalMove(Input.GetAxisRaw("Mouse ScrollWheel"));
    }

    // position(마우스로 찍어서 그리드에 닿은) 위치의 셀 위치 반환
    public Vector3Int GetCellPosition(Vector3 position)
    {
        return GetComponent<Grid>().WorldToCell(position);
    }

    // position(마우스로 찍어서 그리드에 닿은) 위치의 셀 중앙 위치 반환
    public Vector3 GetCellCenterPosition(Vector3 position)
    {
        return grid.GetCellCenterWorld(GetCellPosition(position));
    }

    // 선택된 소품을 소품 목록에 추가
    public void AddPlaceableObject(PlaceableObject placeableObject)
    {
        if (objectList.Contains(placeableObject) == false)
            objectList.Add(placeableObject);
    }

    // 선택된 소품을 소품 목록에서 제거
    public void RemovePlaceableObject(PlaceableObject placeableObject)
    {
        objectList.Remove(placeableObject);
    }

    public GameObject Instantiate(GameObject prefab)
    {
        Vector3 position = GetCellCenterPosition(Vector3.zero);
        Quaternion rotation = Quaternion.identity;

        GameObject obj = Instantiate(prefab, position, rotation);
        objectToPlace = obj.GetComponent<PlaceableObject>();

        return obj;
    }

    // 소품이 차지하는 모든 타일을 반환
    public TileBase[] GetTiles(PlaceableObject placeableObject)
    {
        BoundsInt area = Area(placeableObject);
        List<TileBase> tiles = new List<TileBase>();

        foreach (Vector3Int vi in area.allPositionsWithin)
        {
            Vector3Int pos = new Vector3Int(vi.x, vi.y, 0);

            tiles.Add(mainTilemap.GetTile(pos));
        }

        return tiles.ToArray();
    }

    // 소품이 차지하는 영역을 반환
    BoundsInt Area(PlaceableObject placeableObject)
    {
        BoundsInt area = new BoundsInt();

        area.position = GetCellPosition(placeableObject.GetStartPosition());
        area.size = placeableObject.size + Vector3Int.right + Vector3Int.up;

        return area;
    }
    
    // 그리드에 배치된 소품과 겹치는지 반환
    public bool isOverlapped(PlaceableObject placeableObject)
    {
        return false;

        foreach (PlaceableObject po in objectList)
            if (po != placeableObject)
                if (isIntersect(po, placeableObject))
                    return true;
  
        return false;
    }

    // 두 소품이 겹치는지 반환 (AABB 방식)
    public bool isIntersect(PlaceableObject po1, PlaceableObject po2)
    {
        Box box1 = po1.GetBox();
        Box box2 = po2.GetBox();

        bool conditionX = (box1.minX < box2.maxX) & (box2.minX < box1.maxX);
        bool conditionY = (box1.minY < box2.maxY) & (box2.minY < box1.maxY);
        bool conditionZ = (box1.minZ < box2.maxZ) & (box2.minZ < box1.maxZ);

        return conditionX & conditionY & conditionZ;
    }

    // 선택된 소품을 그리드에 배치 가능한지 반환
    public Placeable isPlaceable(PlaceableObject placeableObject)
    {
        RaycastHit hit;

        // 그리드에 닿지 않았거나, 팔레트 혹은 타임라인 UI 위에 있으면
        if (UI_Utility.ScreenPointRaycast(Camera.main, Input.mousePosition, out hit, 1 << LayerMask.NameToLayer("Floor")) == false
            || UI_Utility.GraphicRaycast(cnvsPalette, Input.mousePosition) || UI_Utility.GraphicRaycast(cnvsTimeline, Input.mousePosition))
            // 범위를 벗어났다.
            return Placeable.OOB;

        // 타 소품과 겹친다.
        if (isOverlapped(placeableObject))
            return Placeable.Overlap;

        // 배치 가능하다.
        return Placeable.Possible;
    }

    int TileToIndex(Tile tile)
    {
        return (int)tile;
    }

    // 선택된 소품의 크기만큼 tile 색상으로 타일을 채운다.
    public void Fill(PlaceableObject placeableObject, Tile tile)
    {
        Vector3Int start = GetCellPosition(placeableObject.GetStartPosition());
        Vector3Int size = placeableObject.size;

        mainTilemap.BoxFill(start, tiles[TileToIndex(tile)],
                            start.x, start.y,
                            start.x + size.x, start.y + size.y);
    }

    // except 소품을 제외하고 타일 색상을 초기화한다.
    public void ClearGrid(PlaceableObject except = null)
    {
        Vector3Int mapSize = mainTilemap.size;

        mainTilemap.ClearAllTiles();

        mainTilemap.size = mapSize;

        foreach (PlaceableObject placeableObject in objectList)
            if (placeableObject != except)
                Fill(placeableObject, Tile.Transparent);
    }

    // 소품의 교유값인 GUID로 타임라인 오브젝트를 찾는다.
    public TimelineObject getTimelineObject(string guid)
    {
        foreach (PlaceableObject po in objectList)
            if (po.GetComponent<TimelineObject>().guid == guid)
                return po.GetComponent<TimelineObject>();

        return null;
    }
}
