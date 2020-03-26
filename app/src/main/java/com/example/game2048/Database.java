package com.example.game2048;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {

    static final String DB_NAME = "db_score";
    static final String TB_NAME = "tb_score";
    static final String TB_BOX = "tb_box";
    static final String SCORE = "score_Old";
    static final String HIGHSCORE = "highScore";
    static final String ROW = "row_i";
    static final String COL = "col_j";
    static final String VAL = "val";

    SQLiteDatabase db;
    Context context;

    public Database(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
        db = this.getWritableDatabase();
    }
    public void QueryData(String sql)
    {
         SQLiteDatabase db = getWritableDatabase();
         db.execSQL(sql);
    }

    public Cursor GetData(String sql)
    {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(sql, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE tb_score (score_Old int, highScore int)";
        db.execSQL(sql);

        //
        String sql1 = "CREATE TABLE tb_box (row_i integer, col_j integer, val integer)";
        db.execSQL(sql1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + TB_BOX);
        onCreate(db);
    }

    public void saveScore(int score) {

        String query = "select * from "+ TB_NAME;
        Cursor c = db.rawQuery(query,null);
        if(c!=null){
            try
            {
                c.moveToFirst();
                c.getString(0);
                // update high score
                String sql_Update = "update "+ TB_NAME + " set "+ SCORE+ " = "+ score;
                db.execSQL(sql_Update);
            }
            catch (Exception e)
            {
                String sql_Insert = "insert into "+ TB_NAME +"("+SCORE+","+HIGHSCORE+") values ( "+score+", 0 )";
                db.execSQL(sql_Insert);
            }
        }
        else {
            String sql_Insert = "insert into "+ TB_NAME +"("+SCORE+","+HIGHSCORE+") values ( "+score+", 0 )";
            db.execSQL(sql_Insert);
        }
    }

    public int getScore(){
        int score =0;
        try
        {
            String query = "select * from "+ TB_NAME;
            Cursor c = db.rawQuery(query,null);
            if(c!=null){
                c.moveToFirst();
                score = Integer.parseInt(c.getString(0));
            }
        }
        catch (Exception e){

        }

        return score;
    }

    public void saveHighScore(int highScore) {
        String query = "select highScore from "+ TB_NAME;
        Cursor c = db.rawQuery(query,null);
        if(c!=null){
            try
            {
                c.moveToFirst();
                c.getString(0);
                // update high score
                String sql_Update = "update "+ TB_NAME + " set "+ HIGHSCORE+ " = "+ highScore;
                db.execSQL(sql_Update);
            }
            catch (Exception e)
            {
                String sql_Insert = "insert into "+ TB_NAME +"("+SCORE+","+HIGHSCORE+") values ( 0, "+ highScore+ ")";
                db.execSQL(sql_Insert);
            }
       }
        else {
            String sql_Insert = "insert into "+ TB_NAME +"("+SCORE+","+HIGHSCORE+") values ( 0, "+ highScore+ ")";
            db.execSQL(sql_Insert);
        }
    }

    public int getHighScore(){
        int score =0;
        try {
            String query = "select highScore from " + TB_NAME;
            Cursor c = db.rawQuery(query, null);
            if (c != null) {
                c.moveToFirst();
                score = Integer.parseInt(c.getString(0));
                //String ss = c.getString(0).toString();
               // String[] lst = c.getColumnNames();
            }
        }
        catch (Exception ex){
            String sss = ex.toString();
        }
        return score;
    }

    public void saveBox(int row, int col, int val) {

        String query = "select * from "+ TB_BOX + " where "+ROW+" = "+row+" and "+COL+" = "+col;
        Cursor c = db.rawQuery(query,null);

        if(c!=null){
            try{
                c.moveToFirst();
                String s =c.getString(0);
                // update TB_BOX set VAL = val where ROW = row and COL = col
                String sql_Update = "update "+TB_BOX+" set "+VAL+" = "+val+" where "+ROW+" = "+row+" and "+COL+" = "+col;
                db.execSQL(sql_Update);
            }
            catch (Exception e){
                String sql_Insert = "Insert into TB_BOX ("+ ROW +", "+ COL +", "+ VAL +") values ("+ row +", "+ col +", "+ val +")";
                db.execSQL(sql_Insert);
            }
        }
        else { // insert
            // Insert into TB_BOX (ROW, COL, VAL) value (row, col, val)
            String sql_Insert = "Insert into TB_BOX ("+ ROW +", "+ COL +", "+ VAL +") values ("+ row +", "+ col +", "+ val +")";
            db.execSQL(sql_Insert);
        }
    }

    public Integer getBox(int row, int col) {
        Integer value=0;

       try
       {
           String query = "select val from "+ TB_BOX + " where "+ROW+" = "+row+" and "+COL+" = "+col;
         Cursor c = db.rawQuery(query,null);
        if(c!=null){
            c.moveToFirst();
            value = Integer.parseInt(c.getString(0));
        //String a =    c.getString(0);
        }

       }
       catch(Exception e) {
           value =0;
        }
        return value;
    }
}
