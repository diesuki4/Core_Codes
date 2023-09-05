using System.Net;
using System.IO;
using System.Collections;
using System.Collections.Generic;

// FTP 서버에 접근해 파일을 업로드/다운로드 하는 클래스다.
public static class FTPManager
{
    static string SERVER = Encoder.Decode("d3d3NS5keW51Lm5ldA==");  // 서버
    static string PORT = Encoder.Decode("NDcwMjg=");                // 포트
    static string id = Encoder.Decode("c21idXNlcg==");              // 아이디
    static string passwd = Encoder.Decode("aXlhc2hpbmFuZGExMiMk");  // 비밀번호
    static string connDest = "ftp://" + SERVER + ":" + PORT + "/";
    static NetworkCredential credential = new NetworkCredential(id, passwd);

    // 지정된 경로에 파일을 업로드
    public static bool Upload(byte[] bytes, string filePath)
    {
        FtpWebRequest ftpWebRequest = (FtpWebRequest)WebRequest.Create(connDest + filePath);

        ftpWebRequest.Credentials = credential;
        ftpWebRequest.UseBinary = false;
        ftpWebRequest.Method = WebRequestMethods.Ftp.UploadFile;

        Stream ftpStream = ftpWebRequest.GetRequestStream();

        ftpStream.Write(bytes, 0, bytes.Length);
        ftpStream.Flush();
        ftpStream.Close();

        FtpWebResponse response = (FtpWebResponse)ftpWebRequest.GetResponse();

        return response.StatusCode == FtpStatusCode.ClosingControl;
    }

    // 지정된 경로에서 파일을 다운로드
    public static byte[] Download(string filePath)
    {
        FtpWebRequest ftpWebRequest = (FtpWebRequest)WebRequest.Create(connDest + filePath);

        ftpWebRequest.Credentials = credential;
        ftpWebRequest.UseBinary = false;
        ftpWebRequest.Method = WebRequestMethods.Ftp.DownloadFile;

        Stream ftpStream = ftpWebRequest.GetResponse().GetResponseStream();
        MemoryStream memoryStream = new MemoryStream();

        ftpStream.CopyTo(memoryStream);
        ftpStream.Close();

        return memoryStream.ToArray();
    }
}
