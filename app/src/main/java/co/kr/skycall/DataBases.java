package co.kr.skycall;

import android.provider.BaseColumns;
import android.util.Log;


public class DataBases {

    //데이터베이스 호출 시 사용될 생성자
    public static final class CreateDB implements BaseColumns {

        public static final String NAME = "name";
        public static final String CONTACT = "contact";
        public static final String EMAIL = "email";
        public static final String _TABLENAME = "address";
        public static final String _CREATE =
                "create table " + _TABLENAME + "("
                + _ID + " integer primary key autoincrement, "
                + NAME + " text default '' , "
                + CONTACT + " text not null , "
                +EMAIL + " text not null );";

        public static final String _CREATE2 =
                "create table g5_member_phone_memos (" +
                        "num integer primary key autoincrement, " +
                        "mb_no integer not null, " +
                        "mb_id text not null," +
                        "phone_no text not null," +
                        "phone_number text not null," +
                        "w_date datetime default current_timestamp," +
                        "memo  text not null);";
    }
}
