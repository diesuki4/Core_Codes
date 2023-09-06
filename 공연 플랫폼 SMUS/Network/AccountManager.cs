using System.Collections;
using System.Collections.Generic;

// 로그인, 로그아웃, 회원가입, 개인정보 변경을 담당하는 계정 매니저이다.
// - 클라이언트로 참여했기 때문에, API 서버 구축이나 세션 관리 등은 하지 않았다.
public static class AccountManager
{
    // DB 이름
    static string DB_USERINFO = Encoder.Decode("dXNlcmluZm8=");

    public static string id;        // 아이디
    public static string genre;     // 가입 시 선택하는 장르
    public static bool isLogined;   // 로그인 여부

    // 로그인
    public static bool Login(string _id, string _passwd)
    {
        string query = string.Format("SELECT * FROM " + DB_USERINFO + " WHERE id = '{0}' AND passwd = '{1}';", _id, Encoder.Encode(_passwd));
        List<Dictionary<string, object>> result = DBManager.Select(query);

        isLogined = (0 < result.Count);

        if (isLogined)
        {
            id = _id;
            genre = result[0]["genre"] as string;
        }
        else
        {
            id = null;
            genre = null;
        }

        return isLogined;
    }

    // 로그아웃
    public static void Logout()
    {
        id = null;
        genre = null;

        isLogined = false;
    }

    // 회원 가입
    public static bool SignUp(string _id, string _passwd, string _genre = "R&B")
    {
        string query = string.Format("INSERT INTO " + DB_USERINFO + " VALUES('{0}', '{1}', '{2}');", _id, Encoder.Encode(_passwd), _genre);

        return DBManager.Execute(query);
    }

    // 개인정보 변경
    public static bool Update(string _passwd, string _genre)
    {
        if (id == null)
            return false;

        string query = string.Format("UPDATE " + DB_USERINFO + " SET passwd = '{0}', genre = '{1}' WHERE id = '{2}';", Encoder.Encode(_passwd), _genre, id);

        return DBManager.Execute(query);
    }
}
