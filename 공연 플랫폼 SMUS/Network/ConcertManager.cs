using System;
using System.Text;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ConcertInfo        // 공연 정보 (DB에서 가져오기 위한)
{
    public int concert_id;      // 공연 ID (PKey)
    public string id;           // 공연자 ID
    public string title;        // 제목
    public string genre;        // 장르
    public string music_name;   // 곡명
}

public class ConcertData        // 공연 데이터 (FTP에서 가져오기 위한)
{
    public int concert_id;      // 공연 ID
    public byte[] thumbnail;    // 썸네일
    public byte[] bgm;          // BGM
    public string cdata;        // 타임라인 데이터
}

// FTP, DB 매니저를 활용해 공연 정보와 데이터를 주고 받는 클래스다.
public static class ConcertManager
{
    static string FTP_THUMB_PATH = Encoder.Decode("dGh1bWJuYWlsLw==");  // FTP의 썸네일 폴더 경로
    static string FTP_BGM_PATH = Encoder.Decode("YmdtLw==");            // FTP의 BGM 폴더 경로
    static string FTP_CDATA_PATH = Encoder.Decode("Y29uY2VydGRhdGEv");  // FTP의 공연 정보 폴더 경로
    static string DB_CONCERTINFO = Encoder.Decode("Y29uY2VydGluZm8=");  // 공연 데이터 DB

    // 새로운 공연을 생성하기 위한 다음 공연 ID 반환
    public static int NextConcertId()
    {
        string query = "SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_NAME = '" + DB_CONCERTINFO + "';";

        return int.Parse(string.Format("{0}", DBManager.Select(query)[0]["AUTO_INCREMENT"]));
    }

    // 공연 데이터를 FTP 서버에 업로드하는 함수다. (생성)
    public static bool NewConcertData(ConcertData concertData)
    {
        return SetConcertData(concertData);
    }

    // 공연 데이터를 FTP 서버에 업로드하는 함수다. (수정/덮어쓰기)
    public static bool SetConcertData(ConcertData concertData)
    {
        int concert_id = concertData.concert_id;

        string filePath = FTP_THUMB_PATH + concert_id + ".png";
        bool bResult1 = FTPManager.Upload(concertData.thumbnail, filePath);

        filePath = FTP_BGM_PATH + concert_id + ".mp3";
        bool bResult2 = FTPManager.Upload(concertData.bgm, filePath);

        filePath = FTP_CDATA_PATH + concert_id + ".cdata";
        bool bResult3 = FTPManager.Upload(Encoding.Default.GetBytes(concertData.cdata), filePath);

        return bResult1 & bResult2 & bResult3;
    }

    // 공연 ID에 해당하는 공연 데이터를 FTP 서버에서 가져오는 함수다.
    public static ConcertData GetConcertData(int concert_id)
    {
        ConcertData concertData = new ConcertData();

        concertData.concert_id = concert_id;

        string filePath = FTP_THUMB_PATH + concert_id + ".png";
        concertData.thumbnail = FTPManager.Download(filePath);

        filePath = FTP_BGM_PATH + concert_id + ".mp3";
        concertData.bgm = FTPManager.Download(filePath);

        filePath = FTP_CDATA_PATH + concert_id + ".cdata";
        concertData.cdata = Encoding.Default.GetString(FTPManager.Download(filePath));

        return concertData;
    }

    // 공연 정보를 DB에 삽입하는 함수다.
    public static bool NewConcert(ConcertInfo concertInfo)
    {
        string query = string.Format("INSERT INTO " + DB_CONCERTINFO + " VALUES(0, '{0}', '{1}', 'R&B', '{1}', 0);",
                                        AccountManager.id, concertInfo.title);

        return DBManager.Execute(query);  
    }

    // DB의 공연 정보를 수정하는 함수다.
    public static bool SetConcert(ConcertInfo concertInfo)
    {
        string query = string.Format("UPDATE " + DB_CONCERTINFO + " SET id = '{0}', title = '{1}', music_name = '{1}' WHERE concert_id = '{2}';",
                                        concertInfo.id, concertInfo.music_name, concertInfo.concert_id);

        return DBManager.Execute(query);  
    }

    // DB에서 공연 ID에 해당하는 공연 정보를 가져오는 함수다.
    public static ConcertInfo GetConcert(int concert_id, bool active = true)
    {
        string activeQuery = (active) ? " AND active = 1" : "";
        string query = string.Format("SELECT * FROM " + DB_CONCERTINFO + " WHERE concert_id = '{0}'" + activeQuery + ";", concert_id);
        List<Dictionary<string, object>> result = DBManager.Select(query);

        ConcertInfo concertInfo = new ConcertInfo();

        concertInfo.concert_id  = (int)result[0]["concert_id"];
        concertInfo.id          = result[0]["id"] as string;
        concertInfo.title       = result[0]["title"] as string;
        concertInfo.genre       = result[0]["genre"] as string;
        concertInfo.music_name  = result[0]["music_name"] as string;

        return concertInfo;
    }

    // DB에서 특정 사용자의 모든 공연을 가져오는 함수다.
    public static List<ConcertInfo> GetConcertsWithId(string id, bool active = true)
    {
        string activeQuery = (active) ? " AND active = 1" : "";
        string query = string.Format("SELECT * FROM " + DB_CONCERTINFO + " WHERE id = '{0}'" + activeQuery + ";", id);
        List<Dictionary<string, object>> result = DBManager.Select(query);

        List<ConcertInfo> concertInfos = new List<ConcertInfo>();

        foreach (Dictionary<string, object> dict in result)
        {
            ConcertInfo concertInfo = new ConcertInfo();

            concertInfo.concert_id  = (int)dict["concert_id"];
            concertInfo.id          = dict["id"] as string;
            concertInfo.title       = dict["title"] as string;
            concertInfo.genre       = dict["genre"] as string;
            concertInfo.music_name  = dict["music_name"] as string;

            concertInfos.Add(concertInfo);
        }

        return concertInfos;
    }

    // DB에서 특정 장르의 모든 공연을 가져오는 함수다.
    public static List<ConcertInfo> GetConcertsWithGenre(string genre, bool active = true)
    {
        string activeQuery = (active) ? " AND active = 1" : "";
        string query = string.Format("SELECT * FROM " + DB_CONCERTINFO + " WHERE genre = '{0}'" + activeQuery + ";", genre);
        List<Dictionary<string, object>> result = DBManager.Select(query);

        List<ConcertInfo> concertInfos = new List<ConcertInfo>();

        foreach (Dictionary<string, object> dict in result)
        {
            ConcertInfo concertInfo = new ConcertInfo();

            concertInfo.concert_id  = (int)dict["concert_id"];
            concertInfo.id          = dict["id"] as string;
            concertInfo.title       = dict["title"] as string;
            concertInfo.genre       = dict["genre"] as string;
            concertInfo.music_name  = dict["music_name"] as string;

            concertInfos.Add(concertInfo);
        }

        return concertInfos;
    }

    // DB에서 공연을 검색하는 함수다.
    public static List<ConcertInfo> SearchConcerts(string searchQuery, bool active = true)
    {
        string activeQuery = (active) ? " AND active = 1" : "";
        string query = string.Format("SELECT * FROM " + DB_CONCERTINFO + " WHERE title LIKE '%{0}%'" + activeQuery + ";", searchQuery);
        List<Dictionary<string, object>> result = DBManager.Select(query);

        List<ConcertInfo> concertInfos = new List<ConcertInfo>();

        foreach (Dictionary<string, object> dict in result)
        {
            ConcertInfo concertInfo = new ConcertInfo();

            concertInfo.concert_id  = (int)dict["concert_id"];
            concertInfo.id          = dict["id"] as string;
            concertInfo.title       = dict["title"] as string;
            concertInfo.genre       = dict["genre"] as string;
            concertInfo.music_name  = dict["music_name"] as string;

            concertInfos.Add(concertInfo);
        }

        return concertInfos;
    }

    // DB에 특정 공연의 활성 여부를 갱신하는 함수다.
    public static bool SetConcertState(int concert_id, bool active)
    {
        string query = string.Format("UPDATE " + DB_CONCERTINFO + " SET active = " + Convert.ToInt32(active) + " WHERE concert_id = '{0}';", concert_id);

        return DBManager.Execute(query);           
    }
}
