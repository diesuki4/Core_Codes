using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using MySql.Data;
using MySql.Data.MySqlClient;

// DB에 접근해 쿼리를 요청하고 결과를 간소화시켜 반환하기 위한 클래스다.
public static class DBManager
{
    static string SERVER = Encoder.Decode("d3d3NS5keW51Lm5ldA==");  // 서버 주소
    static string PORT = Encoder.Decode("NTYwMzM=");                // 포트
    static string database = Encoder.Decode("c211cw==");            // DB 이름
    static string userid = Encoder.Decode("c211cw==");              // DB 아이디
    static string passwd = Encoder.Decode("c211cw==");              // DB 비밀번호
    static string connInfo = "Server=" + SERVER + ";Port=" + PORT + ";Database=" + database + ";User Id=" + userid + ";Password=" + passwd + "";

    static MySqlConnection conn;

    static void Connect()
    {
        conn = new MySqlConnection(connInfo);

        conn.Open();
    }

    static void Disconnect()
    {
        conn.Close();

        conn = null;
    }

    // 쿼리 실행 요청
    public static bool Execute(string query)
    {
        Connect();

        MySqlCommand command = new MySqlCommand(query, conn);

        int rows = command.ExecuteNonQuery();

        Disconnect();

        return 0 < rows;
    }

    // 쿼리 실행 후 결과 반환
    public static List<Dictionary<string, object>> Select(string query)
    {
        Connect();

        DataTable dt = new DataTable();

        MySqlDataAdapter adapter =  new MySqlDataAdapter(query, conn);

        adapter.Fill(dt);

        Disconnect();

        return Simplify(dt);
    }

    // DataTable 형식은 팀원이 사용하기 어렵기 때문에,
    // 레코드(딕셔너리로 구현)의 리스트로 변환하는 함수
    static List<Dictionary<string, object>> Simplify(DataTable dt)
    {
        List<Dictionary<string, object>> list = new List<Dictionary<string, object>>();
        
        for (int i = 0; i < dt.Rows.Count; ++i)
        {
            Dictionary<string, object> dict = new Dictionary<string, object>();

            for (int j = 0; j < dt.Columns.Count; ++j)
                dict[dt.Columns[j].ColumnName] = dt.Rows[i][j];

            list.Add(dict);
        }

        return list;
    }
}
