/*
    원본 문자열의 각 문자와 길이 정보를
    길이 40자 랜덤 문자열에 매핑하여,
    암복호화를 진행한다.
*/

// 길이 20자 문자열까지 지원하는 암복호화 클래스
public class Encryptor
{
    // 길이 정보가 매핑될 인덱스
    private static final int ordinal_10 = 7;
    private static final int ordinal_1    = 33;
    // 각 문자가 매핑될 인덱스
    private static final int ordinal[] = {    34, 4, 25, 12, 9,
                                            31, 17, 22, 5, 14,
                                            3, 30, 15, 11, 29,
                                            20, 8, 38, 13, 27    };
    
    // 암호화
    public static String encrypt(String str)
    {
        if (str == null)
            return null;
        
        String encrypted = null;
        char temp[] = new char[40];
        
        for (int i=0; i<40; i++)
            // 아스키 코드로 'A' ~ 'z' 까지
            temp[i] = (char)(Math.random()*((int)'z' - (int)'A' + 1) + (int)'A');

        int strlen = str.length();
        
        // 길이 정보 매핑
        temp[ordinal_10] = (char)((int)'0' + (int)(strlen/10));
        temp[ordinal_1] = (char)((int)'0' + (int)(strlen%10));
        // 원래 문자열의 각 문자 매핑
        for (int i=0; i<strlen; i++)
            temp[ordinal[i]] = str.charAt(i);
        
        encrypted = new String(temp);
        return encrypted;
    }
    
    // 복호화
    public static String decrypt(String str)
    {
        if (str == null)
            return null;
        
        // 길이 정보 불러오기
        int strlen = ((int)str.charAt(ordinal_10) - (int)'0') * 10 + ((int)str.charAt(ordinal_1) - (int)'0');
        
        String decrypted = "";
        // 원본 문자열 추출
        for (int i=0; i<strlen; i++)
            decrypted += Character.toString(str.charAt(ordinal[i]));
        
        return decrypted;        
    }
}
